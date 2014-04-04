package CoroUtil.pathfinding;

import java.util.ArrayList;


public interface IPFCallback {

	public void pfComplete(PFCallbackItem ci);
	public void manageCallbackQueue();
	public ArrayList<PFCallbackItem> getQueue();
}
