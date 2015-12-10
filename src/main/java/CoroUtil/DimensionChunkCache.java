package CoroUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import CoroUtil.config.ConfigCoroAI;
import CoroUtil.pathfinding.PFQueue;

public class DimensionChunkCache implements IBlockAccess
{
    public int chunkX;
    public int chunkZ;
    public int chunkXMax;
    public int chunkZMax;
    private Chunk[][] chunkArray;

    /** set by !chunk.getAreLevelsEmpty */
    private boolean hasExtendedLevels;

    /** Reference to the World object. */
    private World worldObj;
    
    public static List<Integer> listBlacklistIDs = new ArrayList<Integer>();
    public static List<String> listBlacklistNamess = new ArrayList<String>();
    
    //Static lookup and cache updating
    public static HashMap<Integer, DimensionChunkCache> dimCacheLookup = new HashMap<Integer, DimensionChunkCache>();
    public static void updateAllWorldCache() {
    	//System.out.println("Updating PFCache");
    	WorldServer[] worlds = DimensionManager.getWorlds();
    	
    	for (int i = 0; i < worlds.length; i++) {
    		WorldServer world = worlds[i];
    		
    		boolean skip = false;
    		
    		//world.chunkExists(par1, par2)
    		//if (i == 0) {
    		if (!ConfigCoroAI.chunkCacheOverworldOnly || i == 0) {
    			
    		} else {
    			skip = true;
    		}
    		
    		if (listBlacklistIDs.contains(i)) {
    			skip = true;
    		}
    		
    		for (int j = 0; j < listBlacklistNamess.size(); j++) {
    			if (world != null && world.provider.getDimensionName().contains(listBlacklistNamess.get(j))) {
        			skip = true;
        			break;
        		}
    		}
    		
    		
    		if (!skip) {
    			dimCacheLookup.put(world.provider.getDimensionId(), new DimensionChunkCache(world, true));
    		}
    	}
    }
    
