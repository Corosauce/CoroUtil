package CoroUtil.util;

import CoroUtil.ai.tasks.TaskDigTowardsTarget;
import CoroUtil.config.ConfigCoroUtilAdvanced;
import CoroUtil.forge.CommonProxy;
import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.InvalidBlockStateException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.world.BlockEvent;

import java.util.*;

public class UtilMining {

	public static List<IBlockState> listBlocksBlacklisted = new ArrayList<>();

	public static GameProfile fakePlayerProfile = null;

	public static boolean blockHasCollision(World world, BlockPos pos) {
		return world.getBlockState(pos).getCollisionBoundingBox(world, pos) != Block.NULL_AABB;
	}

	public static boolean isBlockBlacklistedNonTileEntity(World world, BlockPos pos) {
		//using getActualState here fixes things like upper half of double_plant returning the incorrect runtime value
		IBlockState state = world.getBlockState(pos).getActualState(world, pos);

		return CoroUtilBlockState.partialStateInListMatchesFullState(state, listBlocksBlacklisted);
	}

	public static boolean testing(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);

		HashSet<IBlockState> listBlocksBlacklisted = new HashSet<>();
		List<IBlockState> listBlocksBlacklisted2 = new ArrayList<>();


		IBlockState state1 = Blocks.DOUBLE_PLANT.getDefaultState()
				.withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.SUNFLOWER)
				.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER);

		IBlockState state2 = Blocks.DOUBLE_PLANT.getDefaultState()
				.withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.SUNFLOWER)
				.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER);

		IBlockState state3 = Blocks.DOUBLE_PLANT.getDefaultState()
				.withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.SUNFLOWER)

				;

		//setblock ~ ~ ~ minecraft:double_plant variant=sunflower
		String str = "variant=sunflower,half=upper";
		IBlockState state5 = null;

		try {
			state5 = CoroUtilBlockState.convertArgToPartialBlockState(Blocks.DOUBLE_PLANT, str);
			//state5 = CoroUtilBlockState.convertArgToPartialBlockState(Blocks.GRASS, str);
		} catch (NumberInvalidException e) {
			e.printStackTrace();
		} catch (InvalidBlockStateException e) {
			e.printStackTrace();
		}

		IBlockState state4 = Blocks.DOUBLE_PLANT.getStateFromMeta(Blocks.DOUBLE_PLANT.getMetaFromState(state1));

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

	public static boolean isBlockWhitelistedTileEntity(World world, BlockPos pos) {
		//TODO: all of it
		return false;
	}

	public static boolean canMineBlock(World world, BlockCoord pos, Block block) {
		return canMineBlock(world, pos.toBlockPos(), block);
	}

    public static boolean canMineBlock(World world, BlockPos pos, Block block) {
		return canMineBlock(world, pos);
    }

	public static boolean canMineBlock(World world, BlockPos pos) {
		//System.out.println("check: " + block);

		IBlockState state = world.getBlockState(pos);

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

    public static boolean canConvertToRepairingBlock(World world, IBlockState state) {

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
		IBlockState stateRemove = world.getBlockState(pos);

		if (canGrabEventCheck(world, stateRemove, pos)) {

			boolean forceRemoveIfNeeded = true;

			if (fakePlayerProfile == null) {
				fakePlayerProfile = new GameProfile(UUID.fromString("4365749c-bd72-497c-a0dd-73f28dafd8a1"), "coroutilMiningFakePlayer");
			}
			FakePlayer player = FakePlayerFactory.get((WorldServer) world, fakePlayerProfile);
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

	public static boolean canGrabEventCheck(World world, IBlockState state, BlockPos pos) {
		if (!ConfigCoroUtilAdvanced.blockBreakingInvokesCancellableEvent) return true;
		if (world instanceof WorldServer) {
			if (fakePlayerProfile == null) {
				fakePlayerProfile = new GameProfile(UUID.fromString("4365749c-bd72-497c-a0dd-73f28dafd8a1"), "coroutilMiningFakePlayer");
			}
			BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, FakePlayerFactory.get((WorldServer) world, fakePlayerProfile));
			MinecraftForge.EVENT_BUS.post(event);
			return !event.isCanceled();
		} else {
			return false;
		}
	}

}
