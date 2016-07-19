package CoroUtil.world.grid.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
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
        
        Iterator it = Block.REGISTRY.getKeys().iterator();
        
        int i = 0;
        while (it.hasNext()) {
        	String tagName = (String) it.next();
        	Block block = (Block) Block.REGISTRY.getObject(new ResourceLocation(tagName));
        	
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
		

		addToMap(Blocks.GRASS, lightlyPacked);
		addToMap(Blocks.DIRT, lightlyPacked);
		addToMap(Blocks.SAND, lightlyPacked);
		//addToMap(Blocks.slowSand, lightlyPacked);
		addToMap(Blocks.GRAVEL, lightlyPacked);

		addToMap(Blocks.LEAVES, plant);
		
		addToMap(Blocks.OAK_FENCE_GATE, plank);
		addToMap(Blocks.PLANKS, plank);
		addToMap(Blocks.SPRUCE_STAIRS, plank);
		addToMap(Blocks.BIRCH_STAIRS, plank);
		addToMap(Blocks.JUNGLE_STAIRS, plank);
		addToMap(Blocks.SANDSTONE_STAIRS, plank);
		
		
		addToMap(Blocks.GLASS, glass);
		addToMap(Blocks.ICE, glass);
		addToMap(Blocks.GLASS_PANE, glass);
		
		addToMap(Blocks.SANDSTONE, stoneWeak);
		addToMap(Blocks.COBBLESTONE, stoneWeak);
		addToMap(Blocks.MOSSY_COBBLESTONE, stoneWeak);
		addToMap(Blocks.COBBLESTONE_WALL, stoneWeak);
		addToMap(Blocks.COAL_ORE, stoneWeak);
		addToMap(Blocks.DIAMOND_ORE, stoneWeak);
		addToMap(Blocks.EMERALD_ORE, stoneWeak);
		addToMap(Blocks.GOLD_ORE, stoneWeak);
		addToMap(Blocks.IRON_ORE, stoneWeak);
		addToMap(Blocks.LAPIS_ORE, stoneWeak);
		addToMap(Blocks.QUARTZ_ORE, stoneWeak);
		addToMap(Blocks.REDSTONE_ORE, stoneWeak);
		//addToMap(Blocks.oreRedstoneGlowing, stoneWeak);
		addToMap(Blocks.CLAY, stoneWeak);
		addToMap(Blocks.NETHERRACK, stoneWeak);
		addToMap(Blocks.NETHER_BRICK_FENCE, stoneWeak);
		addToMap(Blocks.HARDENED_CLAY, stoneWeak);
		
		
		addToMap(Blocks.LOG, treeWood);
		addToMap(Blocks.LOG2, treeWood);
		
		addToMap(Blocks.DISPENSER, stoneMachine);
		addToMap(Blocks.STICKY_PISTON, stoneMachine);
		addToMap(Blocks.PISTON, stoneMachine);
		addToMap(Blocks.PISTON_EXTENSION, stoneMachine);
		addToMap(Blocks.PISTON_HEAD, stoneMachine);
		addToMap(Blocks.DISPENSER, stoneMachine);
		
		addToMap(Blocks.BRICK_BLOCK, stoneReinforced);
		addToMap(Blocks.STONEBRICK, stoneReinforced);
		addToMap(Blocks.IRON_BARS, stoneReinforced);
		addToMap(Blocks.NETHER_BRICK, stoneReinforced);
		addToMap(Blocks.NETHER_BRICK_STAIRS, stoneReinforced);
		addToMap(Blocks.BRICK_STAIRS, stoneReinforced);
		addToMap(Blocks.STONE_BRICK_STAIRS, stoneReinforced);
		
		addToMap(Blocks.STONE, stoneNatural);
		
		addToMap(Blocks.OBSIDIAN, obsidian);
		addToMap(Blocks.DIAMOND_BLOCK, diamond);
		
		addToMap(Blocks.FIRE, iron);
		
		addToMap(Blocks.BEDROCK, unbreakable);
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
