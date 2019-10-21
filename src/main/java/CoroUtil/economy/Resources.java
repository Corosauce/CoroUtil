package CoroUtil.economy;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompoundNBT;

import org.apache.commons.lang3.mutable.MutableInt;

public class Resources {

	//to be used for entities cargo, buildings own storage, and town economy storage
	
	public MutableInt resWood = new MutableInt();
	public MutableInt resStone = new MutableInt();
	public MutableInt resFood = new MutableInt();
	public MutableInt resCoal = new MutableInt();
	public MutableInt resIron = new MutableInt();
	
	public List<MutableInt> listResources = new ArrayList<MutableInt>();
	
	//relocate to some sort of generic statistical observer class that AI references
	public MutableInt resWoodRate = new MutableInt();
	
	public Resources(Resources cloneSource) {
		this();
		for (int i = 0; i < listResources.size(); i++) {
			listResources.get(i).setValue(cloneSource.listResources.get(i));
		}
	}
	
	public Resources(int... parResources) {
		this();
		for (int i = 0; i < parResources.length; i++) {
			listResources.get(i).setValue(parResources[i]);
		}
	}
	
	public Resources() {
		//resWood.setValue(200);
		listResources.add(resWood);
		listResources.add(resStone);
		listResources.add(resFood);
		listResources.add(resCoal);
		listResources.add(resIron);
	}
	
	//fed its own component
	public void read(CompoundNBT data) {
		resWood.setValue(data.getInt("resWood"));
		resStone.setValue(data.getInt("resStone"));
		resFood.setValue(data.getInt("resFood"));
	}
	
	//write its own component
	public CompoundNBT write() {
		CompoundNBT data = new CompoundNBT();
		data.putInt("resWood", resWood.get());
		data.putInt("resStone", resStone.get());
		data.putInt("resFood", resFood.get());
		return data;
	}
	
	public int getTotalResourceCount() {
		int val = 0;
		for (int i = 0; i < listResources.size(); i++) {
			val += listResources.get(i).get();
		}
		return val;
	}
	
	public void transferResourcesFrom(Resources source) {
		transferResourcesFrom(source, false);
	}
	
	public void transferResourcesFrom(Resources source, boolean drainSourceOnly) {
		for (int i = 0; i < listResources.size(); i++) {
			if (!drainSourceOnly) listResources.get(i).add(source.listResources.get(i).get());
			source.listResources.get(i).setValue(0);
		}
	}
	
	public void sub(Resources source) {
		for (int i = 0; i < listResources.size(); i++) {
			listResources.get(i).setValue(listResources.get(i).get() - source.listResources.get(i).get());
		}
	}
	
	public void mul(float mul) {
		for (int i = 0; i < listResources.size(); i++) {
			listResources.get(i).setValue(listResources.get(i).get() * mul);
		}
	}
	
	public boolean canAfford(Resources source) {
		for (int i = 0; i < listResources.size(); i++) {
			if (listResources.get(i).get() < source.listResources.get(i).get()) return false;
		}
		return true;
	}
	
}

