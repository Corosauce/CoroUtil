package extendedrenderer.particle.entity;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
@SideOnly(Side.CLIENT)
public class EntityTexFX extends EntityRotFX
{
    public int age;
    public float brightness;
    public int textureID;

    //Types, for diff physics rules in wind code
    //Leaves = 0
    //Sand = 1
    public int type = 0;

    public EntityTexFX(World var1, double var2, double var4, double var6, double var8, double var10, double var12, double var14, int colorIndex, int texID)
    {
        super(var1, var2, var4, var6, var8, var10, var12);
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
        else if (colorIndex == 6)
        {
            color = new Color(0xFFFFFF);
        }

        //BRIGHTNESS OVERRIDE! for textures
        this.particleRed = this.particleGreen = this.particleBlue = 0.7F;
        brightness = 0.5F;

        if (colorIndex != 0)
        {
            this.particleRed = color.getRed() / 255F;
            this.particleGreen = color.getGreen() / 255F;
            this.particleBlue = color.getBlue() / 255F;
        }

        this.particleScale = this.rand.nextFloat() * this.rand.nextFloat() * 2.0F + 1.0F;
        this.particleMaxAge = (int)(16.0D/* / ((double)this.rand.nextFloat() * 0.8D + 0.2D)*/) + 2;
        this.particleMaxAge = (int)((float)this.particleMaxAge * var14);
        this.particleGravity = 1.0F;
        //this.particleScale = 5F;
        this.setParticleTextureIndex(textureID);
        renderDistanceWeight = 10.0D;
        setSize(1.0F, 1.0F);
        noClip = false;
    }
    
    @Override
    public boolean isBurning()
    {
    	return false;
    }
    
    @Override
    protected boolean getFlag(int par1)
    {
    	return false;
    }
    
    @Override
    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float par3, float par4, float par5, float par6, float par7) {
    	/*float f6 = ((float)this.particleTextureIndexX + this.particleTextureJitterX / 4.0F) / 16.0F;
        float f7 = f6 + 0.015609375F;
        float f8 = ((float)this.particleTextureIndexY + this.particleTextureJitterY / 4.0F) / 16.0F;
        float f9 = f8 + 0.015609375F;
        float f10 = 0.1F * this.particleScale;*/
        
        /*float f6 = (float)this.particleTextureIndexX / 16.0F;
        float f7 = f6 + 0.0624375F;
        float f8 = (float)this.particleTextureIndexY / 16.0F;
        float f9 = f8 + 0.0624375F;
        float f10 = 1.1F * this.particleScale;*/
        
        /** not sure why other code has issues now for 1.8, these new calculations are done like this:
         * 
         * this class is used for a texture sheet that contains 5 particles horizontally, so 80x16 size
         * start U is index * 16, end is same + 1 more size, use variables below to define these values and do the math
         * start V always 0, end V = particle size since we dont tile them vertically in sheet
         * 
         */

        float particlesInSheet = 5F;
        float particleSize = particlesInSheet / 1F;
        
        float f6 = particleSize * (float)textureID;
        float f7 = f6 + particleSize;
        float f8 = 0;
        float f9 = 1F;
        float f10 = 0.1F * this.particleScale;
        
        if (this.particleIcon != null)
        {
        	f6 = particleIcon.getMinU();
            f7 = particleIcon.getMaxU();
            f8 = particleIcon.getMinV();
            f9 = particleIcon.getMaxV();
        }
        
        //TEMP
        f6 = 0;
        f7 = 1F / 5F;
        //f8 = 0;
        //f9 = 1F;
        
        float f11 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        float f12 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        float f13 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
        
        Minecraft mc = Minecraft.getMinecraft();
        float br = ((0.9F + (mc.gameSettings.gammaSetting * 0.1F)) - (mc.theWorld.calculateSkylightSubtracted(partialTicks) * 0.01F)) * mc.theWorld.getSunBrightness(1F);
        /*if (mc.theWorld.getTotalWorldTime() % 20 == 0) {
        	System.out.println("brightness: " + br);
        }*/
        
        br = 0.55F * Math.max(0.01F, br) * (2F);
        
        float f14 = br;//brightness;
        //par1Tessellator.setColorOpaque_F(f14 * this.particleRed, f14 * this.particleGreen, f14 * this.particleBlue);
        /*par1Tessellator.setColorRGBA_F(this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, Math.max(0F, this.particleAlpha));
        worldRendererIn.pos((double)(f11 - par3 * f10 - par6 * f10), (double)(f12 - par4 * f10), (double)(f13 - par5 * f10 - par7 * f10), (double)f6, (double)f9);
        worldRendererIn.func_181662_b((double)(f11 - par3 * f10 + par6 * f10), (double)(f12 + par4 * f10), (double)(f13 - par5 * f10 + par7 * f10), (double)f6, (double)f8);
        worldRendererIn.func_181662_b((double)(f11 + par3 * f10 + par6 * f10), (double)(f12 + par4 * f10), (double)(f13 + par5 * f10 + par7 * f10), (double)f7, (double)f8);
        worldRendererIn.func_181662_b((double)(f11 + par3 * f10 - par6 * f10), (double)(f12 - par4 * f10), (double)(f13 + par5 * f10 - par7 * f10), (double)f7, (double)f9);*/
        
        //TODO: verify this copied code from EntityFX will work for this, our brightness code is reverted atm
        int i = 0;//this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;
        
        worldRendererIn.pos((double)(f11 - par3 * f10 - par6 * f10), (double)(f12 - par4 * f10), (double)(f13 - par5 * f10 - par7 * f10)).tex((double)f6, (double)f9)
        .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        
        worldRendererIn.pos((double)(f11 - par3 * f10 + par6 * f10), (double)(f12 + par4 * f10), (double)(f13 - par5 * f10 + par7 * f10)).tex((double)f6, (double)f8)
        .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        
        worldRendererIn.pos((double)(f11 + par3 * f10 + par6 * f10), (double)(f12 + par4 * f10), (double)(f13 + par5 * f10 + par7 * f10)).tex((double)f7, (double)f8)
        .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        
        worldRendererIn.pos((double)(f11 + par3 * f10 - par6 * f10), (double)(f12 - par4 * f10), (double)(f13 + par5 * f10 - par7 * f10)).tex((double)f7, (double)f9)
        .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        
        
    }

    @Override
    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setDead();
        }

        if (this.motionX > 0.01F && this.motionZ > 0.01F)
        {
            this.rotationPitch += this.rand.nextInt(6) - 3;
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
        /*this.motionX *= 0.8999999761581421D;
        this.motionY *= 0.8999999761581421D;
        this.motionZ *= 0.8999999761581421D;
        if(this.onGround) {
           this.motionX *= 0.699999988079071D;
           this.motionZ *= 0.699999988079071D;
        }*/
    }

    @Override
    public int getFXLayer()
    {
        return 5;
    }
}
