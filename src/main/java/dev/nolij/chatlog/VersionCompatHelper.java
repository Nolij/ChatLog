package dev.nolij.chatlog;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.TextContent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public final class VersionCompatHelper {
	
	private static final MethodHandle NEW_LITERALTEXTCONTENT;
	private static final MethodHandle PLAINTEXTCONTENT_OF;
	
	private static final MethodHandle CLICKEVENT_ACTION_GETNAME;
	private static final MethodHandle CLICKEVENT_ACTION_ASSTRING;
	
	static {
		final MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
		final MethodHandles.Lookup lookup = MethodHandles.lookup();
		
		MethodHandle newLiteralTextContent = null;
		MethodHandle plainTextContentOf = null;
        try {
	        Class<?> plainTextContent = Class.forName("net.minecraft.class_8828");
			final Method plainTextContentOfMethod = plainTextContent.getMethod("method_54232", String.class);
			plainTextContentOf = lookup.unreflect(plainTextContentOfMethod)
				.asType(MethodType.methodType(TextContent.class, String.class));
        } catch (ClassNotFoundException e) {
            try {
				final Class<?> literalTextContent = Class.forName(mappingResolver.mapClassName("intermediary", "net.minecraft.class_2585"));
                newLiteralTextContent = lookup.unreflectConstructor(literalTextContent.getConstructor(String.class));
            } catch (IllegalAccessException | NoSuchMethodException | ClassNotFoundException ex) {
                throw new AssertionError(ex);
            }
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
		
		NEW_LITERALTEXTCONTENT = newLiteralTextContent;
		PLAINTEXTCONTENT_OF = plainTextContentOf;
		
		MethodHandle clickEventActionGetName = null;
		MethodHandle clickEventActionAsString = null;
		
		try {
			final String methodName = mappingResolver.mapMethodName("intermediary",
				"net.minecraft.class_2558$class_2559", "method_10846", "()Ljava/lang/String;");
			final Method getName = ClickEvent.Action.class.getMethod(methodName);
			clickEventActionGetName = lookup.unreflect(getName);
		} catch (NoSuchMethodException e) {
			final String methodName = mappingResolver.mapMethodName("intermediary",
				"net.minecraft.class_3542", "method_15434", "()Ljava/lang/String;");
			try {
				final Method asString = ClickEvent.Action.class.getMethod(methodName);
				clickEventActionAsString = lookup.unreflect(asString);
			} catch (NoSuchMethodException | IllegalAccessException ex) {
                throw new AssertionError(ex);
            }
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
        
        CLICKEVENT_ACTION_GETNAME = clickEventActionGetName;
		CLICKEVENT_ACTION_ASSTRING = clickEventActionAsString;
	}
	
	public static TextContent textContent(String content) {
		try {
			if (NEW_LITERALTEXTCONTENT != null) {
				return (TextContent) (LiteralTextContent) NEW_LITERALTEXTCONTENT.invokeExact(content);
			} else if (PLAINTEXTCONTENT_OF != null) {
				return (TextContent) PLAINTEXTCONTENT_OF.invokeExact(content);
			}
		} catch (Throwable e) {
            throw new AssertionError(e);
        }
		
		throw new AssertionError();
	}
	
	public static String actionName(ClickEvent.Action action) {
		try {
			if (CLICKEVENT_ACTION_GETNAME != null) {
				return (String) CLICKEVENT_ACTION_GETNAME.invokeExact(action);
			} else if (CLICKEVENT_ACTION_ASSTRING != null) {
				return (String) CLICKEVENT_ACTION_ASSTRING.invokeExact(action);
			}
		} catch (Throwable e) {
			throw new AssertionError(e);
		}
		
		throw new AssertionError();
	}
	
}
