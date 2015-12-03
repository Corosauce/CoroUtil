package CoroPets;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Vec3;
import CoroPets.ai.BehaviorModifier;

public class CommandCoroPets extends CommandBase {

	@Override
	public String getCommandName() {
		return "coropets";
	}

	@Override
	public void processCommand(ICommandSender var1, String[] var2) {
		
		try {
			if(var1 instanceof EntityPlayerMP)
			{
				EntityPlayer player = getCommandSenderAsPlayer(var1);
				
				if (MinecraftServer.getServer().isSinglePlayer() || MinecraftServer.getServer().getConfigurationManager().canSendCommands(player.getGameProfile())) {
					if (var2[0].equals("aitest")) {
						System.out.println("AI TEST MODIFY!");
						BehaviorModifier.test(player.worldObj, new Vec3(player.posX, player.posY, player.posZ), player);
					}
				}
			}
		} catch (Exception ex) {
			System.out.println("Exception handling CoroPets command");
			ex.printStackTrace();
		}
		
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender)
    {
        return par1ICommandSender.canCommandSenderUseCommand(this.getRequiredPermissionLevel(), this.getCommandName());
    }

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "";
	}

}
