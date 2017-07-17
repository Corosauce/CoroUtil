package CoroUtil.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CommandCoroUtilClient extends CommandBase {

	@Override
	public String getName() {
		return "coroutilc";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender var1, String[] var2) {

		try {

			if (var2.length > 0) {
				if (var2[0].equalsIgnoreCase("list")) {
					String param = null;
	        		int dim = ((EntityPlayer)var1).dimension;
	        		if (var2.length > 1) dim = Integer.valueOf(var2[1]);
	        		if (var2.length > 2) param = var2[2];
	        		HashMap<String, Integer> entNames = listEntities(param, dim);

	        		var1.sendMessage(new TextComponentString("List for dimension id: " + dim));

	                Iterator it = entNames.entrySet().iterator();
	                while (it.hasNext()) {
	                    Map.Entry pairs = (Map.Entry)it.next();
	                    var1.sendMessage(new TextComponentString(pairs.getKey() + " = " + pairs.getValue()));
	                    //CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, pairs.getKey() + " = " + pairs.getValue());
	                    //System.out.println(pairs.getKey() + " = " + pairs.getValue());
	                    it.remove();
	                }
				}

			}

		} catch (Exception ex) {
			System.out.println("Exception handling command");
			ex.printStackTrace();
		}

	}

	public HashMap<String, Integer> listEntities(String entName, int dim) {
		HashMap<String, Integer> entNames = new HashMap<String, Integer>();

		World world = Minecraft.getMinecraft().world;



        for (int var33 = 0; var33 < world.loadedEntityList.size(); ++var33)
        {
            Entity ent = (Entity)world.loadedEntityList.get(var33);

            if (EntityList.getEntityString(ent) != null && (entName == null || EntityList.getEntityString(ent).toLowerCase().contains(entName.toLowerCase()))) {
	            int val = 1;
	            if (entNames.containsKey(EntityList.getEntityString(ent))) {
	            	val = entNames.get(EntityList.getEntityString(ent))+1;
	            }
	            entNames.put(EntityList.getEntityString(ent), val);
            }

            entNames.put(ent.toString(), 1);

        }

        entNames.put("!ALL", world.loadedEntityList.size());


        return entNames;
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender par1ICommandSender)
    {
        return true;
    }

	@Override
	public String getUsage(ICommandSender icommandsender) {
		return "Magic dev method!";
	}

}
