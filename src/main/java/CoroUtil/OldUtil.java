package CoroUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FoodStats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import CoroUtil.pathfinding.PathEntityEx;
import CoroUtil.util.BlockCoord;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.Vec3;

public class OldUtil {
	
	public static String refl_mcp_Item_maxStackSize = "maxStackSize";
    public static String refl_s_Item_maxStackSize = "cq";
	//public static String refl_mcp_Item_moveSpeed = "moveSpeed";
	//public static String refl_obf_Item_moveSpeed = "bI";
	
	public static String refl_mcp_EntityPlayer_itemInUse = "itemInUse";
	public static String refl_s_EntityPlayer_itemInUse = "field_71074_e";
	public static String refl_mcp_EntityPlayer_itemInUseCount = "itemInUseCount";
	public static String refl_s_EntityPlayer_itemInUseCount = "field_71072_f";
	public static String refl_mcp_FoodStats_foodLevel = "foodLevel";
	//public static String refl_c_FoodStats_foodLevel = "a";
	public static String refl_s_FoodStats_foodLevel = "field_75127_a";
	
	public static String refl_thrower_mcp = "thrower";
	public static String refl_thrower_obf = "field_70192_c";
	
	public static String refl_loadedChunks_mcp = "loadedChunks";
	public static String refl_loadedChunks_obf = "field_73245_g";
	
	public static String refl_curBlockDamageMP_mcp = "curBlockDamageMP";
	public static String refl_curBlockDamageMP_obf = "field_78770_f";
	
	
	
	public static boolean checkforMCP = true;
	public static boolean runningMCP = true;
	//TODO: USE THIS INSTEAD NOW (for 1.8 only possibly) Launch.blackboard.get("fml.deobfuscatedEnvironment") , if it doesnt exist it means its running in production
	
	//public static HashMap<String, c_EntInterface> playerToAILookup = new HashMap();
	//public static HashMap<String, ICoroAI> playerToCompAILookup = new HashMap();
	
	//Tropicraft reflection
	public static boolean hasTropicraft = true; //try reflection once
	public static String tcE = "tropicraft.entities.";
	//public static String[] koaEnemyWhitelist = {"EntityVMonkey", "EntityTropicalFish", "EntityEIH", "EntityTropicraftWaterMob", "EntityTropiCreeper", "EntityAmphibian"};
	public static String[] koaEnemyWhitelist = {""};
	public static Item fishingRodTropical;
	public static Item dagger;
	public static Item leafBall;
	
	
	public OldUtil() {
		//wut
	}
	
    public static boolean koaEnemy(Entity ent) {
    	try {
    		if (hasTropicraft) {
	    		/*for (String entStr : koaEnemyWhitelist) {
	    			if (Class.forName(tcE + entStr).isInstance(ent)) {
	    				return false;
	    			}
	    		}*/
    		}
    	} catch (Exception ex) {
    		hasTropicraft = false;
    		ex.printStackTrace();
    	}
    	return true;
    }
	
    //TODO: 1.8 needs the block pos for checking open fence gates, fix for ZC
	public static boolean isNoPathBlock(Entity ent, Block parBlock, int meta) {
		if (ent instanceof PlayerEntity) {
			//barricades
			System.out.println("FIX BARRICADES SPECIAL RULE HERE!");
			/*if (id >= 192 && id <= 197) {
				return true;
			}*/
			
			Block block = parBlock;//Block.blocksList[id];
			
			if (block != null && block instanceof FenceBlock) {
				return true;
			}
			
			if (block != null && block instanceof WallBlock) {
				return true;
			}
			
			//force no pass for now till fixed above message
			if (block != null && block instanceof FenceGateBlock) {
				return true;
				//return !BlockFenceGate.isFenceGateOpen(meta);
			}
			
			
		}
		//if (false) {
		/*if (id == ZCBlocks.barrier.blockID) {
			return true;
		}*/
		return false;
	}
	
