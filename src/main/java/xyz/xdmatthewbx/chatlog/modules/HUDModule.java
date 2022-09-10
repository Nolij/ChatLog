package xyz.xdmatthewbx.chatlog.modules;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.component.TranslatableComponent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import xyz.xdmatthewbx.chatlog.render.Renderer;

import java.util.List;

import static xyz.xdmatthewbx.chatlog.ChatLog.*;

@Module
public class HUDModule extends BaseModule {

	public static final String MODULE_ID = "hud";
	public static HUDModule INSTANCE;

	public boolean enabled = true;

	public HUDModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}

	@Override
	public void onInitializeClient() {
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			enabled = chatLogConfig.main.hudModule.enabled;
			return ActionResult.PASS;
		});

		new Renderer() {
			@Override
			public void render(MatrixStack matrix, BufferBuilder buffer, Camera camera) {
				if (enabled && CLIENT.world != null && CLIENT.player != null) {
					final float tickDelta = getTickDelta();
					int fps = CLIENT.fpsCounter;
					double x = MathHelper.lerp(tickDelta, CLIENT.player.lastRenderX, CLIENT.player.getX());
					double y = MathHelper.lerp(tickDelta, CLIENT.player.lastRenderY, CLIENT.player.getY());
					double z = MathHelper.lerp(tickDelta, CLIENT.player.lastRenderZ, CLIENT.player.getZ());
					float yaw = CLIENT.player.getYaw(tickDelta);
					float pitch = CLIENT.player.getPitch(tickDelta);
					Direction facing = CLIENT.player.getHorizontalFacing();
					String facingString = "";
					switch (facing) {
						case NORTH -> facingString = "-Z";
						case SOUTH -> facingString = "+Z";
						case WEST -> facingString = "-X";
						case EAST -> facingString = "+X";
					}
					float scaleFactor = (float) CLIENT.getWindow().getScaleFactor();

					List<Text> lines = List.of(
						MutableText.create(new TranslatableComponent("text.chatlog.hud.fps", fps)),
						MutableText.create(new TranslatableComponent("text.chatlog.hud.coords", x, y, z)),
						MutableText.create(new TranslatableComponent("text.chatlog.hud.facing", facingString, yaw, pitch))
					);

					final float maxTextY = CLIENT.getWindow().getScaledHeight() - CLIENT.textRenderer.fontHeight;

					for (int i = 0; i < lines.size(); i++) {
						Text line = lines.get(i);
						float lineX = Math.min(10 / (scaleFactor > 0 ? scaleFactor : 1), CLIENT.getWindow().getScaledWidth() - CLIENT.textRenderer.getWidth(line));
						float lineY = Math.min((10 + (CLIENT.textRenderer.fontHeight + 10) * i) / (scaleFactor > 0 ? scaleFactor : 1), maxTextY);
//						CLIENT.textRenderer.drawWithShadow(matrix, line, lineX, lineY, ((1 & 0xFF) << 24) | 0x000000);
//						CLIENT.textRenderer.draw(line.asOrderedText(), lineX, lineY, ((1 & 0xFF) << 24) | 0x000000, matrix.peek().getModel(), true);
						CLIENT.textRenderer.draw(line.asOrderedText(), lineX, lineY, ((1 & 0xFF) << 24) | 0x000000, true, matrix.peek().getPosition(), VertexConsumerProvider.immediate(buffer), false, 0, 15728880);
					}
				}
			}
		};
	}

}
