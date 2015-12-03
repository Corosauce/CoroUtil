package CoroUtil.util;

import java.util.Iterator;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class CoroUtilEntity {

	public static boolean canCoordBeSeen(EntityLivingBase ent, int x, int y, int z)
    {
        return ent.worldObj.rayTraceBlocks(new Vec3(ent.posX, ent.posY + (double)ent.getEyeHeight(), ent.posZ), new Vec3(x, y, z)) == null;
    }
    
    public static boolean canCoordBeSeenFromFeet(EntityLivingBase ent, int x, int y, int z)
    {
        return ent.worldObj.rayTraceBlocks(new Vec3(ent.posX, ent.boundingBox.minY+0.15, ent.posZ), new Vec3(x, y, z)) == null;
    }
    
    public static double getDistance(Entity ent, ChunkCoordinates coords)
    {
        double d3 = ent.posX - coords.posX;
        double d4 = ent.posY - coords.posY;
        double d5 = ent.posZ - coords.posZ;
        return (double)MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);
    }
	
	public static double getDistance(Entity ent, TileEntity tEnt)
    {
        double d3 = ent.posX - tEnt.xCoord;
        double d4 = ent.posY - tEnt.yCoord;
        double d5 = ent.posZ - tEnt.zCoord;
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
		return ent.getCommandSenderName();
	}
	
	public static EntityPlayer getPlayerByUUID(UUID uuid) {
		Iterator iterator = MinecraftServer.getServer().getConfigurationManager().playerEntityList.iterator();
        EntityPlayerMP entityplayermp;
        
        while (iterator.hasNext()) {
        	entityplayermp = (EntityPlayerMP) iterator.next();
        	
        	if (entityplayermp.getGameProfile().getId().equals(uuid)) {
        		return entityplayermp;
        	}
        }
        
        return null;
	}
}
