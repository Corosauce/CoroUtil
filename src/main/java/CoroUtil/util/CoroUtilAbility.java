package CoroUtil.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.nbt.NBTTagCompound;
import CoroUtil.ability.Ability;

public class CoroUtilAbility {

	public static NBTTagCompound nbtSyncWriteAbility(String abilityName, ConcurrentHashMap<String, Ability> abilities, boolean fullSync) {
		NBTTagCompound nbt = new NBTTagCompound();
		Ability ability = abilities.get(abilityName);
		if (ability != null) {
			nbt = nbtSyncWriteAbility(ability, fullSync);
		} else {
			System.out.println("Error: failed to find ability to sync: " + abilityName);
		}
		return nbt;
	}
	
	public static NBTTagCompound nbtSyncWriteAbility(Ability ability, boolean fullSync) {
		NBTTagCompound nbt = new NBTTagCompound();
		if (fullSync) {
			nbt.setTag(ability.name, ability.nbtSave());
		} else {
			nbt.setTag(ability.name, ability.nbtSyncWrite());
		}
		return nbt;
	}
	
	public static NBTTagCompound nbtSyncWriteAbilities(ConcurrentHashMap<String, Ability> abilities) {
		return nbtWriteAbilities(abilities, true);
	}
	
	public static NBTTagCompound nbtSaveAbilities(ConcurrentHashMap<String, Ability> abilities) {
		return nbtWriteAbilities(abilities, false);
	}
	
	public static NBTTagCompound nbtWriteAbilities(ConcurrentHashMap<String, Ability> abilities, boolean syncOnly) {
		NBTTagCompound nbt = new NBTTagCompound();
		
		for (Map.Entry<String, Ability> entry : abilities.entrySet()) {
			if (syncOnly) {
				nbt.setTag(entry.getValue().name, entry.getValue().nbtSyncWrite());
			} else {
				nbt.setTag(entry.getValue().name, entry.getValue().nbtSave());
			}
		}
		return nbt;
	}
	
}
