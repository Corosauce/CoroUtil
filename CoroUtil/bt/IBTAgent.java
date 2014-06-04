package CoroUtil.bt;

import net.minecraft.entity.EntityLivingBase;

public interface IBTAgent {
	
	public AIBTAgent getAIBTAgent();
	public EntityLivingBase getEntityLiving();
	public void cleanup();
}
