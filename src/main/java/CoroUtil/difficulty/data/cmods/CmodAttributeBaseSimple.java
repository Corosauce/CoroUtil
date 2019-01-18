package CoroUtil.difficulty.data.cmods;

import CoroUtil.difficulty.data.DataCmod;
import CoroUtil.difficulty.data.DifficultyDataReader;

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
    public double max_value = -1;
    public double difficulty_multiplier;

    @Override
    public String toString() {
        String str = ": " + base_value + " + (" + base_value + " * difficulty * " + difficulty_multiplier + ") max_value: " + max_value;
        if (DifficultyDataReader.getDebugDifficulty() != -1) {
            double val = base_value + (base_value * DifficultyDataReader.getDebugDifficulty() * difficulty_multiplier);
            return super.toString() + str + " = " + val;
        } else {
            return super.toString() + str;
        }

    }
}
