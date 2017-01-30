package CoroUtil.difficulty.buffs;

import net.minecraft.entity.EntityCreature;

/**
 * Created by Corosus on 1/9/2017.
 */
public abstract class BuffBase {

    /**
     * String used to serialize buff in nbt
     *
     * @return
     */
    public abstract String getTagName();

    /**
     * Apply buff first time on entity, safe to call base method redundantly
     *
     * @param ent
     * @param difficulty
     */
    public boolean applyBuff(EntityCreature ent, float difficulty) {
        ent.getEntityData().setBoolean(getTagName(), true);
        return true;
    }

    /**
     * Run after all buffs processed, to apply stuff that might need to happen after other buffs do their initial work
     * Mainly used for when buffs are batch applied like on dice roll or deserialization
     *
     * @param ent
     * @param difficulty
     */
    public void applyBuffPost(EntityCreature ent, float difficulty) {
        return;
    }

    /**
     * Checks to see if theres some random reason why buff cannot be applied, does not factor in if buff was already applied
     *
     * @param ent
     * @param difficulty
     * @return
     */
    public boolean canApplyBuff(EntityCreature ent, float difficulty) {
        return true;
    }

    /**
     * Apply buff if entity reloaded from chunk
     * Override this method if the buff requires restoring states on reload, eg AI tasks, but not attributes
     *
     * @param ent
     * @param difficulty
     */
    public void applyBuffFromReload(EntityCreature ent, float difficulty) {};

    public float getMinRequiredDifficulty() {
        return 0;
    }

}