    public DimensionChunkCache(World world, boolean useLoadedChunks/*, int par2, int par3, int par4, int par5, int par6, int par7*/)
    {
		int chunkCount = 0;
		
    	try {
	    	int minX = 0;
	    	int minZ = 0;
	    	int maxX = 0;
	    	int maxZ = 0;
	    	
	    	List chunks = ((ChunkProviderServer)world.getChunkProvider()).func_152380_a();
    		
	    	if (chunks == null) {
	    		try {
    				chunks = (ArrayList)OldUtil.getPrivateValueSRGMCP(ChunkProviderServer.class, world.getChunkProvider(), OldUtil.refl_loadedChunks_obf, OldUtil.refl_loadedChunks_mcp);
    			} catch (Exception ex2) {
    				System.out.println("SERIOUS REFLECTION FAIL IN DimensionChunkCache");
    			}
	    		/*try {
	    			chunks = (ArrayList)OldUtil.getPrivateValue(ChunkProviderServer.class, world.getChunkProvider(), "loadedChunks");
	    		} catch (Exception ex) {
	    			
	    		}*/
	    	}
    		
    		if (chunks == null) {
    			if (ConfigCoroAI.usePlayerRadiusChunkLoadingForFallback) {
    				System.out.println("unable to get loaded chunks, reverting to potentially cpu/memory heavy player radius method, to deactivate set usePlayerRadiusChunkLoadingForFallback in CoroUtil.cfg to false");
    			} else {
    				System.out.println("loadedChunks is null, DimensionChunkCache unable to cache chunk data for dimension: " + world.provider.getDimensionId() + " - " + world.provider.getDimensionName());
    			}
    		}
	    	
	    	if (chunks != null && useLoadedChunks) {
	    		
	    		//dont forget to readapt this for 1.7.10
	    		//in 1.7.10, i think func_152380_a can return null, so null check our chunks variable
	    		//just added null check for 1.7.2
	    		//Mrbysco was having issues for it in 1.7.10
	    		
	    		
	    		
	    		if (chunks != null) {
		    		for (int i = 0; i < chunks.size(); i++) {
		    			Chunk chunk = (Chunk) chunks.get(i);
		    			
		    			if ((int)chunk.xPosition < minX) minX = chunk.xPosition;
			            if ((int)chunk.zPosition < minZ) minZ = chunk.zPosition;
			            if ((int)chunk.xPosition > maxX) maxX = chunk.xPosition;
			            if ((int)chunk.zPosition > maxZ) maxZ = chunk.zPosition;
		    		}
		    		
		    		/*minX -= 4;
			    	minZ -= 4;
			    	maxX += 4;
			    	maxZ += 4;*/
		    		
		    		this.worldObj = world;
		    		this.chunkX = minX;
			        this.chunkZ = minZ;
			        int var8 = maxX;
			        int var9 = maxZ;
			        
		    		this.chunkArray = new Chunk[var8 - this.chunkX + 1][var9 - this.chunkZ + 1];
		    		this.hasExtendedLevels = true;
		    		
		    		for (int i = 0; i < chunks.size(); i++) {
		    			Chunk chunk = (Chunk) chunks.get(i);
		    			this.chunkArray[chunk.xPosition - this.chunkX][chunk.zPosition - this.chunkZ] = chunk;
		    			chunkCount++;
		    		}
	    		}
	    		
	    	} else if (ConfigCoroAI.usePlayerRadiusChunkLoadingForFallback) {
		    	byte playerRadius = 8;
		    	
		    	for (int i = 0; i < world.playerEntities.size(); ++i)
		        {
		            EntityPlayer var5 = (EntityPlayer)world.playerEntities.get(i);
		            
		            if ((int)var5.posX < minX) minX = (int)var5.posX;
		            if ((int)var5.posZ < minZ) minZ = (int)var5.posZ;
		            if ((int)var5.posX > maxX) maxX = (int)var5.posX;
		            if ((int)var5.posZ > maxZ) maxZ = (int)var5.posZ;
		        }
		    	
		    	minX -= (playerRadius * 16);
		    	minZ -= (playerRadius * 16);
		    	maxX += (playerRadius * 16);
		    	maxZ += (playerRadius * 16);
		    	
		    	this.worldObj = world;
		        this.chunkX = minX >> 4;
		        this.chunkZ = minZ >> 4;
		        int var8 = maxX >> 4;
		        int var9 = maxZ >> 4;
		        this.chunkXMax = var8;
		        this.chunkZMax = var9;
		        this.chunkArray = new Chunk[var8 - this.chunkX + 1][var9 - this.chunkZ + 1];
		        this.hasExtendedLevels = true;
		        
		        for (int i = 0; i < world.playerEntities.size(); ++i)
		        {
		        	
		            EntityPlayer var5 = (EntityPlayer)world.playerEntities.get(i);
		            
		            int pChunkX = MathHelper.floor_double(var5.posX / 16.0D);
		            int pChunkZ = MathHelper.floor_double(var5.posZ / 16.0D);
		            
		            for (int xx = -playerRadius; xx <= playerRadius; ++xx)
		            {
		                for (int zz = -playerRadius; zz <= playerRadius; ++zz)
		                {
		                	if (pChunkX + xx - this.chunkX >= 0 && pChunkZ + zz - this.chunkZ >= 0 && this.chunkArray[pChunkX + xx - this.chunkX][pChunkZ + zz - this.chunkZ] == null) {
		                		Chunk var12 = world.getChunkFromChunkCoords(pChunkX + xx, pChunkZ + zz);
		
		                        if (var12 != null)
		                        {
		                        	//System.out.println("adding to array!");
		                        	chunkCount++;
		                            this.chunkArray[pChunkX + xx - this.chunkX][pChunkZ + zz - this.chunkZ] = var12;
		                        }
		                	}                	
		                }
		            }
		        }
	    	}
	        
	        PFQueue.lastChunkCacheCount = chunkCount;
	        
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		System.out.println("DimensionChunkCache crash, tell Corosus");
    		PFQueue.lastChunkCacheCount = 0;
    	}
        //System.out.println("Total Cached Chunks for Dim " + world.provider.dimensionId + ": " + chunkCount);
    }

