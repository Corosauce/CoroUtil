package com.corosus.coroutil.common.core.util;

import org.antlr.v4.runtime.misc.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

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
        ReflectionHelper.setPrivateValue(classToAccess, instance, value, fieldName);
    }

}
