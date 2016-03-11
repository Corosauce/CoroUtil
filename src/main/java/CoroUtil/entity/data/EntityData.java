package CoroUtil.entity.data;

import net.minecraft.entity.EntityCreature;

public class EntityData {

	private EntityCreature ent;
	
	public EntityData(EntityCreature ent) {
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
	
	public void cleanup() {
		ent = null;
	}

}
