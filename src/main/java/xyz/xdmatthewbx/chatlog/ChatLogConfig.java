package xyz.xdmatthewbx.chatlog;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Modifier;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.clothconfig2.gui.entries.KeyCodeEntry;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.math.MathHelper;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.*;

@Config(name = "chatlog")
@Config.Gui.Background(Config.Gui.Background.TRANSPARENT)
public class ChatLogConfig extends PartitioningSerializer.GlobalData {

	@ConfigEntry.Category("main")
	@ConfigEntry.Gui.TransitiveObject
	public CategoryMain main = new CategoryMain();

	public enum KeyBindMode {
		HOLD, TOGGLE
	}

	public enum EntityColorMode {
		MANUAL, TEAM
	}

	@Config(name = "main")
	public static class CategoryMain implements ConfigData {
		@ConfigEntry.Category("general")
		@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
		public GeneralConfig general = new GeneralConfig();

		@ConfigEntry.Category("render")
		@ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
		public RenderConfig render = new RenderConfig();

		@ConfigEntry.Category("toolTipInfoModule")
		@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
		public ToolTipInfoConfig toolTipInfoModule = new ToolTipInfoConfig();

		@ConfigEntry.Category("perspectiveModule")
		@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
		public PerspectiveConfig perspectiveModule = new PerspectiveConfig();

		@ConfigEntry.Category("antiBlindModule")
		@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
		public AntiBlindConfig antiBlindModule = new AntiBlindConfig();

		@ConfigEntry.Category("fullBrightModule")
		@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
		public FullBrightConfig fullBrightModule = new FullBrightConfig();

		@ConfigEntry.Category("antiFogModule")
		@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
		public AntiFogConfig antiFogModule = new AntiFogConfig();

		@ConfigEntry.Category("antiDistortionModule")
		@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
		public AntiDistortionConfig antiDistortionModule = new AntiDistortionConfig();

		@ConfigEntry.Category("antiOverlayModule")
		@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
		public AntiOverlayConfig antiOverlayModule = new AntiOverlayConfig();

		@ConfigEntry.Category("espModule")
		@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
		public ESPConfig espModule = new ESPConfig();

		@ConfigEntry.Category("inputUnlockModule")
		@ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
		public InputUnlockConfig inputUnlockModule = new InputUnlockConfig();

		@ConfigEntry.Category("freeCamModule")
		@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
		public FreeCamConfig freeCamModule = new FreeCamConfig();

		@ConfigEntry.Category("autoClickerModule")
		@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
		public AutoClickerConfig autoClickerModule = new AutoClickerConfig();
	}

	@Config(name = "general")
	public static class GeneralConfig implements ConfigData {
		public ModifierKeyCode configKeyBind = ModifierKeyCode.of(InputUtil.Type.KEYSYM.createFromCode(InputUtil.GLFW_KEY_RIGHT_SHIFT), Modifier.none());
	}

	@Config(name = "render")
	public static class RenderConfig implements ConfigData {
		public boolean allowRenderThroughBlocks = true;

		public float lineWidth = 2F;

		public boolean disableBobbingWhenCameraLocked = true;
	}

	@Config(name = "toolTipInfoModule")
	public static class ToolTipInfoConfig implements ConfigData {
		public boolean enabled = true;
	}

	@Config(name = "perspectiveModule")
	public static class PerspectiveConfig implements ConfigData {
		public ModifierKeyCode keyBind = ModifierKeyCode.of(InputUtil.Type.KEYSYM.createFromCode(InputUtil.GLFW_KEY_GRAVE_ACCENT), Modifier.none());

		@ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
		public KeyBindMode mode = KeyBindMode.HOLD;
	}

	@Config(name = "antiBlindModule")
	public static class AntiBlindConfig implements ConfigData {
		public boolean enabled = true;
	}

	@Config(name = "fullBrightModule")
	public static class FullBrightConfig implements ConfigData {
		public boolean enabled = true;
	}

	@Config(name = "antiFogModule")
	public static class AntiFogConfig implements ConfigData {
		public boolean enabled = true;
	}

	@Config(name = "antiDistortionModule")
	public static class AntiDistortionConfig implements ConfigData {
		public boolean enabled = true;

		@Slider(prefix = "Opacity: ", suffix = "%", displayFactor = 100.0, step = 0.01)
		public double nauseaOverlayScale = 0.6D;
	}

	@Config(name = "antiOverlayModule")
	public static class AntiOverlayConfig implements ConfigData {
		public boolean enabled = true;

		@Slider(prefix = "Opacity: ", suffix = "%", displayFactor = 100.0, step = 0.01)
		public double overlayOpacity = 0.4D;
	}

