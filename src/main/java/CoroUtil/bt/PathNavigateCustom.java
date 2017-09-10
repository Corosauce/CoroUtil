package CoroUtil.bt;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

//TODO: rebase off of newer mojang version when we actually make use of AIBTAgent again
public class PathNavigateCustom
{
    private EntityLivingBase theEntity;
    private World world;

    /** The PathEntity being followed. */
    private Path currentPath;
    private double speed;

    /**
     * The number of blocks (extra) +/- in each axis that get pulled out as cache for the pathfinder's search space
     */
    private IAttributeInstance pathSearchRange;
    private boolean noSunPathfind;

    /** Time, in number of ticks, following the current path */
    private int totalTicks;

    /**
     * The time when the last position check was done (to detect successful movement)
     */
    private int ticksAtLastPos;

    /**
     * Coordinates of the entity's position last time a check was done (part of monitoring getting 'stuck')
     */
    private Vec3d lastPosCheck = new Vec3d(0.0D, 0.0D, 0.0D);

    /**
     * Specifically, if a wooden door block is even considered to be passable by the pathfinder
     */
    private boolean canPassOpenWoodenDoors = true;

    /** If door blocks are considered passable even when closed */
    private boolean canPassClosedWoodenDoors;

    /** If water blocks are avoided (at least by the pathfinder) */
    private boolean avoidsWater;

    /**
     * If the entity can swim. Swimming AI enables this and the pathfinder will also cause the entity to swim straight
     * upwards when underwater
     */
    private boolean canSwimOnSurface;
    public boolean canSwimInWater; //used for navigating under the water without strait up float forcing (float code is elsewhere usually)
    public boolean canFly; //used for navigating flying paths


    //1.8 added stuff

    private PathFinder pathFinder;
    protected WalkNodeProcessor nodeProcessor;


    public PathNavigateCustom(EntityLivingBase par1EntityLiving, World par2World)
    {
        this.theEntity = par1EntityLiving;
        this.world = par2World;
        this.pathSearchRange = par1EntityLiving.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
        this.pathFinder = getPathFinder();
    }

    protected PathFinder getPathFinder()
    {
        this.nodeProcessor = new WalkNodeProcessor();
        //this.nodeProcessor.setEnterDoors(true);
        return new PathFinder(this.nodeProcessor);
    }

    public void setAvoidsWater(boolean par1)
    {
        this.avoidsWater = par1;
    }

    public boolean getAvoidsWater()
    {
        return this.avoidsWater;
    }

    public void setBreakDoors(boolean par1)
    {
        this.canPassClosedWoodenDoors = par1;
    }

    /**
     * Sets if the entity can enter open doors
     */
    public void setEnterDoors(boolean par1)
    {
        this.canPassOpenWoodenDoors = par1;
    }

    /**
     * Returns true if the entity can break doors, false otherwise
     */
    public boolean getCanBreakDoors()
    {
        return this.canPassClosedWoodenDoors;
    }

    /**
     * Sets if the path should avoid sunlight
     */
    public void setAvoidSun(boolean par1)
    {
        this.noSunPathfind = par1;
    }

    /**
     * Sets the speed
     */
    public void setSpeed(double par1)
    {
        this.speed = par1;
    }

    /**
     * Sets if the entity can swim
     */
    public void setCanSwim(boolean par1)
    {
        this.canSwimOnSurface = par1;
    }

    /**
     * Gets the maximum distance that the path finding will search in.
     */
    public float getPathSearchRange()
    {
        return (float)this.pathSearchRange.getAttributeValue();
    }

    /**
     * Returns the path to the given coordinates
     */
    public Path getPathToXYZ(double x, double y, double z)
    {
    	return this.getPathToPos(new BlockPos(MathHelper.floor(x), (int)y, MathHelper.floor(z)));
        //return !this.canNavigate() ? null : this.world.getEntityPathToXYZ(this.theEntity, MathHelper.floor(x), (int)y, MathHelper.floor(z), this.getPathSearchRange(), this.canPassOpenWoodenDoors, this.canPassClosedWoodenDoors, this.avoidsWater, this.canSwimOnSurface);
    }

