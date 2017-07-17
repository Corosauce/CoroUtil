package CoroUtil.inventory;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ReportedException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class InventoryWrapper implements IInventory {

	public ItemStack[] invList;
	public boolean inventoryChanged = true; //requires outside watcher to set to false after it notices this true
	public String username;

	public void invInitData(NBTTagList stacks, int bufferSize) {
		invList = new ItemStack[bufferSize];
		for (int i = 0; i < stacks.tagCount(); i++) {
            NBTTagCompound tag = stacks.getCompoundTagAt(i);
            byte slot = tag.getByte("Slot");
            if (slot >= 0 && slot < invList.length) {
            	invList[slot] = new ItemStack(tag);
            }
		}
	}

	public void invInitData(ItemStack[] stacks) {
		invList = stacks;
	}

	public NBTTagList invWriteTagList() {
		NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < invList.length; i++) {
            ItemStack stack = invList[i];
            if (stack != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }
		return itemList;
	}

	@Override
	public int getSizeInventory() {
		return invList.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		if (i < invList.length) {
			return invList[i];
		} else {
			return null;
		}
	}

	@Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        invList[slot] = stack;
        if (stack != null && stack.getCount() > getInventoryStackLimit()) {
                stack.setCount(getInventoryStackLimit());
        }
    }

    @Override
    public ItemStack decrStackSize(int slot, int amt) {
        ItemStack stack = getStackInSlot(slot);
        if (stack != null) {
                if (stack.getCount() <= amt) {
                        setInventorySlotContents(slot, null);
                } else {
                        stack = stack.splitStack(amt);
                        if (stack.getCount() == 0) {
                                setInventorySlotContents(slot, null);
                        }
                }
        }
        return stack;
    }

    /*@Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = getStackInSlot(slot);
        if (stack != null) {
                setInventorySlotContents(slot, null);
        }
        return stack;
    }*/

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}

	@Override
	public void markDirty() {
		inventoryChanged = true;
	}



	public boolean addItemStackToInventory(final ItemStack p_70441_1_)
    {
        if (p_70441_1_ != null && p_70441_1_.getCount() != 0 && p_70441_1_.getItem() != null)
        {
            try
            {
                int i;

                if (p_70441_1_.isItemDamaged())
                {
                    i = this.getFirstEmptyStack();

                    if (i >= 0)
                    {
                        this.invList[i] = p_70441_1_.copy();
                        this.invList[i].setAnimationsToGo(5);
                        p_70441_1_.setCount(0);
                        return true;
                    }
                    /*else if (this.player.capabilities.isCreativeMode)
                    {
                        p_70441_1_.getCount() = 0;
                        return true;
                    }*/
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    do
                    {
                        i = p_70441_1_.getCount();
                        p_70441_1_.setCount(this.storePartialItemStack(p_70441_1_));
                    }
                    while (p_70441_1_.getCount() > 0 && p_70441_1_.getCount() < i);
                    /*if (p_70441_1_.getCount() == i && this.player.capabilities.isCreativeMode)

                    {
                        p_70441_1_.getCount() = 0;
                        return true;
                    }
                    else
                    {*/
                        return p_70441_1_.getCount() < i;
                    //}
                }
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Adding item to inventory");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being added");
                crashreportcategory.addCrashSection("Item ID", Integer.valueOf(Item.getIdFromItem(p_70441_1_.getItem())));
                crashreportcategory.addCrashSection("Item data", Integer.valueOf(p_70441_1_.getItemDamage()));
                /*crashreportcategory.addCrashSectionCallable("Item name", new Callable()
                {
                    private static final String __OBFID = "CL_00001710";
                    public String call()
                    {
                        return p_70441_1_.getDisplayName();
                    }
                });*/
                throw new ReportedException(crashreport);
            }
        }
        else
        {
            return false;
        }
    }

	public int getFirstEmptyStack()
    {
        for (int i = 0; i < this.invList.length; ++i)
        {
            if (this.invList[i] == null)
            {
                return i;
            }
        }

        return -1;
    }

	private int storeItemStack(ItemStack p_70432_1_)
    {
        for (int i = 0; i < this.invList.length; ++i)
        {
            if (this.invList[i] != null && this.invList[i].getItem() == p_70432_1_.getItem() && this.invList[i].isStackable() && this.invList[i].getCount() < this.invList[i].getMaxStackSize() && this.invList[i].getCount() < this.getInventoryStackLimit() && (!this.invList[i].getHasSubtypes() || this.invList[i].getItemDamage() == p_70432_1_.getItemDamage()) && ItemStack.areItemStackTagsEqual(this.invList[i], p_70432_1_))
            {
                return i;
            }
        }

        return -1;
    }

	private int storePartialItemStack(ItemStack p_70452_1_)
    {
        Item item = p_70452_1_.getItem();
        int i = p_70452_1_.getCount();
        int j;

        if (p_70452_1_.getMaxStackSize() == 1)
        {
            j = this.getFirstEmptyStack();

            if (j < 0)
            {
                return i;
            }
            else
            {
                if (this.invList[j] == null)
                {
                    this.invList[j] = p_70452_1_.copy();
                }

                return 0;
            }
        }
        else
        {
            j = this.storeItemStack(p_70452_1_);

            if (j < 0)
            {
                j = this.getFirstEmptyStack();
            }

            if (j < 0)
            {
                return i;
            }
            else
            {
                if (this.invList[j] == null)
                {
                    this.invList[j] = new ItemStack(item, 0, p_70452_1_.getItemDamage());

                    if (p_70452_1_.hasTagCompound())
                    {
                        this.invList[j].setTagCompound((NBTTagCompound)p_70452_1_.getTagCompound().copy());
                    }
                }

                int k = i;

                if (i > this.invList[j].getMaxStackSize() - this.invList[j].getCount())
                {
                    k = this.invList[j].getMaxStackSize() - this.invList[j].getCount();
                }

                if (k > this.getInventoryStackLimit() - this.invList[j].getCount())
                {
                    k = this.getInventoryStackLimit() - this.invList[j].getCount();
                }

                if (k == 0)
                {
                    return i;
                }
                else
                {
                    i -= k;
                    this.invList[j].setCount(this.invList[j].getCount() + k);
                    this.invList[j].setAnimationsToGo(5);
                    return i;
                }
            }
        }
    }

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean hasCustomName() {
		return true;
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString("Inventory Wrapper");
	}

	@Override
	public void openInventory(EntityPlayer player) {

	}

	@Override
	public void closeInventory(EntityPlayer player) {

	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {

	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {

	}

	//TODO: 1.8 added
	@Override
	public ItemStack removeStackFromSlot(int index) {
		return null;
	}

    @Override
    public boolean isEmpty()
    {
        for (ItemStack itemstack : this.invList)
        {
            if (!itemstack.isEmpty())
            {
                return false;
            }
        }

        return true;
    }
}
