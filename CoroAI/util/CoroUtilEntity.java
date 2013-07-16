package CoroAI.util;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.Vec3;

public class CoroUtilEntity {

	public static boolean canCoordBeSeen(EntityLiving ent, int x, int y, int z)
    {
        return ent.worldObj.rayTraceBlocks(Vec3.createVectorHelper(ent.posX, ent.posY + (double)ent.getEyeHeight(), ent.posZ), Vec3.createVectorHelper(x, y, z)) == null;
    }
    
    public static boolean canCoordBeSeenFromFeet(EntityLiving ent, int x, int y, int z)
    {
        return ent.worldObj.rayTraceBlocks(Vec3.createVectorHelper(ent.posX, ent.boundingBox.minY+0.15, ent.posZ), Vec3.createVectorHelper(x, y, z)) == null;
    }
}
