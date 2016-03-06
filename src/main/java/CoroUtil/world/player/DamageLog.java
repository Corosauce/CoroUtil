package CoroUtil.world.player;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EntityCreature;

public class DamageLog {

	private EntityCreature ent;
	
	private float totalDamage;
	private int totalTime;
	
	private long lastLogTime;
	private float lastDamage;
	
	private List<Float> listDPSs = new ArrayList<Float>();
	
	public DamageLog(EntityCreature ent) {
		this.ent = ent;
	}
	
	@Override
	public int hashCode() {
		return ent.getEntityId();
	}

	public EntityCreature getEnt() {
		return ent;
	}

	public void setEnt(EntityCreature ent) {
		this.ent = ent;
	}

	public float getTotalDamage() {
		return totalDamage;
	}

	public void setTotalDamage(float totalDamage) {
		this.totalDamage = totalDamage;
	}

	public int getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(int totalTime) {
		this.totalTime = totalTime;
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
		ent = null;
		listDPSs.clear();
	}
	
}
