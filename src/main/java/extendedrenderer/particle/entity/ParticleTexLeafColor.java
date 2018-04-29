package extendedrenderer.particle.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ParticleTexLeafColor extends ParticleTexFX {

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

}
