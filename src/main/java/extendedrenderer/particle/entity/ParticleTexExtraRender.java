package extendedrenderer.particle.entity;

import extendedrenderer.render.RotatingParticleManager;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import CoroUtil.util.CoroUtilParticle;

public class ParticleTexExtraRender extends ParticleTexFX {

	private int severityOfRainRate = 2;

	private int extraParticlesBaseAmount = 5;

	public boolean noExtraParticles = false;
	
	public ParticleTexExtraRender(World worldIn, double posXIn, double posYIn,
			double posZIn, double mX, double mY, double mZ,
			TextureAtlasSprite par8Item) {
		super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);
		
		
	}

	public int getSeverityOfRainRate() {
		return severityOfRainRate;
	}

	public void setSeverityOfRainRate(int severityOfRainRate) {
		this.severityOfRainRate = severityOfRainRate;
	}

	public int getExtraParticlesBaseAmount() {
		return extraParticlesBaseAmount;
	}

	public void setExtraParticlesBaseAmount(int extraParticlesBaseAmount) {
		this.extraParticlesBaseAmount = extraParticlesBaseAmount;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();


		/*int height = this.world.getPrecipitationHeight(new BlockPos(this.posX, this.posY, this.posZ)).getY();
		if (this.posY <= height) this.setExpired();*/

		//this.setExpired();
	}

	@Override
	public void renderParticle(BufferBuilder worldRendererIn, Entity entityIn,
			float partialTicks, float rotationX, float rotationZ,
			float rotationYZ, float rotationXY, float rotationXZ) {

		//override rotations
		if (!facePlayer) {
			rotationX = MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F);
			rotationYZ = MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F);
	        rotationXY = -rotationYZ * MathHelper.sin(this.rotationPitch * (float)Math.PI / 180.0F);
	        rotationXZ = rotationX * MathHelper.sin(this.rotationPitch * (float)Math.PI / 180.0F);
	        rotationZ = MathHelper.cos(this.rotationPitch * (float)Math.PI / 180.0F);
		} else {
			if (this.isSlantParticleToWind()) {
				rotationXZ = (float) -this.motionZ;
				rotationXY = (float) -this.motionX;
			}
			//rotationXZ = 6.28F;
			//rotationXY = 1;
			//rotationZ -= 1;
		}

		
		float f = (float)this.particleTextureIndexX / 16.0F;
        float f1 = f + 0.0624375F;
        float f2 = (float)this.particleTextureIndexY / 16.0F;
        float f3 = f2 + 0.0624375F;
        float f4 = 0.1F * this.particleScale;
		float scaleY = 0.4F * this.particleScale;

		float scale1 = 0.1F * this.particleScale;
		float scale2 = 0.1F * this.particleScale;
		float scale3 = 0.1F * this.particleScale;
		float scale4 = 0.1F * this.particleScale;

		float fixY = 0;

        if (this.particleTexture != null)
        {
            f = this.particleTexture.getMinU();
            f1 = this.particleTexture.getMaxU();
            f2 = this.particleTexture.getMinV();
            f3 = this.particleTexture.getMaxV();

			/*f = this.particleTexture.getInterpolatedU((double)(this.particleTextureJitterX / 4.0F * 16.0F));
			f1 = this.particleTexture.getInterpolatedU((double)((this.particleTextureJitterX + 1.0F) / 4.0F * 16.0F));
			f2 = this.particleTexture.getInterpolatedV((double)(this.particleTextureJitterY / 4.0F * 16.0F));
			f3 = this.particleTexture.getInterpolatedV((double)((this.particleTextureJitterY + 1.0F) / 4.0F * 16.0F));*/

			float part = 16F / 3F;
			float offset = 0;
			float posBottom = (float)(this.posY - 10D);

			float height = this.world.getPrecipitationHeight(new BlockPos(this.posX, this.posY, this.posZ)).getY();

			if (posBottom < height) {
				float diff = height - posBottom;
				offset = diff;
				fixY = 0;//diff * 1.0F;
				if (offset > part) offset = part;
			}

			/*f = this.particleTexture.getInterpolatedU(part);
			f1 = this.particleTexture.getInterpolatedU(part*2F);
			f2 = this.particleTexture.getInterpolatedV((part*2F) + offset);
			f3 = this.particleTexture.getInterpolatedV(part + offset);*/

			/*f = this.particleTexture.getInterpolatedU(4);
			f1 = this.particleTexture.getInterpolatedU(12);
			f2 = this.particleTexture.getInterpolatedV(16);
			f3 = this.particleTexture.getInterpolatedV(8);*/
        }

		int rainDrops = extraParticlesBaseAmount + ((Math.max(0, severityOfRainRate-1)) * 5);

        //test
		//rainDrops = 10;

		//catch code hotload crash, doesnt help much anyways
		try {
			for (int ii = 0; ii < (noExtraParticles ? 1 : Math.min(rainDrops, CoroUtilParticle.maxRainDrops)); ii++) {
				float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
				float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY) + fixY;
				float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);

				double xx = 0;
				double zz = 0;
				double yy = 0;
				if (ii != 0) {
					xx = CoroUtilParticle.rainPositions[ii].xCoord;
					zz = CoroUtilParticle.rainPositions[ii].zCoord;
					yy = CoroUtilParticle.rainPositions[ii].yCoord;

					f5 += xx;
					f6 += yy;
					f7 += zz;
				}

				//prevent precip under overhangs/inside for extra render
				if (this.isDontRenderUnderTopmostBlock()) {
					int height = this.world.getPrecipitationHeight(new BlockPos(this.posX + xx, this.posY, this.posZ + zz)).getY();
					if (this.posY + yy <= height) continue;
				}

				if (ii != 0) {
					RotatingParticleManager.debugParticleRenderCount++;
				}

				/*int height = entityIn.world.getPrecipitationHeight(new BlockPos(ActiveRenderInfo.getPosition().xCoord + f5, this.posY + f6, ActiveRenderInfo.getPosition().zCoord + f7)).getY();
				if (ActiveRenderInfo.getPosition().yCoord + f6 <= height) continue;*/

				int i = 15728640;//this.getBrightnessForRender(partialTicks);
				int j = i >> 16 & 65535;
				int k = i & 65535;
				Vec3d[] avec3d = new Vec3d[] {
						new Vec3d((double)(-rotationX * scale1 - rotationXY * scale1), (double)(-rotationZ * scale1), (double)(-rotationYZ * scale1 - rotationXZ * scale1)),
						new Vec3d((double)(-rotationX * scale2 + rotationXY * scale2), (double)(rotationZ * scale2), (double)(-rotationYZ * scale2 + rotationXZ * scale2)),
						new Vec3d((double)(rotationX * scale3 + rotationXY * scale3), (double)(rotationZ * scale3), (double)(rotationYZ * scale3 + rotationXZ * scale3)),
						new Vec3d((double)(rotationX * scale4 - rotationXY * scale4), (double)(-rotationZ * scale4), (double)(rotationYZ * scale4 - rotationXZ * scale4))};

				/*if (this.field_190014_F != 0.0F)
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
				}*/

				worldRendererIn.pos((double)f5 + avec3d[0].x, (double)f6 + avec3d[0].y, (double)f7 + avec3d[0].z).tex((double)f1, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
				worldRendererIn.pos((double)f5 + avec3d[1].x, (double)f6 + avec3d[1].y, (double)f7 + avec3d[1].z).tex((double)f1, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
				worldRendererIn.pos((double)f5 + avec3d[2].x, (double)f6 + avec3d[2].y, (double)f7 + avec3d[2].z).tex((double)f, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
				worldRendererIn.pos((double)f5 + avec3d[3].x, (double)f6 + avec3d[3].y, (double)f7 + avec3d[3].z).tex((double)f, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		


        
	}

}