	@Config(name = "espModule")
	public static class ESPConfig implements ConfigData {
		public boolean enabled = true;

		@ConfigEntry.Gui.Excluded
		public List<BlockESPFilter> blockFilters = List.of();

		@ConfigEntry.Gui.Excluded
		public List<EntityESPFilter> entityFilters = List.of();

		public List<BlockESPFilterGroup> blockFilterGroups = List.of();

		public List<EntityESPFilterGroup> entityFilterGroups = List.of();
	}

	public static class BlockESPFilterGroup {
		public boolean enabled = true;

		public String name = "";

		public List<BlockESPFilter> filters = List.of();
	}

	public static class EntityESPFilterGroup {
		public boolean enabled = true;

		public String name = "";

		public List<EntityESPFilter> filters = List.of();
	}

	public static class BlockESPFilter {
		public boolean enabled = true;

		@BlockPredicate
		public String blockFilter = "";

		@ConfigEntry.ColorPicker
		public int color = 0xFFFFFF;
	}

	public static class EntityESPFilter {
		public boolean enabled = true;

		@EntitySelector
		public String entityFilter = "";

		@ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
		public EntityColorMode entityColorMode = EntityColorMode.MANUAL;

		@ConfigEntry.ColorPicker
		public int color = 0xFFFFFF;
	}

	@Config(name = "inputUnlockModule")
	public static class InputUnlockConfig implements ConfigData {
		@ConfigEntry.Gui.Tooltip
		public boolean enabled = false;
	}

	@Config(name = "freeCamModule")
	public static class FreeCamConfig implements ConfigData {
		public ModifierKeyCode keyBind = ModifierKeyCode.of(InputUtil.Type.KEYSYM.createFromCode(InputUtil.GLFW_KEY_RIGHT_CONTROL), Modifier.none());

		public boolean renderHand = false;
	}

	@Config(name = "autoClickerModule")
	public static class AutoClickerConfig implements ConfigData {
		public ModifierKeyCode keyBind = ModifierKeyCode.of(InputUtil.Type.KEYSYM.createFromCode(InputUtil.field_32000), Modifier.none());

		@ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
		public KeyBindMode mode = KeyBindMode.HOLD;

		@NonZero
		@Slider(min = 0.0, max = 20.0, suffix = " CPS", step = 0.25)
		public double cps = 7.0;

		@Slider(min = 0.0, max = 100.0, suffix = "ms", step = 5)
		public double maxJitter = 50.0;
	}

