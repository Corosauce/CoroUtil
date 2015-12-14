package extendedrenderer.particle.entity;

import java.awt.Color;

import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
@SideOnly(Side.CLIENT)
public class EntityTexBiomeColorFX extends EntityTexFX
{

    public EntityTexBiomeColorFX(World var1, double var2, double var4, double var6, double var8, double var10, double var12, double var14, int colorIndex, int texID, int meta, int x, int y, int z)
    {
        super(var1, var2, var4, var6, var8, var10, var12, var14, colorIndex, texID);
        textureID = texID;
        this.motionX = var8 + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.05F);
        this.motionY = var10 + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.05F);
        this.motionZ = var12 + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.05F);
        //Color IDS
        //0 = black/regular/default
        //1 = dirt
        //2 = sand
        //3 = water
        //4 = snow
        //5 = stone
        Color color = null;

        if (colorIndex == 0)
        {
            this.particleRed = this.particleGreen = this.particleBlue = this.rand.nextFloat() * 0.3F/* + 0.7F*/;
        }
        else if (colorIndex == 1)
        {
            color = new Color(0x79553a);
        }
        else if (colorIndex == 2)
        {
            color = new Color(0xd6cf98);
        }
        else if (colorIndex == 3)
        {
            color = new Color(0x002aDD);
        }
        else if (colorIndex == 4)
        {
            color = new Color(0xeeffff);
        }
        else if (colorIndex == 5)
        {
            color = new Color(0x79553a);
        }
        
        //int x = 0;
        //int y = 0;
        //int z = 0;
        
        //biome color override
        //int meta = this.worldObj.getBlockMetadata((x, y, z)
        color = new Color(Blocks.leaves.colorMultiplier(worldObj, new BlockPos(x, y, z)));

        //BRIGHTNESS OVERRIDE! for textures
        this.particleRed = this.particleGreen = this.particleBlue = 0.7F;
        brightness = 0.5F;

        //if (colorIndex != 0)
        //{
            this.particleRed = color.getRed() / 255F;
            this.particleGreen = color.getGreen() / 255F;
            this.particleBlue = color.getBlue() / 255F;
        //}

        this.particleScale = this.rand.nextFloat() * this.rand.nextFloat() * 2.0F + 1.0F;
        this.particleMaxAge = (int)(16.0D/* / ((double)this.rand.nextFloat() * 0.8D + 0.2D)*/) + 2;
        this.particleMaxAge = (int)((float)this.particleMaxAge * var14);
        this.particleGravity = 1.0F;
        //this.particleScale = 5F;
        this.setParticleTextureIndex(textureID);
        renderDistanceWeight = 10.0D;
        setSize(0.2F, 0.2F);
        noClip = false;
    }
    
    public boolean isBurning()
    {
    	return false;
    }
    
    protected boolean getFlag(int par1)
    {
    	return false;
    }

    public void onUpdate2()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setDead();
        }

        //this.particleTextureIndex = 7 - this.particleAge * 8 / this.particleMaxAge;
        //this.particleTextureIndex = 7 - this.particleAge * 8 / this.particleMaxAge;
        this.setParticleTextureIndex(textureID);//mod_EntMover.effWindID;
        //this.motionY += 0.0040D;
        this.motionY -= 0.04D * (double)this.particleGravity;
        //this.motionY -= 0.05000000074505806D;
        float var20 = 0.98F;
        this.motionX *= (double)var20;
        this.motionY *= (double)var20;
        this.motionZ *= (double)var20;
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        /*this.motionX *= 0.8999999761581421D;
        this.motionY *= 0.8999999761581421D;
        this.motionZ *= 0.8999999761581421D;
        if(this.onGround) {
           this.motionX *= 0.699999988079071D;
           this.motionZ *= 0.699999988079071D;
        }*/
    }

    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setDead();
        }

        double speed = Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
        
        if (speed > 0.04F)
        {
        	int speed2 = (int)(speed * 100);
            this.rotationPitch += this.rand.nextInt(speed2) - (speed2 / 2);
            this.rotationYaw += this.rand.nextInt(speed2) - (speed2 / 2);
        }

        //this.particleTextureIndex = 7 - this.particleAge * 8 / this.particleMaxAge;
        //this.particleTextureIndex = 7 - this.particleAge * 8 / this.particleMaxAge;
        setParticleTextureIndex(textureID);//mod_EntMover.effWindID;
        //this.motionY += 0.0040D;
        this.motionY -= (0.04D * this.rand.nextFloat()) * (double)this.particleGravity;
        //this.motionY -= 0.05000000074505806D;
        float var20 = 1F - (0.08F * this.rand.nextFloat());
        this.motionX *= (double)var20;
        this.motionY *= (double)var20;
        this.motionZ *= (double)var20;
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.setPosition(posX, posY, posZ);
        this.lastTickPosX = this.posX;
        this.lastTickPosY = this.posY;
        this.lastTickPosZ = this.posZ;
        //this.boundingBox. = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
        /*this.motionX *= 0.8999999761581421D;
        this.motionY *= 0.8999999761581421D;
        this.motionZ *= 0.8999999761581421D;
        if(this.onGround) {
           this.motionX *= 0.699999988079071D;
           this.motionZ *= 0.699999988079071D;
        }*/
    }

    public int getFXLayer()
    {
        return 5;
    }
}
