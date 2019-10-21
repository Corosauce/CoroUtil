package CoroUtil.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompoundNBT;

public interface ISerializableNBT {

	public void readFromNBT(CompoundNBT nbt);
	
	public CompoundNBT writeToNBT(CompoundNBT nbt);
	
}
