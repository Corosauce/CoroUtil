package CoroPets;

import java.util.List;

import net.minecraft.entity.EntityCreature;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent.Save;
import CoroUtil.pets.PetsManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EventHandlerForge {

	@SubscribeEvent
	public void deathEvent(LivingDeathEvent event) {
		
	}
	
	@SubscribeEvent
	public void pickupEvent(EntityItemPickupEvent event) {
		
	}
	
	@SubscribeEvent
	public void worldSave(Save event) {
		
		//this is called for every dimension
		
		if (((WorldServer)event.world).provider.dimensionId == 0) {
			
		}
	}
	
	@SubscribeEvent
	public void constructEntity(EntityJoinWorldEvent event) {
		if (event.entity instanceof EntityCreature) {
			EntityCreature creature = (EntityCreature) event.entity;
			
			if (creature.getEntityData().getBoolean(CoroPets.tameString)) {
				PetsManager.instance().hookPetInstanceReloaded(creature);
				//PetsManager.instance().addPet(parOwner, parEnt);
			}
			
			System.out.println(creature.getEntityData());
		}
	}
	
	@SubscribeEvent
	public void unloadChunk(ChunkEvent.Unload event) {
		for (int i = 0; i < 16; i++) {
			List listSubChunk = event.getChunk().entityLists[i];
			for (Object obj : listSubChunk) {
				if (obj instanceof EntityCreature) {
					EntityCreature ent = (EntityCreature) obj;
					if (ent.getEntityData().getBoolean(CoroPets.tameString)) {
						PetsManager.instance().hookPetInstanceUnloaded(ent);
					}
				}
			}
		}
	}
}
