package CoroUtil.difficulty.data.cmods;

import CoroUtil.difficulty.data.DataCmod;

/**
 * Created by Corosus on 2/26/2017.
 */
public class CmodTemplate extends DataCmod {
    public String template;

    @Override
    public String toString() {
        return super.toString() + " { " + template + " }";
    }
}
