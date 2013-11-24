package CoroAI;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHalfSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3Pool;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeDirection;
import CoroAI.config.ConfigCoroAI;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class DimensionChunkCache implements IBlockAccess
{
    private int chunkX;
    private int chunkZ;
    private Chunk[][] chunkArray;

    /** set by !chunk.getAreLevelsEmpty */
    private boolean hasExtendedLevels;

    /** Reference to the World object. */
    private World worldObj;
    
    //Static lookup and cache updating
    public static HashMap<Integer, DimensionChunkCache> dimCacheLookup = new HashMap<Integer, DimensionChunkCache>();
    public static void updateAllWorldCache() {
    	//System.out.println("Updating PFCache");
    	WorldServer[] worlds = DimensionManager.getWorlds();
    	
    	for (int i = 0; i < worlds.length; i++) {
    		WorldServer world = worlds[i];
    		
    		
    		
    		//world.chunkExists(par1, par2)
    		//if (i == 0) {
    		if (!ConfigCoroAI.chunkCacheOverworldOnly || i == 0) {
    			dimCacheLookup.put(world.provider.dimensionId, new DimensionChunkCache(world, true));
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
	    	
	    	if (useLoadedChunks) {
	    		
	    		ArrayList chunks = null;
	    		
	    		try {
	    			chunks = (ArrayList)c_CoroAIUtil.getPrivateValue(ChunkProviderServer.class, world.getChunkProvider(), "loadedChunks");
	    		} catch (Exception ex) {
	    			try {
	    				chunks = (ArrayList)c_CoroAIUtil.getPrivateValueSRGMCP(ChunkProviderServer.class, world.getChunkProvider(), c_CoroAIUtil.refl_loadedChunks_obf, c_CoroAIUtil.refl_loadedChunks_mcp);
	    			} catch (Exception ex2) {
	    				System.out.println("SERIOUS REFLECTION FAIL IN DimensionChunkCache");
	    			}
	    		}
	    		
	    		
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
	    		
	    	} else {
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
	
	@SideOnly(Side.CLIENT)

    /**
     * set by !chunk.getAreLevelsEmpty
     */
    public boolean extendedLevelsInChunkCache()
    {
        return this.hasExtendedLevels;
    }

    /**
     * Returns the block ID at coords x,y,z
     */
    public int getBlockId(int par1, int par2, int par3)
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
                return var6 == null ? 0 : var6.getBlockID(par1 & 15, par2, par3 & 15);
            }
            else
            {
                return 0;
            }
        }
    }

    /**
     * Returns the TileEntity associated with a given block in X,Y,Z coordinates, or null if no TileEntity exists
     */
    public TileEntity getBlockTileEntity(int par1, int par2, int par3)
    {
        int var4 = (par1 >> 4) - this.chunkX;
        int var5 = (par3 >> 4) - this.chunkZ;
        if (var4 >= 0 && var4 < this.chunkArray.length && var5 >= 0 && var5 < this.chunkArray[var4].length)
        {
            Chunk var6 = this.chunkArray[var4][var5];
            return var6 == null ? null : var6.getChunkBlockTileEntity(par1 & 15, par2, par3 & 15);
        }
        else
        {
            return null;
        }
    }

    @SideOnly(Side.CLIENT)
    public float getBrightness(int par1, int par2, int par3, int par4)
    {
        int var5 = this.getLightValue(par1, par2, par3);

        if (var5 < par4)
        {
            var5 = par4;
        }

        return this.worldObj.provider.lightBrightnessTable[var5];
    }

    @SideOnly(Side.CLIENT)

    /**
     * Any Light rendered on a 1.8 Block goes through here
     */
    public int getLightBrightnessForSkyBlocks(int par1, int par2, int par3, int par4)
    {
        int var5 = this.getSkyBlockTypeBrightness(EnumSkyBlock.Sky, par1, par2, par3);
        int var6 = this.getSkyBlockTypeBrightness(EnumSkyBlock.Block, par1, par2, par3);

        if (var6 < par4)
        {
            var6 = par4;
        }

        return var5 << 20 | var6 << 4;
    }

    @SideOnly(Side.CLIENT)

    /**
     * Returns how bright the block is shown as which is the block's light value looked up in a lookup table (light
     * values aren't linear for brightness). Args: x, y, z
     */
    public float getLightBrightness(int par1, int par2, int par3)
    {
        return this.worldObj.provider.lightBrightnessTable[this.getLightValue(par1, par2, par3)];
    }

    @SideOnly(Side.CLIENT)

    /**
     * Gets the light value of the specified block coords. Args: x, y, z
     */
    public int getLightValue(int par1, int par2, int par3)
    {
        return this.getLightValueExt(par1, par2, par3, true);
    }

    @SideOnly(Side.CLIENT)

    /**
     * Get light value with flag
     */
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
                return this.chunkArray[var5][var6].getBlockLightValue(par1 & 15, par2, par3 & 15, this.worldObj.skylightSubtracted);
            }
        }
        else
        {
            return 15;
        }
    }

    /**
     * Returns the block metadata at coords x,y,z
     */
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
    public Material getBlockMaterial(int par1, int par2, int par3)
    {
        int var4 = this.getBlockId(par1, par2, par3);
        return var4 == 0 ? Material.air : Block.blocksList[var4].blockMaterial;
    }

    @SideOnly(Side.CLIENT)

    /**
     * Gets the biome for a given set of x/z coordinates
     */
    public BiomeGenBase getBiomeGenForCoords(int par1, int par2)
    {
        return this.worldObj.getBiomeGenForCoords(par1, par2);
    }

    @SideOnly(Side.CLIENT)

    /**
     * Returns true if the block at the specified coordinates is an opaque cube. Args: x, y, z
     */
    public boolean isBlockOpaqueCube(int par1, int par2, int par3)
    {
        Block var4 = Block.blocksList[this.getBlockId(par1, par2, par3)];
        return var4 == null ? false : var4.isOpaqueCube();
    }

    /**
     * Indicate if a material is a normal solid opaque cube.
     */
    public boolean isBlockNormalCube(int par1, int par2, int par3)
    {
        Block var4 = Block.blocksList[this.getBlockId(par1, par2, par3)];
        return var4 == null ? false : var4.blockMaterial.blocksMovement() && var4.renderAsNormalBlock();
    }

    @SideOnly(Side.CLIENT)

    /**
     * Returns true if the block at the given coordinate has a solid (buildable) top surface.
     */
    public boolean doesBlockHaveSolidTopSurface(int par1, int par2, int par3)
    {
        Block var4 = Block.blocksList[this.getBlockId(par1, par2, par3)];
        return var4 == null ? false : (var4.blockMaterial.isOpaque() && var4.renderAsNormalBlock() ? true : (var4 instanceof BlockStairs ? (this.getBlockMetadata(par1, par2, par3) & 4) == 4 : (var4 instanceof BlockHalfSlab ? (this.getBlockMetadata(par1, par2, par3) & 8) == 8 : false)));
    }

    /**
     * Return the Vec3Pool object for this world.
     */
    public Vec3Pool getWorldVec3Pool()
    {
        return this.worldObj.getWorldVec3Pool();
    }

    @SideOnly(Side.CLIENT)

    /**
     * Returns true if the block at the specified coordinates is empty
     */
    public boolean isAirBlock(int par1, int par2, int par3)
    {
        Block var4 = Block.blocksList[this.getBlockId(par1, par2, par3)];
        return var4 == null;
    }

    @SideOnly(Side.CLIENT)

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
            if (par1EnumSkyBlock == EnumSkyBlock.Sky && this.worldObj.provider.hasNoSky)
            {
                return 0;
            }
            else
            {
                int var5;
                int var6;

                if (Block.useNeighborBrightness[this.getBlockId(par2, par3, par4)])
                {
                    var5 = this.getSpecialBlockBrightness(par1EnumSkyBlock, par2, par3 + 1, par4);
                    var6 = this.getSpecialBlockBrightness(par1EnumSkyBlock, par2 + 1, par3, par4);
                    int var7 = this.getSpecialBlockBrightness(par1EnumSkyBlock, par2 - 1, par3, par4);
                    int var8 = this.getSpecialBlockBrightness(par1EnumSkyBlock, par2, par3, par4 + 1);
                    int var9 = this.getSpecialBlockBrightness(par1EnumSkyBlock, par2, par3, par4 - 1);

                    if (var6 > var5)
                    {
                        var5 = var6;
                    }

                    if (var7 > var5)
                    {
                        var5 = var7;
                    }

                    if (var8 > var5)
                    {
                        var5 = var8;
                    }

                    if (var9 > var5)
                    {
                        var5 = var9;
                    }

                    return var5;
                }
                else
                {
                    var5 = (par2 >> 4) - this.chunkX;
                    var6 = (par4 >> 4) - this.chunkZ;
                    return this.chunkArray[var5][var6].getSavedLightValue(par1EnumSkyBlock, par2 & 15, par3, par4 & 15);
                }
            }
        }
        else
        {
            return par1EnumSkyBlock.defaultLightValue;
        }
    }

    @SideOnly(Side.CLIENT)

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

    @SideOnly(Side.CLIENT)

    /**
     * Returns current world height.
     */
    public int getHeight()
    {
        return 256;
    }

    /**
     * Is this block powering in the specified direction Args: x, y, z, direction
     */
    public int isBlockProvidingPowerTo(int par1, int par2, int par3, int par4)
    {
        int i1 = this.getBlockId(par1, par2, par3);
        return i1 == 0 ? 0 : Block.blocksList[i1].isProvidingStrongPower(this, par1, par2, par3, par4);
    }
    
    public boolean isBlockSolidOnSide(int x, int y, int z, ForgeDirection fd, boolean bool) {
    	return true;
    }

}
