package CoroPets;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    public static Minecraft mc;

    public ClientProxy()
    {
        mc = Minecraft.getMinecraft();
    }

    @Override
    public void init()
    {
        super.init();
    }
}
