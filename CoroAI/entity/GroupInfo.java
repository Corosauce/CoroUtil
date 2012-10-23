package CoroAI.entity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.src.Block;

public class GroupInfo 
{
	public static GroupInfo i = new GroupInfo();
	
	public static List<InfoResource> resources;
	
	public GroupInfo() {
		i = this;
		resources = new ArrayList();
		
		
	}
	
	public static InfoResource addResource(int x, int y, int z, EnumResource parType) {
		int id = 0;
		if (parType == EnumResource.WOOD) id = Block.wood.blockID;
		
		InfoResource ia = new InfoResource(x, y, z, id, parType);
		resources.add(ia);
		return ia;
	}
	
	public static InfoArea getFirstResource(EnumResource type) {
		for (int i = 0; i < resources.size(); i++) {
			if (resources.get(i).type == type) {
				return resources.get(i);
			}
		}
		return null;
	}
}
