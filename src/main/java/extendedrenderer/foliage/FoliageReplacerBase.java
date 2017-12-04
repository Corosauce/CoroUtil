package extendedrenderer.foliage;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class FoliageReplacerBase {

    public abstract boolean validFoliageSpot(World world, BlockPos pos);

    public abstract void addForPos(World world, BlockPos pos);

}
