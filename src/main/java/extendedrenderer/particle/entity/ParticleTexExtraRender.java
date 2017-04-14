package extendedrenderer.particle.entity;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import CoroUtil.util.CoroUtilParticle;

public class ParticleTexExtraRender extends ParticleTexFX {

	public int severityOfRainRate = 2;
	
	public ParticleTexExtraRender(World worldIn, double posXIn, double posYIn,
			double posZIn, double mX, double mY, double mZ,
			TextureAtlasSprite par8Item) {
		super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);
		
		
	}

	@Override
	public void renderParticle(VertexBuffer worldRendererIn, Entity entityIn,
			float partialTicks, float rotationX, float rotationZ,
			float rotationYZ, float rotationXY, float rotationXZ) {

		//override rotations
		if (!facePlayer) {
			rotationX = MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F);
			rotationYZ = MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F);
	        rotationXY = -rotationYZ * MathHelper.sin(this.rotationPitch * (float)Math.PI / 180.0F);
	        rotationXZ = rotationX * MathHelper.sin(this.rotationPitch * (float)Math.PI / 180.0F);
	        rotationZ = MathHelper.cos(this.rotationPitch * (float)Math.PI / 180.0F);
		}
		
		float f = (float)this.particleTextureIndexX / 16.0F;
        float f1 = f + 0.0624375F;
        float f2 = (float)this.particleTextureIndexY / 16.0F;
        float f3 = f2 + 0.0624375F;
        float f4 = 0.1F * this.particleScale;

        if (this.particleTexture != null)
        {
            f = this.particleTexture.getMinU();
            f1 = this.particleTexture.getMaxU();
            f2 = this.particleTexture.getMinV();
            f3 = this.particleTexture.getMaxV();
        }
		
		int rainDrops = 5 + ((Math.max(0, severityOfRainRate-1)) * 5);
		
		for (int ii = 0; ii < Math.min(rainDrops, CoroUtilParticle.maxRainDrops); ii++) {
			float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
	        float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
	        float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
	        
	        if (ii != 0) {
	        	f5 += CoroUtilParticle.rainPositions[ii].xCoord;
	        	f6 += CoroUtilParticle.rainPositions[ii].yCoord;
	        	f7 += CoroUtilParticle.rainPositions[ii].zCoord;
	        }
	        
	        int i = this.getBrightnessForRender(partialTicks);
	        int j = i >> 16 & 65535;
	        int k = i & 65535;
	        Vec3d[] avec3d = new Vec3d[] {new Vec3d((double)(-rotationX * f4 - rotationXY * f4), (double)(-rotationZ * f4), (double)(-rotationYZ * f4 - rotationXZ * f4)), new Vec3d((double)(-rotationX * f4 + rotationXY * f4), (double)(rotationZ * f4), (double)(-rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double)(rotationX * f4 + rotationXY * f4), (double)(rotationZ * f4), (double)(rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double)(rotationX * f4 - rotationXY * f4), (double)(-rotationZ * f4), (double)(rotationYZ * f4 - rotationXZ * f4))};

	        if (this.field_190014_F != 0.0F)
	        {
	            float f8 = this.field_190014_F + (this.field_190014_F - this.field_190015_G) * partialTicks;
	            float f9 = MathHelper.cos(f8 * 0.5F);
	            float f10 = MathHelper.sin(f8 * 0.5F) * (float)field_190016_K.xCoord;
	            float f11 = MathHelper.sin(f8 * 0.5F) * (float)field_190016_K.yCoord;
	            float f12 = MathHelper.sin(f8 * 0.5F) * (float)field_190016_K.zCoord;
	            Vec3d vec3d = new Vec3d((double)f10, (double)f11, (double)f12);

	            for (int l = 0; l < 4; ++l)
	            {
	                avec3d[l] = vec3d.scale(2.0D * avec3d[l].dotProduct(vec3d)).add(avec3d[l].scale((double)(f9 * f9) - vec3d.dotProduct(vec3d))).add(vec3d.crossProduct(avec3d[l]).scale((double)(2.0F * f9)));
	            }
	        }

	        worldRendererIn.pos((double)f5 + avec3d[0].xCoord, (double)f6 + avec3d[0].yCoord, (double)f7 + avec3d[0].zCoord).tex((double)f1, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
	        worldRendererIn.pos((double)f5 + avec3d[1].xCoord, (double)f6 + avec3d[1].yCoord, (double)f7 + avec3d[1].zCoord).tex((double)f1, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
	        worldRendererIn.pos((double)f5 + avec3d[2].xCoord, (double)f6 + avec3d[2].yCoord, (double)f7 + avec3d[2].zCoord).tex((double)f, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
	        worldRendererIn.pos((double)f5 + avec3d[3].xCoord, (double)f6 + avec3d[3].yCoord, (double)f7 + avec3d[3].zCoord).tex((double)f, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
		}

        
	}

}
