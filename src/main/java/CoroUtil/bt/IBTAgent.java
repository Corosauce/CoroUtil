package CoroUtil.bt;

import net.minecraft.entity.LivingEntity;

public interface IBTAgent {
	
	public AIBTAgent getAIBTAgent();
	public LivingEntity getEntityLiving();
	public void cleanup();
}

