package CoroUtil.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import CoroUtil.OldUtil;
import CoroUtil.config.ConfigCoroUtil;
import CoroUtil.pathfinding.PFQueue;

public class DimensionChunkCacheNew implements IBlockAccess {

	public static List<Integer> listBlacklistIDs = new ArrayList<Integer>();
    public static List<String> listBlacklistNamess = new ArrayList<String>();
    
    //Static lookup and cache updating
    public static HashMap<Integer, DimensionChunkCacheNew> dimCacheLookup = new HashMap<Integer, DimensionChunkCacheNew>();
    
    public int chunkX;
    public int chunkZ;
    public int chunkXMax;
    public int chunkZMax;
    private Chunk[][] chunkArray;

    /** set by !chunk.getAreLevelsEmpty */
    private boolean hasExtendedLevels;

    /** Reference to the World object. */
    private World worldObj;
    
    public static void updateAllWorldCache() {
    	//System.out.println("Updating PFCache");
    	WorldServer[] worlds = DimensionManager.getWorlds();
    	
    	for (int i = 0; i < worlds.length; i++) {
    		WorldServer world = worlds[i];
    		
    		boolean skip = false;
    		
    		//world.chunkExists(par1, par2)
    		//if (i == 0) {
    		if (!ConfigCoroUtil.chunkCacheOverworldOnly || i == 0) {
    			
    		} else {
    			skip = true;
    		}
    		
    		if (listBlacklistIDs.contains(i)) {
    			skip = true;
    		}
    		
    		for (int j = 0; j < listBlacklistNamess.size(); j++) {
    			if (world != null && world.provider.getDimensionType().getName().contains(listBlacklistNamess.get(j))) {
        			skip = true;
        			break;
        		}
    		}
    		
    		
    		if (!skip) {
    			dimCacheLookup.put(world.provider.getDimension(), new DimensionChunkCacheNew(world, true));
    		}
    	}
    }
    
    public DimensionChunkCacheNew(World world, boolean useLoadedChunks/*, int par2, int par3, int par4, int par5, int par6, int par7*/)
    {
		int chunkCount = 0;
		
    	try {
	    	int minX = 0;
	    	int minZ = 0;
	    	int maxX = 0;
	    	int maxZ = 0;
	    	
	    	List chunks = Lists.newArrayList(((ChunkProviderServer)world.getChunkProvider()).getLoadedChunks());
    		
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
    			if (ConfigCoroUtil.usePlayerRadiusChunkLoadingForFallback) {
    				System.out.println("unable to get loaded chunks, reverting to potentially cpu/memory heavy player radius method, to deactivate set usePlayerRadiusChunkLoadingForFallback in CoroUtil.cfg to false");
    			} else {
    				System.out.println("loadedChunks is null, DimensionChunkCache unable to cache chunk data for dimension: " + world.provider.getDimension() + " - " + world.provider.getDimensionType().getName());
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
		    			
		    			if ((int)chunk.x < minX) minX = chunk.x;
			            if ((int)chunk.z < minZ) minZ = chunk.z;
			            if ((int)chunk.x > maxX) maxX = chunk.x;
			            if ((int)chunk.z > maxZ) maxZ = chunk.z;
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
		    			this.chunkArray[chunk.x - this.chunkX][chunk.z - this.chunkZ] = chunk;
		    			chunkCount++;
		    		}
	    		}
	    		
	    	} else if (ConfigCoroUtil.usePlayerRadiusChunkLoadingForFallback) {
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
		            
		            int pChunkX = MathHelper.floor(var5.posX / 16.0D);
		            int pChunkZ = MathHelper.floor(var5.posZ / 16.0D);
		            
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
	
	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		int i = (pos.getX() >> 4) - this.chunkX;
        int j = (pos.getZ() >> 4) - this.chunkZ;
        if (i < 0 || i >= chunkArray.length || j < 0 || j >= chunkArray[i].length) return null;
        if (chunkArray[i][j] == null) return null;
        return this.chunkArray[i][j].getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
	}

	
	@Override
	public int getCombinedLight(BlockPos pos, int lightValue) {
		return 0;
	}

	@Override
	public IBlockState getBlockState(BlockPos pos) {
		if (pos.getY() >= 0 && pos.getY() < 256)
        {
            int i = (pos.getX() >> 4) - this.chunkX;
            int j = (pos.getZ() >> 4) - this.chunkZ;
            //TODO: 1.8 this line is saying our cache is too big? array sizes are beyond acceptable sizes or something
            System.out.println("PFQUEUE FIX ME IM BROKEN");
            if (i < 0 || i >= chunkArray.length || j < 0 || i >= chunkArray[i].length) return Blocks.AIR.getDefaultState();

            if (i >= 0 && i < this.chunkArray.length && j >= 0 && j < this.chunkArray[i].length)
            {
                Chunk chunk = this.chunkArray[i][j];

                if (chunk != null)
                {
                    return chunk.getBlockState(pos);
                }
            }
        }

        return Blocks.AIR.getDefaultState();
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {
		IBlockState state = getBlockState(pos);
		return state.getBlock().isAir(state, this, pos);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Biome getBiome(BlockPos pos) {
		return this.worldObj.getBiome(pos);
	}

	/*@SideOnly(Side.CLIENT)
	@Override
	public boolean extendedLevelsInChunkCache() {
		return this.hasExtendedLevels;
	}*/

	@Override
	public int getStrongPower(BlockPos pos, EnumFacing direction) {
		IBlockState iblockstate = this.getBlockState(pos);
        return iblockstate.getBlock().getStrongPower(iblockstate, this, pos, direction);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public WorldType getWorldType() {
		return this.worldObj.getWorldType();
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
		int x = (pos.getX() >> 4) - this.chunkX;
        int z = (pos.getZ() >> 4) - this.chunkZ;
        if (pos.getY() >= 0 && pos.getY() < 256) return _default;
        if (x < 0 || x >= chunkArray.length || z < 0 || x >= chunkArray[x].length) return _default;

        IBlockState state = getBlockState(pos);
        return state.getBlock().isSideSolid(state, this, pos, side);
	}

}
