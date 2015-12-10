package CoroUtil.world.grid.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import CoroUtil.util.CoroUtilBlock;

public class BlockStaticDataMap {
	public static HashMap<String, Float> mapBlockWeight;
	public static HashMap<String, Integer> mapBlockHashNameToID;
	public static HashMap<Integer, String> mapBlockHashIDToName;
	
	public static void initWeightMap() {
		mapBlockWeight = new HashMap<String, Float>();
		mapBlockHashNameToID = new HashMap<String, Integer>();
		mapBlockHashIDToName = new HashMap<Integer, String>();
		
		List<String> fails = new ArrayList<String>();
    	
    	//System.out.println("TEST OUTPUT OF UNLOCALIZED NAMES! GO!");
        /*for (int i = 0; i < Block.blocksList.length; i++) {
        	Block block = Block.blocksList[i];
        	if (block != null) {
        		String hash;// = block.getClass().toString() + "|" + block.getUnlocalizedName() + "|" + Block.lightValue[i];
        		hash = block.getUnlocalizedName();
        		//System.out.println("ID: " + i + " - " + hash);
        		if (mapBlockHashNameToID.containsKey(hash) && !block.getUnlocalizedName().equals("tile.ForgeFiller")) {
        			fails.add("ID: " + i + " vs " + mapBlockHashNameToID.get(hash) + " - " + hash);
        		}
        		mapBlockHashNameToID.put(hash, i);
        		mapBlockHashIDToName.put(i, hash);
        	}
        }*/
        
        Iterator it = Block.blockRegistry.getKeys().iterator();
        
        int i = 0;
        while (it.hasNext()) {
        	String tagName = (String) it.next();
        	Block block = (Block) Block.blockRegistry.getObject(tagName);
        	
        	if (block != null) {
        		String hash;// = block.getClass().toString() + "|" + block.getUnlocalizedName() + "|" + Block.lightValue[i];
        		hash = CoroUtilBlock.getNameByBlock(block);
        		//System.out.println("ID: " + i + " - " + hash);
        		if (mapBlockHashNameToID.containsKey(hash)/* && !block.getUnlocalizedName().equals("tile.ForgeFiller")*/) {
        			fails.add("ID: " + i + " vs " + mapBlockHashNameToID.get(hash) + " - " + hash);
        		}
        		mapBlockHashNameToID.put(hash, i);
        		mapBlockHashIDToName.put(i, hash);
        	}
        }
        
		float m = 5;
		
		float lightlyPacked = 1; //dirt, sand
		
		float plant = m*0.5F; //barely stronger than dirt
		
		float glass = m;
		float crystal = m;
		
		float plank = m*2;
		float mixedManufactured = m*2; //rails, circuitry stuff
		
		float treeWood = m*3;
		float stoneWeak = m*3;
		float woodMachines = m*3;
		
		float stoneMachine = m*4;
		
		float stoneReinforced = m*5; //brick
		
		float gold = m*6;
		
		float stoneNatural = m*7;
		
		float iron = m*8;
		
		float steel = m*10; //not vanilla
		
		float obsidian = m*15;
		
		float diamond = m*30;
		
		float unbreakable = -1; //bedrock
		
		//in future, even vanilla ids might not be reliable, so use objects name for vanilla
		

		addToMap(Blocks.grass, lightlyPacked);
		addToMap(Blocks.dirt, lightlyPacked);
		addToMap(Blocks.sand, lightlyPacked);
		//addToMap(Blocks.slowSand, lightlyPacked);
		addToMap(Blocks.gravel, lightlyPacked);

		addToMap(Blocks.leaves, plant);
		
		addToMap(Blocks.oak_fence_gate, plank);
		addToMap(Blocks.planks, plank);
		addToMap(Blocks.spruce_stairs, plank);
		addToMap(Blocks.birch_stairs, plank);
		addToMap(Blocks.jungle_stairs, plank);
		addToMap(Blocks.sandstone_stairs, plank);
		
		
		addToMap(Blocks.glass, glass);
		addToMap(Blocks.ice, glass);
		addToMap(Blocks.glass_pane, glass);
		
		addToMap(Blocks.sandstone, stoneWeak);
		addToMap(Blocks.cobblestone, stoneWeak);
		addToMap(Blocks.mossy_cobblestone, stoneWeak);
		addToMap(Blocks.cobblestone_wall, stoneWeak);
		addToMap(Blocks.coal_ore, stoneWeak);
		addToMap(Blocks.diamond_ore, stoneWeak);
		addToMap(Blocks.emerald_ore, stoneWeak);
		addToMap(Blocks.gold_ore, stoneWeak);
		addToMap(Blocks.iron_ore, stoneWeak);
		addToMap(Blocks.lapis_ore, stoneWeak);
		addToMap(Blocks.quartz_ore, stoneWeak);
		addToMap(Blocks.redstone_ore, stoneWeak);
		//addToMap(Blocks.oreRedstoneGlowing, stoneWeak);
		addToMap(Blocks.clay, stoneWeak);
		addToMap(Blocks.netherrack, stoneWeak);
		addToMap(Blocks.nether_brick_fence, stoneWeak);
		addToMap(Blocks.hardened_clay, stoneWeak);
		
		
		addToMap(Blocks.log, treeWood);
		addToMap(Blocks.log2, treeWood);
		
		addToMap(Blocks.dispenser, stoneMachine);
		addToMap(Blocks.sticky_piston, stoneMachine);
		addToMap(Blocks.piston, stoneMachine);
		addToMap(Blocks.piston_extension, stoneMachine);
		addToMap(Blocks.piston_head, stoneMachine);
		addToMap(Blocks.dispenser, stoneMachine);
		
		addToMap(Blocks.brick_block, stoneReinforced);
		addToMap(Blocks.stonebrick, stoneReinforced);
		addToMap(Blocks.iron_bars, stoneReinforced);
		addToMap(Blocks.nether_brick, stoneReinforced);
		addToMap(Blocks.nether_brick_stairs, stoneReinforced);
		addToMap(Blocks.brick_stairs, stoneReinforced);
		addToMap(Blocks.stone_brick_stairs, stoneReinforced);
		
		addToMap(Blocks.stone, stoneNatural);
		
		addToMap(Blocks.obsidian, obsidian);
		addToMap(Blocks.diamond_block, diamond);
		
		addToMap(Blocks.fire, iron);
		
		addToMap(Blocks.bedrock, unbreakable);
	}
	