    public Path getPathToPos(BlockPos pos)
    {
        if (!this.canNavigate())
        {
            return null;
        }
        else
        {
            float f = this.getPathSearchRange();
            this.world.profiler.startSection("pathfind");
            BlockPos blockpos = new BlockPos(this.theEntity);
            int i = (int)(f + 8.0F);
            ChunkCache chunkcache = new ChunkCache(this.world, blockpos.add(-i, -i, -i), blockpos.add(i, i, i), 0);
            Path pathentity = null;//this.pathFinder.createEntityPathTo(chunkcache, this.theEntity, pos, f);
            this.world.profiler.endSection();
            return pathentity;
        }
    }

    /**
     * Try to find and set a path to XYZ. Returns true if successful.
     */
    public boolean tryMoveToXYZ(double par1, double par3, double par5, double par7)
    {
    	Path pathentity = this.getPathToXYZ((double)MathHelper.floor(par1), (double)((int)par3), (double)MathHelper.floor(par5));
        return this.setPath(pathentity, par7);
    }

    /**
     * Returns the path to the given EntityLiving
     */
    public Path getPathToEntityLiving(Entity par1Entity)
    {
        //return !this.canNavigate() ? null : this.world.getPathEntityToEntity(this.theEntity, par1Entity, this.getPathSearchRange(), this.canPassOpenWoodenDoors, this.canPassClosedWoodenDoors, this.avoidsWater, this.canSwimOnSurface);

    	if (!this.canNavigate())
        {
            return null;
        }
        else
        {
            float f = this.getPathSearchRange();
            this.world.profiler.startSection("pathfind");
            BlockPos blockpos = (new BlockPos(this.theEntity)).up();
            int i = (int)(f + 16.0F);
            ChunkCache chunkcache = new ChunkCache(this.world, blockpos.add(-i, -i, -i), blockpos.add(i, i, i), 0);
            Path pathentity = null;//this.pathFinder.createEntityPathTo(chunkcache, this.theEntity, par1Entity, f);
            this.world.profiler.endSection();
            return pathentity;
        }
    }

    /**
     * Try to find and set a path to EntityLiving. Returns true if successful.
     */
    public boolean tryMoveToEntityLiving(Entity par1Entity, double par2)
    {
    	Path pathentity = this.getPathToEntityLiving(par1Entity);
        return pathentity != null ? this.setPath(pathentity, par2) : false;
    }

    /**
     * sets the active path data if path is 100% unique compared to old path, checks to adjust path for sun avoiding
     * ents and stores end coords
     */
    public boolean setPath(Path par1PathEntity, double par2)
    {
        if (par1PathEntity == null)
        {
            this.currentPath = null;
            return false;
        }
        else
        {
            if (!par1PathEntity.isSamePath(this.currentPath))
            {
                this.currentPath = par1PathEntity;
            }

            if (this.noSunPathfind)
            {
                this.removeSunnyPath();
            }

            if (this.currentPath.getCurrentPathLength() == 0)
            {
                return false;
            }
            else
            {
                this.speed = par2;
                Vec3d vec3 = this.getEntityPosition();
                this.ticksAtLastPos = this.totalTicks;
                this.lastPosCheck = vec3;
                return true;
            }
        }
    }

    /**
     * gets the actively used PathEntity
     */
    public Path getPath()
    {
        return this.currentPath;
    }

    public void onUpdateNavigation()
    {
        ++this.totalTicks;

        if (!this.noPath())
        {
            if (this.canNavigate())
            {
                this.pathFollow();
            } else {
            	//System.out.println(this.getEntityPosition());
            }

            if (!this.noPath())
            {
                Vec3d vec3 = this.currentPath.getPosition(this.theEntity);

                if (vec3 != null)
                {
                    ((IBTAgent)this.theEntity).getAIBTAgent().moveHelper.setMoveTo(vec3.x, vec3.y, vec3.z, this.speed);
                }
            }
        }
    }

