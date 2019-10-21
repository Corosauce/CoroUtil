package CoroUtil.entity.data;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.CreatureEntity;

public class EntityData {

	private CreatureEntity ent;
	
	public EntityData(CreatureEntity ent) {
		this.ent = ent;
	}
	
	@Override
	public int hashCode() {
		return ent.getEntityId();
	}

	public CreatureEntity getEnt() {
		return ent;
	}

	public void setEnt(CreatureEntity ent) {
		this.ent = ent;
	}
	
	public void cleanup() {
		ent = null;
	}

}

