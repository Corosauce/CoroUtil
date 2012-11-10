package net.minecraft.src;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

import CoroAI.PFQueue;
import CoroAI.PathEntityEx;
import CoroAI.entity.c_EnhAI;
import CoroAI.entity.c_PlayerProxy;

import net.minecraft.client.Minecraft;
import net.minecraft.src.*;

public class c_CoroAIUtil {
	
	public static String refl_mcp_Item_maxStackSize = "maxStackSize";
	public static String refl_c_Item_maxStackSize = "cg";
    public static String refl_s_Item_maxStackSize = "cg";
	public static String refl_mcp_Item_navigator = "navigator";
	public static String refl_c_Item_navigator = "bN";
	public static String refl_s_Item_navigator = "bN";
	
	public static String refl_mcp_EntityPlayer_itemInUse = "itemInUse";
	public static String refl_c_EntityPlayer_itemInUse = "f";
	public static String refl_s_EntityPlayer_itemInUse = "f";
	public static String refl_mcp_EntityPlayer_itemInUseCount = "itemInUseCount";
	public static String refl_c_EntityPlayer_itemInUseCount = "g";
	public static String refl_s_EntityPlayer_itemInUseCount = "g";
	public static String refl_mcp_FoodStats_foodLevel = "foodLevel";
	public static String refl_c_FoodStats_foodLevel = "a";
	public static String refl_s_FoodStats_foodLevel = "a";
	
	@SideOnly(Side.CLIENT)
	public static Minecraft mc;
	
	//Tropicraft reflection
	public static boolean hasTropicraft = true; //try reflection once
	public static String tcE = "net.tropicraft.entities.";
	public static String[] koaEnemyWhitelist = {"EntityVMonkey", "EntityTropicalFish", "EntityEIH", "EntityTropicraftWaterMob", "EntityTropiCreeper", "EntityAmphibian"};
	public static Item fishingRodTropical;
	public static Item swordZircon;
	public static Item leafBall;
	
	public c_CoroAIUtil() {
		
	}
	
    public static boolean koaEnemy(Entity ent) {
    	try {
    		if (hasTropicraft) {
	    		for (String entStr : koaEnemyWhitelist) {
	    			if (Class.forName(tcE + entStr).isInstance(ent)) {
	    				return false;
	    			}
	    		}
    		}
    	} catch (Exception ex) {
    		hasTropicraft = false;
    		//ex.printStackTrace();
    	}
    	return true;
    }
    
    public static boolean isEnemy(c_PlayerProxy ent, Entity enemy) {
    	if (enemy instanceof EntityLiving && !(enemy == ent)
    			&& !(enemy instanceof EntityCreeper
    			|| enemy instanceof EntityWaterMob
    			|| enemy instanceof EntityEnderman
    			|| enemy instanceof EntityPlayer
    			|| !koaEnemy(enemy))) {
			return true;
		}
    	return false;
    }
	
	@SideOnly(Side.CLIENT)
	public static void watchWorldObj() {
		if (mc != null) {
			if (mc.theWorld != PFQueue.worldMap) {
	    		System.out.print("PFQueue detecting new world, updating...");
	    		PFQueue.worldMap = mc.theWorld;
	    	}
		} else mc = ModLoader.getMinecraftInstance();
	}
	
	public static boolean isNoPathBlock(Entity ent, int id, int meta) {
		if (ent instanceof EntityPlayer) {
			//barricades
			if (id >= 192 && id <= 197) {
				return true;
			}
			
			Block block = Block.blocksList[id];
			
			if (block != null && block instanceof BlockFence) {
				return true;
			}
			
			if (block != null && block instanceof BlockWall) {
				return true;
			}
			
			if (block != null && block instanceof BlockFenceGate) {
				return !BlockFenceGate.isFenceGateOpen(meta);
			}
			
			
		}
		//if (false) {
		/*if (id == ZCBlocks.barrier.blockID) {
			return true;
		}*/
		return false;
	}
	
	public static Field s_getItemInUse() {
		return tryGetField(EntityPlayer.class, refl_s_EntityPlayer_itemInUse, refl_mcp_EntityPlayer_itemInUse);
	}
	
	public static Field s_getItemInUseCount() {
		return tryGetField(EntityPlayer.class, refl_s_EntityPlayer_itemInUseCount, refl_mcp_EntityPlayer_itemInUseCount);
	}
	
	public static Field s_getFoodLevel() {
		return tryGetField(FoodStats.class, refl_s_FoodStats_foodLevel, refl_mcp_FoodStats_foodLevel);
	}
	
	public static Field tryGetField(Class theClass, String obf, String mcp) {
		Field field = null;
		try {
			field = theClass.getDeclaredField(obf);
			field.setAccessible(true);
		} catch (Exception ex) {
			try {
				field = theClass.getDeclaredField(mcp);
				field.setAccessible(true);
			} catch (Exception ex2) { ex2.printStackTrace(); }
		}
		return field;
	}
	
