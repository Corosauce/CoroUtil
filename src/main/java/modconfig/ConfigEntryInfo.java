package modconfig;

import modconfig.gui.GuiBetterTextField;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConfigEntryInfo {
	public int index;
	public String name;
	public Object value;
	
	/** Comment/description associated with value */
	public String comment;
	
	public boolean markForUpdate = false;
	
	@SideOnly(Side.CLIENT)
	public GuiBetterTextField editBox;
	
	public ConfigEntryInfo(int parIndex, String parName, Object parVal, String parComment) {
		index = parIndex;
		name = parName;
		value = parVal;
		comment = parComment;
		
        if (getEffectiveSide() == Side.CLIENT) initButton();
	}
	
	@SideOnly(Side.CLIENT)
	public void initButton() {
		int buttonWidth = 130;
        int buttonHeight = 16;
		editBox = new GuiBetterTextField(Minecraft.getMinecraft().fontRenderer, 0, 0, buttonWidth, buttonHeight);
		editBox.setText(value.toString());
	}

	//fix for missing side check, forge fixed for 1.11.2 but not for 1.10.2, this lets me avoid reworking 5 methods to pass the world object for a more proper check
	public static Side getEffectiveSide() {
		Thread thr = Thread.currentThread();
		if (thr.getName().contains("Netty Epoll Server IO")) {
			return Side.SERVER;
		} else {
			return FMLCommonHandler.instance().getEffectiveSide();
		}
	}
}
