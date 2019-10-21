package CoroUtil.util;

import CoroUtil.ai.tasks.TaskDigTowardsTarget;
import CoroUtil.config.ConfigBlockDestruction;
import CoroUtil.config.ConfigCoroUtilAdvanced;
import CoroUtil.forge.CULog;
import CoroUtil.forge.CommonProxy;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.*;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.command.InvalidBlockStateException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.world.BlockEvent;

import java.util.*;

public class UtilMining {

	/**
	 *
	 * needs to be mined:
	 * - if collision
	 *
	 * regular blocks:
	 * - always breaks unless in blacklist
	 * - list decides:
	 * -- actual break?
	 * -- or repairing block
	 *
	 * tile entities:
	 * - we cant use repairing block on this (for now)
	 * - list decides:
	 * -- can be mined
	 * -- cant be mined
	 *
	 * - if we readd simudigging, use these rules to work around unmineable things
	 */

	public static List<BlockState> listBlocksBlacklistedRepairing = new ArrayList<>();
	public static List<BlockState> listTileEntitiesWhitelistedBreakable = new ArrayList<>();

	public static GameProfile fakePlayerProfile = null;

	public static class ClientData {
		public static List<BlockState> listBlocksBlacklistedRepairing = new ArrayList<>();
		public static List<BlockState> listTileEntitiesWhitelistedBreakable = new ArrayList<>();
	}

	/**
	 * Basically check if theres collision
	 * @param world
	 * @param pos
	 * @return
	 */
	public static boolean needsToMineBlock(World world, BlockPos pos) {

		//optimizations to avoid doing full collision check where possible
		if (world.isAirBlock(pos)) return false;
		if (world.getBlockState(pos).getBlock() == CommonProxy.blockRepairingBlock) return false;

		return blockHasCollision(world, pos);
	}

	/**
	 * Server side only
	 *
	 * Returns false if its a block without collision
	 * Then checks if its a tile entity that is whitelisted or other config allows all tile entities
	 *
	 * @param world
	 * @param pos
	 * @return
	 */
	public static boolean canMineBlockNew(World world, BlockPos pos) {
		if (!needsToMineBlock(world, pos)) return false;
		if (!canBreakBlockOrTileEntity(world, pos, false)) return false;
		return true;
	}

	public static boolean blockHasCollision(World world, BlockPos pos) {
		return world.getBlockState(pos).getCollisionBoundingBox(world, pos) != Block.NULL_AABB;
	}

	/**
	 * Server side only
	 *
	 * for now all regular blocks can be mined until we implement a 3rd list that blacklists that
	 *
	 * Can break as in break or convert to repairing block, do anything with really
	 *
	 * @param world
	 * @param pos
	 * @return
	 */
	public static boolean canBreakBlockOrTileEntity(World world, BlockPos pos, boolean isClient) {
		boolean isTileEntity = world.getTileEntity(pos) != null;

		//THIS IS FOR choosing break / repair mode, not if it can be mined, dont use here
		//boolean cantMineRegularBlock = !isTileEntity && UtilMining.isBlockBlacklistedNonTileEntity(world, pos, false);

		boolean cantMineTileEntity = isTileEntity && !UtilMining.isBlockWhitelistedToBreakTileEntity(world, pos, isClient);

		//TODO: using an invasion context specific config here, if we use this method elsewhere this is a problem
		return /*!cantMineRegularBlock || */!cantMineTileEntity || !TaskDigTowardsTarget.preventMinedTileEntitiesDuringInvasions;
	}

	/**
	 *
	 * @param world
	 * @param pos
	 * @return
	 */
	public static boolean canConvertToRepairingBlockNew(World world, BlockPos pos, boolean isClient) {
		boolean isTileEntity = world.getTileEntity(pos) != null;

		//boolean isClient = false;

		if (!isTileEntity) {
			return !isBlockBlacklistedFromRepairingBlockNonTileEntity(world, pos, isClient);
		}

		return false;
	}

	public static boolean isBlockBlacklistedFromRepairingBlockNonTileEntity(World world, BlockPos pos, boolean client) {
		//using getActualState here fixes things like upper half of double_plant returning the incorrect runtime value
		BlockState state = world.getBlockState(pos).getActualState(world, pos);

		return CoroUtilBlockState.partialStateInListMatchesFullState(state, client ? ClientData.listBlocksBlacklistedRepairing : listBlocksBlacklistedRepairing);
	}

