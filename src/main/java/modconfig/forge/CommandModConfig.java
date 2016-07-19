package modconfig.forge;

import java.util.ArrayList;
import java.util.List;

import modconfig.ConfigMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import CoroUtil.util.CoroUtil;

public class CommandModConfig extends CommandBase {

	@Override
	public String getCommandName() {
		return "config";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "";
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr, BlockPos pos)
    {
		List<String> list = new ArrayList<String>(ConfigMod.configLookup.get(getCommandName()).valsBoolean.keySet());
		list.addAll(ConfigMod.configLookup.get(getCommandName()).valsInteger.keySet());
		list.addAll(ConfigMod.configLookup.get(getCommandName()).valsString.keySet());
        return list;
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender var1, String[] var2) {
		
		try {
			if(var1 instanceof EntityPlayerMP)
			{
				int cmd = 0;
				int modid = 1;
				int field = 2;
				int vall = 3;
				EntityPlayer player = getCommandSenderAsPlayer(var1);
				EntityPlayerMP playerMP = (EntityPlayerMP) player;
				if (var2.length > 0) {
					if (var2[cmd].equalsIgnoreCase("get")) {
						if (var2.length > 2) {
							Object obj = ConfigMod.getField(var2[modid], var2[field]);
							if (obj != null) {
								var1.addChatMessage(new ChatComponentText(var2[field] + " = " + obj));
							} else {
								CoroUtil.sendPlayerMsg(playerMP, "failed to get " + var2[field]);
							}
						} else {
							CoroUtil.sendPlayerMsg(playerMP, "get requires 3 parameters");
						}
					} else if (var2[cmd].equalsIgnoreCase("set")) {
						if (var2.length > 2) {
							
							parseSetCommand((EntityPlayerMP) var1, var2);
							
							/*String val = "";
							for (int i = vall; i < var2.length; i++) val += var2[i] + (i != var2.length-1 ? " " : "");
							if (ConfigMod.updateField(var2[modid], var2[field], val)) {
								CoroUtil.sendPlayerMsg(playerMP, "set " + var2[field] + " to " + val);
								
								List blah = new ArrayList();
								
								blah.add((String)var2[field]);
								blah.add((String)val);
								
								ConfigMod.eventChannel.sendTo(PacketHelper.getModConfigPacket(var2[modid]), (EntityPlayerMP)player);
								//MinecraftServer.getServer().getConfigurationManager().sendPacketToAllPlayers(PacketHelper.getModConfigPacket(var2[modid]));
							} else {
								CoroUtil.sendPlayerMsg(playerMP, "failed to set " + var2[field]);
							}*/
						} else {
							CoroUtil.sendPlayerMsg(playerMP, "set requires 3+ parameters");
						}
					} else if (var2[cmd].equalsIgnoreCase("update")) {
						ConfigMod.eventChannel.sendTo(PacketHelper.getModConfigPacket(var2[modid]), (EntityPlayerMP)player);
						//MinecraftServer.getServer().getConfigurationManager().sendPacketToAllPlayers(PacketHelper.getModConfigPacket(var2[modid]));
					} else if (var2[cmd].equalsIgnoreCase("menu") || var2[cmd].equalsIgnoreCase("gui")) {
						NBTTagCompound nbt = new NBTTagCompound();
						nbt.setString("command", "openGUI");
						ConfigMod.eventChannel.sendTo(PacketHelper.getModConfigPacketMenu(), (EntityPlayerMP)player);
					}
					
				} else {
					//((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(PacketHelper.getModConfigPacketMenu());
					ConfigMod.eventChannel.sendTo(PacketHelper.getModConfigPacketMenu(), (EntityPlayerMP)player);
				}
			}
		} catch (Exception ex) {
			System.out.println("Exception handling Config Mod command");
			ex.printStackTrace();
		}
		
	}
	
	public static void parseSetCommand(EntityPlayerMP playerMP, String[] var2) {
		int cmd = 0;
		int modid = 1;
		int field = 2;
		int vall = 3;
		
		String val = "";
		for (int i = vall; i < var2.length; i++) val += var2[i] + (i != var2.length-1 ? " " : "");
		if (ConfigMod.updateField(var2[modid], var2[field], val)) {
			CoroUtil.sendPlayerMsg(playerMP, "set " + var2[field] + " to " + val);
			
			List blah = new ArrayList();
			
			blah.add((String)var2[field]);
			blah.add((String)val);
			
			ConfigMod.eventChannel.sendTo(PacketHelper.getModConfigPacket(var2[modid]), playerMP);
			//MinecraftServer.getServer().getConfigurationManager().sendPacketToAllPlayers(PacketHelper.getModConfigPacket(var2[modid]));
		} else {
			CoroUtil.sendPlayerMsg(playerMP, "failed to set " + var2[field]);
		}
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender)
    {
        return par1ICommandSender.canCommandSenderUseCommand(this.getRequiredPermissionLevel(), this.getCommandName());
    }

}
