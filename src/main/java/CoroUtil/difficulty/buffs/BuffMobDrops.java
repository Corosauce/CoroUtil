package CoroUtil.difficulty.buffs;

import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.difficulty.data.DifficultyDataReader;
import CoroUtil.difficulty.data.cmods.CmodMobDrops;
import CoroUtil.forge.CULog;
import net.minecraft.entity.EntityCreature;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

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
        CmodMobDrops cmod = (CmodMobDrops)UtilEntityBuffs.getCmodData(ent, getTagName());

        if (cmod != null) {

            LootTable loot = DifficultyDataReader.getData().lookupLootTables.get(cmod.loot_table);

            //if no coroutil based loot table found, see if its a vanilla loot table, or maybe some other mods loot table
            if (loot == null) {
                loot = ent.world.getLootTableManager().getLootTableFromLocation(new ResourceLocation(cmod.loot_table));
            }

            if (loot != null) {
                UtilEntityBuffs.processLootTableOnEntity(ent, loot, event);
            } else {
                CULog.err("couldnt find loot table: " + cmod.loot_table);
            }
        } else {
            CULog.dbg("couldnt get cmod mod drops data");
        }
    }
}
