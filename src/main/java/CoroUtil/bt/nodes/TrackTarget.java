package CoroUtil.bt.nodes;

import java.util.List;
import java.util.Random;

import org.apache.commons.io.filefilter.AgeFileFilter;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import CoroUtil.bt.Behavior;
import CoroUtil.bt.BlackboardBase;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.bt.IBTAgent;
import CoroUtil.bt.selector.Selector;

public class TrackTarget extends Selector {
	
	public IBTAgent entInt;
	public EntityLiving ent;
	public BlackboardBase blackboard;
	
	public TrackTarget(Behavior parParent, IBTAgent parEnt, BlackboardBase parBB) {
		super(parParent);
		blackboard = parBB;
		entInt = parEnt;
		ent = (EntityLiving)parEnt;
	}

	@Override
	public EnumBehaviorState tick() {
		
		if (blackboard.isFighting.getValue() && blackboard.shouldChaseTarget.getValue()) {
			blackboard.trackTarget(false);
		}
		
		return super.tick();
	}
	
}
