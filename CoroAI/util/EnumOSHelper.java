package CoroAI.util;

import net.minecraft.util.EnumOS;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)

public class EnumOSHelper
{
    public static final int[] field_90049_a = new int[EnumOS.values().length];

    static
    {
        try
        {
            field_90049_a[EnumOS.LINUX.ordinal()] = 1;
        }
        catch (NoSuchFieldError nosuchfielderror)
        {
            ;
        }

        try
        {
            field_90049_a[EnumOS.SOLARIS.ordinal()] = 2;
        }
        catch (NoSuchFieldError nosuchfielderror1)
        {
            ;
        }

        try
        {
            field_90049_a[EnumOS.WINDOWS.ordinal()] = 3;
        }
        catch (NoSuchFieldError nosuchfielderror2)
        {
            ;
        }

        try
        {
            field_90049_a[EnumOS.MACOS.ordinal()] = 4;
        }
        catch (NoSuchFieldError nosuchfielderror3)
        {
            ;
        }
    }
}
