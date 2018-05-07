package extendedrenderer.particle.entity;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.registries.IRegistryDelegate;

public class ParticleTexLeafColor extends ParticleTexFX {
    
    private static final Field _blockColorMap = ReflectionHelper.findField(BlockColors.class, "blockColorMap");
    
    private static BlockColors colors;
    private static Map<IRegistryDelegate<Block>, IBlockColor> blockColorMap;

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
	    // tallgrass apparently isnt colorized or some weird thing, in vanilla
		if (!hasColor(state)) {
		    state = worldIn.getBlockState(pos.down());
		}
		
		int i = colors.colorMultiplier(state, this.world, pos, 0);
	    //some mods dont use biome coloring and have their color in texture, so lets fallback to green until a texture scanning solution is used
		if (!hasColor(state) || (i & 0xFFFFFF) == 0xFFFFFF) {
		    i = 5811761; //color for vanilla leaf in forest biome
		}

        this.particleRed *= (float)(i >> 16 & 255) / 255.0F;
        this.particleGreen *= (float)(i >> 8 & 255) / 255.0F;
        this.particleBlue *= (float)(i & 255) / 255.0F;
	}
	
	private final boolean hasColor(IBlockState state) {
	    return blockColorMap.containsKey(state.getBlock().delegate);
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
