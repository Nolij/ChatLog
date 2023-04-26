package xyz.xdmatthewbx.chatlog.modules;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

import static xyz.xdmatthewbx.chatlog.ChatLog.CONFIG;
import static xyz.xdmatthewbx.chatlog.ChatLog.registerChangeListener;

@Module
public class ToolTipInfoModule extends BaseModule {

	public static final String MODULE_ID = "tooltip_info";
	public static ToolTipInfoModule INSTANCE;

	public boolean enabled = true;

	public ToolTipInfoModule() {
		super(MODULE_ID);
		INSTANCE = this;
	}

	@Override
	public void onInitializeClient() {
		registerChangeListener(CONFIG, (configHolder, chatLogConfig) -> {
			enabled = chatLogConfig.main.toolTipInfoModule.enabled;
			return InteractionResult.PASS;
		});
	}

	public MutableComponent generateClickInfo(ClickEvent clickEvent) {
		return MutableComponent.create(new TranslatableContents("text.chatlog.tooltipinfo.display", null, new Object[] { clickEvent.getAction().getName().toUpperCase(), clickEvent.getValue() }))
			.withStyle(ChatFormatting.DARK_GRAY);
	}

	public HoverEvent getHoverEvent(Style style) {
		ClickEvent clickEvent = style.getClickEvent();
		if (clickEvent != null) {
			HoverEvent hoverEvent = style.getHoverEvent();
			MutableComponent hoverText = MutableComponent.create(new LiteralContents(""));
			MutableComponent clickInfoText = generateClickInfo(clickEvent);
			if (hoverEvent == null) {
				LOGGER.debug("NO HOVEREVENT");
				hoverText = clickInfoText;
			} else if (hoverEvent.getAction() == HoverEvent.Action.SHOW_TEXT) {
				LOGGER.debug("SHOW_TEXT");
				hoverText.append(hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT));
//				if (hoverText.getString().contains(clickEvent.getValue())) return style;
				hoverText.append(MutableComponent.create(new LiteralContents("\n\n")).append(clickInfoText));
			} else {
				List<Component> lines = List.of();
				try {
					if (hoverEvent.getAction() == HoverEvent.Action.SHOW_ENTITY) {
						LOGGER.debug("SHOW_ENTITY");
						//noinspection DataFlowIssue
						lines = hoverEvent.getValue(HoverEvent.Action.SHOW_ENTITY).getTooltipLines();
					} else if (hoverEvent.getAction() == HoverEvent.Action.SHOW_ITEM) {
						LOGGER.debug("SHOW_ITEM");
						//noinspection DataFlowIssue
						lines = hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM).getItemStack().getTooltipLines(CLIENT.player, CLIENT.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
					}
				} catch (NullPointerException ex) {
					LOGGER.debug(ex.toString());
				}
				if (lines.size() > 0) {
					for (Component line : lines) {
						LOGGER.debug(line.getString());
						hoverText.append(line);
						hoverText.append("\n");
					}
					hoverText.append("\n");
				}
				hoverText.append(clickInfoText);
			}
			hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText);
			return hoverEvent;
		}
		return style.getHoverEvent();
	}

}