    /**
     * set by !chunk.getAreLevelsEmpty
     */
    @Override
    public boolean extendedLevelsInChunkCache()
    {
        return this.hasExtendedLevels;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
    	if (pos.getY() >= 0 && pos.getY() < 256)
        {
            int i = (pos.getX() >> 4) - this.chunkX;
            int j = (pos.getZ() >> 4) - this.chunkZ;
            if (i < 0 || i >= chunkArray.length || j < 0 || i >= chunkArray[i].length) return Blocks.air.getDefaultState();

            if (i >= 0 && i < this.chunkArray.length && j >= 0 && j < this.chunkArray[i].length)
            {
                Chunk chunk = this.chunkArray[i][j];

                if (chunk != null)
                {
                    return chunk.getBlockState(pos);
                }
            }
        }

        return Blocks.air.getDefaultState();
    }

    /**
     * Returns the TileEntity associated with a given block in X,Y,Z coordinates, or null if no TileEntity exists
     */
    @Override
    public TileEntity getTileEntity(BlockPos pos)
    {
        int i = (pos.getX() >> 4) - this.chunkX;
        int j = (pos.getZ() >> 4) - this.chunkZ;
        if (i < 0 || i >= chunkArray.length || j < 0 || j >= chunkArray[i].length) return null;
        if (chunkArray[i][j] == null) return null;
        return this.chunkArray[i][j].getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
    }

    
    /*public float getBrightness(int par1, int par2, int par3, int par4)
    {
        int var5 = this.getLightValue(par1, par2, par3);

        if (var5 < par4)
        {
            var5 = par4;
        }

        return this.worldObj.provider.lightBrightnessTable[var5];
    }*/

    /**
     * Any Light rendered on a 1.8 Block goes through here
     */
    
    public int getLightBrightnessForSkyBlocks(int par1, int par2, int par3, int par4)
    {
        int var5 = this.getSkyBlockTypeBrightness(EnumSkyBlock.SKY, par1, par2, par3);
        int var6 = this.getSkyBlockTypeBrightness(EnumSkyBlock.Block, par1, par2, par3);

        if (var6 < par4)
        {
            var6 = par4;
        }

        return var5 << 20 | var6 << 4;
    }

    /**
     * Returns how bright the block is shown as which is the block's light value looked up in a lookup table (light
     * values aren't linear for brightness). Args: x, y, z
     */
    public float getLightBrightness(int par1, int par2, int par3)
    {
        return this.worldObj.provider.getLightBrightnessTable()[this.getBlockLightValue(par1, par2, par3)];
    }

    /**
     * Gets the light value of the specified block coords. Args: x, y, z
     */
    /*public int getLightValue(int par1, int par2, int par3)
    {
        return this.getLightValueExt(par1, par2, par3, true);
    }*/

    public int getBlockLightValue(int par1, int par2, int par3)
    {
        return this.getBlockLightValue_do(par1, par2, par3, true);
    }

    /**
     * Gets the light value of a block location. This is the actual function that gets the value and has a bool flag
     * that indicates if its a half step block to get the maximum light value of a direct neighboring block (left,
     * right, forward, back, and up)
     */
    public int getBlockLightValue_do(int par1, int par2, int par3, boolean par4)
    {
    	
    	//CoroAI.dbg("test this replacement usage of skylightSubtracted being 0");
    	int skylightSubtracted = 0;
    	
        if (par1 >= -30000000 && par3 >= -30000000 && par1 < 30000000 && par3 < 30000000)
        {
            if (par4 && this.getBlock(par1, par2, par3).getUseNeighborBrightness())
            {
                int l1 = this.getBlockLightValue_do(par1, par2 + 1, par3, false);
                int l = this.getBlockLightValue_do(par1 + 1, par2, par3, false);
                int i1 = this.getBlockLightValue_do(par1 - 1, par2, par3, false);
                int j1 = this.getBlockLightValue_do(par1, par2, par3 + 1, false);
                int k1 = this.getBlockLightValue_do(par1, par2, par3 - 1, false);

                if (l > l1)
                {
                    l1 = l;
                }

                if (i1 > l1)
                {
                    l1 = i1;
                }

                if (j1 > l1)
                {
                    l1 = j1;
                }

                if (k1 > l1)
                {
                    l1 = k1;
                }

                return l1;
            }
            else if (par2 < 0)
            {
                return 0;
            }
            else
            {
                if (par2 >= 256)
                {
                    par2 = 255;
                }

                Chunk chunk = this.getChunkFromChunkCoords(par1 >> 4, par3 >> 4);
                par1 &= 15;
                par3 &= 15;
                if (chunk != null) {
                	return chunk.getBlockLightValue(par1, par2, par3, skylightSubtracted);
                } else {
                	//CoroAI.dbg("null chunk, returning 0 for getBlockLightValue_do");
                	return 0;
                }
                
            }
        }
        else
        {
            return 15;
        }
    }
    
