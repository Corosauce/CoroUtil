package CoroUtil.test;

import CoroUtil.config.ConfigCoroUtilAdvanced;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class Headshots {

	public static void hookLivingHurt(LivingHurtEvent event) {
		
		if (!ConfigCoroUtilAdvanced.headshots) return;
		
		if (event.getEntity() != null && event.getEntity() instanceof EntityLivingBase) {
			EntityLivingBase ent = (EntityLivingBase) event.getEntity();
			if (ent instanceof EntityZombie || ent instanceof EntitySkeleton) {
				if (!ent.isChild()) {
					if (event.getSource() instanceof EntityDamageSourceIndirect) {
						EntityDamageSourceIndirect source = (EntityDamageSourceIndirect) event.getSource();
						
						//System.out.println("source damage: " + source.getSourceOfDamage());
						
						if (source.getImmediateSource() instanceof EntityArrow) {
							double realPosY = source.getImmediateSource().posY + source.getImmediateSource().motionY;
							double arrowHeight = realPosY - event.getEntity().getEntityBoundingBox().minY;
							/*System.out.println("arrow height: " + arrowHeight);
							System.out.println("mob: " + event.getEntity());*/
							
							//make sure height is within range
							if (arrowHeight >= 1.47D && arrowHeight <= 2.3) {
								//System.out.println("headshot!");
								event.setAmount(event.getAmount() * 4F);
							}
							
							//System.out.println("damage total: " + event.getAmount() + " - arrow height: " + arrowHeight);
						}
					}
				}
			}
		}
	}
	
}
