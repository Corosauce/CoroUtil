package CoroUtil.client;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.Button;

public interface IScrollingGUI {

	public void onElementSelected(int par1);
	public int getSelectedElement();
	public Button getSelectButton();
	public void drawBackground();
	
}
