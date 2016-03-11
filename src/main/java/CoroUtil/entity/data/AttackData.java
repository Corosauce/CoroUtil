package CoroUtil.entity.data;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EntityCreature;

public class AttackData extends EntityData {
	private long lastLogTime;
	private float lastDamage;
	
	private List<Float> listDPSs = new ArrayList<Float>();
	
	public AttackData(EntityCreature ent) {
		super(ent);
	}

	public long getLastLogTime() {
		return lastLogTime;
	}

	public void setLastLogTime(long lastLogTime) {
		this.lastLogTime = lastLogTime;
	}

	public float getLastDamage() {
		return lastDamage;
	}

	public void setLastDamage(float lastDamage) {
		this.lastDamage = lastDamage;
	}

	public List<Float> getListDPSs() {
		return listDPSs;
	}

	public void setListDPSs(List<Float> listDPSs) {
		this.listDPSs = listDPSs;
	}
	
	public void cleanup() {
		super.cleanup();
		listDPSs.clear();
	}
	
}
