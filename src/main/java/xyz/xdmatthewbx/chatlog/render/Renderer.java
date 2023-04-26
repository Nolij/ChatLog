package xyz.xdmatthewbx.chatlog.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import xyz.xdmatthewbx.chatlog.ChatLog;

import java.util.LinkedList;
import java.util.List;

public abstract class Renderer {

	public static final Tesselator TESSELLATOR = Tesselator.getInstance();
	public static final BufferBuilder BUFFER = TESSELLATOR.getBuilder();

	private static final List<Renderer> RENDERERS = new LinkedList<>();

	@FunctionalInterface
	public interface RenderCall {
		void render(PoseStack matrix, BufferBuilder buffer, Camera camera);
	}

	public static void render(PoseStack matrix, Camera camera, RenderCall callback) {
		matrix.pushPose();

		if (ChatLog.CONFIG.get().main.render.allowRenderThroughBlocks) {
			RenderSystem.disableDepthTest();
		}
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.lineWidth(ChatLog.CONFIG.get().main.render.lineWidth);

		BUFFER.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

		callback.render(matrix, BUFFER, camera);

		if (BUFFER.building()) {
			TESSELLATOR.end();
		}
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();

		matrix.popPose();
		RenderSystem.applyModelViewMatrix();
	}

	public static void renderAll(PoseStack matrix, Camera camera) {
		if (RENDERERS.isEmpty()) return;

		render(matrix, camera, (_matrix, buffer, _camera) -> RENDERERS.forEach(renderer -> renderer.render(_matrix, buffer, _camera)));
	}

	public static void renderLine(PoseStack matrix, BufferBuilder buffer, Camera camera, Vec3 a, Vec3 b, float red, float green, float blue, float alpha) {
		Vec3 pos = camera.getPosition();

		buffer
			.vertex(
				matrix.last().pose(),
				(float) (a.x - pos.x),
				(float) (a.y - pos.y),
				(float) (a.z - pos.z)
			)
			.color(red, green, blue, alpha)
			.uv2(1)
			.endVertex();
		buffer
			.vertex(
				matrix.last().pose(),
				(float) (b.x - pos.x),
				(float) (b.y - pos.y),
				(float) (b.z - pos.z)
			)
			.color(red, green, blue, alpha)
			.uv2(1)
			.endVertex();
	}

	public static void renderCuboid(PoseStack matrix, BufferBuilder buffer, Camera camera, Vec3 a, Vec3 b, float red, float green, float blue, float alpha) {
//		Box box = new Box(a, b);
//		WorldRenderer.drawBox(matrix, buffer, box, red, green, blue, alpha);
		Vec3 size = b.subtract(a);
		renderLine(matrix, buffer, camera, a, a.add(size.x, 0, 0), red, green, blue, alpha);
		renderLine(matrix, buffer, camera, a, a.add(0, size.y, 0), red, green, blue, alpha);
		renderLine(matrix, buffer, camera, a, a.add(0, 0, size.z), red, green, blue, alpha);
		renderLine(matrix, buffer, camera, a.add(size.x, 0, size.z), a.add(size.x, 0, 0), red, green, blue, alpha);
		renderLine(matrix, buffer, camera, a.add(size.x, 0, size.z), b, red, green, blue, alpha);
		renderLine(matrix, buffer, camera, a.add(size.x, 0, size.z), a.add(0, 0, size.z), red, green, blue, alpha);
		renderLine(matrix, buffer, camera, a.add(size.x, size.y, 0), a.add(size.x, 0, 0), red, green, blue, alpha);
		renderLine(matrix, buffer, camera, a.add(size.x, size.y, 0), a.add(0, size.y, 0), red, green, blue, alpha);
		renderLine(matrix, buffer, camera, a.add(size.x, size.y, 0), b, red, green, blue, alpha);
		renderLine(matrix, buffer, camera, a.add(0, size.y, size.z), a.add(0, 0, size.z), red, green, blue, alpha);
		renderLine(matrix, buffer, camera, a.add(0, size.y, size.z), a.add(0, size.y, 0), red, green, blue, alpha);
		renderLine(matrix, buffer, camera, a.add(0, size.y, size.z), b, red, green, blue, alpha);
	}

	public static void renderCuboid(PoseStack matrix, BufferBuilder buffer, Camera camera, Vec3 a, Vec3 b, int color, int alpha) {
		int red = color >> 16 & 0xFF;
		int green = color >> 8 & 0xFF;
		int blue = color & 0xFF;
		renderCuboid(matrix, buffer, camera, a, b, ((float) red) / 255F, ((float) green) / 255F, ((float) blue) / 255F, ((float) alpha) / 255F);
	}

	public Renderer() {
		RENDERERS.add(this);
	}

	public abstract void render(PoseStack matrix, BufferBuilder buffer, Camera camera);

}
