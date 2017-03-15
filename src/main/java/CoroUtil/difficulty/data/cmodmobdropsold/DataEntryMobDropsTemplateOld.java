package CoroUtil.difficulty.data.cmodmobdropsold;

import CoroUtil.difficulty.data.DataEntryBase;
import CoroUtil.difficulty.data.cmodmobdrops.DataEntryMobDropsDrop;

import java.util.List;

/**
 * Created by Corosus on 2/1/2017.
 */
@Deprecated
public class DataEntryMobDropsTemplateOld extends DataEntryBase {

    public String name;
    public String type;
    public int rand_weight;
    public float level_min;
    public float level_max;

    public List<DataEntryMobDropsDrop> drops;

}
