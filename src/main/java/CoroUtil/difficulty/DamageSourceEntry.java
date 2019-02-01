package CoroUtil.difficulty;

import net.minecraft.util.math.BlockPos;

/**
 * Currently just used for tracking the highest damage done in a chunk for debugging sake
 */
public class DamageSourceEntry {

    public String source_entity_true = "";
    public String source_entity_immediate = "";
    public String target_entity = "";
    public String source_type = "";
    public float highestDamage = 0;
    public float damageTimeAveraged = 0;
    public long lastLogTime;
    public float timeDiffSeconds;
    public BlockPos source_pos = new BlockPos(0, 0, 0);

    @Override
    public String toString() {
        return "source_entity_true: " + source_entity_true + ", source_entity_immediate: " + source_entity_immediate + ", source_type: " + source_type +
                ", target_entity: " + target_entity + ", highestDamage: " + highestDamage + ", damageTimeAveraged: " + damageTimeAveraged +
                ", timeDiffSeconds: " + timeDiffSeconds + ", lastLogTime: " + lastLogTime + ", source_pos: " + source_pos.toString();
    }
}
