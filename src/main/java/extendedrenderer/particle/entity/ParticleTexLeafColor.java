package extendedrenderer.particle.entity;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.registries.IRegistryDelegate;
import org.apache.commons.lang3.ArrayUtils;

import CoroUtil.util.CoroUtilColor;
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

	private static final Field _blockColorMap = ReflectionHelper.findField(BlockColors.class, "blockColorMap");
	private static Map<IRegistryDelegate<Block>, IBlockColor> blockColorMap;

	private static ConcurrentHashMap<IBlockState, int[]> colorCache = new ConcurrentHashMap<>();
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
			try {
				blockColorMap = (Map<IRegistryDelegate<Block>, IBlockColor>) _blockColorMap.get(colors);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
		BlockPos pos = new BlockPos(posXIn, posYIn, posZIn);
		IBlockState state = worldIn.getBlockState(pos);

	    // top of double plants doesn't have variant property
		if (state.getBlock() instanceof BlockDoublePlant && state.getValue(BlockDoublePlant.HALF) == EnumBlockHalf.UPPER) {
		    state = state.withProperty(BlockDoublePlant.VARIANT, worldIn.getBlockState(pos.down()).getValue(BlockDoublePlant.VARIANT));
		}

		int multiplier = this.colors.colorMultiplier(state, this.world, pos, 0);

		int[] colors = colorCache.get(state);
		if (colors == null) {

		    colors = CoroUtilColor.getColors(state);

		    if (colors.length == 0) {

		    	//if there is no color to use AND theres no multiplier, fallback to good ol green
				if (!hasColor(state) || (multiplier & 0xFFFFFF) == 0xFFFFFF) {
					multiplier = 5811761; //color for vanilla leaf in forest biome
				}

				//add just white that will get colormultiplied
				colors = new int[] { 0xFFFFFF };
		    }
		    // Remove duplicate colors from end of array, this will skew the random choice later
			if (colors.length > 1) {
				while (colors[colors.length - 1] == colors[colors.length - 2]) {
					colors = ArrayUtils.remove(colors, colors.length - 1);
				}
			}
		    colorCache.put(state, colors);
		}
		
		// Randomize the color with exponential decrease in likelihood. That is, the first color has a 50% chance, then 25%, etc.
		int randMax = 1 << (colors.length - 1);
		int choice = 32 - Integer.numberOfLeadingZeros(worldIn.rand.nextInt(randMax));
		int color = colors[choice];

		float mr = ((multiplier >>> 16) & 0xFF) / 255f;
		float mg = ((multiplier >>> 8) & 0xFF) / 255f;
		float mb = (multiplier & 0xFF) / 255f;

		this.particleRed *= (float) (color >> 16 & 255) / 255.0F * mr;
		this.particleGreen *= (float) (color >> 8 & 255) / 255.0F * mg;
		this.particleBlue *= (float) (color & 255) / 255.0F * mb;
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

	private final boolean hasColor(IBlockState state) {
		return blockColorMap.containsKey(state.getBlock().delegate);
	}

}
