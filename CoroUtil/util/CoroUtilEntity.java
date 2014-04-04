package CoroUtil.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class CoroUtilEntity {

	public static boolean canCoordBeSeen(EntityLivingBase ent, int x, int y, int z)
    {
        return ent.worldObj.clip(Vec3.createVectorHelper(ent.posX, ent.posY + (double)ent.getEyeHeight(), ent.posZ), Vec3.createVectorHelper(x, y, z)) == null;
    }
    
    public static boolean canCoordBeSeenFromFeet(EntityLivingBase ent, int x, int y, int z)
    {
        return ent.worldObj.clip(Vec3.createVectorHelper(ent.posX, ent.boundingBox.minY+0.15, ent.posZ), Vec3.createVectorHelper(x, y, z)) == null;
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
	
	public static void moveTowards(Entity ent, Entity targ, float speed) {
		double vecX = targ.posX - ent.posX;
		double vecY = targ.posY - ent.posY;
		double vecZ = targ.posZ - ent.posZ;

		double dist2 = (double)Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
		ent.motionX += vecX / dist2 * speed;
		ent.motionY += vecY / dist2 * speed;
		ent.motionZ += vecZ / dist2 * speed;
	}
}