	public static Field s_getItemInUse() {
		return tryGetField(PlayerEntity.class, refl_s_EntityPlayer_itemInUse, refl_mcp_EntityPlayer_itemInUse);
	}
	
	public static Field s_getItemInUseCount() {
		return tryGetField(PlayerEntity.class, refl_s_EntityPlayer_itemInUseCount, refl_mcp_EntityPlayer_itemInUseCount);
	}
	
	public static Field s_getFoodLevel() {
		return tryGetField(FoodStats.class, refl_s_FoodStats_foodLevel, refl_mcp_FoodStats_foodLevel);
	}
	
	public static Field tryGetField(Class theClass, String obf, String mcp) {
		Field field = null;
		try {
			field = theClass.getDeclaredField(obf);//field = theClass.getDeclaredField(ObfuscationReflectionHelper.remapFieldNames(theClass.getName(), new String[] { obf })[0]);
			field.setAccessible(true);
		} catch (Exception ex) {
			try {
				field = theClass.getDeclaredField(mcp);
				field.setAccessible(true);
			} catch (Exception ex2) { ex2.printStackTrace(); }
		}
		return field;
	}
	
	/*public static void check() {
		checkforMCP = false;
		try {
			runningMCP = getPrivateValue(Vec3.class, Vec3.fakePool, "fakePool") != null;
		} catch (Exception e) {
			runningMCP = false;
			System.out.println("CoroAI: 'fakePool' field not found, mcp mode disabled");
		}
	}*/
	
	public static void check() {
		checkforMCP = false;
		try {
			//runningMCP = getPrivateValue(MinecraftServer.class, MinecraftServer.getServer(), "tickables") != null;
			//runningMCP = getPrivateValue(MinecraftServer.class, FMLCommonHandler.instance().getMinecraftServerInstance(), "motd") != null;
			runningMCP = getPrivateValue(Vec3i.class, null, "NULL_VECTOR") != null;
		} catch (Exception e) {
			runningMCP = false;
			System.out.println("CoroAI: 'tickables' field not found, mcp mode disabled");
		}
	}
	
