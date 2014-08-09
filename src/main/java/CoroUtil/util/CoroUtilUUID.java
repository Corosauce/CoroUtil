package CoroUtil.util;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class CoroUtilUUID {

	//meant for server side global dimension scan, when to use though? with chunkloading and all this might need to get used a lot... perhaps use a range based one
	//in fact, feed in a list of potentially loadable UUIDs while iterating over a list, for more efficient processing
	private static Entity getEntityFromUUID(World parWorld, long UUIDMost, long UUIDLeast)
    {
		Entity ent = null;
        
		UUID uuid = new UUID(UUIDMost, UUIDLeast);
        List list = parWorld.loadedEntityList;//.getEntitiesWithinAABB(EntityLivingBase.class, this.boundingBox.expand(10.0D, 10.0D, 10.0D));
        Iterator iterator = list.iterator();

        while (iterator.hasNext())
        {
            EntityLivingBase entitylivingbase = (EntityLivingBase)iterator.next();

            if (entitylivingbase.getUniqueID().equals(uuid))
            {
            	ent = entitylivingbase;
                break;
            }
        }
        
        return ent;
        
    }
	
}
