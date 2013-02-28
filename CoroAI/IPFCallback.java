package CoroAI;

import java.util.ArrayList;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathEntity;

public interface IPFCallback {

	public void pfComplete(PFCallbackItem ci);
	public void manageCallbackQueue();
	public ArrayList<PFCallbackItem> getQueue();
}