    /**
     * Get light value with flag
     *//*
    public int getLightValueExt(int par1, int par2, int par3, boolean par4)
    {
        if (par1 >= -30000000 && par3 >= -30000000 && par1 < 30000000 && par3 <= 30000000)
        {
            int var5;
            int var6;

            if (par4)
            {
                var5 = this.getBlockId(par1, par2, par3);

                if (var5 == Block.stoneSingleSlab.blockID || var5 == Block.woodSingleSlab.blockID || var5 == Block.tilledField.blockID || var5 == Block.stairsWoodOak.blockID || var5 == Block.stairsCobblestone.blockID)
                {
                    var6 = this.getLightValueExt(par1, par2 + 1, par3, false);
                    int var7 = this.getLightValueExt(par1 + 1, par2, par3, false);
                    int var8 = this.getLightValueExt(par1 - 1, par2, par3, false);
                    int var9 = this.getLightValueExt(par1, par2, par3 + 1, false);
                    int var10 = this.getLightValueExt(par1, par2, par3 - 1, false);

                    if (var7 > var6)
                    {
                        var6 = var7;
                    }

                    if (var8 > var6)
                    {
                        var6 = var8;
                    }

                    if (var9 > var6)
                    {
                        var6 = var9;
                    }

                    if (var10 > var6)
                    {
                        var6 = var10;
                    }

                    return var6;
                }
            }

            if (par2 < 0)
            {
                return 0;
            }
            else if (par2 >= 256)
            {
                var5 = 15 - this.worldObj.skylightSubtracted;

                if (var5 < 0)
                {
                    var5 = 0;
                }

                return var5;
            }
            else
            {
                var5 = (par1 >> 4) - this.chunkX;
                var6 = (par3 >> 4) - this.chunkZ;
                
	                Chunk chunk = this.getChunkFromChunkCoords(var5, var6);
	                if (chunk != null) {
	                	return chunk.getBlockLightValue(par1 & 15, par2, par3 & 15, this.worldObj.skylightSubtracted);
	                } else {
	                	return 0;
	                }
                
            }
        }
        else
        {
            return 15;
        }
    }*/

    /**
     * Returns the block metadata at coords x,y,z
     */
    @Override
    public int getBlockMetadata(int par1, int par2, int par3)
    {
        if (par2 < 0)
        {
            return 0;
        }
        else if (par2 >= 256)
        {
            return 0;
        }
        else
        {
            int var4 = (par1 >> 4) - this.chunkX;
            int var5 = (par3 >> 4) - this.chunkZ;
            if (var4 >= 0 && var4 < this.chunkArray.length && var5 >= 0 && var5 < this.chunkArray[var4].length)
            {
                Chunk var6 = this.chunkArray[var4][var5];
                return var6 == null ? 0 : var6.getBlockMetadata(par1 & 15, par2, par3 & 15);
            }
            return 0;
        }
    }

    /**
     * Returns the block's material.
     */
    /*@Override
    public Material getBlockMaterial(int par1, int par2, int par3)
    {
        int var4 = this.getBlockId(par1, par2, par3);
        return var4 == 0 ? Material.air : Block.blocksList[var4].blockMaterial;
    }*/