	public static boolean isBlockWhitelistedToBreakTileEntity(World world, BlockPos pos, boolean client) {

		BlockState state = world.getBlockState(pos).getActualState(world, pos);

		return CoroUtilBlockState.partialStateInListMatchesFullState(state, client ? ClientData.listTileEntitiesWhitelistedBreakable : listTileEntitiesWhitelistedBreakable);

		//return false;
	}

	public static boolean testing(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);

		HashSet<BlockState> listBlocksBlacklisted = new HashSet<>();
		List<BlockState> listBlocksBlacklisted2 = new ArrayList<>();


		BlockState state1 = Blocks.DOUBLE_PLANT.getDefaultState()
				.withProperty(DoublePlantBlock.VARIANT, DoublePlantBlock.EnumPlantType.SUNFLOWER)
				.withProperty(DoublePlantBlock.HALF, DoublePlantBlock.EnumBlockHalf.UPPER);

		BlockState state2 = Blocks.DOUBLE_PLANT.getDefaultState()
				.withProperty(DoublePlantBlock.VARIANT, DoublePlantBlock.EnumPlantType.SUNFLOWER)
				.withProperty(DoublePlantBlock.HALF, DoublePlantBlock.EnumBlockHalf.UPPER);

		BlockState state3 = Blocks.DOUBLE_PLANT.getDefaultState()
				.withProperty(DoublePlantBlock.VARIANT, DoublePlantBlock.EnumPlantType.SUNFLOWER)

				;

		//setblock ~ ~ ~ minecraft:double_plant variant=sunflower
		String str = "variant=sunflower,half=upper";
		BlockState state5 = null;

		try {
			state5 = CoroUtilBlockState.convertArgToPartialBlockState(Blocks.DOUBLE_PLANT, str);
			//state5 = CoroUtilBlockState.convertArgToPartialBlockState(Blocks.GRASS, str);
		} catch (NumberInvalidException e) {
			e.printStackTrace();
		} catch (InvalidBlockStateException e) {
			e.printStackTrace();
		}

		BlockState state4 = Blocks.DOUBLE_PLANT.getStateFromMeta(Blocks.DOUBLE_PLANT.getMetaFromState(state1));

		listBlocksBlacklisted.add(state2);
		if (state5 != null) listBlocksBlacklisted2.add(state5);

		if (listBlocksBlacklisted2.contains(state1)/*state1.equals(state2)*/) {
			System.out.println("within list state match");
		} else {
			System.out.println("within list state mismatch");
		}

		if (state5 != null && CoroUtilBlockState.partialStateMatchesFullState(state5, state1)) {
			System.out.println("partial state match");
		} else {
			System.out.println("partial state mismatch");
		}

