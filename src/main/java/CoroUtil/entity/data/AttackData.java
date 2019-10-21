package CoroUtil.entity.data;

import java.util.ArrayList;
import java.util.List;

import CoroUtil.difficulty.DamageSourceEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;

public class AttackData extends EntityData {
	private long lastLogTime;
	private float lastDamage;
	
	private List<Float> listDPSs = new ArrayList<Float>();

	private Entity source_entityTrue = null;
	private Entity source_entityImmediate = null;
	private String source_type = "";

	public DamageSourceEntry highestDamage;
	
	public AttackData(CreatureEntity ent) {
		super(ent);
		highestDamage = new DamageSourceEntry();
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
		if (source_type == null) {
			source_type = "<NULL>";
		}
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

	public Entity getSource_entityTrue() {
		return source_entityTrue;
	}

	public void setSource_entityTrue(Entity source_entityTrue) {
		this.source_entityTrue = source_entityTrue;
	}

	public Entity getSource_entityImmediate() {
		return source_entityImmediate;
	}

	public void setSource_entityImmediate(Entity source_entityImmediate) {
		this.source_entityImmediate = source_entityImmediate;
	}

	public String getSource_type() {
		return source_type;
	}

	public void setSource_type(String source_type) {
		this.source_type = source_type;
	}

	public void cleanup() {
		super.cleanup();
		listDPSs.clear();
	}
	
}

