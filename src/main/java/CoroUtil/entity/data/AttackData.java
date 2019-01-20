package CoroUtil.entity.data;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.util.DamageSource;

public class AttackData extends EntityData {
	private long lastLogTime;
	private float lastDamage;
	
	private List<Float> listDPSs = new ArrayList<Float>();

	private Entity source_entityTrue = null;
	private Entity source_entityImmediate = null;
	private String source_type = "";
	//private
	
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

	public void trackSources(DamageSource source) {
		source_entityTrue = source.getTrueSource();
		source_type = source.getDamageType();
		source_entityImmediate = source.getImmediateSource();
	}

	public boolean isSameSource(DamageSource source) {
		if (!source.getDamageType().equals(source_type)) {
			return false;
		}

		if (source.getTrueSource() != source_entityTrue) {
			return false;
		}

		if (source.getImmediateSource() != source_entityImmediate) {
			return false;
		}

		return true;
	}
	
	public void cleanup() {
		super.cleanup();
		listDPSs.clear();
	}
	
}
