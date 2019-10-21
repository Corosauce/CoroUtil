package CoroUtil.tile;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

/* Used for handling special ways of interaction
 * 
 * For now just clickLeft and clickRight, but in future, a system for handling block face specific sections, for keypads etc
 *  
 *  */
public interface ITileInteraction {

	public void clickedLeft();
	public void clickedRight(PlayerEntity player, int face, float localVecX, float localVecY, float localVecZ);
	
}
