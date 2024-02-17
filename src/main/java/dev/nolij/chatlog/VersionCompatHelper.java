package dev.nolij.chatlog;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.TextContent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public final class VersionCompatHelper {
	
	private static final MethodHandle NEW_LITERALTEXTCONTENT;
	private static final MethodHandle PLAINTEXTCONTENT_OF;
	
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
	
}
