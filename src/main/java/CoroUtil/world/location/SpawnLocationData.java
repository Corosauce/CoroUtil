package CoroUtil.world.location;

import java.util.UUID;

import CoroUtil.util.BlockCoord;

public class SpawnLocationData {

	public BlockCoord coords;
	public String type;
	public UUID entityUUID;
	
	public SpawnLocationData(BlockCoord parCoords, String parType) {
		coords = parCoords;
		type = parType;
	}
}
