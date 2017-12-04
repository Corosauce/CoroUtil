package extendedrenderer.foliage;

import java.util.ArrayList;
import java.util.List;

public class FoliageLocationData {

    public FoliageReplacerBase foliageReplacer;
    public List<Foliage> listFoliage = new ArrayList<>();

    public FoliageLocationData(FoliageReplacerBase foliageReplacer) {
        this.foliageReplacer = foliageReplacer;
    }
}
