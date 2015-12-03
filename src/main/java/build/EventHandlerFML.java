package build;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

public class EventHandlerFML {
	
	@SubscribeEvent
	public void tickServer(ServerTickEvent event) {
		
		if (event.phase == Phase.START) {
			BuildServerTicks.onTickInGame();
		}
		
	}
	
	@SubscribeEvent
	public void tickRender(RenderTickEvent event) {
		if (FMLClientHandler.instance().getClient().thePlayer != null) {
			if (event.phase == Phase.END) {
				BuildClientTicks.i.onRenderTick();
			}
		}
	}
	
	@SubscribeEvent
	public void tickClient(ClientTickEvent event) {
		if (event.phase == Phase.END) {
			BuildClientTicks.i.onTickInGame();
		}
	}
}
