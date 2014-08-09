package CoroUtil.client;

import net.minecraft.client.gui.GuiButton;

public interface IScrollingGUI {

	public void onElementSelected(int par1);
	public int getSelectedElement();
	public GuiButton getSelectButton();
	public void drawBackground();
	
}
