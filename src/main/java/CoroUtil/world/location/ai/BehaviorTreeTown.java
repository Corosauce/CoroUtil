package CoroUtil.world.location.ai;

import CoroUtil.bt.selector.Selector;
import CoroUtil.world.location.town.TownObject;

/* This class manages economy and base building, or at least started out here */
public class BehaviorTreeTown {

	TownObject location;
	Selector trunk;
	
	public BehaviorTreeTown(TownObject parLocation) {
		location = parLocation;
	}
	
	public void tick() {
		//System.out.println("-------------");
		trunk.tick();
	}
	
}
