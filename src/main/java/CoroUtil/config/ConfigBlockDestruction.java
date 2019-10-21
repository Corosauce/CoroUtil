package CoroUtil.config;

import CoroUtil.forge.CULog;
import CoroUtil.packet.PacketHelper;
import CoroUtil.util.CoroUtilBlockState;
import CoroUtil.util.UtilMining;
import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigBlockDestruction implements IConfigCategory {

	//@ConfigComment("This is used to ")

	//TODO: should there be a 3rd list for regular blocks?
	//list 1: blocks that cant be broken or converted
	// - dont mine
	//list 2: if entry not in list 1, blocks that must be broken, not converted
	// - dont repair block

	@ConfigComment("Regular blocks that cant be turned into repairing blocks and must be properly destroyed with their item dropped onto the ground, you can specify just block names or partial block states or meta values, examples: desirepaths:grass_worn_2 double_plant[variant=sunflower,half=upper] grass double_plant[variant=double_rose] stone log[0] log[1]")
	public static String blacklistRepairable_RegularBlocks = "wooden_door iron_door acacia_door jungle_door birch_door spruce_door";

	//TODO: this name is ok i think unless we add repairing block support, currently this allows certain tile entities to get fully broken
	@ConfigComment("For Tile Entities that should be fully breakable")
	public static String whitelistMineable_TileEntities = "";

	public static boolean blacklistMineable_RegularBlocks_useAsWhitelist = false;

	@Override
	public String getName() {
		return "BlockDestruction";
	}

	@Override
	public String getRegistryName() {
		return "coroutilbd";
	}

	@Override
	public String getConfigFileName() {
		return "CoroUtil" + File.separator + getName();
	}

	@Override
	public String getCategory() {
		return getName();
	}

	@Override
	public void hookUpdatedValues() {
        UtilMining.processBlockLists();

        //TODO: sync on connect too
        PacketHelper.syncBlockLists();
	}

}

