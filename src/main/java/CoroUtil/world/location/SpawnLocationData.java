package CoroUtil.world.location;

import java.util.UUID;

import net.minecraft.util.ChunkCoordinates;

public class SpawnLocationData {

	public ChunkCoordinates coords;
	public String type;
	public UUID entityUUID;
	
	public SpawnLocationData(ChunkCoordinates parCoords, String parType) {
		coords = parCoords;
		type = parType;
	}
}