    /**
     * Gets the biome for a given set of x/z coordinates
     */
    @Override
    public BiomeGenBase getBiomeGenForCoords(int par1, int par2)
    {
        return this.worldObj.getBiomeGenForCoords(par1, par2);
    }

    /**
     * Returns true if the block at the specified coordinates is an opaque cube. Args: x, y, z
     */
    /*@Override
    public boolean isBlockOpaqueCube(int par1, int par2, int par3)
    {
        Block var4 = Block.blocksList[this.getBlockId(par1, par2, par3)];
        return var4 == null ? false : var4.isOpaqueCube();
    }

    *//**
     * Indicate if a material is a normal solid opaque cube.
     *//*
    @Override
    public boolean isBlockNormalCube(int par1, int par2, int par3)
    {
        Block var4 = Block.blocksList[this.getBlockId(par1, par2, par3)];
        return var4 == null ? false : var4.blockMaterial.blocksMovement() && var4.renderAsNormalBlock();
    }

    *//**
     * Returns true if the block at the given coordinate has a solid (buildable) top surface.
     *//*
    @Override
    public boolean doesBlockHaveSolidTopSurface(int par1, int par2, int par3)
    {
        Block var4 = Block.blocksList[this.getBlockId(par1, par2, par3)];
        return var4 == null ? false : (var4.blockMaterial.isOpaque() && var4.renderAsNormalBlock() ? true : (var4 instanceof BlockStairs ? (this.getBlockMetadata(par1, par2, par3) & 4) == 4 : (var4 instanceof BlockHalfSlab ? (this.getBlockMetadata(par1, par2, par3) & 8) == 8 : false)));
    }*/

    /**
     * Return the Vec3Pool object for this world.
     */
    /*@Override
    public Vec3Pool getWorldVec3Pool()
    {
        return this.worldObj.getWorldVec3Pool();
    }*/

    /**
     * Returns true if the block at the specified coordinates is empty
     */
    @Override
    public boolean isAirBlock(int p_147437_1_, int p_147437_2_, int p_147437_3_)
    {
        return this.getBlock(p_147437_1_, p_147437_2_, p_147437_3_).isAir(this, p_147437_1_, p_147437_2_, p_147437_3_);
    }

    /**
     * Brightness for SkyBlock.Sky is clear white and (through color computing it is assumed) DEPENDENT ON DAYTIME.
     * Brightness for SkyBlock.Block is yellowish and independent.
     */
    public int getSkyBlockTypeBrightness(EnumSkyBlock par1EnumSkyBlock, int par2, int par3, int par4)
    {
        if (par3 < 0)
        {
            par3 = 0;
        }

        if (par3 >= 256)
        {
            par3 = 255;
        }

        if (par3 >= 0 && par3 < 256 && par2 >= -30000000 && par4 >= -30000000 && par2 < 30000000 && par4 <= 30000000)
        {
            if (par1EnumSkyBlock == EnumSkyBlock.SKY && this.worldObj.provider.getHasNoSky())
            {
                return 0;
            }
            else
            {
                int l;
                int i1;

                if (this.getBlock(par2, par3, par4).getUseNeighborBrightness())
                {
                    l = this.getSpecialBlockBrightness(par1EnumSkyBlock, par2, par3 + 1, par4);
                    i1 = this.getSpecialBlockBrightness(par1EnumSkyBlock, par2 + 1, par3, par4);
                    int j1 = this.getSpecialBlockBrightness(par1EnumSkyBlock, par2 - 1, par3, par4);
                    int k1 = this.getSpecialBlockBrightness(par1EnumSkyBlock, par2, par3, par4 + 1);
                    int l1 = this.getSpecialBlockBrightness(par1EnumSkyBlock, par2, par3, par4 - 1);

                    if (i1 > l)
                    {
                        l = i1;
                    }

                    if (j1 > l)
                    {
                        l = j1;
                    }

                    if (k1 > l)
                    {
                        l = k1;
                    }

                    if (l1 > l)
                    {
                        l = l1;
                    }

                    return l;
                }
                else
                {
                    l = (par2 >> 4) - this.chunkX;
                    i1 = (par4 >> 4) - this.chunkZ;
                    return this.chunkArray[l][i1].getSavedLightValue(par1EnumSkyBlock, par2 & 15, par3, par4 & 15);
                }
            }
        }
        else
        {
            return par1EnumSkyBlock.defaultLightValue;
        }
    }

