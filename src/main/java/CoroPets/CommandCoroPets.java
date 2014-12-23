package CoroPets;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Vec3;
import CoroPets.ai.BehaviorModifier;
import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.quest.PlayerQuests;
import CoroUtil.quest.quests.ActiveQuest;
import CoroUtil.quest.quests.ItemQuest;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.CoroUtilItem;

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
				
				if (MinecraftServer.getServer().isSinglePlayer() || MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile())) {
					if (var2[0].equals("aitest")) {
						System.out.println("AI TEST MODIFY!");
						BehaviorModifier.test(player.worldObj, Vec3.createVectorHelper(player.posX, player.posY, player.posZ), CoroUtilEntity.getName(player));
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