		return false;
	}

	@Deprecated
	public static boolean canMineBlock(World world, BlockCoord pos, Block block) {
		return canMineBlock(world, pos.toBlockPos(), block);
	}

	@Deprecated
    public static boolean canMineBlock(World world, BlockPos pos, Block block) {
		return canMineBlock(world, pos);
    }

	@Deprecated
	public static boolean canMineBlock(World world, BlockPos pos) {
		//System.out.println("check: " + block);

		BlockState state = world.getBlockState(pos);

		//dont mine tile entities
		if (state.getBlock().isAir(state, world, pos) || state.getBlock() == CommonProxy.blockRepairingBlock) {
			return false;
		}
		if (TaskDigTowardsTarget.preventMinedTileEntitiesDuringInvasions && world.getTileEntity(pos) != null) {
			return false;
		}
    	/*if (block == Blocks.obsidian) {
    		return false;
    	}*/
		if (state.getMaterial().isLiquid()) {
			return false;
		}

		return true;
	}

	@Deprecated
    public static boolean canConvertToRepairingBlock(World world, BlockState state) {

		if (state.getMaterial() == Material.GLASS) {
			return true;
		}

		//should cover most all types we dont want to put into repairing state
		//TODO: this covers too many actually, like glass, fence
		/**
		 * The main goal was to protect against multi block blocks i think
		 * - door
		 * - double plant
		 * - tall flowers
		 * - sugar cane
		 * - vine?
		 *
		 * need a generic method for this, or a blacklist?
		 * - implement a configurable blacklist/whitelist with the bad things already added for blacklist mode?
		 *
		 * - black/white list for blocks that can be damaged
		 * - black/white list for blocks that can be converted to repairing block
		 * - tile entity black(?)/white list: eio conduits
		 *
		 * - print out a list of blocks with "convertable / not convertable" result
		 * - client side in world debug to show red/green for every block in a close range?
		 */
		if (!state.isFullCube()) {
			return false;
		}
		return true;
	}

	public static boolean tryRemoveBlockWithFakePlayer(World world, BlockPos pos) {
		BlockState stateRemove = world.getBlockState(pos);

		if (canGrabEventCheck(world, stateRemove, pos)) {

			boolean forceRemoveIfNeeded = true;

			if (fakePlayerProfile == null) {
				fakePlayerProfile = new GameProfile(UUID.fromString("4365749c-bd72-497c-a0dd-73f28dafd8a1"), "coroutilMiningFakePlayer");
			}
			FakePlayer player = FakePlayerFactory.get((ServerWorld) world, fakePlayerProfile);
			//for good measure
			player.setPosition(pos.getX(), pos.getY(), pos.getZ());

			//this is the general structure used by player removal, we will follow but also make sure blocks actually always removed, not live by exact rules
			boolean actuallyRemoved = stateRemove.getBlock().removedByPlayer(stateRemove, world, pos, player, true);
			boolean canHarvest = stateRemove.getBlock().canHarvestBlock(world, pos, player);

			//if block wont play nice, force it now, youre not stopping my zombie miners
			if (!actuallyRemoved && forceRemoveIfNeeded) {
				world.setBlockToAir(pos);
				actuallyRemoved = true;
			}

			if (actuallyRemoved) {
				stateRemove.getBlock().onBlockDestroyedByPlayer(world, pos, stateRemove);
				if (canHarvest) {
					ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
					stateRemove.getBlock().harvestBlock(world, player, pos, stateRemove, world.getTileEntity(pos), stack);
				}
			}
		}

		return false;
	}

	public static boolean canGrabEventCheck(World world, BlockState state, BlockPos pos) {
		if (!ConfigCoroUtilAdvanced.blockBreakingInvokesCancellableEvent) return true;
		if (world instanceof ServerWorld) {
			if (fakePlayerProfile == null) {
				fakePlayerProfile = new GameProfile(UUID.fromString("4365749c-bd72-497c-a0dd-73f28dafd8a1"), "coroutilMiningFakePlayer");
			}
			BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, FakePlayerFactory.get((ServerWorld) world, fakePlayerProfile));
			MinecraftForge.EVENT_BUS.post(event);
			return !event.isCanceled();
		} else {
			return false;
		}
	}

	public static void processBlockBlacklist(String config, List<BlockState> list) {
		try {
			String[] names = config.split(" ");
			for (int i = 0; i < names.length; i++) {
				names[i] = names[i].trim();

				String name = "";
				String metaOrState = "";
				//int meta = 0;

				if (names[i].contains("[")) {
					name = names[i].split("\\[")[0];
					try {
						metaOrState = names[i].split("\\[")[1];
						metaOrState = metaOrState.substring(0, metaOrState.length()-1);
						//meta = Integer.valueOf(names[i].split(":")[1]);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					name = names[i];
				}

				Block block = Block.getBlockFromName(name);
				if (block != null) {
					BlockState state = null;
					try {
						if (metaOrState.equals("")) {
							state = CoroUtilBlockState.getStatelessBlock(block);
						} else {
							state = CoroUtilBlockState.convertArgToPartialBlockState(block, metaOrState);
						}

						//state = block.getStateFromMeta(meta);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					if (state != null) {
						CULog.dbg("Adding: " + state);
						list.add(state);
					}

				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void processBlockLists() {
		try {

			CULog.dbg("Processing destructable blocks for CoroUtil");
			UtilMining.listBlocksBlacklistedRepairing.clear();
			UtilMining.listTileEntitiesWhitelistedBreakable.clear();

			processBlockBlacklist(ConfigBlockDestruction.blacklistRepairable_RegularBlocks, UtilMining.listBlocksBlacklistedRepairing);
			processBlockBlacklist(ConfigBlockDestruction.whitelistMineable_TileEntities, UtilMining.listTileEntitiesWhitelistedBreakable);

			//List<String> list = new ArrayList<>();
			//Matcher m = Pattern.compile(",(?![^()]*\\))").matcher(blacklistMineable_RegularBlocks);
			//Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(blacklistMineable_RegularBlocks);
			//while (m.find()) {
			//System.out.println(m.group(1));
			//String entry = m.group(1);
			//}

			//eg: double_plant variant=sunflower,half=upper;grass;double_plant variant=double_rose;desirepaths:grass_worn_2
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
