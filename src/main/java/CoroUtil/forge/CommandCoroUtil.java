package CoroUtil.forge;

import java.text.DecimalFormat;
import java.util.*;

import CoroUtil.block.TileEntityRepairingBlock;
import CoroUtil.config.ConfigDynamicDifficulty;
import CoroUtil.difficulty.BuffedLocation;
import CoroUtil.difficulty.DynamicDifficulty;
import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.difficulty.data.DifficultyDataReader;
import CoroUtil.difficulty.data.spawns.DataActionMobSpawns;
import CoroUtil.difficulty.data.spawns.DataMobSpawnsTemplate;
import CoroUtil.util.*;
import CoroUtil.world.WorldDirector;
import CoroUtil.world.WorldDirectorManager;
import CoroUtil.world.location.ISimulationTickable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;
import CoroUtil.OldUtil;
import CoroUtil.pathfinding.PFQueue;
import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.quest.PlayerQuests;
import CoroUtil.quest.quests.ActiveQuest;
import CoroUtil.quest.quests.ItemQuest;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CommandCoroUtil extends CommandBase {

	@Override
	public String getName() {
		return "coroutil";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender var1, String[] var2) {
		
		EntityPlayer player = null;
		if (var1 instanceof EntityPlayer) {
			player = (EntityPlayer) var1;
		}
		World world = var1.getEntityWorld();
		int dimension = world.provider.getDimension();
		BlockPos posBlock = var1.getPosition();
		Vec3d posVec = var1.getPositionVector();
		
		try {
			/*if(var1 instanceof EntityPlayerMP)
			{*/
				//EntityPlayer player = getCommandSenderAsPlayer(var1);
				
				if (var2[0].equals("testquest") && player != null) {
					String createQuestStr = "CoroUtil.quest.quests.ItemQuest";
					PlayerQuests plQuests = PlayerQuestManager.i().getPlayerQuests(player);
					ActiveQuest aq = PlayerQuests.createQuestFromString(createQuestStr);
					
					System.out.println("trying to create quest from str: " + createQuestStr);
					
					if (aq != null) {
						aq.initCreateObject(plQuests);
						
						aq.initFirstTime(dimension);
						((ItemQuest)aq).initCustomData(CoroUtilItem.getNameByItem(Items.DIAMOND), 5, false);
						
						PlayerQuestManager.i().getPlayerQuests(CoroUtilEntity.getName(player)).questAdd(aq);
						System.out.println("create success type: " + aq.questType);
					} else {
						System.out.println("failed to create quest " + createQuestStr);
					}
					
					plQuests.saveAndSyncPlayer();
				} else if (var2[0].equals("buffloc")) {
					int distRadius = 32;
					float difficulty = 2;
					if (var2.length > 1) {
						difficulty = Float.valueOf(var2[1]);
					}
					ISimulationTickable zone = WorldDirectorManager.instance().getCoroUtilWorldDirector(world).getTickingSimulationByLocation(new BlockCoord(posBlock));
					if (zone == null) {
						DynamicDifficulty.buffLocation(world, new BlockCoord(posBlock), distRadius, difficulty);
						System.out.println("buffed zone at " + posBlock);
					} else {
						System.out.println("buffed zone already at " + posBlock);
					}
				} else if (var2[0].equals("buffremove")) {
					boolean removeAll = false;
					if (var2.length > 1) {
						removeAll = var2[1].equalsIgnoreCase("all");
					}
					WorldDirector wd = WorldDirectorManager.instance().getCoroUtilWorldDirector(world);
					if (removeAll) {
						Iterator<ISimulationTickable> it = wd.listTickingLocations.iterator();
						while(it.hasNext()) {
							ISimulationTickable loc = it.next();
							if (loc instanceof BuffedLocation) {
								wd.removeTickingLocation(loc);
								it.remove();
								System.out.println("removed buffed zone at " + loc.getOrigin());
							}
						}

					} else {
						ISimulationTickable zone = wd.getTickingSimulationByLocation(new BlockCoord(posBlock));
						if (zone != null) {
							wd.removeTickingLocation(zone);
							System.out.println("removed buffed zone at " + posBlock);
						} else {
							System.out.println("cant find buffed zone at " + posBlock);
						}
					}

				} else if (var2[0].equals("height")) {
					var1.sendMessage(new TextComponentString("height: " + world.getHeight(posBlock)));
				} else if (var2[0].equals("heightprecip")) {
					var1.sendMessage(new TextComponentString("heightprecip: " + world.getPrecipitationHeight(posBlock)));
				} else if (var2[0].equals("aitest")) {
					/*System.out.println("AI TEST MODIFY!");
					BehaviorModifier.test(world, Vec3.createVectorHelper(player.posX, player.posY, player.posZ), CoroUtilEntity.getName(player));*/
					
					/*TaskDigTowardsTarget task = new TaskDigTowardsTarget();
					
					System.out.println("ENHANCE!");
					BehaviorModifier.enhanceZombiesToDig(DimensionManager.getWorld(0), new Vec3(player.posX, player.posY, player.posZ), new Class[] { TaskDigTowardsTarget.class, TaskCallForHelp.class }, 5, 0.8F);*/
				} else if (var2[0].equalsIgnoreCase("spawn") && player != null) {
					
					String prefix = "";
					if (var2.length > 1) {
						String mobToSpawn = var2[1];

						int count = 1;

						if (var2.length > 2) {
							count = Integer.valueOf(var2[2]);
						}

						for (int i = 0; i < count; i++) {
							Entity ent = EntityList.createEntityByIDFromName(new ResourceLocation(prefix + mobToSpawn), world);

							if (ent == null)
								ent = EntityList.createEntityByIDFromName(new ResourceLocation(mobToSpawn), world);

							if (ent == null) {
								List<String> entsToSpawn = listEntitiesSpawnable(mobToSpawn);
								if (entsToSpawn.size() > 0) {
									for (int j = 0; j < entsToSpawn.size(); j++) {
										Entity ent2 = EntityList.createEntityByIDFromName(new ResourceLocation(entsToSpawn.get(j)), world);
										boolean livingOnly = true;
										if (ent2 != null && (!livingOnly || ent2 instanceof EntityLivingBase)) {
											CoroUtilMisc.sendCommandSenderMsg(player, "spawned: " + CoroUtilEntity.getName(ent2));
											spawnEntity(player, ent2);
										}
									}
								} else {
									CoroUtilMisc.sendCommandSenderMsg(player, "found nothing to spawn");
								}
							} else {
								if (ent != null) {

									CoroUtilMisc.sendCommandSenderMsg(player, "spawned: " + CoroUtilEntity.getName(ent));
									spawnEntity(player, ent);

								}
							}
						}
					} else {
						CoroUtilMisc.sendCommandSenderMsg(player, "eg: 'coroutil spawn zombie 1', or 'coroutil spawn <ALL>' (spawns every spawnable possible, might crash game)");
					}
				} else if (var2[0].equalsIgnoreCase("get")) {
	        		if (var2[1].equalsIgnoreCase("count")) {
	        			boolean exact = false;
	        			if (var2.length > 3) exact = var2[3].equals("exact");
	        			CoroUtilMisc.sendCommandSenderMsg(var1, var2[2] + " count: " + getEntityCount(var2[2], false, exact, dimension));
	        		} else if (var2[1].equalsIgnoreCase("PFQueue")) {
	        			if (var2[2].equalsIgnoreCase("lastpf")) {
	        				CoroUtilMisc.sendCommandSenderMsg(var1, "last PF Time: " + ((System.currentTimeMillis() - PFQueue.lastSuccessPFTime) / 1000F));
	        				//var1.sendChatToPlayer(var2[2] + " set to: " + c_CoroAIUtil.getPrivateValueBoth(PFQueue.class, PFQueue.instance, var2[2], var2[2]));
	        			} if (var2[2].equalsIgnoreCase("stats")) {
	        				CoroUtilMisc.sendCommandSenderMsg(var1, "PFQueue Stats");
	        				CoroUtilMisc.sendCommandSenderMsg(var1, "-------------");
	        				CoroUtilMisc.sendCommandSenderMsg(var1, PFQueue.lastQueueSize + " - " + "PF queue size");
	        				CoroUtilMisc.sendCommandSenderMsg(var1, PFQueue.lastChunkCacheCount + " - " + "Cached chunks");
	        				CoroUtilMisc.sendCommandSenderMsg(var1, PFQueue.statsPerSecondPath + " - " + "Pathfinds / 10 sec");
	        				CoroUtilMisc.sendCommandSenderMsg(var1, PFQueue.statsPerSecondPathSkipped + " - " + "Old PF Skips / 10 sec");
	        				CoroUtilMisc.sendCommandSenderMsg(var1, PFQueue.statsPerSecondNodeMaxIter + " - " + "Big PF Skips / 10 sec");
	        				CoroUtilMisc.sendCommandSenderMsg(var1, PFQueue.statsPerSecondNode + " - " + "Nodes ran / 10 sec");
	        					        				
	        				
	        				
	        			} else {
	        				//var1.sendChatToPlayer("Last chunk cache count: " + PFQueue.lastChunkCacheCount);
	        				CoroUtilMisc.sendCommandSenderMsg(var1, var2[2] + " set to: " + OldUtil.getPrivateValueBoth(PFQueue.class, PFQueue.instance, var2[2], var2[2]));
	        			}
	        		}
	        	} else if (var2[0].equalsIgnoreCase("kill")) {
	        		boolean exact = false;
	        		//int dim = world.provider.getDimension();
        			//if (var2.length > 2) exact = var2[2].equals("exact");
	        		if (var2.length > 2) dimension = Integer.valueOf(var2[1]);
	        		CoroUtilMisc.sendCommandSenderMsg(var1, var2[1] + " count killed: " + getEntityCount(var2[1], true, exact, dimension));
	        	} else if (var2[0].equalsIgnoreCase("list")) {
	        		String param = null;
	        		//int dim = world.provider.getDimension();
	        		
	        		String fullCommand = "";
	        		for (String entry : var2) {
	        			fullCommand += entry + " ";
	        		}
	        		boolean simple = false;
	        		if (fullCommand.contains(" simple")) {
	        			simple = true;
	        		} else {
	        			if (var2.length > 1) dimension = Integer.valueOf(var2[1]);
		        		if (var2.length > 2) param = var2[2];
	        		}
	        		HashMap<String, Integer> entNames = listEntities(param, dimension, simple);
	                
	        		CoroUtilMisc.sendCommandSenderMsg(var1, "List for dimension id: " + dimension);
	        		
	                Iterator it = entNames.entrySet().iterator();
	                while (it.hasNext()) {
	                    Map.Entry pairs = (Map.Entry)it.next();
	                    CoroUtilMisc.sendCommandSenderMsg(var1, pairs.getKey() + " = " + pairs.getValue());
	                    //System.out.println(pairs.getKey() + " = " + pairs.getValue());
	                    it.remove();
	                }
				} else if (var2[0].equalsIgnoreCase("printEntities")) {
					player.sendMessage(new TextComponentString("Printing all entities names that might be usable in HW-Invasions, use these names in the mob_spawns json, copy these from forge logs for easier use"));
					for (Map.Entry<ResourceLocation, EntityEntry> entry : ForgeRegistries.ENTITIES.getEntries()) {
						if (EntityCreature.class.isAssignableFrom(entry.getValue().getEntityClass())) {
							player.sendMessage(new TextComponentString(entry.getValue().getRegistryName().toString()));
						}
					}
	        	} else if (var2[0].equalsIgnoreCase("location")) {
					String param = null;
					//int dim = world.provider.getDimension();
					int indexStart = 0;

					String fullCommand = "";
					for (String entry : var2) {
						fullCommand += entry + " ";
					}
					boolean simple = true;
	        		/*if (fullCommand.contains(" simple")) {
	        			simple = true;
	        		} else {*/
					//using index start instead of dimension
					if (var2.length > 1) indexStart = Integer.valueOf(var2[1]);
					if (var2.length > 2) param = var2[2];
					//}
					List<String> data = listEntitiesLocations(param, dimension, simple, indexStart);

					CoroUtilMisc.sendCommandSenderMsg(var1, "Location list for dimension id: " + dimension);
					for (String entry : data) {
						CoroUtilMisc.sendCommandSenderMsg(var1, entry);
					}
				} else if (var2[0].equalsIgnoreCase("debugdps")) {
					if ((var1 instanceof EntityPlayer)) {
						EntityPlayer ent = (EntityPlayer) var1;

						if (var2.length >= 2) {
							ent = world.getPlayerEntityByName(var2[1]);
							if (ent == null) {
								CoroUtilMisc.sendCommandSenderMsg(var1, "Couldnt find player by name: " + var2[1]);
							}
						}

						if (ent != null) {
							net.minecraft.util.math.Vec3d posVec2 = new net.minecraft.util.math.Vec3d(ent.posX, ent.posY + (ent.getEyeHeight() - ent.getDefaultEyeHeight()), ent.posZ);//player.getPosition(1F);
							BlockCoord pos = new BlockCoord(MathHelper.floor(posVec2.x), MathHelper.floor(posVec2.y), MathHelper.floor(posVec2.z));
							CoroUtilMisc.sendCommandSenderMsg(var1, "Printed extra dps debug to server console");
							DynamicDifficulty.getDifficultyScaleForPosDPS(ent.world, pos, true, (EntityPlayer) var1);
						}
					}
				} else if (var2[0].equalsIgnoreCase("difficulty") || var2[0].equalsIgnoreCase("diff")) {
					if ((var1 instanceof EntityPlayer)) {
						EntityPlayer ent = (EntityPlayer) var1;

						if (var2.length >= 2) {
							ent = world.getPlayerEntityByName(var2[1]);
							if (ent == null) {
								CoroUtilMisc.sendCommandSenderMsg(var1, "Couldnt find player by name: " + var2[1]);
							}
						}

						if (ent != null) {

							//net.minecraft.util.Vec3 posVec = ent.getPosition(1F);
							net.minecraft.util.math.Vec3d posVec2 = new net.minecraft.util.math.Vec3d(ent.posX, ent.posY + (ent.getEyeHeight() - ent.getDefaultEyeHeight()), ent.posZ);//player.getPosition(1F);
							BlockCoord pos = new BlockCoord(MathHelper.floor(posVec2.x), MathHelper.floor(posVec2.y), MathHelper.floor(posVec2.z));
							//long dayNumber = (ent.world.getWorldTime() / CoroUtilWorldTime.getDayLength()) + 1;
							CoroUtilMisc.sendCommandSenderMsg(var1, "Difficulties for player: ");
							CoroUtilMisc.sendCommandSenderMsg(var1, "equipment rating: " + TextFormatting.GREEN + roundVal(DynamicDifficulty.getDifficultyScaleForPlayerEquipment(ent))
									+ TextFormatting.RESET + " weight: " + TextFormatting.YELLOW + ConfigDynamicDifficulty.weightPlayerEquipment + TextFormatting.RESET + " = "
									+ TextFormatting.RED + roundVal((DynamicDifficulty.getDifficultyScaleForPlayerEquipment(ent) * ConfigDynamicDifficulty.weightPlayerEquipment)));

							CoroUtilMisc.sendCommandSenderMsg(var1, "player server time: " + TextFormatting.GREEN + roundVal(DynamicDifficulty.getDifficultyScaleForPlayerServerTime(ent))
									+ TextFormatting.RESET + " weight: " + TextFormatting.YELLOW + ConfigDynamicDifficulty.weightPlayerServerTime + TextFormatting.RESET + " = "
									+ TextFormatting.RED + roundVal((DynamicDifficulty.getDifficultyScaleForPlayerServerTime(ent) * ConfigDynamicDifficulty.weightPlayerServerTime)));

							CoroUtilMisc.sendCommandSenderMsg(var1, "avg chunk time: " + TextFormatting.GREEN + roundVal(DynamicDifficulty.getDifficultyScaleForPosOccupyTime(ent.world, pos))
									+ TextFormatting.RESET + " weight: " + TextFormatting.YELLOW + ConfigDynamicDifficulty.weightPosOccupy + TextFormatting.RESET + " = "
									+ TextFormatting.RED + roundVal((DynamicDifficulty.getDifficultyScaleForPosOccupyTime(ent.world, pos) * ConfigDynamicDifficulty.weightPosOccupy)));

							CoroUtilMisc.sendCommandSenderMsg(var1, "best dps: " + TextFormatting.GREEN + roundVal(DynamicDifficulty.getDifficultyScaleForPosDPS(ent.world, pos))
									+ TextFormatting.RESET + " weight: " + TextFormatting.YELLOW + ConfigDynamicDifficulty.weightDPS + TextFormatting.RESET + " = "
									+ TextFormatting.RED + roundVal((DynamicDifficulty.getDifficultyScaleForPosDPS(ent.world, pos) * ConfigDynamicDifficulty.weightDPS)));

							CoroUtilMisc.sendCommandSenderMsg(var1, "health: " + TextFormatting.GREEN + roundVal(DynamicDifficulty.getDifficultyScaleForHealth(ent))
									+ TextFormatting.RESET + " weight: " + TextFormatting.YELLOW + ConfigDynamicDifficulty.weightHealth + TextFormatting.RESET + " = "
									+ TextFormatting.RED + roundVal((DynamicDifficulty.getDifficultyScaleForHealth(ent) * ConfigDynamicDifficulty.weightHealth)));

							CoroUtilMisc.sendCommandSenderMsg(var1, "dist from spawn: " + TextFormatting.GREEN + roundVal(DynamicDifficulty.getDifficultyScaleForDistFromSpawn(ent))
									+ TextFormatting.RESET + " weight: " + TextFormatting.YELLOW + ConfigDynamicDifficulty.weightDistFromSpawn + TextFormatting.RESET + " = "
									+ TextFormatting.RED + roundVal((DynamicDifficulty.getDifficultyScaleForDistFromSpawn(ent) * ConfigDynamicDifficulty.weightDistFromSpawn)));

							CoroUtilMisc.sendCommandSenderMsg(var1, "buffed location: " + TextFormatting.GREEN + roundVal(DynamicDifficulty.getDifficultyForBuffedLocation(world, pos))
									+ TextFormatting.RESET + " weight: " + TextFormatting.YELLOW + ConfigDynamicDifficulty.weightBuffedLocation + TextFormatting.RESET + " = "
									+ TextFormatting.RED + roundVal((DynamicDifficulty.getDifficultyForBuffedLocation(world, pos) * ConfigDynamicDifficulty.weightBuffedLocation)));

							CoroUtilMisc.sendCommandSenderMsg(var1, "debuffed location: " + TextFormatting.GREEN + roundVal(DynamicDifficulty.getDifficultyForDebuffedLocation(world, pos))
									+ TextFormatting.RESET + " weight: " + TextFormatting.YELLOW + ConfigDynamicDifficulty.weightDebuffedLocation + TextFormatting.RESET + " = "
									+ TextFormatting.RED + roundVal((DynamicDifficulty.getDifficultyForDebuffedLocation(world, pos) * ConfigDynamicDifficulty.weightDebuffedLocation)));

							CoroUtilMisc.sendCommandSenderMsg(var1, "invasion skip buff: " + TextFormatting.GREEN + roundVal(DynamicDifficulty.getInvasionSkipBuff(ent)));
							CoroUtilMisc.sendCommandSenderMsg(var1, "------------");
							CoroUtilMisc.sendCommandSenderMsg(var1, "average: " + TextFormatting.GREEN + DynamicDifficulty.getDifficultyScaleAverage(ent.world, ent, pos));
						}
					}
				} else if (var2[0].equalsIgnoreCase("setdps")) {
					float dps = 0;
					if (var2.length > 1) {
						dps = Float.valueOf(var2[1]);
					}
					DynamicDifficulty.setDifficultyScaleForPosDPS(world, new BlockCoord(player.getPosition()), dps, 30);
					CoroUtilMisc.sendCommandSenderMsg(player, "set dps for loaded chunks within 30 block radius to " + dps);
				} else if (var2[0].equalsIgnoreCase("resetdps")) {
					DynamicDifficulty.setDifficultyScaleForPosDPS(world, new BlockCoord(player.getPosition()), 0, 50);
					CoroUtilMisc.sendCommandSenderMsg(player, "reset dps for loaded chunks within 50 block radius");
				} else if (var2[0].equalsIgnoreCase("testitem")) {
					/**
					 * ResourceLocation resourcelocation = new ResourceLocation(id);
					 Item item = (Item)Item.REGISTRY.getObject(resourcelocation);
					 */
					Item item = Item.getByNameOrId("particleman:particleglove");
					if (item != null) {
						System.out.println("! " + item.getRegistryName().toString());
					}
					if (player != null) {
						player.inventory.addItemStackToInventory(new ItemStack(item, 1));
					}
				} else if (var2[0].equalsIgnoreCase("testloot")) {

					//TODO: MOVE ALL THIS BELOW TO ITS OWN COMMAND WITHIN COROUTIL LIB

				} else if (var2[0].equalsIgnoreCase("reloadData")) {
					DifficultyDataReader.loadFiles();
					var1.sendMessage(new TextComponentString("Difficulty data reloaded"));
				} else if (var2[0].equalsIgnoreCase("registry")) {
					/*for (Map.Entry<String, Class <? extends Entity >> entry : EntityList.NAME_TO_CLASS.entrySet()) {
						var1.sendMessage(new TextComponentString(entry.getKey()));
						System.out.println(entry.getKey());
					}*/
					//TODO: VALIDATE ITEMS!!!
					//actually spawns the mobs
				} else if (var2[0].equalsIgnoreCase("ts") || var2[0].equalsIgnoreCase("testSpawn")) {
					if (player != null) {
						String profileName = var2[1];
						DataMobSpawnsTemplate profileFound = null;
						for (DataMobSpawnsTemplate profile : DifficultyDataReader.getData().listMobSpawnTemplates) {
							if (profile.name.equals(profileName)) {
								profileFound = profile;
								break;
							}
						}

						if (profileFound != null) {

							boolean spawnAll = false;
							if (var2.length > 2) {
								spawnAll = var2[2].equalsIgnoreCase("all");
							}

							if (spawnAll) {
								for (DataActionMobSpawns spawns : profileFound.spawns) {
									for (String spawn : spawns.entities) {
										for (int i = 0; i < spawns.count; i++) {
											spawnInvasionMob(world, player, spawn, spawns, posVec);
										}
									}
								}
							} else {
								Random rand = new Random();
								DataActionMobSpawns spawns = profileFound.spawns.get(rand.nextInt(profileFound.spawns.size()));
								String spawn = spawns.entities.get(rand.nextInt(spawns.entities.size()));

								spawnInvasionMob(world, player, spawn, spawns, posVec);
							}


							/*var1.sendMessage(new TextComponentString(TextFormatting.GREEN + "Invasion profile validation test"));
							String data = profileFound.toString();
							String[] list = data.split(" \\| ");
							for (String entry : list) {
								var1.sendMessage(new TextComponentString(entry));
							}*/
						} else {
							var1.sendMessage(new TextComponentString("Could not find template by name " + profileName));
						}

					}
					//its actually called template, but old command was profile so ill still support it
				} else if (var2[0].equalsIgnoreCase("tp") || var2[0].equalsIgnoreCase("testProfile") ||
						var2[0].equalsIgnoreCase("tt") || var2[0].equalsIgnoreCase("testTemplate")) {
					if (player != null) {
						try {
							DifficultyDataReader.setDebugFlattenCmodsAndConditions(false);
							DifficultyDataReader.setDebugValidate(true);

							String profileName = var2[1];

							BlockCoord pos = new BlockCoord(MathHelper.floor(posVec.x), MathHelper.floor(posVec.y), MathHelper.floor(posVec.z));
							double difficultyScale = DynamicDifficulty.getDifficultyScaleAverage(world, player, pos);
							if (var2.length >= 3) difficultyScale = Double.valueOf(var2[2]);

							DifficultyDataReader.setDebugDifficulty(difficultyScale);

							DataMobSpawnsTemplate profileFound = null;
							for (DataMobSpawnsTemplate profile : DifficultyDataReader.getData().listMobSpawnTemplates) {
								if (profile.name.equals(profileName)) {
									profileFound = profile;
									break;
								}
							}

							if (profileFound != null) {
								var1.sendMessage(new TextComponentString(TextFormatting.GREEN + "Invasion template validation test, difficulty: " + difficultyScale));
								String data = profileFound.toString();
								String[] list = data.split(" \\| ");
								for (String entry : list) {
									var1.sendMessage(new TextComponentString(entry));
								}

								DifficultyDataReader.setDebugFlattenCmodsAndConditions(true);

								var1.sendMessage(new TextComponentString(TextFormatting.GREEN + "Invasion template validation test with cmod/condition templates flattened, difficulty: " + difficultyScale));
								data = profileFound.toString();
								list = data.split(" \\| ");
								for (String entry : list) {
									var1.sendMessage(new TextComponentString(entry));
								}
							} else {
								var1.sendMessage(new TextComponentString("Could not find template by name " + profileName));
							}
						} finally {
							DifficultyDataReader.setDebugFlattenCmodsAndConditions(false);
							DifficultyDataReader.setDebugValidate(false);
							DifficultyDataReader.setDebugDifficulty(-1);
						}

					}
				} else if (var2[0].equalsIgnoreCase("testaabb")) {

				    //added to track down what block from a mod is returning a null AABB that crashes pathfinder

                    System.out.println("");
                    System.out.println("TRY 1");
                    System.out.println("");
                    for (Block block : Block.REGISTRY) {
					    for (IBlockState state : block.getBlockState().getValidStates()) {
                            AxisAlignedBB aabb = block.getBoundingBox(state, world, new BlockPos(0, 64, 0));
                            if (aabb != null) {
                                System.out.println("name: " + block.getRegistryName() + " aabb: " + aabb);
                            } else {
                                System.out.println("NULL AABB FOR: " + block.getRegistryName() + " aabb: " + aabb);
                            }
                        }

					}

                    System.out.println("");
                    System.out.println("TRY 2");
                    System.out.println("");
                    for (Block block : Block.REGISTRY) {
                        for (IBlockState state : block.getBlockState().getValidStates()) {
                            AxisAlignedBB aabb = state.getBoundingBox(world, new BlockPos(0, 64, 0));
                            if (aabb != null) {
                                System.out.println("name: " + block.getRegistryName() + " aabb: " + aabb);
                            } else {
                                System.out.println("NULL AABB FOR: " + block.getRegistryName() + " aabb: " + aabb);
                            }
                        }
                    }
				} else if (var2[0].equalsIgnoreCase("testRepair")) {
					Vec3d vec = var1.getPositionVector();
					int sx = MathHelper.floor(parseCoordinate(vec.x, var2[1], false).getResult());
					int sy = MathHelper.floor(parseCoordinate(vec.y, var2[2], false).getResult());
					int sz = MathHelper.floor(parseCoordinate(vec.z, var2[3], false).getResult());

					BlockPos pos = new BlockPos(sx, sy, sz);
					IBlockState state = world.getBlockState(pos);

					if (UtilMining.canConvertToRepairingBlock(world, state)) {
						var1.sendMessage(new TextComponentString("Setting coord to repairing block, block was: " + state));
						TileEntityRepairingBlock.replaceBlockAndBackup(world, pos);
						/*world.setBlockState(pos, CommonProxy.blockRepairingBlock.getDefaultState());
						TileEntity tEnt = world.getTileEntity(pos);
						if (tEnt instanceof TileEntityRepairingBlock) {
							((TileEntityRepairingBlock) tEnt).setBlockData(state);
						}*/
					} else {
						var1.sendMessage(new TextComponentString("Coordinate does not support repairing block, block was: " + state));
						/*Block.spawnAsEntity(world, pos, new ItemStack(state.getBlock(), 1));
						world.setBlockToAir(pos);*/
					}
				} else if (var2[0].equalsIgnoreCase("testPower")) {
					Vec3d vec = var1.getPositionVector();
					int sx = MathHelper.floor(parseCoordinate(vec.x, var2[1], false).getResult());
					int sy = MathHelper.floor(parseCoordinate(vec.y, var2[2], false).getResult());
					int sz = MathHelper.floor(parseCoordinate(vec.z, var2[3], false).getResult());

					BlockPos pos = new BlockPos(sx, sy, sz);
					IBlockState state = world.getBlockState(pos);

					CoroUtilCompatibility.testPowerInfo(player, pos);
				} else if (var2[0].equalsIgnoreCase("testBiomeSpawns")) {

					boolean showAll = false;
					if (var2.length >= 2) showAll = (var2[1].equalsIgnoreCase("all"));

					var1.sendMessage(new TextComponentString("testing biome entity spawn data for weights, show all? = " + showAll));
					//System.out.println("testing biome entity spawn data for weights, show all? = " + showAll);

					for (Biome biome : Biome.REGISTRY) {

						if (showAll) {
							var1.sendMessage(new TextComponentString("Processing entries for biome: " + biome.biomeName));
						}

						for (EnumCreatureType type : EnumCreatureType.values()) {

							if (showAll) {
								var1.sendMessage(new TextComponentString("Processing entries for EnumCreatureType: " + type.name()));
							}

							List<Biome.SpawnListEntry> list = biome.getSpawnableList(type);
							boolean found = false;
							int totalWeight = 0;

							if (showAll) {
								var1.sendMessage(new TextComponentString("SpawnListEntry size: " + list.size()));
							}

							for (Biome.SpawnListEntry entry : list) {
								totalWeight += entry.itemWeight;
								if (entry.itemWeight == 0) {
									var1.sendMessage(new TextComponentString("found zero weight entry: " + entry.entityClass.getName() + " for biome '" + biome.biomeName + "' for EnumCreatureType '" + type.name()));
									found = true;
								} else if (showAll) {
									var1.sendMessage(new TextComponentString("weight entry: " + entry.entityClass.getName() + "weight: " + entry.itemWeight));
								}
							}

							if (found) {
								if (totalWeight == 0) {
									var1.sendMessage(new TextComponentString("CRASH RISK! total weight is 0 but list is not empty, mods that add the entity classes above need to sanitize their config input to avoid adding 0 weight spawnables"));
									var1.sendMessage(new TextComponentString("issue for biome '" + biome.biomeName + "' for EnumCreatureType '" + type.name() + "', SpawnListEntry size: " + list.size()));
								}

							}

							if (showAll) {
								var1.sendMessage(new TextComponentString("total weight of list: " + totalWeight));
							}
						}
					}
				}
			/*}*/
		} catch (Exception ex) {
			System.out.println("Exception handling CoroUtil command");
			ex.printStackTrace();
		}
		
	}
	
	public void spawnEntity(EntityPlayer player, Entity ent) {
		double dist = 1D;
		
		double finalX = player.posX - (Math.sin(player.rotationYaw * 0.01745329F) * dist);
		double finalZ = player.posZ + (Math.cos(player.rotationYaw * 0.01745329F) * dist);
		
		double finalY = player.posY;
		
		ent.setPosition(finalX, finalY, finalZ);

		//OLD COMMENT: moved to after spawn, so client has an entity at least before syncs fire
		//new comment: vanilla sets onInitialSpawn before spawning, so do it that way, need for better syncing
		if (ent instanceof EntityLiving) ((EntityLiving)ent).onInitialSpawn(player.world.getDifficultyForLocation(new BlockPos(ent)), null);
		player.world.spawnEntity(ent);
	}
	
	public List<String> listEntitiesSpawnable(String entName) {
		List<String> entNames = new ArrayList<String>();
        
		Iterator it = EntityList.getEntityNameList().iterator();
		
		while (it.hasNext()) {
			ResourceLocation entry = (ResourceLocation) it.next();
			
			if (entName.equals("<ALL>") || entry.toString().toLowerCase().contains(entName.toLowerCase())) {
				entNames.add(entry.toString());
			}
		}
		
		return entNames;
	}
	
	public HashMap<String, Integer> listEntities(String entName, int dim, boolean simpleNames) {
		HashMap<String, Integer> entNames = new HashMap<String, Integer>();
        
		World world = DimensionManager.getWorld(dim);
		
        for (int var33 = 0; var33 < world.loadedEntityList.size(); ++var33)
        {
            Entity ent = (Entity)world.loadedEntityList.get(var33);
            
            String entClass = ent.getClass().getCanonicalName();
            
            if (simpleNames) {
            	entClass = EntityList.getEntityString(ent);
            }
            
            if (entClass != null && (entName == null || /*EntityList.getEntityString(ent)*/entClass.toLowerCase().contains(entName.toLowerCase()))) {
	            int val = 1;
	            
	            if (entNames.containsKey(entClass)) {
	            	val = entNames.get(entClass)+1;
	            }
	            entNames.put(entClass, val);
            }
            
        }
        
        //entNames.put("Total count: ", world.loadedEntityList.size());
        
        return entNames;
	}
	
	public List<String> listEntitiesLocations(String entName, int dim, boolean simpleNames, int indexStart) {
		//HashMap<String, Integer> entNames = new HashMap<String, Integer>();
		List<String> listData = new ArrayList<String>();
        
		World world = DimensionManager.getWorld(dim);
		
		int matches = 0;
		
        for (int var33 = 0; var33 < world.loadedEntityList.size(); ++var33)
        {
            Entity ent = (Entity)world.loadedEntityList.get(var33);
            
            String entClass = ent.getClass().getCanonicalName();
            
            if (simpleNames) {
            	entClass = EntityList.getEntityString(ent);
            }
            
            if (entClass != null && (entName == null || /*EntityList.getEntityString(ent)*/entClass.toLowerCase().contains(entName.toLowerCase()))) {
            	
            	if (indexStart <= matches) {
            		listData.add("pos: " + MathHelper.floor(ent.posX) + ", " + MathHelper.floor(ent.posY) + ", " + MathHelper.floor(ent.posZ) + ", " + entClass);
            		if (listData.size() >= 10) {
            			return listData;
            		}
            	}
            	
	            /*int val = 1;
	            
	            if (entNames.containsKey(entClass)) {
	            	val = entNames.get(entClass)+1;
	            }
	            entNames.put(entClass, val);*/
            	matches++;
            }
            
        }
        
        //entNames.put("Total count: ", world.loadedEntityList.size());
        
        return listData;
	}
	
	public int getEntityCount(String entName, boolean killEntities, boolean exact, int dim) {
		int count = 0;
		
        for (int var33 = 0; var33 < DimensionManager.getWorld(dim).loadedEntityList.size(); ++var33)
        {
            Entity ent = (Entity)DimensionManager.getWorld(dim).loadedEntityList.get(var33);
            
            if (EntityList.getEntityString(ent) != null && (EntityList.getEntityString(ent).equals(entName) || (!exact && EntityList.getEntityString(ent).toLowerCase().contains(entName.toLowerCase())))) {
            	count++;
            	if (killEntities) {
            		//ent.attackEntityFrom(DamageSource.generic, 60);
            		ent.setDead();
            	}
            }
        }
        
        return count;
	}

	public void spawnInvasionMob(World world, EntityPlayer player, String spawn, DataActionMobSpawns spawns, Vec3d posVec) {

		if (spawn.equals("minecraft:bat")) {
			spawn = "coroutil:bat_smart";
		}

		Class clazz = EntityList.getClass(new ResourceLocation(spawn));

		if (clazz != null) {
			Entity entL = EntityList.newEntity(clazz, world);
			if (entL != null && entL instanceof EntityCreature) {

				EntityCreature ent = (EntityCreature) entL;

				BlockCoord pos = new BlockCoord(MathHelper.floor(posVec.x), MathHelper.floor(posVec.y), MathHelper.floor(posVec.z));
				float difficultyScale = DynamicDifficulty.getDifficultyScaleAverage(world, player, pos);

				ent.getEntityData().setBoolean(UtilEntityBuffs.dataEntityWaveSpawned, true);
				UtilEntityBuffs.registerAndApplyCmods(ent, spawns.cmods, difficultyScale);

				ent.getEntityData().setBoolean(UtilEntityBuffs.dataEntityInitialSpawn, true);
				spawnEntity(player, ent);
				ent.getEntityData().setBoolean(UtilEntityBuffs.dataEntityInitialSpawn, false);

				//add a bit of random to space multi spawns out
				ent.setPosition(ent.posX + world.rand.nextDouble(), ent.posY, ent.posZ + world.rand.nextDouble());

				//CoroUtilMisc.sendCommandSenderMsg(player, "spawned: " + CoroUtilEntity.getName(ent));
			} else {
				player.sendMessage(new TextComponentString("entity instance null or not EntityCreature"));
			}
		} else {
			player.sendMessage(new TextComponentString("entity class null"));
		}
	}

	public String roundVal(float val) {
		DecimalFormat df = new DecimalFormat("#.##");
		return df.format(val);
	}

	public String roundVal(double val) {
		DecimalFormat df = new DecimalFormat("#.##");
		return df.format(val);
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender par1ICommandSender)
    {
        return par1ICommandSender.canUseCommand(this.getRequiredPermissionLevel(), this.getName());
    }

	@Override
	public String getUsage(ICommandSender icommandsender) {
		return "Magic dev method!";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

}
