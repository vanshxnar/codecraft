package dev.codecraft.client.playground;

import dev.codecraft.playground.PlaygroundBackend;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Runs Playground calls against the integrated (singleplayer) server.
 *
 * Lesson code runs on its own thread, so every call is handed to the server thread and
 * waited on -- world mutation off-thread would corrupt chunk state.
 */
public final class ClientPlaygroundBackend implements PlaygroundBackend {
	private static final long SERVER_CALL_TIMEOUT_MS = 2000;

	@Override
	public void say(String message) {
		onServer(player -> player.sendSystemMessage(Component.literal(message)));
	}

	@Override
	public void showTitle(String message) {
		onServer(player -> player.displayClientMessage(Component.literal(message), true));
	}

	@Override
	public void placeBlock(int dx, int dy, int dz, String blockId) {
		onServer(player -> {
			Block block = BuiltInRegistries.BLOCK.get(id(blockId));
			player.serverLevel().setBlock(pos(player, dx, dy, dz), block.defaultBlockState(), 3);
		});
	}

	@Override
	public void breakBlock(int dx, int dy, int dz) {
		onServer(player -> player.serverLevel().setBlock(pos(player, dx, dy, dz), Blocks.AIR.defaultBlockState(), 3));
	}

	@Override
	public String getBlock(int dx, int dy, int dz) {
		return callOnServer(player -> {
			Block block = player.serverLevel().getBlockState(pos(player, dx, dy, dz)).getBlock();
			return BuiltInRegistries.BLOCK.getKey(block).getPath();
		}, "air");
	}

	@Override
	public void spawnEntity(int dx, int dy, int dz, String entityId) {
		onServer(player -> {
			EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id(entityId));
			type.spawn(player.serverLevel(), pos(player, dx, dy, dz), MobSpawnType.COMMAND);
		});
	}

	@Override
	public void teleportPlayer(int dx, int dy, int dz) {
		onServer(player -> {
			BlockPos target = pos(player, dx, dy, dz);
			player.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
		});
	}

	@Override
	public void particles(int dx, int dy, int dz, String particleId, int count) {
		onServer(player -> {
			ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.get(id(particleId));
			if (type instanceof SimpleParticleType simple) {
				BlockPos target = pos(player, dx, dy, dz);
				player.serverLevel().sendParticles(simple,
						target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, count, 0.3, 0.3, 0.3, 0.02);
			}
		});
	}

	@Override
	public void giveItem(String itemId, int count) {
		onServer(player -> {
			Item item = BuiltInRegistries.ITEM.get(id(itemId));
			player.getInventory().add(new ItemStack(item, count));
		});
	}

	@Override
	public void playSound(String soundId) {
		onServer(player -> {
			SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(id(soundId));
			if (sound != null) {
				player.serverLevel().playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
			}
		});
	}

	@Override
	public int playerX() {
		return callOnServer(player -> player.blockPosition().getX(), 0);
	}

	@Override
	public int playerY() {
		return callOnServer(player -> player.blockPosition().getY(), 0);
	}

	@Override
	public int playerZ() {
		return callOnServer(player -> player.blockPosition().getZ(), 0);
	}

	@Override
	public boolean isDay() {
		return callOnServer(player -> player.serverLevel().isDay(), true);
	}

	private static ResourceLocation id(String value) {
		return ResourceLocation.parse(value);
	}

	private static BlockPos pos(ServerPlayer player, int dx, int dy, int dz) {
		return player.blockPosition().offset(dx, dy, dz);
	}

	private static void onServer(Consumer<ServerPlayer> action) {
		callOnServer(player -> {
			action.accept(player);
			return null;
		}, null);
	}

	private static <T> T callOnServer(Function<ServerPlayer, T> action, T fallback) {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer localPlayer = client.player;
		if (!client.hasSingleplayerServer() || localPlayer == null) {
			return fallback;
		}
		MinecraftServer server = client.getSingleplayerServer();
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<T> result = new AtomicReference<>(fallback);
		AtomicReference<RuntimeException> failure = new AtomicReference<>();
		// Never Minecraft#execute here: it runs inline when already on the caller's thread,
		// and lesson code is never on the server thread anyway.
		server.executeIfPossible(() -> {
			try {
				ServerPlayer serverPlayer = server.getPlayerList().getPlayer(localPlayer.getUUID());
				if (serverPlayer != null) {
					result.set(action.apply(serverPlayer));
				}
			} catch (RuntimeException e) {
				failure.set(e);
			} finally {
				latch.countDown();
			}
		});
		boolean completed;
		try {
			completed = latch.await(SERVER_CALL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return fallback;
		}
		if (!completed) {
			// The server thread never picked the task up -- almost always because the game is
			// paused (it stops ticking then). Say so instead of handing back a bogus default.
			throw new IllegalStateException(
					"Couldn't reach the world -- the game looks paused. Return to the game and run again.");
		}
		if (failure.get() != null) {
			throw failure.get();
		}
		return result.get();
	}
}
