package CoroUtil.config;

import CoroUtil.forge.CULog;
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
		try {

			CULog.dbg("Processing destructable blocks for CoroUtil");
			UtilMining.listBlocksBlacklisted.clear();

			//List<String> list = new ArrayList<>();
			//Matcher m = Pattern.compile(",(?![^()]*\\))").matcher(blacklistMineable_RegularBlocks);
			//Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(blacklistMineable_RegularBlocks);
			//while (m.find()) {
				//System.out.println(m.group(1));
				//String entry = m.group(1);
			//}

			//eg: double_plant variant=sunflower,half=upper;grass;double_plant variant=double_rose;desirepaths:grass_worn_2

			String[] names = blacklistMineable_RegularBlocks.split(" ");
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
					IBlockState state = null;
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
						UtilMining.listBlocksBlacklisted.add(state);
					}

				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
