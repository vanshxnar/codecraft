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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Deliberately in the default (unnamed) package: lesson code compiled by JavaRunner is
 * also unpackaged, so it can call Playground.xyz(...) with no import statement -- lesson
 * authors shouldn't need to know what a package is yet.
 *
 * All positions are relative offsets from the local player's current block position.
 * Scoped to singleplayer: calls are a no-op if there's no local integrated server running.
 */
public final class Playground {
	private static final long SERVER_CALL_TIMEOUT_MS = 2000;

	private Playground() {
	}

	public static void placeBlock(int dx, int dy, int dz, String blockId) {
		runOnServer(player -> {
			BlockPos pos = player.blockPosition().offset(dx, dy, dz);
			Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace(blockId));
			player.serverLevel().setBlock(pos, block.defaultBlockState(), 3);
		});
	}

	public static void breakBlock(int dx, int dy, int dz) {
		runOnServer(player -> {
			BlockPos pos = player.blockPosition().offset(dx, dy, dz);
			player.serverLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		});
	}

	/** Reads back the block id at a relative position, e.g. "stone" or "air" -- lets lesson code react to the world. */
	public static String getBlock(int dx, int dy, int dz) {
		return callOnServer(player -> {
			BlockPos pos = player.blockPosition().offset(dx, dy, dz);
			Block block = player.serverLevel().getBlockState(pos).getBlock();
			return BuiltInRegistries.BLOCK.getKey(block).getPath();
		}, "air");
	}

	public static void spawnEntity(int dx, int dy, int dz, String entityId) {
		runOnServer(player -> {
			BlockPos pos = player.blockPosition().offset(dx, dy, dz);
			EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.withDefaultNamespace(entityId));
			type.spawn(player.serverLevel(), pos, MobSpawnType.COMMAND);
		});
	}

	public static void teleportPlayer(int dx, int dy, int dz) {
		runOnServer(player -> {
			BlockPos pos = player.blockPosition().offset(dx, dy, dz);
			player.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
		});
	}

	/** Drops a shower of particles at a relative position, e.g. "happy_villager", "flame", "cloud", "explosion". */
	public static void particles(int dx, int dy, int dz, String particleId, int count) {
		runOnServer(player -> {
			BlockPos pos = player.blockPosition().offset(dx, dy, dz);
			ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.withDefaultNamespace(particleId));
			if (type instanceof SimpleParticleType simple) {
				player.serverLevel().sendParticles(simple,
						pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, count, 0.3, 0.3, 0.3, 0.02);
			}
		});
	}

	/** Puts an item in the player's inventory, e.g. "diamond", "torch". */
	public static void giveItem(String itemId, int count) {
		runOnServer(player -> {
			Item item = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(itemId));
			player.getInventory().add(new ItemStack(item, count));
		});
	}

	public static void say(String message) {
		runOnServer(player -> player.sendSystemMessage(Component.literal(message)));
	}

	private static void runOnServer(java.util.function.Consumer<ServerPlayer> action) {
		callOnServer(player -> {
			action.accept(player);
			return null;
		}, null);
	}

	private static <T> T callOnServer(Function<ServerPlayer, T> action, T defaultValue) {
		Minecraft client = Minecraft.getInstance();
		if (!client.hasSingleplayerServer()) {
			return defaultValue;
		}
		LocalPlayer localPlayer = client.player;
		if (localPlayer == null) {
			return defaultValue;
		}
		MinecraftServer server = client.getSingleplayerServer();
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<T> result = new AtomicReference<>(defaultValue);
		server.execute(() -> {
			try {
				ServerPlayer serverPlayer = server.getPlayerList().getPlayer(localPlayer.getUUID());
				if (serverPlayer != null) {
					result.set(action.apply(serverPlayer));
				}
			} finally {
				latch.countDown();
			}
		});
		try {
			latch.await(SERVER_CALL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return result.get();
	}
}
