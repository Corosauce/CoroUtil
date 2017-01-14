package CoroUtil.util;

import net.minecraft.entity.EntityLivingBase;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * TODO: respect infernal mobs config of specifically disabled modifiers, and maybe some other things?
 *
 * Created by Corosus on 1/7/2017.
 */
public class CoroUtilCrossMod {

    private static boolean checkHasInfernalMobs = true;
    private static boolean hasInfernalMobs = false;

    public static List<String> listModifiers = new ArrayList<>();

    static {
        listModifiers.add("1UP");
        listModifiers.add("Alchemist");
        listModifiers.add("Berserk");
        listModifiers.add("Blastoff");
        listModifiers.add("Bulwark");
        listModifiers.add("Choke");
        listModifiers.add("Cloaking");
        listModifiers.add("Darkness");
        listModifiers.add("Ender");
        listModifiers.add("Exhaust");
        listModifiers.add("Fiery");
        listModifiers.add("Ghastly");
        listModifiers.add("Gravity");
        listModifiers.add("LifeSteal");
        listModifiers.add("Ninja");
        listModifiers.add("Poisonous");
        listModifiers.add("Quicksand");
        listModifiers.add("Regen");
        listModifiers.add("Rust");
        listModifiers.add("Sapper");
        listModifiers.add("Sprint");
        listModifiers.add("Sticky");
        listModifiers.add("Storm");
        listModifiers.add("Vengeance");
        listModifiers.add("Weakness");
        listModifiers.add("Webber");
        listModifiers.add("Wither");

    }

    /**
     * Check if infernal mobs mod is loaded, cache result
     *
     * @return
     */
    public static boolean hasInfernalMobs() {
        if (!checkHasInfernalMobs) {
            return hasInfernalMobs;
        } else {
            checkHasInfernalMobs = false;
            try {
                Class clazz = Class.forName("atomicstryker.infernalmobs.common.InfernalMobsCore");
                if (clazz != null) {
                    hasInfernalMobs = true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return hasInfernalMobs;
        }
    }

    public static boolean infernalMobs_AddRandomModifiers(EntityLivingBase ent, int modifierCount) {
        String listMods = "";

        if (modifierCount >= listModifiers.size()) {
            modifierCount = listModifiers.size() - 1;
        }

        List<Integer> listInts = new ArrayList<>();
        for (int i = 0; i < listModifiers.size(); i++) { listInts.add(i); }
        Collections.shuffle(listInts);

        for (int i = 0; i < modifierCount; i++) {
            listMods += listModifiers.get(listInts.get(i)) + " ";
        }

        if (!listMods.equals("")) {
            return infernalMobs_AddModifiers(ent, listMods);
        } else {
            //didnt fail, just nothing to apply because difficulty too low
            return true;
        }
    }

    public static boolean infernalMobs_AddModifiers(EntityLivingBase ent, String modifiers) {

        if (!hasInfernalMobs()) return false;

        /**
         *
         * need to do this:
         * InfernalMobsCore.proxy.getRareMobs().remove(mob);
         * InfernalMobsCore.instance().addEntityModifiersByString(mob, modifier);
         *
         */

        try {
            Class clazz = Class.forName("atomicstryker.infernalmobs.common.InfernalMobsCore");
            if (clazz != null) {
                Method method = clazz.getDeclaredMethod("instance");
                Object obj = method.invoke(null);
                Method methodMods = obj.getClass().getDeclaredMethod("addEntityModifiersByString", EntityLivingBase.class, String.class);
                methodMods.invoke(obj, ent, modifiers);
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return false;
    }

}
