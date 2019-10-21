package CoroUtil.world.player;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class MobEnhancer {

	public static void processMobEnhancements(CreatureEntity ent, float difficultyScale) {
		if (ent instanceof ZombieEntity) {
			ZombieEntity zombie = (ZombieEntity) ent;
			zombie.setChild(false);
		}
		
		//extra xp
		try {
			int xp = ObfuscationReflectionHelper.getPrivateValue(MobEntity.class, ent, "field_70728_aV", "experienceValue");
			xp += difficultyScale * 10F;
			ObfuscationReflectionHelper.setPrivateValue(MobEntity.class, ent, xp, "field_70728_aV", "experienceValue");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