    /**
     * is only used on stairs and tilled fields
     */
    public int getSpecialBlockBrightness(EnumSkyBlock par1EnumSkyBlock, int par2, int par3, int par4)
    {
        if (par3 < 0)
        {
            par3 = 0;
        }

        if (par3 >= 256)
        {
            par3 = 255;
        }

        if (par3 >= 0 && par3 < 256 && par2 >= -30000000 && par4 >= -30000000 && par2 < 30000000 && par4 <= 30000000)
        {
            int var5 = (par2 >> 4) - this.chunkX;
            int var6 = (par4 >> 4) - this.chunkZ;
            return this.chunkArray[var5][var6].getSavedLightValue(par1EnumSkyBlock, par2 & 15, par3, par4 & 15);
        }
        else
        {
            return par1EnumSkyBlock.defaultLightValue;
        }
    }

    /**
     * Returns current world height.
     */
    @Override
    public int getHeight()
    {
        return 256;
    }

    /**
     * Is this block powering in the specified direction Args: x, y, z, direction
     */
    @Override
    public int isBlockProvidingPowerTo(int par1, int par2, int par3, int par4)
    {
        return this.getBlock(par1, par2, par3).isProvidingStrongPower(this, par1, par2, par3, par4);
    }
    
    @Override
    public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default)
    {
        if (x < -30000000 || z < -30000000 || x >= 30000000 || z >= 30000000)
        {
            return _default;
        }

        return getBlock(x, y, z).isSideSolid(this, x, y, z, side);
    }
    
    public Chunk getChunkFromBlockCoords(int par1, int par2)
    {
        return this.getChunkFromChunkCoords(par1 >> 4, par2 >> 4);
    }

    /**
     * Returns back a chunk looked up by chunk coordinates Args: x, y
     */
    public Chunk getChunkFromChunkCoords(int par1, int par2)
    {
    	//fix offsets for the chunk array
    	par1 -= this.chunkX;
    	par2 -= this.chunkZ;
    	if (par1 > 0 && par2 > 0 && par1 < chunkArray.length && par2 < chunkArray[par1].length) {
    		return this.chunkArray[par1][par2];
    	} else {
    		//System.out.println("requested chunk data outside chunk array bounds");
    		return null;
    	}
    }
    
    /**
     * Returns the y coordinate with a block in it at this x, z coordinate
     */
    public int getHeightValue(int par1, int par2)
    {
        if (par1 >= -30000000 && par2 >= -30000000 && par1 < 30000000 && par2 < 30000000)
        {
            /*if (!this.chunkExists(par1 >> 4, par2 >> 4))
            {
                return 0;
            }
            else
            {*/
                Chunk chunk = this.getChunkFromChunkCoords(par1 >> 4, par2 >> 4);
                if (chunk != null) {
                	return chunk.getHeightValue(par1 & 15, par2 & 15);
                } else {
                	return -1;
                }
            //}
        }
        else
        {
            return 0;
        }
    }

    /**
     * Gets the heightMapMinimum field of the given chunk, or 0 if the chunk is not loaded. Coords are in blocks. Args:
     * X, Z
     */
    public int getChunkHeightMapMinimum(int par1, int par2)
    {
        if (par1 >= -30000000 && par2 >= -30000000 && par1 < 30000000 && par2 < 30000000)
        {
            /*if (!this.chunkExists(par1 >> 4, par2 >> 4))
            {
                return 0;
            }
            else
            {*/
                Chunk chunk = this.getChunkFromChunkCoords(par1 >> 4, par2 >> 4);
                return chunk.heightMapMinimum;
            //}
        }
        else
        {
            return 0;
        }
    }

}