	public static void setPrivateValueBoth(Class var0, Object var1, String obf, String mcp, Object var3) {
		if (checkforMCP) check();
    	try {
    		
    		if (!runningMCP) {
    			//CHANGED FOR 1.7!!
                setPrivateValue(var0, var1, obf, var3);
            	//ObfuscationReflectionHelper.setPrivateValue(var0, var1, obf, var3);
    		} else {
    			setPrivateValue(var0, var1, mcp, var3);
    		}
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
	
	public static Object getPrivateValueSRGMCP(Class var0, Object var1, String srg, String mcp) {
    	if (checkforMCP) check();
    	try {
    		
    		if (!runningMCP) {
    			return getPrivateValue(var0, var1, srg);
    		} else {
    			return getPrivateValue(var0, var1, mcp);
    		}
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
	
	public static void setPrivateValueSRGMCP(Class var0, Object var1, String obf, String mcp, Object var3) {
		if (checkforMCP) check();
		try {
    		
    		if (!runningMCP) {
    			setPrivateValue(var0, var1, obf, var3);
    		} else {
    			setPrivateValue(var0, var1, mcp, var3);
    		}
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
    
    public static Object getPrivateValueBoth(Class var0, Object var1, String obf, String mcp) {
    	if (checkforMCP) check();
    	try {
    		
    		if (!runningMCP) {
    			return ObfuscationReflectionHelper.getPrivateValue(var0, var1, obf);
    		} else {
    			return getPrivateValue(var0, var1, mcp);
    		}
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public static Object getPrivateValue(Class var0, Object var1, String var2)/* throws IllegalArgumentException, SecurityException, NoSuchFieldException*/
    {
        try
        {
            Field var3 = var0.getDeclaredField(var2);
            var3.setAccessible(true);
            return var3.get(var1);
        }
        catch (Exception var4)
        {
        	//var4.printStackTrace();
        	//FMLCommonHandler.instance().raiseException(var4, "An impossible error has occured!", true);
            //ModLoader.throwException("An impossible error has occured!", var4);
            return null;
        }
    }
    
    static Field field_modifiers = null;
    
    public static void setPrivateValue(Class var0, Object var1, int var2, Object var3) throws IllegalArgumentException, SecurityException, NoSuchFieldException
    {
        try
        {
            Field var4 = var0.getDeclaredFields()[var2];
            var4.setAccessible(true);
            int var5 = field_modifiers.getInt(var4);

            if ((var5 & 16) != 0)
            {
                field_modifiers.setInt(var4, var5 & -17);
            }

            var4.set(var1, var3);
        }
        catch (IllegalAccessException var6)
        {
            //logger.throwing("ModLoader", "setPrivateValue", var6);
            //throwException("An impossible error has occured!", var6);
        }
    }
    
    public static void setPrivateValue(Class var0, Object var1, String var2, Object var3) throws IllegalArgumentException, SecurityException, NoSuchFieldException
    {
        try
        {
        	if (field_modifiers == null) {
        		field_modifiers = Field.class.getDeclaredField("modifiers");
                field_modifiers.setAccessible(true);
        	}
        	
            Field var4 = var0.getDeclaredField(var2);
            int var5 = field_modifiers.getInt(var4);

            if ((var5 & 16) != 0)
            {
                field_modifiers.setInt(var4, var5 & -17);
            }

            var4.setAccessible(true);
            //added in 1.6.4
            field_modifiers.setInt(var4, var4.getModifiers() & ~Modifier.FINAL);
            var4.set(var1, var3);
        }
        catch (IllegalAccessException var6)
        {
            //logger.throwing("ModLoader", "setPrivateValue", var6);
            //throwException("An impossible error has occured!", var6);
        }
    }

	/*public void setBlocksExplodable(boolean var1)
    {
        try
        {
            int var2;

            if (!var1)
            {
                blockResistance = new float[Block.blocksList.length];

                for (var2 = 0; var2 < Block.blocksList.length; ++var2)
                {
                    if (Block.blocksList[var2] != null)
                    {
                        blockResistance[var2] = Block.blocksList[var2].blockResistance;

                        if (var2 != Block.glass.blockID || !mod_SdkGuns.bulletsDestroyGlass)
                        {
                            Block.blocksList[var2].blockResistance = 6000000.0F;
                        }
                    }
                }
            }
            else if (blockResistance != null)
            {
                for (var2 = 0; var2 < Block.blocksList.length; ++var2)
                {
                    if (Block.blocksList[var2] != null)
                    {
                        Block.blocksList[var2].blockResistance = blockResistance[var2];
                    }
                }
            }

            areBlocksExplodable = var1;
        }
        catch (Exception var3)
        {
            ModLoader.getLogger().throwing("mod_SdkFps", "setBlocksExplodable", var3);
            SdkTools.ThrowException(String.format("Error setting blocks explodable: %b.", new Object[] {Boolean.valueOf(var1)}), var3);
        }
    }*/
    
    public static Item getTropiItemRefl(String fieldName, Item cache) {
		//Item item = null;
    	try {
    		if (hasTropicraft) {
    			if (cache == null) {
	    			Class clazz = Class.forName("tropicraft.items.TropicraftItems");
	    			
	    			if (clazz != null) {
	    				cache = (Item)getPrivateValue(clazz, clazz, fieldName);
	    				if (cache == null) {
	    					hasTropicraft = false;
	    				} else {
	    					setPrivateValue(OldUtil.class, OldUtil.class, fieldName, cache);
	    				}
	    			} else {
	    				hasTropicraft = false;    				
	    			}
    			} else {
    				
    			}
    		}
    	} catch (Exception ex) {
    		hasTropicraft = false;
    		System.out.println("this really shouldnt ever happen unless fishing job is used outside tropicraft");
    		//ex.printStackTrace();
    	}
    	return cache;
    	//ent.setCurrentItem(TropicraftMod.fishingRodTropical.itemID);
    }
    
    public static boolean isServer() {
    	return false;
    }
	
    public static PlayerEntity getFirstPlayer() {
    	//if (mc == null) mc = ModLoader.getMinecraftInstance();
    	//return mc.player;
    	return null;
    }
    
    public static boolean isChest(Block parBlock) {
    	if (CoroUtilBlock.isAir(parBlock)) return false;
    	//if (id == 0) return false;
    	//Block block = Block.blocksList[id];
    	if (parBlock != null) {
    		if (parBlock instanceof ChestBlock) return true;
    	}
		return false;
	}
    
    public static PathEntityEx pathToEntity; //compile compatible method, aiplayer uses watcher on this
    public static boolean newPath = false;
    
    public static void playerPathfindCallback(PathEntityEx pathEx) {
    	/*c_AIP.i.*/pathToEntity = pathEx;
    	newPath = true;
    }
    
    /*public static Entity getEntByPersistantID(World world, int id) {
		try {
			for (int i = 0; i < world.loadedEntityList.size(); i++) {
				Entity ent = (Entity)world.loadedEntityList.get(i);
				if (ent instanceof ICoroAI) {
					if (((ICoroAI) ent).getAIAgent().entID == id && id != -1) {
						return (Entity)ent;
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}*/
    
    //public static int getAge(EntityLivingBase ent) { return ent.entityAge; }
    //public static void addAge(EntityLivingBase ent, int offsetAge) { ent.entityAge += offsetAge; }
    //public static void checkDespawn(EntityLiving ent) { ent.checkDespawn(); }
    public static float getMoveSpeed(LivingEntity ent) { return (float) ent.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).get(); }
    //public static void setMoveSpeed(EntityLivingBase ent, float speed) { ent.getAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(speed); }
    //public static void setHealth(EntityLivingBase ent, int health) { ent.health = health; }
    public static void jump(LivingEntity ent) { ent.motionY = 0.42F;/*ent.jump();*/ }
    public static boolean chunkExists(World world, int x, int z) { return world.isBlockLoaded(new BlockPos(x * 16, 128, z * 16)); } //fixed for 1.5
    
    public static BlockCoord entToCoord(Entity ent) { return new BlockCoord(MathHelper.floor(ent.posX), MathHelper.floor(ent.posY), MathHelper.floor(ent.posZ)); }
    public static double getDistance(Entity ent, BlockCoord coords) { return ent.getDistance(coords.posX, coords.posY, coords.posZ); }
    public static double getDistanceXZ(Entity ent, BlockCoord coords) { return ent.getDistance(coords.posX, ent.posY, coords.posZ); }
    public static double getDistanceXZ(BlockCoord coords, BlockCoord coords2) { return Math.sqrt(coords.distanceSq(coords2.posX, coords.posY, coords2.posZ)); }
    public static boolean canVecSeeCoords (World parWorld, Vec3 parVec, double posX, double posY, double posZ) {	return parWorld.rayTraceBlocks(new Vec3d(parVec.xCoord, parVec.yCoord, parVec.zCoord), new Vec3d(posX, posY, posZ)) == null; }
    public static boolean canEntSeeCoords (Entity ent, double posX, double posY, double posZ) {	return ent.world.rayTraceBlocks(new Vec3d(ent.posX, ent.getBoundingBox().minY + (double)ent.getEyeHeight(), ent.posZ), new Vec3d(posX, posY, posZ)) == null; }
    public static boolean canCoordsSeeCoords (World world, double posX, double posY, double posZ, double posX2, double posY2, double posZ2) {	return world.rayTraceBlocks(new Vec3d(posX, posY, posZ), new Vec3d(posX2, posY2, posZ2)) == null; }
    //public static void dropItems(EntityLivingBase ent, boolean what, int what2) { ent.dropFewItems(what, what2); }
}

