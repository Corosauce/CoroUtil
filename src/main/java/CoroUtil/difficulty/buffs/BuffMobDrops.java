package CoroUtil.difficulty.buffs;

import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.difficulty.data.DifficultyDataReader;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

/**
 * Created by Corosus on 1/9/2017.
 */
public class BuffMobDrops extends BuffBase {

    @Override
    public String getTagName() {
        return UtilEntityBuffs.dataEntityBuffed_MobDrops;
    }

    @Override
    public boolean applyBuff(EntityCreature ent, float difficulty) {
        return super.applyBuff(ent, difficulty);
    }

    @Override
    public void applyBuffFromReload(EntityCreature ent, float difficulty) {
        applyBuff(ent, difficulty);
    }

    @Override
    public void applyBuffOnDeath(EntityCreature ent, float difficulty, LivingDeathEvent event) {
        LootTable loot = UtilEntityBuffs.getRandomLootForDifficulty(ent, difficulty);
        if (loot != null) {
            UtilEntityBuffs.processLootTableOnEntity(ent, loot, event);
        }
    }
}
