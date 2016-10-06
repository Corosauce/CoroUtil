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
	
	//TODO: readd GUI
	@SideOnly(Side.CLIENT)
	public GuiBetterTextField editBox;
	
	public ConfigEntryInfo(int parIndex, String parName, Object parVal, String parComment) {
		index = parIndex;
		name = parName;
		value = parVal;
		comment = parComment;
		
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) initButton();
	}
	
	@SideOnly(Side.CLIENT)
	public void initButton() {
		int buttonWidth = 130;
        int buttonHeight = 16;
        //TODO: readd GUI
		editBox = new GuiBetterTextField(Minecraft.getMinecraft().fontRendererObj, 0, 0, buttonWidth, buttonHeight);
		editBox.setText(value.toString());
	}
}
