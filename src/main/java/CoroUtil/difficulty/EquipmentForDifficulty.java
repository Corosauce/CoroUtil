package CoroUtil.difficulty;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;

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
    private List<Potion> listPotions;

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

    public List<Potion> getListPotions() {
        return listPotions;
    }

    public void setListPotions(List<Potion> listPotions) {
        this.listPotions = listPotions;
    }

    public EntityEquipmentSlot getSlotForSlotID(int ID) {
        if (ID == 0) return EntityEquipmentSlot.HEAD;
        if (ID == 1) return EntityEquipmentSlot.CHEST;
        if (ID == 2) return EntityEquipmentSlot.LEGS;
        if (ID == 3) return EntityEquipmentSlot.FEET;
        return null;
    }

}
