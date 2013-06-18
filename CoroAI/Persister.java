package CoroAI;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;

import java.util.List;

public class Persister {

	public static Persister instance;
	public static World worldRef;
	
	public static int loadInRange = 128;
	public static int loadOutRange = 128;
	
	public static int monitorDelayAmount = 60;
	public static int monitorDelay = 60;
	
	public Persister() {
		instance = this;
		//worldRef = parWorld;
	}
	
	public static void tick(World parWorld) {
		
		//disable
		if (true) return;
		
		worldRef = parWorld;
		if (worldRef != null && !worldRef.isRemote) {
			if (monitorDelay > 0) monitorDelay--;
			if (monitorDelay == 0) {
				monitorDelay = monitorDelayAmount;
				watchEntities();
				watchChunklessEntities();
			}
		}
	}
	
	public static void watchChunklessEntities() {
		for (int i = 0; i < worldRef.weatherEffects.size(); i++) {
			Entity ent = (Entity)worldRef.weatherEffects.get(i);
			
			if (ent instanceof c_IEnhAI) {
				if (worldRef.getClosestPlayerToEntity(ent, loadInRange) != null) {
					worldRef.weatherEffects.remove(ent);
					//worldRef.spawnEntityInWorld(ent);
					//worldRef.getChunkFromBlockCoords((int)ent.posX, (int)ent.posZ).addEntity(ent);
					worldRef.getChunkFromChunkCoords(MathHelper.floor_double(ent.posX / 16.0D), MathHelper.floor_double(ent.posZ / 16.0D)).addEntity(ent);
					
					worldRef.loadedEntityList.add(ent);
					System.out.println("weather ent -> world ent");
				}
			}
		}
	}
	
	public static void watchEntities() {
		for (int i = 0; i < worldRef.loadedEntityList.size(); i++) {
			Entity ent = (Entity)worldRef.loadedEntityList.get(i);
			
			if (ent instanceof c_IEnhAI) {
				if (worldRef.getClosestPlayerToEntity(ent, loadOutRange) == null) {
					
					//Entity var2 = (Entity)this.unloadedEntityList.get(var1);
		            int var3 = ent.chunkCoordX;
		            int var4 = ent.chunkCoordZ;

		            if (ent.addedToChunk/* && worldRef.chunkExists(var3, var4)*/)
		            {
		                worldRef.getChunkFromChunkCoords(var3, var4).removeEntity(ent);
		            }
		            List list = null;
		            try {
		            	list = (List)c_CoroAIUtil.getPrivateValueBoth(World.class, worldRef, "worldAccesses", "u");
		            } catch (Exception ex) {
		            	ex.printStackTrace();
		            }
		            for (int var2 = 0; var2 < list.size(); ++var2)
		            {
		            	//OFF FOR MC 1.5 REFACTOR!!!
		            	System.out.println("OFF FOR MC 1.5 REFACTOR!!!");
		                //((IWorldAccess)list.get(var2)).releaseEntitySkin(ent);
		            }
		            //worldRef.releaseEntitySkin(ent);
		            worldRef.loadedEntityList.remove(ent);
					worldRef.addWeatherEffect(ent);
					System.out.println("world ent -> weather ent");
					//worldRef.weatherEffects.add(ent);
					//worldRef.spawnEntityInWorld(ent);
				}
			}
		}
	}
}
