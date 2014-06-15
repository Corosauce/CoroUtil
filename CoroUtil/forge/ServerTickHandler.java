package CoroUtil.forge;

import java.util.EnumSet;

import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import CoroUtil.formation.Manager;
import CoroUtil.quest.PlayerQuestManager;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class ServerTickHandler implements ITickHandler
{
	
	public static CoroAI mod;
	public static World lastWorld = null;
	public static Manager formationManager;
	
    public ServerTickHandler(CoroAI mod_ZombieAwareness) {
    	mod = mod_ZombieAwareness;
    	
	}

    @Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		if (type.equals(EnumSet.of(TickType.WORLDLOAD))) {
        	World world = (World)tickData[0];
        	if (world.provider.dimensionId == 0) {
        		CoroAI.initTry();
        	}
        }
	}

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        if (type.equals(EnumSet.of(TickType.SERVER)))
        {
        	onTickInGame();
        }
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.SERVER, TickType.WORLDLOAD);
    }

    @Override
    public String getLabel() { return null; }
    

    public void onTickInGame()
    {
    	if (formationManager == null) formationManager = new Manager();
    	
    	//might not account for dynamic dimension addition during runtime
    	if (lastWorld != DimensionManager.getWorld(0)) {
    		lastWorld = DimensionManager.getWorld(0);
    		
    		World worlds[] = DimensionManager.getWorlds();
    		for (int i = 0; i < worlds.length; i++) {
    			worlds[i].addWorldAccess(new CoroAIWorldAccess());
    		}
    	}
    	
    	if (formationManager != null) formationManager.tickUpdate();
    	
    	//Quest system
    	World worlds[] = DimensionManager.getWorlds();
		for (int i = 0; i < worlds.length; i++) {
			PlayerQuestManager.i().tick(worlds[i]);
		}
    	
    }
}
