package CoroUtil.forge;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import CoroUtil.bt.IBTAgent;

public class CoroAIWorldAccess implements IWorldEventListener {

	@Override
	public void notifyBlockUpdate(World worldIn, BlockPos pos,
			IBlockState oldState, IBlockState newState, int flags) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyLightSet(BlockPos pos) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2,
			int y2, int z2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void playSoundToAllNearExcept(EntityPlayer player,
			SoundEvent soundIn, SoundCategory category, double x, double y,
			double z, float volume, float pitch) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void playRecord(SoundEvent soundIn, BlockPos pos) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void spawnParticle(int particleID, boolean ignoreRange,
			double xCoord, double yCoord, double zCoord, double xSpeed,
			double ySpeed, double zSpeed, int... parameters) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEntityAdded(Entity entityIn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEntityRemoved(Entity entity) {
		// TODO Auto-generated method stub
		//if (entity instanceof ICoroAI && ((ICoroAI)entity).getAIAgent() != null) ((ICoroAI)entity).getAIAgent().cleanup();
		if (entity instanceof IBTAgent && ((IBTAgent)entity).getAIBTAgent() != null) ((IBTAgent)entity).getAIBTAgent().cleanup();
	}

	@Override
	public void broadcastSound(int soundID, BlockPos pos, int data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn,
			int data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
		// TODO Auto-generated method stub
		
	}

	

	

}
