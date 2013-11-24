package CoroAI.forge;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IWorldAccess;
import CoroAI.componentAI.ICoroAI;

public class CoroAIWorldAccess implements IWorldAccess {

	@Override
	public void markBlockForUpdate(int i, int j, int k) {
		// TODO Auto-generated method stub

	}

	@Override
	public void markBlockForRenderUpdate(int i, int j, int k) {
		// TODO Auto-generated method stub

	}

	@Override
	public void markBlockRangeForRenderUpdate(int i, int j, int k, int l,
			int i1, int j1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void playSound(String s, double d0, double d1, double d2, float f,
			float f1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void playSoundToNearExcept(EntityPlayer entityplayer, String s,
			double d0, double d1, double d2, float f, float f1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void spawnParticle(String s, double d0, double d1, double d2,
			double d3, double d4, double d5) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEntityCreate(Entity entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEntityDestroy(Entity entity) {
		// TODO Auto-generated method stub
		if (entity instanceof ICoroAI && ((ICoroAI)entity).getAIAgent() != null) ((ICoroAI)entity).getAIAgent().cleanup();
	}

	@Override
	public void playRecord(String s, int i, int j, int k) {
		// TODO Auto-generated method stub

	}

	@Override
	public void broadcastSound(int i, int j, int k, int l, int i1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void playAuxSFX(EntityPlayer entityplayer, int i, int j, int k,
			int l, int i1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroyBlockPartially(int i, int j, int k, int l, int i1) {
		// TODO Auto-generated method stub

	}

}
