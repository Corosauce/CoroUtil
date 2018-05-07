package extendedrenderer.particle.entity;

import java.util.IdentityHashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import CoroUtil.util.CoroUtilColor;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockDoublePlant.EnumBlockHalf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ParticleTexLeafColor extends ParticleTexFX {
    
    // Save a few stack depth by caching this
    private static BlockColors colors;
    
    private static Map<IBlockState, int[]> colorCache = new IdentityHashMap<>();
    static {        
        ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(rm -> colorCache.clear());
    }

	//only use positives for now
	public float rotationYawMomentum = 0;
	public float rotationPitchMomentum = 0;

	public ParticleTexLeafColor(World worldIn, double posXIn, double posYIn,
			double posZIn, double mX, double mY, double mZ,
			TextureAtlasSprite par8Item) {
		super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);
		
		if (colors == null) {
		    colors = Minecraft.getMinecraft().getBlockColors();
		}
		
		BlockPos pos = new BlockPos(posXIn, posYIn, posZIn);
		IBlockState state = worldIn.getBlockState(pos);
	    // top of double plants doesn't have variant property
		if (state.getBlock() instanceof BlockDoublePlant && state.getValue(BlockDoublePlant.HALF) == EnumBlockHalf.UPPER) {
		    state = state.withProperty(BlockDoublePlant.VARIANT, worldIn.getBlockState(pos.down()).getValue(BlockDoublePlant.VARIANT));
		}

		int mult = colors.colorMultiplier(state, this.world, pos, 0);
		int[] colors = colorCache.get(state);
		if (colors == null) {
		    colors = CoroUtilColor.getColors(state, mult);
		    if (colors.length == 0) {
		        colors = new int[] { 5811761 }; // fallback to default leaf color
		    }
		    while (colors[colors.length - 1] == colors[colors.length - 2]) {
		        colors = ArrayUtils.remove(colors, colors.length - 1);
		    }
		    colorCache.put(state, colors);
		}
		
		// Randomize the color with exponential decrease in likelihood. That is, the first color has a 50% chance, then 25%, etc.
		System.out.println(colors.length);
		int randMax = 1 << (colors.length - 1);
		int choice = 32 - Integer.numberOfLeadingZeros(worldIn.rand.nextInt(randMax));
		int color = colors[choice];

        this.particleRed *= (float)(color >> 16 & 255) / 255.0F;
        this.particleGreen *= (float)(color >> 8 & 255) / 255.0F;
        this.particleBlue *= (float)(color & 255) / 255.0F;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		//make leafs catch on the ground and cause them to bounce up and slow a bit for effect
		if (isCollidedVerticallyDownwards && rand.nextInt(10) == 0) {
			double speed = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			if (speed > 0.07) {
				this.motionY = 0.02D + rand.nextDouble() * 0.03D;
				this.motionX *= 0.6D;
				this.motionZ *= 0.6D;

				rotationYawMomentum = 30;
				rotationPitchMomentum = 30;
			}
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
