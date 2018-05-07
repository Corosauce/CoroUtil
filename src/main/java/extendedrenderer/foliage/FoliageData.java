package extendedrenderer.foliage;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;

import java.util.concurrent.ConcurrentHashMap;

public class FoliageData {

    //orig values
    public static ConcurrentHashMap<IBlockState, IBakedModel> backupBakedModelStore = new ConcurrentHashMap<>();

}
