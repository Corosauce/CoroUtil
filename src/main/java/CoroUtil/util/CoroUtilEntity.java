package CoroUtil.util;

import java.util.Iterator;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class CoroUtilEntity {

	public static boolean canCoordBeSeen(EntityLivingBase ent, int x, int y, int z)
    {
        return ent.worldObj.rayTraceBlocks(new Vec3d(ent.posX, ent.posY + (double)ent.getEyeHeight(), ent.posZ), new Vec3d(x, y, z)) == null;
    }
    
    public static boolean canCoordBeSeenFromFeet(EntityLivingBase ent, int x, int y, int z)
    {
        return ent.worldObj.rayTraceBlocks(new Vec3d(ent.posX, ent.getEntityBoundingBox().minY+0.15, ent.posZ), new Vec3d(x, y, z)) == null;
    }
    
    public static double getDistance(Entity ent, BlockCoord coords)
    {
        double d3 = ent.posX - coords.posX;
        double d4 = ent.posY - coords.posY;
        double d5 = ent.posZ - coords.posZ;
        return (double)MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);
    }
	
	public static double getDistance(Entity ent, TileEntity tEnt)
    {
        double d3 = ent.posX - tEnt.getPos().getX();
        double d4 = ent.posY - tEnt.getPos().getY();
        double d5 = ent.posZ - tEnt.getPos().getZ();
        return (double)MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);
    }
	
	public static Vec3 getTargetVector(EntityLivingBase parEnt, EntityLivingBase target) {
    	double vecX = target.posX - parEnt.posX;
    	double vecY = target.posY - parEnt.posY;
    	double vecZ = target.posZ - parEnt.posZ;
    	double dist = Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
    	Vec3 vec3 = new Vec3(vecX / dist, vecY / dist, vecZ / dist);
    	return vec3;
    }
	
	public static void moveTowards(Entity ent, Entity targ, float speed) {
		double vecX = targ.posX - ent.posX;
		double vecY = targ.posY - ent.posY;
		double vecZ = targ.posZ - ent.posZ;

		double dist2 = (double)Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
		ent.motionX += vecX / dist2 * speed;
		ent.motionY += vecY / dist2 * speed;
		ent.motionZ += vecZ / dist2 * speed;
	}
	
	public static String getName(Entity ent) {
		return ent != null ? ent.getName() : "nullObject";
	}
	
	public static EntityPlayer getPlayerByUUID(UUID uuid) {
		Iterator iterator = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerList().iterator();
        EntityPlayerMP entityplayermp;
        
        while (iterator.hasNext()) {
        	entityplayermp = (EntityPlayerMP) iterator.next();
        	
        	if (entityplayermp.getGameProfile().getId().equals(uuid)) {
        		return entityplayermp;
        	}
        }
        
        return null;
	}
	
	/**
     * Returns the closest vulnerable player to this entity within the given radius, or null if none is found
     */
    public static EntityPlayer getClosestVulnerablePlayerToEntity(World world, Entity p_72856_1_, double p_72856_2_)
    {
        return getClosestVulnerablePlayer(world, p_72856_1_.posX, p_72856_1_.posY, p_72856_1_.posZ, p_72856_2_);
    }

    /**
     * Returns the closest vulnerable player within the given radius, or null if none is found.
     */
    public static EntityPlayer getClosestVulnerablePlayer(World world, double p_72846_1_, double p_72846_3_, double p_72846_5_, double p_72846_7_)
    {
        double d4 = -1.0D;
        EntityPlayer entityplayer = null;

        for (int i = 0; i < world.playerEntities.size(); ++i)
        {
            EntityPlayer entityplayer1 = (EntityPlayer)world.playerEntities.get(i);

            if (!entityplayer1.capabilities.disableDamage && entityplayer1.isEntityAlive())
            {
                double d5 = entityplayer1.getDistanceSq(p_72846_1_, p_72846_3_, p_72846_5_);
                double d6 = p_72846_7_;

                if (entityplayer1.isSneaking())
                {
                    d6 = p_72846_7_ * 0.800000011920929D;
                }

                if (entityplayer1.isInvisible())
                {
                    float f = entityplayer1.getArmorVisibility();

                    if (f < 0.1F)
                    {
                        f = 0.1F;
                    }

                    d6 *= (double)(0.7F * f);
                }

                if ((p_72846_7_ < 0.0D || d5 < d6 * d6) && (d4 == -1.0D || d5 < d4))
                {
                    d4 = d5;
                    entityplayer = entityplayer1;
                }
            }
        }

        return entityplayer;
    }

    public static boolean canProcessForList(String playerName, String list, boolean whitelistMode) {
        if (whitelistMode) {
            if (!list.contains(playerName)) {
                return false;
            }
        } else {
            if (list.contains(playerName)) {
                return false;
            }
        }
        return true;
    }

    public static Class getClassFromRegisty(String name) {
        return EntityList.NAME_TO_CLASS.get(name);
    }
}
