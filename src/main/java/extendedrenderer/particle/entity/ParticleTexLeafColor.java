package extendedrenderer.particle.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ParticleTexLeafColor extends ParticleTexFX {

	//only use positives for now
	public float rotationYawMomentum = 0;
	public float rotationPitchMomentum = 0;

	public ParticleTexLeafColor(World worldIn, double posXIn, double posYIn,
			double posZIn, double mX, double mY, double mZ,
			TextureAtlasSprite par8Item) {
		super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);
		
		BlockPos pos = new BlockPos(posXIn, posYIn, posZIn);
		IBlockState state = worldIn.getBlockState(pos);
		int i = Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, this.world, pos, 0);
		//some mods dont use biome coloring and have their color in texture, so lets fallback to green until a texture scanning solution is used
		if (i == -1) {
			//color for vanilla leaf in forest biome
			i = 5811761;
		}
        this.particleRed *= (float)(i >> 16 & 255) / 255.0F;
        this.particleGreen *= (float)(i >> 8 & 255) / 255.0F;
        this.particleBlue *= (float)(i & 255) / 255.0F;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		//make leafs catch on the ground and cause them to bounce up and slow a bit for effect
		if (isCollidedVerticallyDownwards) {
			this.motionY = 0.05;
			this.motionX *= 0.6F;
			this.motionZ *= 0.6F;

			rotationYawMomentum = 30;
			rotationPitchMomentum = 30;
		}

		if (rotationYawMomentum > 0) {

			this.rotationYaw += rotationYawMomentum;

			rotationYawMomentum -= 1.5F;

			if (rotationYawMomentum < 0) {
				rotationYawMomentum = 0;
			}
		}

		if (rotationPitchMomentum > 0) {

			this.rotationPitch += rotationPitchMomentum;

			rotationPitchMomentum -= 1.5F;

			if (rotationPitchMomentum < 0) {
				rotationPitchMomentum = 0;
			}
		}
	}
}