    private void pathFollow()
    {
        Vec3d vec3 = this.getEntityPosition();
        int i = this.currentPath.getCurrentPathLength();

        double adjY = 0;

        //so, the pathnodes were in the water, but they should be out of the water (vanilla pathfinder maybe puts them above water, but mine puts them in water)
        //this code below patches the issue, saw successfull results while live debugging
        //this introduces a new problem, tricks the AI into thinking it can get out of water where theres 1 block higher than water blocking him
        //make AI able to hop/climb out of water with a 1 high wall around it to fix

        //System.out.println(this.getEntityPosition() + " - " + this.currentPath.getCurrentPathIndex() + " / " + i);
        //if (MathHelper.floor(vec3.z) > -17 && MathHelper.floor(vec3.z) < -13) {
        	//System.out.println(this.getEntityPosition() + " - " + this.currentPath.getCurrentPathIndex() + " / " + i);

        	PathPoint pp = this.currentPath.getPathPointFromIndex(this.currentPath.getCurrentPathIndex());
        	Block block = this.theEntity.world.getBlockState(new BlockPos(pp.x, pp.y, pp.z)).getBlock();
        	//System.out.println("block type for next node: " + block);

        	if (block.getMaterial(block.getDefaultState()) == Material.WATER || block.getMaterial(block.getDefaultState()) == Material.LAVA) {
        		//System.out.println("adjusting water based node pos");
        		adjY = 1;
        	}
        //}

        //make this path trimming code only run when our fix isnt running, so it doesnt break water nav correction
        if (adjY == 0) {
	        for (int j = this.currentPath.getCurrentPathIndex(); j < this.currentPath.getCurrentPathLength(); ++j)
	        {
	            if (this.currentPath.getPathPointFromIndex(j).y != (int)vec3.y)
	            {
	                i = j;
	                break;
	            }
	        }
        }

        float f = this.theEntity.width * this.theEntity.width;
        int k;

        for (k = this.currentPath.getCurrentPathIndex(); k < i; ++k)
        {
            if (vec3.squareDistanceTo(this.currentPath.getVectorFromIndex(this.theEntity, k)) < (double)f)
            {
                this.currentPath.setCurrentPathIndex(k + 1);
            } else {

	            /*double dist = new Vec3(vec3.x, vec3.y, vec3.z).squareDistanceTo(this.currentPath.getVectorFromIndex(this.theEntity, k));
	            if (dist < 4D) {
	            	System.out.println("dist: " + dist + " - " + vec3.x + " - " + vec3.z);
	            }

	            //water nav help
	            if (dist < (double)f)
	            {
	            	System.out.println("trigger water nav help");
	                this.currentPath.setCurrentPathIndex(k + 1);
	            }*/
            }

        }

        k = MathHelper.ceil(this.theEntity.width);
        int l = (int)this.theEntity.height + 1;
        int i1 = k;

        for (int j1 = i - 1; j1 >= this.currentPath.getCurrentPathIndex(); --j1)
        {
            if (this.isDirectPathBetweenPoints(vec3, this.currentPath.getVectorFromIndex(this.theEntity, j1), k, l, i1))
            {
                this.currentPath.setCurrentPathIndex(j1);
                break;
            }
        }

        if (this.totalTicks - this.ticksAtLastPos > 100)
        {
            if (vec3.squareDistanceTo(this.lastPosCheck) < 2.25D)
            {
                this.clearPathEntity();
            }

            this.ticksAtLastPos = this.totalTicks;
            this.lastPosCheck = vec3;
        }
    }

    /**
     * If null path or reached the end
     */
    public boolean noPath()
    {
        return this.currentPath == null || this.currentPath.isFinished();
    }

