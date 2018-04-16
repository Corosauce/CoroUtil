package CoroUtil.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class TileEntitySacrifice extends TileEntity implements ITickable
{

	@Override
    public void update()
    {
    	
    	if (!world.isRemote) {
    		

    	}
    }

    public NBTTagCompound writeToNBT(NBTTagCompound var1)
    {
        return super.writeToNBT(var1);
    }

    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);

    }

    public void rightClickBlock() {

	}
}
