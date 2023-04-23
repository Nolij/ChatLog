package xyz.xdmatthewbx.chatlog.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientChunkManager;
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
import net.minecraft.world.chunk.ChunkStatus;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.joml.Matrix4f;
import xyz.xdmatthewbx.chatlog.ChatLog;
import xyz.xdmatthewbx.chatlog.ChatLogConfig;
import xyz.xdmatthewbx.chatlog.render.Renderer;
import xyz.xdmatthewbx.chatlog.util.SimplePalettedContainer;

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

	private final ConcurrentHashMap<BlockPos, SubChunkCache> blockCache = new ConcurrentHashMap<>();
	private final List<Pair<Entity, Integer>> entityCache = new ArrayList<>();
	private final ConcurrentHashMap<String, Predicate<CachedBlockPosition>> blockPredicateCache = new ConcurrentHashMap<>();
	private final AtomicReference<ClientWorld> cachedWorld = new AtomicReference<>();

	public List<Pair<Predicate<CachedBlockPosition>, Integer>> blockFilters = new ArrayList<>();
	public List<ImmutableTriple<EntitySelector, ChatLogConfig.EntityColorMode, Integer>> entityFilters = new ArrayList<>();

	private BlockPredicateArgumentType blockPredicateArgumentType;

	private Predicate<CachedBlockPosition> getBlockPredicate(String selector) {
		return cachedBlockPosition -> {
			if (cachedBlockPosition.getWorld() == null)
				return false;

			Predicate<CachedBlockPosition> predicate =
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
		BlockPos subChunkOrigin = new BlockPos(blockPos.getX() & ~(0xf), blockPos.getY() & ~(0xf), blockPos.getZ() & ~(0xf));
		synchronized (blockCache) {
			SubChunkCache subChunk = blockCache.get(subChunkOrigin);
			if (match != null) {
				if (subChunk == null) {
					subChunk = new SubChunkCache();
					blockCache.put(subChunkOrigin, subChunk);
				}
				subChunk.setColorForBlockPos(blockPos, match.getRight());
			} else if (subChunk != null) {
				subChunk.setColorForBlockPos(blockPos, null);
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
		var commandBuildContext = CommandBuildContext.createConfigurable(ChatLog.CLIENT.world.getRegistryManager(), ChatLog.CLIENT.world.getEnabledFlags());
		blockPredicateArgumentType = BlockPredicateArgumentType.blockPredicate(commandBuildContext);
		var chunks = ChatLog.CLIENT.world.getChunkManager().chunks.chunks;
		for (int i = 0; i < chunks.length(); i++) {
			var chunk = chunks.get(i);
			if (chunk == null) continue;
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
					.filter(x -> entityFilter.entityFilter.downcast(x) != null)
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

	/**
	 * Generate a buffer of the required outlines for the given subchunk.
	 * @param cache subchunk cache object that contains block colors adn shapes
	 * @param chunkOrigin position of this subchunk (given by block with lowest XYZ coordinates)
	 * @return the compiled buffer, or null if it couldn't be compiled due to
	 * chunk not existing
	 */
	private VertexBuffer generateBuffer(SubChunkCache cache, BlockPos chunkOrigin) {
		ClientChunkManager chunkManager = CLIENT.world.getChunkManager();
		var chunk = chunkManager.getChunk(ChunkSectionPos.getSectionCoord(chunkOrigin.getX()), ChunkSectionPos.getSectionCoord(chunkOrigin.getZ()), ChunkStatus.FULL, false);
		if (chunk == null)
			return null;
		final ShapeContext shapeContext = ShapeContext.of(CLIENT.player);
		MatrixStack stack = new MatrixStack();
		VertexBuffer buffer = new VertexBuffer();
		BufferBuilder builder = new BufferBuilder(256);
		builder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
		for (BlockPos blockPos : BlockPos.iterate(chunkOrigin, chunkOrigin.add(15, 15, 15))) {
			var color = cache.getColorForBlockPos(blockPos);
			if (color != null) {
				BlockState blockState = chunk.getBlockState(blockPos);
				VoxelShape shape =
					blockState.getOutlineShape(
						CLIENT.world,
						blockPos,
						shapeContext
					);
				float red = (color >> 16 & 0xFF) / 255F;
				float green = (color >> 8 & 0xFF) / 255F;
				float blue = (color & 0xFF) / 255F;
				var x = blockPos.getX() - chunkOrigin.getX();
				var y = blockPos.getY() - chunkOrigin.getY();
				var z = blockPos.getZ() - chunkOrigin.getZ();
				WorldRenderer.drawShapeOutline(stack, builder, shape, x, y, z, red, green, blue, 1F);
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

		ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
			SCAN_POOL.submit(() -> {
				for (int y = chunk.getBottomY(); y < chunk.getTopY(); y += 16) {
					cacheChunkAsync(chunk.getPos().getBlockPos(0, y, 0).toImmutable());
				}
			});
		});

		ClientTickEvents.END_WORLD_TICK.register(client -> {
			if (ChatLog.CLIENT.world == null)
				return;

			assert CLIENT.world != null;

			if (cachedWorld.get() != CLIENT.world)
				resetBlockCache();

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
					Frustum frustum = new Frustum(matrix.peek().getModel(), CLIENT.gameRenderer.getBasicProjectionMatrix(CLIENT.options.getFov().get()));
					Vec3d cameraPos = camera.getPos();
					frustum.setPosition(cameraPos.x, cameraPos.y, cameraPos.z);
					frustum.offsetToIncludeCamera(8);
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
							matrix.push();
							// move to subchunk-relative position
							matrix.translate(entry.getKey().getX() - (float)cameraPos.x, entry.getKey().getY() - (float)cameraPos.y, entry.getKey().getZ() - (float)cameraPos.z);
							cacheBuffer.draw(matrix.peek().getModel(), projMatrix, GameRenderer.getPositionColorShader());
							matrix.pop();
						}
					}
					// now unbind
					VertexBuffer.unbind();
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
				Box box = new Box(origin, origin.add(16, 16, 16));
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
			if(buffer != null)
				MinecraftClient.getInstance().execute(buffer::close);
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
