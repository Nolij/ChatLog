package xyz.xdmatthewbx.chatlog.modules;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.*;
import net.minecraft.text.component.LiteralComponent;
import net.minecraft.text.component.TranslatableComponent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

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
			return ActionResult.PASS;
		});
	}

	public void textFormatter(MutableText text) {
		text.setStyle(styleFormatter(text.getStyle()));
	}

	public Style styleFormatter(Style style) {
		return style.withHoverEvent(getHoverEvent(style));
	}

	public MutableText generateClickInfo(ClickEvent clickEvent) {
		return MutableText.create(new TranslatableComponent("text.chatlog.tooltipinfo.display", clickEvent.getAction().getName().toUpperCase(), clickEvent.getValue()))
			.formatted(Formatting.DARK_GRAY);
	}

	public HoverEvent getHoverEvent(Style style) {
		ClickEvent clickEvent = style.getClickEvent();
		if (clickEvent != null) {
			HoverEvent hoverEvent = style.getHoverEvent();
			MutableText hoverText = MutableText.create(new LiteralComponent(""));
			MutableText clickInfoText = generateClickInfo(clickEvent);
			if (hoverEvent == null) {
				LOGGER.debug("NO HOVEREVENT");
				hoverText = clickInfoText;
			} else if (hoverEvent.getAction() == HoverEvent.Action.SHOW_TEXT) {
				LOGGER.debug("SHOW_TEXT");
				hoverText.append(hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT));
//				if (hoverText.getString().contains(clickEvent.getValue())) return style;
				hoverText.append(MutableText.create(new LiteralComponent("\n\n")).append(clickInfoText));
			} else {
				List<Text> lines = List.of();
				try {
					if (hoverEvent.getAction() == HoverEvent.Action.SHOW_ENTITY) {
						LOGGER.debug("SHOW_ENTITY");
						lines = hoverEvent.getValue(HoverEvent.Action.SHOW_ENTITY).asTooltip();
					} else if (hoverEvent.getAction() == HoverEvent.Action.SHOW_ITEM) {
						LOGGER.debug("SHOW_ITEM");
						lines = hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM).asStack().getTooltip(CLIENT.player, CLIENT.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL);
					}
				} catch (NullPointerException ex) {
					LOGGER.debug(ex.toString());
				}
				if (lines.size() > 0) {
					for (Text line : lines) {
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
