package com.corosus.coroutil.util;

import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class OldUtil {

    static Field field_modifiers = null;

    public static Object getPrivateValue(Class var0, Object var1, String var2)
    {
        try
        {
            Field var3 = var0.getDeclaredField(var2);
            var3.setAccessible(true);
            return var3.get(var1);
        }
        catch (Exception var4)
        {
            return null;
        }
    }

    public static <T, E> void setPrivateValue(@NotNull final Class<? super T> classToAccess, @NotNull final T instance, @NotNull final String fieldName, @Nullable final E value)
    {
        ObfuscationReflectionHelper.setPrivateValue(classToAccess, instance, value, fieldName);
    }

}