    /**
     * sets active PathEntity to null
     */
    public void clearPathEntity()
    {
        this.currentPath = null;
    }

    private Vec3d getEntityPosition()
    {
        return new Vec3d(this.theEntity.posX, (double)this.getPathableYPos(), this.theEntity.posZ);
    }

    /**
     * Gets the safe pathing Y position for the entity depending on if it can path swim or not
     */
    private int getPathableYPos()
    {
        if (this.theEntity.isInWater() && this.canSwimOnSurface)
        {
            int i = (int)this.theEntity.getEntityBoundingBox().minY;
            Block block = this.world.getBlockState(new BlockPos(MathHelper.floor(this.theEntity.posX), i, MathHelper.floor(this.theEntity.posZ))).getBlock();
            int k = 0;

            do
            {
            	if (block != Blocks.FLOWING_WATER && block != Blocks.WATER)
                {
                    return i;
                }

                ++i;
                block = this.world.getBlockState(new BlockPos(MathHelper.floor(this.theEntity.posX), i, MathHelper.floor(this.theEntity.posZ))).getBlock();
                ++k;
            }
            while (k <= 16);

            return (int)this.theEntity.getEntityBoundingBox().minY;
        }
        else
        {
            return (int)(this.theEntity.getEntityBoundingBox().minY + 0.5D);
        }
    }

    /**
     * If on ground or swimming and can swim
     */
    private boolean canNavigate()
    {
    	//System.out.println("is in fluid: " + this.isInFluid() + " - " + this.getEntityPosition());
        return this.canFly || this.canSwimInWater || this.theEntity.onGround || (this.canSwimOnSurface && this.isInFluid());
    }

    /**
     * Returns true if the entity is in water or lava, false otherwise
     */
    private boolean isInFluid()
    {
        return /*true*//*this.theEntity.isInsideOfMaterial(Material.water)*/this.theEntity.isInWater()/* || (this.theEntity.isnotcolliding())*/;
    }

    /**
     * Trims path data from the end to the first sun covered block
     */
    private void removeSunnyPath()
    {
        if (!this.world.canBlockSeeSky(new BlockPos(MathHelper.floor(this.theEntity.posX), (int)(this.theEntity.getEntityBoundingBox().minY + 0.5D), MathHelper.floor(this.theEntity.posZ))))
        {
            for (int i = 0; i < this.currentPath.getCurrentPathLength(); ++i)
            {
                PathPoint pathpoint = this.currentPath.getPathPointFromIndex(i);

                if (this.world.canBlockSeeSky(new BlockPos(pathpoint.x, pathpoint.y, pathpoint.z)))
                {
                    this.currentPath.setCurrentPathLength(i - 1);
                    return;
                }
            }
        }
    }