	public static void addToMap(Block block, float strength) {
		/*if (mapBlockWeight.containsKey(block.getUnlocalizedName())) {
			System.out.println("EPOCH DATA MAP WARNING, adding existing unlocalizedname to map: " + block.getUnlocalizedName());
		}
		mapBlockWeight.put(block.getUnlocalizedName(), strength);*/
		
		if (mapBlockWeight.containsKey(CoroUtilBlock.getNameByBlock(block))) {
			System.out.println("EPOCH DATA MAP WARNING, adding existing unlocalizedname to map: " + block.getUnlocalizedName());
		}
		mapBlockWeight.put(CoroUtilBlock.getNameByBlock(block), strength);
	}
	
	public static float getBlockStength(String name) {
		float str = 1F;
		if (mapBlockWeight.containsKey(name)) {
			return mapBlockWeight.get(name);
		} else {
			//System.out.println("epoch block name to strength lookup fail for: " + name);
		}
		return str;
	}
	
	public static float getBlockStength(Block id) {
		float str = 1F;
		if (mapBlockHashIDToName != null) {
			if (mapBlockHashIDToName.containsKey(id)) {
				return getBlockStength(mapBlockHashIDToName.get(id));
			} else {
				System.out.println("block data map block id to name lookup fail for: " + id);
			}
		} else {
			//its null because init method above not called
		}
		return str;
	}
	
	public static float getBlockMaxHealth(Block id) {
		return 10F * getBlockStength(id);
	}
}
