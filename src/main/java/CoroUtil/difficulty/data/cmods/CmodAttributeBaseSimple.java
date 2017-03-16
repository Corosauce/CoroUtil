package CoroUtil.difficulty.data.cmods;

import CoroUtil.difficulty.data.DataCmod;

/**
 * Created by Corosus on 2/26/2017.
 *
 * keeping it simple for now, will expand with full attribute power later if needed
 *
 * old design:
 *
 * {
 "cmod": "attribute_health",
 "operator": 0,
 "value": 1.5,
 "basevalue?": 0,
 "use_difficulty": false,

 "comment": "//need some way to multiply from difficulty, same for speed, xp, also a cap!"
 },
 */
public class CmodAttributeBaseSimple extends DataCmod {
    public double base_value = -1;
    public double difficulty_multiplier;
}
