package xyz.xdmatthewbx.chatlog.modules;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandBuildContext;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Pair;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.ChunkCache;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.ChatLogConfig;
import xyz.xdmatthewbx.chatlog.render.Renderer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static xyz.xdmatthewbx.chatlog.ChatLog.*;

@Module
public class ESPModule extends BaseModule {

	public static final String MODULE_ID = "esp";
	public static ESPModule INSTANCE;

	public boolean enabled = true;

	public ESPModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}

	private final ConcurrentHashMap<BlockPos, Integer> blockCache = new ConcurrentHashMap<>();
	private final List<Pair<Entity, Integer>> entityCache = new ArrayList<>();
	private final ConcurrentHashMap<String, Predicate<CachedBlockPosition>> blockPredicateCache = new ConcurrentHashMap<>();
	private final AtomicReference<ClientWorld> cachedWorld = new AtomicReference<>();

	public List<Pair<Predicate<CachedBlockPosition>, Integer>> blockFilters = new ArrayList<>();
	public List<ImmutableTriple<EntitySelector, ChatLogConfig.EntityColorMode, Integer>> entityFilters = new ArrayList<>();

	private BlockPredicateArgumentType blockPredicateArgumentType;

	private Predicate<CachedBlockPosition> getBlockPredicate(String selector) {
		return cachedBlockPosition -> {
			if (cachedBlockPosition.getWorld() == null) return false;
			try {
				Predicate<CachedBlockPosition> predicate = blockPredicateCache.get(selector);
				if (predicate == null) {
					predicate = blockPredicateArgumentType.parse(new StringReader(selector));
					blockPredicateCache.put(selector, predicate);
				}

				return predicate.test(cachedBlockPosition);
			} catch (CommandSyntaxException e) {
				return false;
			}
		};
	}

	public void cacheBlockPos(BlockPos blockPos) {
		if (ChatLog.CLIENT.world == null) return;
		cacheBlockPos(ChatLog.CLIENT.world, blockPos);
	}

	public void cacheBlockPos(BlockView world, BlockPos blockPos) {
		if (!enabled || ChatLog.CLIENT.world == null || blockFilters.isEmpty()) return;
		CachedBlockPosition cachedBlockPosition = new CachedBlockPosition(ChatLog.CLIENT.world, blockPos, false);
		cachedBlockPosition.state = world.getBlockState(blockPos);
		cachedBlockPosition.blockEntity = world.getBlockEntity(blockPos);
		cachedBlockPosition.cachedEntity = true;
		Pair<Predicate<CachedBlockPosition>, Integer> match = null;
		for (var filter : blockFilters) {
			if (filter.getLeft().test(cachedBlockPosition)) {
				match = filter;
				break;
			}
		}
		synchronized (blockCache) {
			boolean isCached = blockCache.containsKey(blockPos) && blockCache.get(blockPos) != null;
			if (!isCached && match != null) {
				blockCache.put(blockPos, match.getRight());
			} else if (isCached && match == null) {
				blockCache.remove(blockPos, blockCache.get(blockPos)); // yes the get is necessary. Removing it causes scenarios where the cache is cleared
			}
		}
	}

	private final Stack<BlockPos> blockCacheQueue = new Stack<>();
	public void cacheBlockPosAsync(BlockPos blockPos) {
		if (blockCacheQueue.contains(blockPos))
			return;

		blockCacheQueue.push(blockPos);
	}

	private final Stack<BlockPos> subChunkCacheQueue = new Stack<>();
	public void cacheChunkAsync(BlockPos origin) {
		if (subChunkCacheQueue.contains(origin))
			return;

		subChunkCacheQueue.push(origin.toImmutable());
	}

	private void resetBlockCache() {
		blockCache.clear();
		blockCacheQueue.clear();
		subChunkCacheQueue.clear();
		blockPredicateCache.clear();
		cachedWorld.set(ChatLog.CLIENT.world);
		if (!enabled || blockFilters.isEmpty() || ChatLog.CLIENT.world == null) return;
		var commandBuildContext = new CommandBuildContext(ChatLog.CLIENT.world.getRegistryManager());
		blockPredicateArgumentType = BlockPredicateArgumentType.blockPredicate(commandBuildContext);
		var chunks = ChatLog.CLIENT.world.getChunkManager().chunks.chunks;
		for (int i = 0; i < chunks.length(); i++) {
			var chunk = chunks.get(i);
			if (chunk == null) continue;
//			for (int x = 0; x < 16; x++) {
//				for (int y = chunk.getBottomY(); y < chunk.getTopY(); y++) {
//					for (int z = 0; z < 16; z++) {
//						BlockPos blockPos = chunk.getPos().getBlockPos(x, y, z);
//						cacheBlockPos(blockPos);
//					}
//				}
//			}
			SCAN_POOL.submit(() -> {
				for (int y = chunk.getBottomY(); y < chunk.getTopY(); y += 16) {
					cacheChunkAsync(chunk.getPos().getBlockPos(0, y, 0).toImmutable());
				}
			});
		}
	}

	public void updateEntityCache() {
		entityCache.clear();

		if (CLIENT.world == null || CLIENT.player == null || entityFilters.isEmpty())
			return;

		for (var filter : entityFilters) {
			var entityFilter = filter.getLeft();

			var offsetPosition = entityFilter.positionOffset.apply(CLIENT.player.getPos());
			var positionPredicate = entityFilter.getPositionPredicate(offsetPosition);
			var boundsBox = entityFilter.box != null ? entityFilter.box.offset(offsetPosition) : null;
			var unsortedEntities =
				StreamSupport.stream(CLIENT.world.getEntities().spliterator(), true)
					.filter(x -> entityFilter.box == null || boundsBox.contains(x.getPos()))
					.filter(positionPredicate)
					.collect(Collectors.toList());
			var entities = entityFilter.getEntities(offsetPosition, unsortedEntities);

			for (Entity entity : entities) {
				int color;
				switch (filter.getMiddle()) {
					case MANUAL -> color = filter.getRight();
					case TEAM -> color = entity.getTeamColorValue();
					default -> throw new IllegalStateException();
				}

				entityCache.add(new Pair<>(entity, color));
			}
		}
	}

	private final ForkJoinPool SCAN_POOL = new ForkJoinPool();

	@Override
	public void onInitializeClient() {
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			enabled = chatLogConfig.main.espModule.enabled;

			var legacyBlockFilters = CONFIG.get().main.espModule.blockFilters;
			if (!legacyBlockFilters.isEmpty()) {
				var legacyGroup = new ChatLogConfig.BlockESPFilterGroup();
				legacyGroup.enabled = chatLogConfig.main.espModule.enabled;
				legacyGroup.name = "MIGRATED FILTERS";
				legacyGroup.filters = legacyBlockFilters;
				chatLogConfig.main.espModule.blockFilterGroups.add(legacyGroup);
				chatLogConfig.main.espModule.blockFilters = List.of();
			}

			var legacyEntityFilters = CONFIG.get().main.espModule.entityFilters;
			if (!legacyEntityFilters.isEmpty()) {
				var legacyGroup = new ChatLogConfig.EntityESPFilterGroup();
				legacyGroup.enabled = chatLogConfig.main.espModule.enabled;
				legacyGroup.name = "MIGRATED FILTERS";
				legacyGroup.filters = legacyEntityFilters;
				chatLogConfig.main.espModule.entityFilterGroups.add(legacyGroup);
				chatLogConfig.main.espModule.entityFilters = List.of();
			}

			blockFilters.clear();
			for (var filterGroup : chatLogConfig.main.espModule.blockFilterGroups) {
				if (!filterGroup.enabled)
					continue;

				for (var filter : filterGroup.filters) {
					if (!filter.enabled)
						continue;

					blockFilters.add(new Pair<>(getBlockPredicate(filter.blockFilter), filter.color));
				}
			}

			entityFilters.clear();
			for (var filterGroup : chatLogConfig.main.espModule.entityFilterGroups) {
				if (!filterGroup.enabled)
					continue;

				for (var filter : filterGroup.filters) {
					if (!filter.enabled)
						continue;

					try {
						entityFilters.add(
							new ImmutableTriple<>(
								new EntitySelectorReader(
									new StringReader(filter.entityFilter)
								).read(),
								filter.entityColorMode,
								filter.color));
					} catch (CommandSyntaxException ignored) {}
				}
			}

			resetBlockCache();

			return ActionResult.PASS;
		});

		ClientTickEvents.START.register(client -> {
			if (ChatLog.CLIENT.world == null)
				return;

			assert CLIENT.world != null;
			var chunks = CLIENT.world.getChunkManager().chunks.chunks;
			for (int i = 0; i < chunks.length(); i++) {
				var chunk = chunks.get(i);
				if (chunk == null) continue;
				try {
					SCAN_POOL.submit(() ->
						chunk.getBlockEntities().keySet().forEach(this::cacheBlockPos));
				} catch (ConcurrentModificationException ignored) { }
			}

			while (!blockCacheQueue.isEmpty()) {
				BlockPos block = blockCacheQueue.pop();
//				cacheBlockPos(block);
				SCAN_POOL.submit(() -> cacheBlockPos(block));
//				BlockPos subChunkOrigin = new BlockPos(block.getX() & ~(0xf), block.getY() & ~(0xf), block.getZ() & ~(0xf)).toImmutable();
//				cacheChunkAsync(subChunkOrigin);
			}

			while (!subChunkCacheQueue.isEmpty()) {
				BlockPos origin = subChunkCacheQueue.pop();
				ChunkCache chunkCache = new ChunkCache(ChatLog.CLIENT.world, origin, origin.add(15, 15, 15));
				SCAN_POOL.submit(() -> {
					for (int x = 0; x < 16; x++) {
						for (int z = 0; z < 16; z++) {
							for (int y = 0; y < 16; y++) {
								cacheBlockPos(chunkCache, origin.add(x, y, z));
							}
						}
					}
				});
			}

			this.updateEntityCache();
		});

		new Renderer() {
			@Override
			public void render(MatrixStack matrix, BufferBuilder buffer, Camera camera) {
				if (enabled && CLIENT.world != null && CLIENT.player != null) {
					final float tickDelta = getTickDelta();
					if (cachedWorld.get() != CLIENT.world) resetBlockCache();
					for (var blockPos : blockCache.keySet()) {
						Integer color = blockCache.get(blockPos);
						if (color != null && CLIENT.world.getChunkManager().isChunkLoaded(ChunkSectionPos.getSectionCoord(blockPos.getX()), ChunkSectionPos.getSectionCoord(blockPos.getZ()))) {
							var chunk = CLIENT.world.getChunkManager().getChunk(ChunkSectionPos.getSectionCoord(blockPos.getX()), ChunkSectionPos.getSectionCoord(blockPos.getZ()));
							if (chunk != null) {
								BlockState state = chunk.getBlockState(blockPos);
								VoxelShape shape = state.getOutlineShape(CLIENT.world, blockPos, ShapeContext.of(CLIENT.player));
								float red = (color >> 16 & 0xFF) / 255F;
								float green = (color >> 8 & 0xFF) / 255F;
								float blue = (color & 0xFF) / 255F;
								WorldRenderer.drawShapeOutline(matrix, buffer, shape, blockPos.getX() - camera.getPos().x, blockPos.getY() - camera.getPos().y, blockPos.getZ() - camera.getPos().z, red, green, blue, 1F);
							} else {
								throw new IllegalStateException();
//								Vec3d a = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
//								renderCuboid(matrix, buffer, camera, a, a.add(1, 1, 1), blockCache.get(blockPos), 255);
							}
						}
					}
					for (var entry : entityCache) {
						Entity entity = entry.getLeft();
						Box hitbox = entity.getBoundingBox();
						hitbox = hitbox
							.offset(hitbox.getCenter().multiply(-1, 0, -1))
							.offset(0, -hitbox.minY, 0)
							.offset(
								MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()),
								MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY()),
								MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ())
							);
						renderCuboid(matrix, buffer, camera, new Vec3d(hitbox.minX, hitbox.minY, hitbox.minZ), new Vec3d(hitbox.maxX, hitbox.maxY, hitbox.maxZ), entry.getRight(), 255);
					}
				}
			}
		};
	}

}