	public ChatLogConfig() {
		GuiRegistry guiRegistry = AutoConfig.getGuiRegistry(ChatLogConfig.class);
		guiRegistry.registerPredicateProvider((i13n, field, config, defaults, guiProvider) -> {
			if (field.isAnnotationPresent(ConfigEntry.Gui.Excluded.class)) {
				return Collections.emptyList();
			}
			KeyCodeEntry entry = ConfigEntryBuilder.create()
				.startModifierKeyCodeField(MutableText.of(new TranslatableTextContent(i13n, null, TranslatableTextContent.EMPTY_ARGUMENTS)), getUnsafely(field, config, ModifierKeyCode.unknown()))
				.setModifierDefaultValue(() -> getUnsafely(field, defaults))
				.setModifierSaveConsumer(newValue -> setUnsafely(field, config, newValue.clearModifier()))
				.build();
			return Collections.singletonList(entry);
		}, field -> field.getType() == ModifierKeyCode.class);
		guiRegistry.registerAnnotationProvider((i13n, field, config, defaults, guiProvider) -> {
			Slider bounds = field.getAnnotation(Slider.class);
			var displayFactor = (bounds.displayFactor() * bounds.step());
			return Collections.singletonList(ConfigEntryBuilder.create()
				.startIntSlider(MutableText.of(
					new TranslatableTextContent(i13n, null, TranslatableTextContent.EMPTY_ARGUMENTS)),
					MathHelper.ceil(Utils.getUnsafely(field, config, 0.0) / bounds.step()),
					MathHelper.ceil(bounds.min() / bounds.step()),
					MathHelper.ceil(bounds.max() / bounds.step()))
				.setDefaultValue(() -> MathHelper.ceil((double) getUnsafely(field, defaults) / bounds.step()))
				.setSaveConsumer((newValue) -> setUnsafely(field, config, newValue * bounds.step()))
				.setTextGetter(intValue -> {
					var value = intValue * displayFactor;
					return MutableText.of(
						new LiteralTextContent(
							bounds.prefix() + (value % 1 > 0 ? String.valueOf(value) : String.valueOf((int) value)) + bounds.suffix()
						)
					);
				})
				.setErrorSupplier(value -> field.isAnnotationPresent(NonZero.class) && value == 0 ? Optional.of(MutableText.of(new TranslatableTextContent("text.chatlog.config.error.nonZero", null, TranslatableTextContent.EMPTY_ARGUMENTS))) : Optional.empty())
				.build());
		}, (field) -> field.getType() == Double.TYPE || field.getType() == Double.class, Slider.class);
		guiRegistry.registerAnnotationProvider((i13n, field, config, defaults, guiProvider) ->
			Collections.singletonList(ConfigEntryBuilder.create()
				.startStrField(
					MutableText.of(new TranslatableTextContent(i13n, null, TranslatableTextContent.EMPTY_ARGUMENTS)),
					getUnsafely(field, config, ""))
				.setDefaultValue(() -> getUnsafely(field, defaults))
				.setSaveConsumer((newValue) -> setUnsafely(field, config, newValue))
				.setErrorSupplier(value -> {
					if (value.length() == 0) return Optional.empty();
//					try {
//						BlockPredicateArgumentType.blockPredicate().parse(new StringReader(value));
//					} catch (CommandSyntaxException ex) {
//						return Optional.of(MutableText.create(new LiteralComponent(ex.getMessage())));
//					} // TODO: FIX
					return Optional.empty();
				})
				.build()), (field) -> field.getType() == String.class, BlockPredicate.class);
		guiRegistry.registerAnnotationProvider((i13n, field, config, defaults, guiProvider) ->
			Collections.singletonList(ConfigEntryBuilder.create()
				.startStrField(
					MutableText.of(new TranslatableTextContent(i13n, null, TranslatableTextContent.EMPTY_ARGUMENTS)),
					getUnsafely(field, config, ""))
				.setDefaultValue(() -> getUnsafely(field, defaults))
				.setSaveConsumer((newValue) -> setUnsafely(field, config, newValue))
				.setErrorSupplier(value -> {
					if (value.length() == 0) return Optional.empty();
					try {
						new EntitySelectorReader(new StringReader(value)).read();
					} catch (CommandSyntaxException ex) {
						return Optional.of(MutableText.of(new LiteralTextContent(ex.getMessage())));
					}
					return Optional.empty();
				})
				.build()), (field) -> field.getType() == String.class, EntitySelector.class);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD})
	@interface NonZero {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD})
	@interface Slider {
		double min() default 0.0;

		double max() default 1.0;

		String prefix() default "";

		String suffix() default "";

		double displayFactor() default 1.0;

		double step() default 1.0;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD})
	@interface BlockPredicate {

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD})
	@interface EntitySelector {

	}

	public static Gson buildGson(GsonBuilder builder) {
		builder.serializeNulls();
		builder.setPrettyPrinting();

		builder.registerTypeHierarchyAdapter(ModifierKeyCode.class, new TypeAdapter<>() {
			@Override
			public void write(JsonWriter out, Object value) throws IOException {
				assert value instanceof ModifierKeyCode;
				ModifierKeyCode modifierKeyCode = (ModifierKeyCode) value;
				out
					.beginObject()
					.name("keyCode")	.value(modifierKeyCode.getKeyCode().getTranslationKey())
					.name("modifier")	.value((int) modifierKeyCode.getModifier().getValue())
					.endObject();
			}

			@Override
			public Object read(JsonReader in) throws IOException {
				in.beginObject();
				String keyCode = null;
				short modifier = 0;
				while (in.hasNext()) {
					switch (in.nextName()) {
						case "keyCode"	-> keyCode = in.nextString();
						case "modifier"	-> modifier = (short) in.nextInt();
					}
				}
				assert keyCode != null;
				in.endObject();
				if (keyCode.endsWith(".unknown")) {
					return ModifierKeyCode.unknown();
				}
				return ModifierKeyCode.of(InputUtil.fromTranslationKey(keyCode), Modifier.of(modifier));
			}
		});

		return builder.create();
	}

	@SuppressWarnings("unchecked")
	public static <V> V getUnsafely(Field field, Object obj) {
		if (obj == null) {
			return null;
		} else {
			try {
				field.setAccessible(true);
				return (V) field.get(obj);
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	public static <V> V getUnsafely(Field field, Object obj, V defaultValue) {
		V ret = getUnsafely(field, obj);
		if (ret == null) {
			ret = defaultValue;
		}

		return ret;
	}

	public static void setUnsafely(Field field, Object obj, Object newValue) {
		if (obj != null) {
			try {
				field.setAccessible(true);
				field.set(obj, newValue);
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

}
