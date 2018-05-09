package CoroUtil.difficulty.data.cmods;

import CoroUtil.difficulty.data.DataCmod;

/**
 * Created by Corosus on 2/26/2017.
 */
public class CmodMobDrops extends DataCmod {
    public String loot_table;

    @Override
    public String toString() {
        return super.toString() + ": " + loot_table;
    }
}
