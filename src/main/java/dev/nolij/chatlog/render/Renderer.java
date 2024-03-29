package dev.nolij.chatlog.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.nolij.chatlog.ChatLog;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedList;
import java.util.List;

public abstract class Renderer {
	
	private static final List<Renderer> RENDERERS = new LinkedList<>();

	@FunctionalInterface
	public interface RenderCall {
		void render(MatrixStack matrix, BufferBuilder buffer, Camera camera);
	}

	public static void render(MatrixStack matrix, Camera camera, RenderCall callback) {
		matrix.push();

		if (ChatLog.CONFIG.get().main.render.allowRenderThroughBlocks) {
			RenderSystem.disableDepthTest();
		}
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
		RenderSystem.lineWidth(ChatLog.CONFIG.get().main.render.lineWidth);

		var buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

		callback.render(matrix, buffer, camera);

		if (buffer.isBuilding()) {
			Tessellator.getInstance().draw();
		}
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();

		matrix.pop();
		RenderSystem.applyModelViewMatrix();
	}

	public static void renderAll(MatrixStack matrix, Camera camera) {
		if (RENDERERS.isEmpty()) return;

		render(matrix, camera, (_matrix, buffer, _camera) -> RENDERERS.forEach(renderer -> renderer.render(_matrix, buffer, _camera)));
	}

	public static void renderLine(MatrixStack matrix, BufferBuilder buffer, Camera camera, Vec3d a, Vec3d b, float red, float green, float blue, float alpha) {
		Vec3d pos = camera.getPos();

		buffer
			.vertex(
				matrix.peek().getPositionMatrix(),
				(float) (a.x - pos.x),
				(float) (a.y - pos.y),
				(float) (a.z - pos.z)
			)
			.color(red, green, blue, alpha)
			.light(1)
			.next();
		buffer
			.vertex(
				matrix.peek().getPositionMatrix(),
				(float) (b.x - pos.x),
				(float) (b.y - pos.y),
				(float) (b.z - pos.z)
			)
			.color(red, green, blue, alpha)
			.light(1)
			.next();
	}

	public static void renderCuboid(MatrixStack matrix, BufferBuilder buffer, Camera camera, Vec3d a, Vec3d b, float red, float green, float blue, float alpha) {
//		Box box = new Box(a, b);
//		WorldRenderer.drawBox(matrix, buffer, box, red, green, blue, alpha);
		Vec3d size = b.subtract(a);
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

	public static void renderCuboid(MatrixStack matrix, BufferBuilder buffer, Camera camera, Vec3d a, Vec3d b, int color, int alpha) {
		int red = color >> 16 & 0xFF;
		int green = color >> 8 & 0xFF;
		int blue = color & 0xFF;
		renderCuboid(matrix, buffer, camera, a, b, ((float) red) / 255F, ((float) green) / 255F, ((float) blue) / 255F, ((float) alpha) / 255F);
	}

	public Renderer() {
		RENDERERS.add(this);
	}

	public abstract void render(MatrixStack matrix, BufferBuilder buffer, Camera camera);

}
