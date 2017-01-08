package CoroUtil.util;

import net.minecraft.entity.EntityLivingBase;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Corosus on 1/7/2017.
 */
public class CoroUtilCrossMod {

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

    public static boolean infernalMobs_AddModifiers(EntityLivingBase ent, String modifiers) {

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
