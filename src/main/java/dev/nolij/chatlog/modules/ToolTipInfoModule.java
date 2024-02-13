package dev.nolij.chatlog.modules;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static dev.nolij.chatlog.ChatLog.CONFIG;
import static dev.nolij.chatlog.ChatLog.registerChangeListener;

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
			enabled = chatLogConfig.main.toolTipInfoModule.enabled && chatLogConfig.main.general.enabled;
			return ActionResult.PASS;
		});
	}

	public MutableText generateClickInfo(ClickEvent clickEvent) {
		return MutableText.of(new TranslatableTextContent("text.chatlog.tooltipinfo.display", null,
				new Object[] { clickEvent.getAction().getName().toUpperCase(), clickEvent.getValue() }))
			.formatted(Formatting.DARK_GRAY);
	}

	private static final Map<Style, HoverEvent> hoverEventCache = Collections.synchronizedMap(new WeakHashMap<>());

	public HoverEvent getHoverEvent(Style _style) {
		return hoverEventCache.computeIfAbsent(_style, style -> {
			ClickEvent clickEvent = style.getClickEvent();
			if (clickEvent != null) {
				HoverEvent hoverEvent = style.getHoverEvent();
				MutableText hoverText = MutableText.of(new LiteralTextContent(""));
				MutableText clickInfoText = generateClickInfo(clickEvent);
				if (hoverEvent == null) {
					LOGGER.debug("NO HOVEREVENT");
					hoverText = clickInfoText;
				} else if (hoverEvent.getAction() == HoverEvent.Action.SHOW_TEXT) {
					LOGGER.debug("SHOW_TEXT");
					hoverText.append(hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT));
					hoverText.append(MutableText.of(new LiteralTextContent("\n\n")).append(clickInfoText));
				} else {
					List<Text> lines = List.of();
					try {
						if (hoverEvent.getAction() == HoverEvent.Action.SHOW_ENTITY) {
							LOGGER.debug("SHOW_ENTITY");
							//noinspection DataFlowIssue
							lines = hoverEvent.getValue(HoverEvent.Action.SHOW_ENTITY).asTooltip();
						} else if (hoverEvent.getAction() == HoverEvent.Action.SHOW_ITEM) {
							LOGGER.debug("SHOW_ITEM");
							//noinspection DataFlowIssue
							lines = hoverEvent
								.getValue(HoverEvent.Action.SHOW_ITEM)
								.asStack()
								.getTooltip(
									CLIENT.player,
									CLIENT.options.advancedItemTooltips
										? TooltipContext.Default.ADVANCED
										: TooltipContext.Default.BASIC
								);
						}
					} catch (NullPointerException ex) {
						LOGGER.debug(ex.toString());
					}
					if (!lines.isEmpty()) {
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
		});
	}

}