    /**
     * Returns true when an entity of specified size could safely walk in a straight line between the two points. Args:
     * pos1, pos2, entityXSize, entityYSize, entityZSize
     */
    private boolean isDirectPathBetweenPoints(Vec3d par1Vec3, Vec3d par2Vec3, int par3, int par4, int par5)
    {
        int l = MathHelper.floor(par1Vec3.x);
        int i1 = MathHelper.floor(par1Vec3.z);
        double d0 = par2Vec3.x - par1Vec3.x;
        double d1 = par2Vec3.z - par1Vec3.z;
        double d2 = d0 * d0 + d1 * d1;

        if (d2 < 1.0E-8D)
        {
            return false;
        }
        else
        {
            double d3 = 1.0D / Math.sqrt(d2);
            d0 *= d3;
            d1 *= d3;
            par3 += 2;
            par5 += 2;

            if (!this.isSafeToStandAt(l, (int)par1Vec3.y, i1, par3, par4, par5, par1Vec3, d0, d1))
            {
                return false;
            }
            else
            {
                par3 -= 2;
                par5 -= 2;
                double d4 = 1.0D / Math.abs(d0);
                double d5 = 1.0D / Math.abs(d1);
                double d6 = (double)(l * 1) - par1Vec3.x;
                double d7 = (double)(i1 * 1) - par1Vec3.z;

                if (d0 >= 0.0D)
                {
                    ++d6;
                }

                if (d1 >= 0.0D)
                {
                    ++d7;
                }

                d6 /= d0;
                d7 /= d1;
                int j1 = d0 < 0.0D ? -1 : 1;
                int k1 = d1 < 0.0D ? -1 : 1;
                int l1 = MathHelper.floor(par2Vec3.x);
                int i2 = MathHelper.floor(par2Vec3.z);
                int j2 = l1 - l;
                int k2 = i2 - i1;

                do
                {
                    if (j2 * j1 <= 0 && k2 * k1 <= 0)
                    {
                        return true;
                    }

                    if (d6 < d7)
                    {
                        d6 += d4;
                        l += j1;
                        j2 = l1 - l;
                    }
                    else
                    {
                        d7 += d5;
                        i1 += k1;
                        k2 = i2 - i1;
                    }
                }
                while (this.isSafeToStandAt(l, (int)par1Vec3.y, i1, par3, par4, par5, par1Vec3, d0, d1));

                return false;
            }
        }
    }

    /**
     * Returns true when an entity could stand at a position, including solid blocks under the entire entity. Args:
     * xOffset, yOffset, zOffset, entityXSize, entityYSize, entityZSize, originPosition, vecX, vecZ
     */
    private boolean isSafeToStandAt(int par1, int par2, int par3, int par4, int par5, int par6, Vec3d par7Vec3, double par8, double par10)
    {
        int k1 = par1 - par4 / 2;
        int l1 = par3 - par6 / 2;

        if (!this.isPositionClear(k1, par2, l1, par4, par5, par6, par7Vec3, par8, par10))
        {
            return false;
        }
        else
        {
            for (int i2 = k1; i2 < k1 + par4; ++i2)
            {
                for (int j2 = l1; j2 < l1 + par6; ++j2)
                {
                    double d2 = (double)i2 + 0.5D - par7Vec3.x;
                    double d3 = (double)j2 + 0.5D - par7Vec3.z;

                    if (d2 * par8 + d3 * par10 >= 0.0D)
                    {
                    	Block block = this.world.getBlockState(new BlockPos(i2, par2 - 1, j2)).getBlock();
                        Material material = block.getMaterial(block.getDefaultState());

                        if (material == Material.AIR)
                        {
                            return false;
                        }

                        if (material == Material.WATER && !this.theEntity.isInWater())
                        {
                            return false;
                        }

                        if (material == Material.LAVA)
                        {
                            return false;
                        }
                    }
                }
            }

            return true;
        }
    }

    /**
     * Returns true if an entity does not collide with any solid blocks at the position.
     */
    private boolean isPositionClear(int p_179692_1_, int p_179692_2_, int p_179692_3_, int p_179692_4_, int p_179692_5_, int p_179692_6_, Vec3d p_179692_7_, double p_179692_8_, double p_179692_10_)
    {
        for (BlockPos blockpos : BlockPos.getAllInBox(new BlockPos(p_179692_1_, p_179692_2_, p_179692_3_), new BlockPos(p_179692_1_ + p_179692_4_ - 1, p_179692_2_ + p_179692_5_ - 1, p_179692_3_ + p_179692_6_ - 1)))
        {
            double d0 = (double)blockpos.getX() + 0.5D - p_179692_7_.x;
            double d1 = (double)blockpos.getZ() + 0.5D - p_179692_7_.z;

            if (d0 * p_179692_8_ + d1 * p_179692_10_ >= 0.0D)
            {
                Block block = this.world.getBlockState(blockpos).getBlock();

                if (!block.isPassable(this.world, blockpos))
                {
                    return false;
                }
            }
        }

        return true;
    }
}
