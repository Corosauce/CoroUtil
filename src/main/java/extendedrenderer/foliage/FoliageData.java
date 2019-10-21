package extendedrenderer.foliage;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;

import java.util.concurrent.ConcurrentHashMap;

public class FoliageData {

    //orig values
    public static ConcurrentHashMap<BlockState, IBakedModel> backupBakedModelStore = new ConcurrentHashMap<>();

}

