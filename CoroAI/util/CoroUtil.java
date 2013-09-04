package CoroAI.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

public class CoroUtil {

	public static void faceEntity(Entity entToRotate, Entity par1Entity, float par2, float par3)
    {
        double d0 = par1Entity.posX - entToRotate.posX;
        double d1 = par1Entity.posZ - entToRotate.posZ;
        double d2;

        if (par1Entity instanceof EntityLivingBase)
        {
        	EntityLivingBase entityliving = (EntityLivingBase)par1Entity;
            d2 = entityliving.posY + (double)entityliving.getEyeHeight() - (entToRotate.posY + (double)entToRotate.getEyeHeight());
        }
        else
        {
            d2 = (par1Entity.boundingBox.minY + par1Entity.boundingBox.maxY) / 2.0D - (entToRotate.posY + (double)entToRotate.getEyeHeight());
        }

        double d3 = (double)MathHelper.sqrt_double(d0 * d0 + d1 * d1);
        float f2 = (float)(Math.atan2(d1, d0) * 180.0D / Math.PI) - 90.0F;
        float f3 = (float)(-(Math.atan2(d2, d3) * 180.0D / Math.PI));
        entToRotate.rotationPitch = updateRotation(entToRotate.rotationPitch, f3, par3);
        entToRotate.rotationYaw = updateRotation(entToRotate.rotationYaw, f2, par2);
    }
	
	public static float updateRotation(float par1, float par2, float par3)
    {
        float f3 = MathHelper.wrapAngleTo180_float(par2 - par1);

        if (f3 > par3)
        {
            f3 = par3;
        }

        if (f3 < -par3)
        {
            f3 = -par3;
        }

        return par1 + f3;
    }
	
}
