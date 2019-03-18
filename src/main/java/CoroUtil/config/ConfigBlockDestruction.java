package CoroUtil.config;

import CoroUtil.forge.CULog;
import CoroUtil.packet.PacketHelper;
import CoroUtil.util.CoroUtilBlockState;
import CoroUtil.util.UtilMining;
import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigBlockDestruction implements IConfigCategory {

	@ConfigComment("")
	public static String blacklistMineable_RegularBlocks = "";

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
