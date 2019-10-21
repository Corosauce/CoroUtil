package CoroUtil.difficulty.buffs;

import CoroUtil.difficulty.UtilEntityBuffs;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

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
    public boolean applyBuff(CreatureEntity ent, float difficulty) {
        CompoundNBT data = ent.getEntityData().getCompound(UtilEntityBuffs.dataEntityBuffed_Data);
        data.putBoolean(getTagName(), true);
        ent.getEntityData().setTag(UtilEntityBuffs.dataEntityBuffed_Data, data);
        return true;
    }

    /**
     * Run after all buffs processed, to apply stuff that might need to happen after other buffs do their initial work
     * Mainly used for when buffs are batch applied like on dice roll or deserialization
     *
     * @param ent
     * @param difficulty
     */
    public void applyBuffPost(CreatureEntity ent, float difficulty) {
        return;
    }

    /**
     * Checks to see if theres some random reason why buff cannot be applied, does not factor in if buff was already applied
     *
     * @param ent
     * @param difficulty
     * @return
     */
    public boolean canApplyBuff(CreatureEntity ent, float difficulty) {
        return true;
    }

    /**
     * Apply buff if entity reloaded from chunk
     * Override this method if the buff requires restoring states on reload, eg AI tasks, but not attributes
     *
     * @param ent
     * @param difficulty
     */
    public void applyBuffFromReload(CreatureEntity ent, float difficulty) {};

    public void applyBuffOnDeath(CreatureEntity ent, float difficulty, LivingDeathEvent event) {};

    public float getMinRequiredDifficulty() {
        return 0;
    }

}

