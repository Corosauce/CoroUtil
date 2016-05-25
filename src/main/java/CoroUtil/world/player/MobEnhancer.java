package CoroUtil.world.player;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class MobEnhancer {

	public static void processMobEnhancements(EntityCreature ent, float difficultyScale) {
		if (ent instanceof EntityZombie) {
			EntityZombie zombie = (EntityZombie) ent;
			zombie.setChild(false);
		}
		
		//extra xp
		try {
			int xp = ObfuscationReflectionHelper.getPrivateValue(EntityLiving.class, ent, "field_70728_aV", "experienceValue");
			xp += difficultyScale * 10F;
			ObfuscationReflectionHelper.setPrivateValue(EntityLiving.class, ent, xp, "field_70728_aV", "experienceValue");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
