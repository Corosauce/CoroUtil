package CoroUtil.difficulty;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;

import java.util.List;

/**
 * Created by Corosus on 1/9/2017.
 */
public class EquipmentForDifficulty {

    //ordered head to toe
    private List<ItemStack> listArmor;
    private ItemStack weapon;
    private ItemStack weaponOffhand;
    //unused for now, worth considering in future
    private List<Effect> listPotions;

    public EquipmentForDifficulty() {

    }

    public List<ItemStack> getListArmor() {
        return listArmor;
    }

    public void setListArmor(List<ItemStack> listArmor) {
        this.listArmor = listArmor;
    }

    public ItemStack getWeapon() {
        return weapon;
    }

    public void setWeapon(ItemStack weapon) {
        this.weapon = weapon;
    }

    public ItemStack getWeaponOffhand() {
        return weaponOffhand;
    }

    public void setWeaponOffhand(ItemStack weaponOffhand) {
        this.weaponOffhand = weaponOffhand;
    }

    public List<Effect> getListPotions() {
        return listPotions;
    }

    public void setListPotions(List<Effect> listPotions) {
        this.listPotions = listPotions;
    }

    public EquipmentSlotType getSlotForSlotID(int ID) {
        if (ID == 0) return EquipmentSlotType.HEAD;
        if (ID == 1) return EquipmentSlotType.CHEST;
        if (ID == 2) return EquipmentSlotType.LEGS;
        if (ID == 3) return EquipmentSlotType.FEET;
        return null;
    }

}