	public static void setPrivateValueBoth(Class var0, Object var1, String obf, String mcp, Object var3) {
    	try {
            try {
                setPrivateValue(var0, var1, obf, var3);
            } catch (NoSuchFieldException ex) {
                setPrivateValue(var0, var1, mcp, var3);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static Object getPrivateValueBoth(Class var0, Object var1, String obf, String mcp) {
    	try {
            try {
                return getPrivateValue(var0, var1, obf);
            } catch (NoSuchFieldException ex) {
                return getPrivateValue(var0, var1, mcp);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public static Object getPrivateValue(Class var0, Object var1, String var2) throws IllegalArgumentException, SecurityException, NoSuchFieldException
    {
        try
        {
            Field var3 = var0.getDeclaredField(var2);
            var3.setAccessible(true);
            return var3.get(var1);
        }
        catch (IllegalAccessException var4)
        {
            ModLoader.throwException("An impossible error has occured!", var4);
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
    
    public static boolean AIRightClickHook(c_PlayerProxy ent, ItemStack itemToUse) {
    	/*if (itemToUse.getItem() instanceof ZCSdkItemGun) {
			if (ZCSdkTools.useItemInInventory(ent.fakePlayer, ((ZCSdkItemGun)itemToUse.getItem()).requiredBullet.shiftedIndex) <= 0) {
				return false;
			}
		}*/
    	return true;
    }
    
    public static void setPrjOwnerHook(c_PlayerProxy ent, Entity lastEnt) {
        
    	/*if (ent instanceof ZCSdkEntityBullet) {
			((ZCSdkEntityBullet) ent).owner = this;
		}*/
    }
    
    public static void setItems_JobHunt(c_PlayerProxy ent) {
    	//System.out.println("setItems_JobHunt broken");
    	//Melee slot
    	getTropiItemRefl("swordZircon", swordZircon);
    	if (swordZircon != null) ent.inventory.addItemStackToInventory(new ItemStack(swordZircon, 1));
		//Ranged slot
    	getTropiItemRefl("leafBall", leafBall);
    	if (leafBall != null) ent.inventory.addItemStackToInventory(new ItemStack(leafBall, 1));
		
		ent.wantedItems.add(Item.fishRaw.shiftedIndex);
		ent.wantedItems.add(Item.fishCooked.shiftedIndex);
		ent.wantedItems.add(Item.porkRaw.shiftedIndex);
		ent.wantedItems.add(Item.porkCooked.shiftedIndex);
		ent.wantedItems.add(Item.chickenRaw.shiftedIndex);
		ent.wantedItems.add(Item.chickenCooked.shiftedIndex);
    }
    
    public static Item getTropiItemRefl(String fieldName, Item cache) {
		//Item item = null;
    	try {
    		if (hasTropicraft) {
    			if (cache == null) {
	    			Class clazz = Class.forName("net.tropicraft.mods.TropicraftMod");
	    			
	    			if (clazz != null) {
	    				cache = (Item)getPrivateValue(clazz, clazz, fieldName);
	    				if (cache == null) {
	    					hasTropicraft = false;
	    				} else {
	    					setPrivateValue(c_CoroAIUtil.class, c_CoroAIUtil.class, fieldName, cache);
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
    	//ent.setCurrentItem(TropicraftMod.fishingRodTropical.shiftedIndex);
    }
    
    public static void equipFishingRod(c_EnhAI ent) {
    	
    	getTropiItemRefl("fishingRodTropical", fishingRodTropical);
    	if (fishingRodTropical != null) ent.setCurrentItem(fishingRodTropical.shiftedIndex);
    	
    	/*try {
    		if (hasTropicraft) {
    			if (fishingRodTropical == null) {
	    			Class clazz = Class.forName("net.tropicraft.mods.TropicraftMod");
	    			
	    			if (clazz != null) {
	    				fishingRodTropical = (Item)getPrivateValue(clazz, clazz, "fishingRodTropical");
	    				if (fishingRodTropical == null) {
	    					hasTropicraft = false;
	    					return;
	    				} else {
	    					ent.setCurrentItem(fishingRodTropical.shiftedIndex);
	    				}
	    			} else {
	    				hasTropicraft = false;    				
	    			}
    			} else {
    				ent.setCurrentItem(fishingRodTropical.shiftedIndex);
    			}
    		}
    	} catch (Exception ex) {
    		hasTropicraft = false;
    		System.out.println("this really shouldnt ever happen unless fishing job is used outside tropicraft");
    		//ex.printStackTrace();
    	}*/
    	//ent.setCurrentItem(TropicraftMod.fishingRodTropical.shiftedIndex);
    }
    
    public static boolean tryTransferToChest(c_EnhAI ent, int x, int y, int z) {
    	
    	TileEntityChest chest = (TileEntityChest)ent.worldObj.getBlockTileEntity(x, y, z);
		if (chest != null) {
			ent.openHomeChest();
			ent.transferItems(ent.inventory, chest, -1, -1, true);
			return true;
		}
		return false;
    }
    
    public static void setItems_JobGather(c_PlayerProxy ent) {
    	ent.wantedItems.add(Block.wood.blockID);
    }
    
    public static void setItems_JobFish(c_PlayerProxy ent) {
    	getTropiItemRefl("swordZircon", swordZircon);
    	if (swordZircon != null) ent.inventory.addItemStackToInventory(new ItemStack(swordZircon, 1));
		//Ranged slot
    	getTropiItemRefl("fishingRodTropical", fishingRodTropical);
    	if (fishingRodTropical != null) ent.inventory.addItemStackToInventory(new ItemStack(fishingRodTropical, 1));
		
		ent.wantedItems.add(Item.fishRaw.shiftedIndex);
		ent.wantedItems.add(Item.fishCooked.shiftedIndex);
    }
    
    public static boolean isServer() {
    	return false;
    }
	
    public static EntityPlayer getFirstPlayer() {
    	//if (mc == null) mc = ModLoader.getMinecraftInstance();
    	//return mc.thePlayer;
    	return null;
    }
    
    public static boolean isChest(int id) {
    	if (id == 0) return false;
    	Block block = Block.blocksList[id];
    	if (block != null) {
    		if (block instanceof BlockChest) return true;
    	}
		return false;
	}
    
    public static PathEntityEx pathToEntity; //compile compatible method, aiplayer uses watcher on this
    public static boolean newPath = false;
    
    public static void playerPathfindCallback(PathEntityEx pathEx) {
    	/*c_AIP.i.*/pathToEntity = pathEx;
    	newPath = true;
    }
}
