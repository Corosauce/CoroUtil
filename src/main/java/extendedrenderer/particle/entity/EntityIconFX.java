package extendedrenderer.particle.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityIconFX extends EntityRotFX
{
    public EntityIconFX(World par1World, double par2, double par4, double par6, TextureAtlasSprite par8Item)
    {
        super(par1World, par2, par4, par6, 0.0D, 0.0D, 0.0D);
        this.setParticleIcon(par8Item);
        this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
        this.particleGravity = 0.1F;//Block.blockSnow.blockParticleGravity;
        //this.particleScale /= 2.0F;
        particleTextureJitterX = 3;
        particleTextureJitterY = 3;
    }

    public EntityIconFX(World par1World, double par2, double par4, double par6, double par8, double par10, double par12, TextureAtlasSprite par14Item)
    {
        this(par1World, par2, par4, par6, par14Item);
        this.motionX *= 0.10000000149011612D;
        this.motionY *= 0.10000000149011612D;
        this.motionZ *= 0.10000000149011612D;
        this.motionX += par8;
        this.motionY += par10;
        this.motionZ += par12;
    }

    public int getFXLayer()
    {
        return renderOrder != -1 ? renderOrder : 2;
    }

    @Override
    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float par3, float par4, float par5, float par6, float par7) {
    	float f6 = ((float)this.particleTextureIndexX + this.particleTextureJitterX / 4.0F) / 16.0F;
        float f7 = f6 + 0.015609375F;
        float f8 = ((float)this.particleTextureIndexY + this.particleTextureJitterY / 4.0F) / 16.0F;
        float f9 = f8 + 0.015609375F;
        float f10 = 0.1F * this.particleScale;

        if (this.particleIcon != null)
        {
        	f6 = particleIcon.getMinU();
            f7 = particleIcon.getMaxU();
            f8 = particleIcon.getMinV();
            f9 = particleIcon.getMaxV();
        }

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
        .color(this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha).lightmap(j, k).endVertex();
        
        worldRendererIn.pos((double)(f11 - par3 * f10 + par6 * f10), (double)(f12 + par4 * f10), (double)(f13 - par5 * f10 + par7 * f10)).tex((double)f6, (double)f8)
        .color(this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha).lightmap(j, k).endVertex();
        
        worldRendererIn.pos((double)(f11 + par3 * f10 + par6 * f10), (double)(f12 + par4 * f10), (double)(f13 + par5 * f10 + par7 * f10)).tex((double)f7, (double)f8)
        .color(this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha).lightmap(j, k).endVertex();
        
        worldRendererIn.pos((double)(f11 + par3 * f10 - par6 * f10), (double)(f12 - par4 * f10), (double)(f13 + par5 * f10 - par7 * f10)).tex((double)f7, (double)f9)
        .color(this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha).lightmap(j, k).endVertex();
        
        
    }
}
