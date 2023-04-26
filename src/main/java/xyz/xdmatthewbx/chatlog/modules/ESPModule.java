package xyz.xdmatthewbx.chatlog.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandBuildContext.Configurable;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.joml.Matrix4f;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.ChatLogConfig;
import xyz.xdmatthewbx.chatlog.ChatLogConfig.BlockESPFilter;
import xyz.xdmatthewbx.chatlog.ChatLogConfig.BlockESPFilterGroup;
import xyz.xdmatthewbx.chatlog.ChatLogConfig.EntityESPFilter;
import xyz.xdmatthewbx.chatlog.ChatLogConfig.EntityESPFilterGroup;
import xyz.xdmatthewbx.chatlog.render.Renderer;
import xyz.xdmatthewbx.chatlog.util.SimplePalettedContainer;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
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

	private final ConcurrentHashMap<BlockPos, SubChunkCache> blockCache = new ConcurrentHashMap<>();
	private final List<Tuple<Entity, Integer>> entityCache = new ArrayList<>();
	private final ConcurrentHashMap<String, Predicate<BlockInWorld>> blockPredicateCache = new ConcurrentHashMap<>();
	private final AtomicReference<ClientLevel> cachedWorld = new AtomicReference<>();

	public List<Tuple<Predicate<BlockInWorld>, Integer>> blockFilters = new ArrayList<>();
	public List<ImmutableTriple<EntitySelector, ChatLogConfig.EntityColorMode, Integer>> entityFilters = new ArrayList<>();

	private final List<ForkJoinTask<?>> submittedScans = new LinkedList<>();

	private BlockPredicateArgument blockPredicateArgumentType;

	private void submitAsyncTask(Runnable r) {
		ExecutorService service = Util.backgroundExecutor();
		if (service instanceof ForkJoinPool) {
			synchronized (submittedScans) {
				submittedScans.add(((ForkJoinPool)service).submit(r));
			}
		} else {
			/* assume direct executor */
			service.submit(r);
		}
	}

	private Predicate<BlockInWorld> getBlockPredicate(String selector) {
		return cachedBlockPosition -> {
			if (cachedBlockPosition.getLevel() == null)
				return false;

			Predicate<BlockInWorld> predicate =
				blockPredicateCache
					.computeIfAbsent(
						selector,
						s -> {
							try {
								return blockPredicateArgumentType.parse(new StringReader(s));
							} catch (CommandSyntaxException e) {
								return ignored -> false;
							}
						}
					);

			return predicate.test(cachedBlockPosition);
		};
	}

	public void cacheBlockPos(BlockPos blockPos) {
		if (ChatLog.CLIENT.level == null) return;
		cacheBlockPos(ChatLog.CLIENT.level, blockPos);
	}

	public void cacheBlockPos(BlockGetter world, BlockPos blockPos) {
		if (!enabled || ChatLog.CLIENT.level == null || blockFilters.isEmpty()) return;
		BlockInWorld cachedBlockPosition = new BlockInWorld(ChatLog.CLIENT.level, blockPos, false);
		cachedBlockPosition.state = world.getBlockState(blockPos);
		cachedBlockPosition.entity = world.getBlockEntity(blockPos);
		cachedBlockPosition.cachedEntity = true;
		Tuple<Predicate<BlockInWorld>, Integer> match = null;
		for (var filter : blockFilters) {
			if (filter.getA().test(cachedBlockPosition)) {
				match = filter;
				break;
			}
		}
		BlockPos subChunkOrigin = new BlockPos(blockPos.getX() & ~(0xf), blockPos.getY() & ~(0xf), blockPos.getZ() & ~(0xf));
		synchronized (blockCache) {
			SubChunkCache subChunk = blockCache.get(subChunkOrigin);
			if (match != null) {
				if (subChunk == null) {
					subChunk = new SubChunkCache();
					blockCache.put(subChunkOrigin, subChunk);
				}
				subChunk.setColorForBlockPos(blockPos, match.getB());
			} else if (subChunk != null) {
				subChunk.setColorForBlockPos(blockPos, null);
			}
		}
	}

	private final LinkedHashSet<BlockPos> blockCacheQueue = new LinkedHashSet<>();
	public void cacheBlockPosAsync(BlockPos blockPos) {
		synchronized (blockCacheQueue) {
			if (blockCacheQueue.contains(blockPos))
				return;

			blockCacheQueue.add(blockPos);
		}
	}

	private final LinkedHashSet<BlockPos> subChunkCacheQueue = new LinkedHashSet<>();
	public void cacheChunkAsync(BlockPos origin) {
		synchronized (subChunkCacheQueue) {
			origin = origin.immutable();
			if (subChunkCacheQueue.contains(origin))
				return;

			subChunkCacheQueue.add(origin);
		}
	}

	private void resetBlockCache() {
		// make sure all scans stop
		synchronized (submittedScans) {
			/* cancel all current scans first */
			for (ForkJoinTask<?> task : submittedScans) {
				task.cancel(true);
			}
			/* now join to ensure they all actually aren't running */
			for (ForkJoinTask<?> task : submittedScans) {
				task.quietlyJoin();
			}
			submittedScans.clear();
		}
		blockCache.values().forEach(SubChunkCache::freeCurrentBuffer);
		blockCache.clear();
		synchronized (blockCacheQueue) {
			blockCacheQueue.clear();
		}
		synchronized (subChunkCacheQueue) {
			subChunkCacheQueue.clear();
		}
		blockPredicateCache.clear();
		cachedWorld.set(ChatLog.CLIENT.level);
		if (!enabled || blockFilters.isEmpty() || ChatLog.CLIENT.level == null) return;
		var commandBuildContext = CommandBuildContext.configurable(ChatLog.CLIENT.level.registryAccess(), ChatLog.CLIENT.level.enabledFeatures());
		blockPredicateArgumentType = BlockPredicateArgument.blockPredicate(commandBuildContext);
		var chunks = ChatLog.CLIENT.level.getChunkSource().storage.chunks;
		for (int i = 0; i < chunks.length(); i++) {
			var chunk = chunks.get(i);
			if (chunk == null) continue;
			for (int y = chunk.getMinBuildHeight(); y < chunk.getMaxBuildHeight(); y += 16) {
				cacheChunkAsync(chunk.getPos().getBlockAt(0, y, 0).immutable());
			}
		}
	}

	public void updateEntityCache() {
		entityCache.clear();

		if (CLIENT.level == null || CLIENT.player == null || entityFilters.isEmpty())
			return;

		for (var filter : entityFilters) {
			var entityFilter = filter.getLeft();

			var offsetPosition = entityFilter.position.apply(CLIENT.player.position());
			var positionPredicate = entityFilter.getPredicate(offsetPosition);
			var boundsBox = entityFilter.aabb != null ? entityFilter.aabb.move(offsetPosition) : null;
			var unsortedEntities =
				StreamSupport.stream(CLIENT.level.entitiesForRendering().spliterator(), true)
					.filter(x -> entityFilter.type.tryCast(x) != null)
					.filter(x -> entityFilter.aabb == null || boundsBox.contains(x.position()))
					.filter(positionPredicate)
					.collect(Collectors.toList());
			var entities = entityFilter.sortAndLimit(offsetPosition, unsortedEntities);

			for (Entity entity : entities) {
				int color;
				switch (filter.getMiddle()) {
					case MANUAL -> color = filter.getRight();
					case TEAM -> color = entity.getTeamColor();
					default -> throw new IllegalStateException();
				}

				entityCache.add(new Tuple<>(entity, color));
			}
		}
	}

	/**
	 * Generate a buffer of the required outlines for the given subchunk.
	 * @param cache subchunk cache object that contains block colors adn shapes
	 * @param chunkOrigin position of this subchunk (given by block with lowest XYZ coordinates)
	 * @return the compiled buffer, or null if it couldn't be compiled due to
	 * chunk not existing
	 */
	private VertexBuffer generateBuffer(SubChunkCache cache, BlockPos chunkOrigin) {
		ClientChunkCache chunkManager = CLIENT.level.getChunkSource();
		var chunk = chunkManager.getChunk(SectionPos.blockToSectionCoord(chunkOrigin.getX()), SectionPos.blockToSectionCoord(chunkOrigin.getZ()), ChunkStatus.FULL, false);
		if (chunk == null)
			return null;
		final CollisionContext shapeContext = CollisionContext.of(CLIENT.player);
		PoseStack stack = new PoseStack();
		VertexBuffer buffer = new VertexBuffer();
		BufferBuilder builder = new BufferBuilder(256);
		builder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
		for (BlockPos blockPos : BlockPos.betweenClosed(chunkOrigin, chunkOrigin.offset(15, 15, 15))) {
			var color = cache.getColorForBlockPos(blockPos);
			if (color != null) {
				BlockState blockState = chunk.getBlockState(blockPos);
				VoxelShape shape =
					blockState.getShape(
						CLIENT.level,
						blockPos,
						shapeContext
					);
				float red = (color >> 16 & 0xFF) / 255F;
				float green = (color >> 8 & 0xFF) / 255F;
				float blue = (color & 0xFF) / 255F;
				var x = blockPos.getX() - chunkOrigin.getX();
				var y = blockPos.getY() - chunkOrigin.getY();
				var z = blockPos.getZ() - chunkOrigin.getZ();
				LevelRenderer.renderShape(stack, builder, shape, x, y, z, red, green, blue, 1F);
			}
		}
		BufferBuilder.RenderedBuffer rendered = builder.end();
		buffer.bind();
		buffer.upload(rendered);
		// no need to unbind here as we are about to render it
		return buffer;
	}

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

					blockFilters.add(new Tuple<>(getBlockPredicate(filter.blockFilter), filter.color));
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
								new EntitySelectorParser(
									new StringReader(filter.entityFilter)
								).parse(),
								filter.entityColorMode,
								filter.color));
					} catch (CommandSyntaxException ignored) {}
				}
			}

			resetBlockCache();

			return InteractionResult.PASS;
		});

		ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
			for (int y = chunk.getMinBuildHeight(); y < chunk.getMaxBuildHeight(); y += 16) {
				cacheChunkAsync(chunk.getPos().getBlockAt(0, y, 0).immutable());
			}
		});

		ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
			for (int y = chunk.getMinBuildHeight(); y < chunk.getMaxBuildHeight(); y += 16) {
				SubChunkCache cache = blockCache.remove(chunk.getPos().getBlockAt(0, y, 0).immutable());
				if (cache != null)
					cache.freeCurrentBuffer();
			}
		});

		ClientTickEvents.END_WORLD_TICK.register(client -> {
			if (ChatLog.CLIENT.level == null)
				return;

			assert CLIENT.level != null;

			synchronized (submittedScans) {
				submittedScans.removeIf(ForkJoinTask::isDone);
			}

			if (cachedWorld.get() != CLIENT.level)
				resetBlockCache();

			synchronized (blockCacheQueue) {
				for (BlockPos block : blockCacheQueue) {
					PathNavigationRegion chunkCache = new PathNavigationRegion(ChatLog.CLIENT.level, block, block);
					submitAsyncTask(() -> cacheBlockPos(chunkCache, block));
				}
				blockCacheQueue.clear();
			}

			synchronized (subChunkCacheQueue) {
				PathNavigationRegion chunkCache = null;
				BlockPos prevOrigin = null;
				for (BlockPos origin : subChunkCacheQueue) {
					// ChunkCache uses full world height, avoid recreating new one
					// for each subchunk
					if (prevOrigin == null || prevOrigin.getX() != origin.getX() || prevOrigin.getZ() != origin.getZ()) {
						prevOrigin = origin;
						chunkCache = new PathNavigationRegion(ChatLog.CLIENT.level,
							new BlockPos(origin.getX(), ChatLog.CLIENT.level.getMinBuildHeight(), origin.getZ()),
							new BlockPos(origin.getX(), ChatLog.CLIENT.level.getMaxBuildHeight(), origin.getZ())
						);
					}
					final PathNavigationRegion targetCache = chunkCache;
					submitAsyncTask(() -> {
						for (int x = 0; x < 16; x++) {
							for (int z = 0; z < 16; z++) {
								for (int y = 0; y < 16; y++) {
									cacheBlockPos(targetCache, origin.offset(x, y, z));
								}
							}
						}
					});
				}
				subChunkCacheQueue.clear();
			}

			this.updateEntityCache();
		});

		new Renderer() {
			@Override
			public void render(PoseStack matrix, BufferBuilder buffer, Camera camera) {
				if (enabled && CLIENT.level != null && CLIENT.player != null) {
					final float tickDelta = getTickDelta();
					Frustum frustum = new Frustum(matrix.last().pose(), CLIENT.gameRenderer.getProjectionMatrix(CLIENT.options.fov().get()));
					Vec3 cameraPos = camera.getPosition();
					frustum.prepare(cameraPos.x, cameraPos.y, cameraPos.z);
					frustum.offsetToFullyIncludeCameraCube(8);
					Matrix4f projMatrix = RenderSystem.getProjectionMatrix();
					// loop over every subchunk in the cache
					// this is a hot path (runs every frame) so keep it fast
					for (Map.Entry<BlockPos, SubChunkCache> entry : blockCache.entrySet()) {
						var cache = entry.getValue();
						// only render visible subchunks
						if (cache.isVisible(frustum, entry.getKey())) {
							VertexBuffer cacheBuffer;
							// synchronize so that we do not miss any incoming
							// block changes (which would cause an old cache
							// to be stored)
							// in most cases this will be instant since the
							// cache will be built
							synchronized (cache) {
								cacheBuffer = cache.currentBuffer;
								if (cacheBuffer == null) {
									cacheBuffer = generateBuffer(cache, entry.getKey());
									if (cacheBuffer == null)
										continue;
									cache.currentBuffer = cacheBuffer;
									// buffer is bound in generateBuffer
								} else
									cacheBuffer.bind();
							}
							matrix.pushPose();
							// move to subchunk-relative position
							matrix.translate(entry.getKey().getX() - (float)cameraPos.x, entry.getKey().getY() - (float)cameraPos.y, entry.getKey().getZ() - (float)cameraPos.z);
							cacheBuffer.drawWithShader(matrix.last().pose(), projMatrix, GameRenderer.getPositionColorShader());
							matrix.popPose();
						}
					}
					// now unbind
					VertexBuffer.unbind();
					for (var entry : entityCache) {
						Entity entity = entry.getA();
						AABB hitbox = entity.getBoundingBox();
						hitbox = hitbox
							.move(hitbox.getCenter().multiply(-1, 0, -1))
							.move(0, -hitbox.minY, 0)
							.move(
								Mth.lerp(tickDelta, entity.xOld, entity.getX()),
								Mth.lerp(tickDelta, entity.yOld, entity.getY()),
								Mth.lerp(tickDelta, entity.zOld, entity.getZ())
							);
						renderCuboid(matrix, buffer, camera, new Vec3(hitbox.minX, hitbox.minY, hitbox.minZ), new Vec3(hitbox.maxX, hitbox.maxY, hitbox.maxZ), entry.getB(), 255);
					}
				}
			}
		};
	}

	static final class SubChunkCache {
		private SimplePalettedContainer<Integer> colorPalette;

		private byte[] colorByLocalBlockPos;
		private VertexBuffer currentBuffer;
		private int frameFrustumLastChecked = 0;
		private boolean isFrustumVisible = true;

		public SubChunkCache() {
			this.colorPalette = new SimplePalettedContainer<>(16);
			/* use ID 0 to represent no color */
			this.colorPalette.put(null);
			this.colorByLocalBlockPos = null;
			this.currentBuffer = null;
		}

		public boolean isVisible(Frustum frustum, BlockPos origin) {
			boolean performFrustumCheck = !isFrustumVisible || (frameFrustumLastChecked % 4) == 0;
			if (performFrustumCheck) {
				AABB box = new AABB(origin, origin.offset(16, 16, 16));
				isFrustumVisible = frustum.isVisible(box);
			}
			frameFrustumLastChecked++;
			return isFrustumVisible;
		}

		private static int arrayIndex(BlockPos pos) {
			return ((pos.getY() & 15) << 8) | ((pos.getZ() & 15) << 4) | (pos.getX() & 15);
		}

		public Integer getColorForBlockPos(BlockPos pos) {
			if (this.colorByLocalBlockPos == null)
				return null;
			int idx = colorByLocalBlockPos[arrayIndex(pos)];
			return colorPalette.get(idx);
		}

		public void freeCurrentBuffer() {
			VertexBuffer buffer;
			synchronized (this) {
				buffer = this.currentBuffer;
				this.currentBuffer = null;
			}
			if (buffer != null)
				Minecraft.getInstance().execute(buffer::close);
		}

		public void setColorForBlockPos(BlockPos pos, Integer i) {
			synchronized (this) {
				if (this.colorByLocalBlockPos == null) {
					if (i == null)
						return;
					this.colorByLocalBlockPos = new byte[4096];
				}
				try {
					this.colorByLocalBlockPos[arrayIndex(pos)] = i != null ? (byte)this.colorPalette.put(i) : 0;
					if (this.currentBuffer != null) {
						this.freeCurrentBuffer();
					}
				} catch (IndexOutOfBoundsException e) {
					ChatLog.LOGGER.error("Couldn't set color for pos", e);
				}
			}
		}
	}
}
