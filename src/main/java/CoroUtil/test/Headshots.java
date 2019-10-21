package CoroUtil.test;

import CoroUtil.config.ConfigCoroUtilAdvanced;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class Headshots {

	public static void hookLivingHurt(LivingHurtEvent event) {
		
		if (!ConfigCoroUtilAdvanced.headshots) return;
		
		if (event.getEntity() != null && event.getEntity() instanceof LivingEntity) {
			LivingEntity ent = (LivingEntity) event.getEntity();
			if (ent instanceof ZombieEntity || ent instanceof SkeletonEntity) {
				if (!ent.isChild()) {
					if (event.getSource() instanceof IndirectEntityDamageSource) {
						IndirectEntityDamageSource source = (IndirectEntityDamageSource) event.getSource();
						
						//System.out.println("source damage: " + source.getSourceOfDamage());
						
						if (source.getImmediateSource() instanceof AbstractArrowEntity) {
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
