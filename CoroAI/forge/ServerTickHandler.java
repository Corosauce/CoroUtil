package CoroAI.forge;

import java.util.EnumSet;

import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import CoroAI.formation.Manager;
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
        return EnumSet.of(TickType.SERVER);
    }

    @Override
    public String getLabel() { return null; }
    

    public void onTickInGame()
    {
    	if (formationManager == null) formationManager = new Manager();
    	
    	if (lastWorld != DimensionManager.getWorld(0)) {
    		lastWorld = DimensionManager.getWorld(0);
    		
    		World worlds[] = DimensionManager.getWorlds();
    		for (int i = 0; i < worlds.length; i++) {
    			worlds[i].addWorldAccess(new CoroAIWorldAccess());
    		}
    	}
    	
    	if (formationManager != null) formationManager.tickUpdate();
    }
}
