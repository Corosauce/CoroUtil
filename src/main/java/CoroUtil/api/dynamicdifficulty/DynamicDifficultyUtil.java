package CoroUtil.api.dynamicdifficulty;

import CoroUtil.difficulty.DynamicDifficulty;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.common.Optional;

public class DynamicDifficultyUtil {

	@Optional.Method(modid = "CoroAI")
	public static float getDifficultyAveragedForArea(PlayerEntity player, int x, int y, int z) {
		
		DynamicDifficulty.getDifficultyScaleAverage(player, x, y, z);
		
		return 0;
	}
	
	@Optional.Method(modid = "CoroAI")
	public static float getDifficultyAveragedForArea(LivingEntity spawnedEntity, int x, int y, int z) {
		
		//TODO: cache so there is less player lookup thrashing for spawn candidate code
		PlayerEntity player = spawnedEntity.world.getClosestPlayerToEntity(spawnedEntity, -1);
		
		if (player != null) {
			return getDifficultyAveragedForArea(player, x, y, z);
		}
		
		return 0;
	}
	
}
