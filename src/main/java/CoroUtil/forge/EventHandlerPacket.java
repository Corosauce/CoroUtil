package CoroUtil.forge;

import CoroUtil.client.debug.DebugRenderEntry;
import CoroUtil.client.debug.DebugRenderer;
import CoroUtil.packet.PacketHelper;
import CoroUtil.util.UtilMining;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import CoroUtil.packet.INBTPacketHandler;

public class EventHandlerPacket {
	
	//if im going to load nbt, i probably should package it at the VERY end of the packet so it loads properly
	//does .payload continue from where i last read or is it whole thing?
	//maybe i should just do nbt only
	
	//changes from 1.6.4 to 1.7.2:
	//all nbt now:
	//- inv writes stack to nbt, dont use buffer
	//- any sending code needs a full reverification that it matches up with how its received in this class
	//- READ ABOVE ^
	//- CoroAI_Inv could be factored out and replaced with CoroAI_Ent, epoch entities use it this way

	@OnlyIn(Dist.CLIENT)
	public World getClientWorld() {
		return Minecraft.getInstance().world;
	}
	
	@OnlyIn(Dist.CLIENT)
	public PlayerEntity getClientPlayer() {
		return Minecraft.getInstance().player;
	}
	
	@OnlyIn(Dist.CLIENT)
	public INBTPacketHandler getClientDataInterface() {
		if (Minecraft.getInstance().currentScreen instanceof INBTPacketHandler) {
			return (INBTPacketHandler)Minecraft.getInstance().currentScreen;
		}
		return null;
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onPacketFromServer(FMLNetworkEvent.ClientCustomPacketEvent event) {
		try {
			final CompoundNBT nbt = PacketHelper.readNBTTagCompound(event.getPacket().payload());

			String command = nbt.getString("command");

			if (command.equals("Ent_Motion")) {

				final int entID = nbt.getInt("entityID");

				Minecraft.getInstance().execute(() -> {

					Entity entity = getClientWorld().getEntityByID(entID);
					if (entity != null) {
						entity.motionX += nbt.getDouble("motionX");
						entity.motionY += nbt.getDouble("motionY");
						entity.motionZ += nbt.getDouble("motionZ");
					}

				});

			} else if (command.equals("UpdateBlockList")) {
				Minecraft.getInstance().execute(() -> {
					World world = Minecraft.getInstance().world;
					if (world == null) return;
					String blacklistMineable_RegularBlocks = nbt.getString("blacklistRepairable_RegularBlocks");
					String whitelistMineable_TileEntities = nbt.getString("whitelistMineable_TileEntities");
					UtilMining.ClientData.listBlocksBlacklistedRepairing.clear();
					UtilMining.ClientData.listTileEntitiesWhitelistedBreakable.clear();
					UtilMining.processBlockBlacklist(blacklistMineable_RegularBlocks, UtilMining.ClientData.listBlocksBlacklistedRepairing);
					UtilMining.processBlockBlacklist(whitelistMineable_TileEntities, UtilMining.ClientData.listTileEntitiesWhitelistedBreakable);
				});
			} else if (command.equals("DebugRender")) {

				Minecraft.getInstance().execute(() -> {
					World world = Minecraft.getInstance().world;
					if (world == null) return;
					BlockPos pos = new BlockPos(nbt.getInt("posX"), nbt.getInt("posY"), nbt.getInt("posZ"));
					DebugRenderEntry entry = new DebugRenderEntry(pos, world.getGameTime() + nbt.getInt("time"), nbt.getInt("color"));
					DebugRenderer.addRenderable(entry);
				});

			} else if (command.equals("DebugRenderClear")) {
				Minecraft.getInstance().execute(() -> DebugRenderer.clearRenderables());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/*@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPacketFromServer(FMLNetworkEvent.ClientCustomPacketEvent event) {
		
		try {
			NBTTagCompound nbt = PacketHelper.readNBTTagCompound(event.packet.payload());
			
			String command = nbt.getString("command");
			
			//System.out.println("CoroUtil packet command from server: " + command);
			
			if (command.equals("CoroAI_Inv")) {
				int entID = nbt.getInt("entID");
				ItemStack is = ItemStack.loadItemStackFromNBT(nbt.getCompound("itemstack"));
				
				Entity entity = getClientWorld().getEntityByID(entID);
				if (entity instanceof ICoroAI) {
					if (entity instanceof EntityLivingBase) {
						((EntityLivingBase) entity).setCurrentItemOrArmor(0, is);
					}
				}
			} else if (command.equals("CoroAI_TEntDW")) {
				//UNFINISHED
			} else if (command.equals("NBTData_GUI")) {
				NBTDataManager.nbtDataFromServer(nbt);
			} else if (command.equals("NBTData_CONT")) {
				INBTPacketHandler nbtHandler = getClientDataInterface();
				if (nbtHandler != null) {
					nbtHandler.nbtDataFromServer(nbt);
				}
				
			} else if (command.equals("CoroAI_Ent")) {
				
				int entID = nbt.getInt("entityID");
				
				Entity entity = getClientWorld().getEntityByID(entID);
				if (entity instanceof IEntityPacket) {
					((IEntityPacket) entity).handleNBTFromServer(nbt.getCompound("abilities"));
				}
				
				NBTTagCompound abilities = nbt.getCompound("abilities");
				Iterator it = abilities.func_150296_c().iterator();
                while (it.hasNext()) {
                	String tagName = (String) it.next();
                	NBTTagCompound entry = abilities.getCompound(tagName);
                }
			} else if (command.equals("QuestData")) {
				//receiving quest data for a specific player
				NBTTagCompound data = nbt.getCompound("data");
				
				PlayerQuests quests = PlayerQuestManager.i().getPlayerQuests(getClientPlayer());
				
				//clear quests since we reload fully to sync
				quests.reset();
				quests.nbtLoad(data);
			} else if (command.equals("Ent_Motion")) {
				
				int entID = nbt.getInt("entityID");
				
				Entity entity = getClientWorld().getEntityByID(entID);
				if (entity != null) {
					entity.motionX += nbt.getDouble("motionX");
					entity.motionY += nbt.getDouble("motionY");
					entity.motionZ += nbt.getDouble("motionZ");
				}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	@SubscribeEvent
	public void onPacketFromClient(FMLNetworkEvent.ServerCustomPacketEvent event) {
		EntityPlayer entP = ((NetHandlerPlayServer)event.handler).playerEntity;
		
		try {
			NBTTagCompound nbt = PacketHelper.readNBTTagCompound(event.packet.payload());
			
			String command = nbt.getString("command");
			
			//System.out.println("CoroUtil packet command from client: " + command);
			
			if (command.equals("CoroAI_TEntCmd")) {
				int dimID = nbt.getInt("dimID");
				int x = nbt.getInt("x");
				int y = nbt.getInt("y");
				int z = nbt.getInt("z");
				NBTTagCompound nbtData = nbt.getCompound("data");
				
				World world = DimensionManager.getWorld(dimID);
				if (world != null) {
					TileEntity tEnt = world.getTileEntity(new BlockPos(x, y, z));
					if (tEnt instanceof ITilePacket) {
						System.out.println("CONFIRM THIS SHOULD BE nbtData and not just nbt var");
						((ITilePacket) tEnt).handleClientSentNBT(CoroUtilEntity.getName(entP), nbtData);
					}
				}
			} else if (command.equals("NBTData_CONT")) {
				if (entP.openContainer instanceof INBTPacketHandler) {
					((INBTPacketHandler)entP.openContainer).nbtDataFromClient(CoroUtilEntity.getName(entP), nbt);
				}
			} else if (command.equals("NBTData_GUI")) {
				NBTDataManager.nbtDataFromClient(CoroUtilEntity.getName(entP), nbt);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}*/
	
}
