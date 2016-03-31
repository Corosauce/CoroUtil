package CoroUtil.api.dynamicdifficulty;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import CoroUtil.world.player.DynamicDifficulty;
import cpw.mods.fml.common.Optional;

public class DynamicDifficultyUtil {

	@Optional.Method(modid = "CoroAI")
	public static float getDifficultyAveragedForArea(EntityPlayer player, int x, int y, int z) {
		
		DynamicDifficulty.getDifficultyScaleAverage(player, x, y, z);
		
		return 0;
	}
	
	@Optional.Method(modid = "CoroAI")
	public static float getDifficultyAveragedForArea(EntityLivingBase spawnedEntity, int x, int y, int z) {
		
		//TODO: cache so there is less player lookup thrashing for spawn candidate code
		EntityPlayer player = spawnedEntity.worldObj.getClosestPlayerToEntity(spawnedEntity, -1);
		
		if (player != null) {
			return getDifficultyAveragedForArea(player, x, y, z);
		}
		
		return 0;
	}
	
}
