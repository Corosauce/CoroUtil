package CoroUtil.difficulty.data.cmods;

import CoroUtil.difficulty.data.DataCmod;

import java.util.List;

/**
 * Created by Corosus on 2/26/2017.
 */
public class CmodAIInfernal extends DataCmod {
    public List<String> modifiers;

    @Override
    public String toString() {
        return super.toString() + ", mods: " + modifiers.spliterator();
    }
}
