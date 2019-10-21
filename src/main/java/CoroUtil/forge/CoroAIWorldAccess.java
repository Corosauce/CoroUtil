package CoroUtil.forge;

import CoroUtil.entity.IWorldAccessHooks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import CoroUtil.bt.IBTAgent;

public class CoroAIWorldAccess implements IWorldEventListener {

	@Override
	public void notifyBlockUpdate(World worldIn, BlockPos pos,
                                  BlockState oldState, BlockState newState, int flags) {
		
		
	}

	@Override
	public void notifyLightSet(BlockPos pos) {
		
		
	}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2,
			int y2, int z2) {
		
		
	}

	@Override
	public void playSoundToAllNearExcept(PlayerEntity player,
                                         SoundEvent soundIn, SoundCategory category, double x, double y,
                                         double z, float volume, float pitch) {
		
		
	}

	@Override
	public void playRecord(SoundEvent soundIn, BlockPos pos) {
		
		
	}

	@Override
	public void spawnParticle(int particleID, boolean ignoreRange,
			double xCoord, double yCoord, double zCoord, double xSpeed,
			double ySpeed, double zSpeed, int... parameters) {
		
		
	}

	@Override
	public void onEntityAdded(Entity entityIn) {
		
		
	}

	@Override
	public void onEntityRemoved(Entity entity) {
		
		//if (entity instanceof ICoroAI && ((ICoroAI)entity).getAIAgent() != null) ((ICoroAI)entity).getAIAgent().cleanup();
		if (entity instanceof IBTAgent && ((IBTAgent)entity).getAIBTAgent() != null) ((IBTAgent)entity).getAIBTAgent().cleanup();

		if (entity instanceof IWorldAccessHooks) {
			((IWorldAccessHooks) entity).onEntityRemoved();
		}
	}

	@Override
	public void broadcastSound(int soundID, BlockPos pos, int data) {
		
		
	}

	@Override
	public void playEvent(PlayerEntity player, int type, BlockPos blockPosIn,
                          int data) {
		
		
	}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
		
		
	}

	@Override
	public void spawnParticle(int p_190570_1_, boolean p_190570_2_, boolean p_190570_3_, double p_190570_4_,
			double p_190570_6_, double p_190570_8_, double p_190570_10_, double p_190570_12_, double p_190570_14_,
			int... p_190570_16_) {
		// TODO Auto-generated method stub
		
	}

	

	

}
