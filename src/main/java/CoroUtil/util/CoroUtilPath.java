package CoroUtil.util;

import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.ChunkCoordinates;

public class CoroUtilPath {

	public static PathEntity getSingleNodePath(ChunkCoordinates coords) {
		PathPoint points[] = new PathPoint[1];
        points[0] = new PathPoint(coords.posX, coords.posY, coords.posZ);
		PathEntity pe = new PathEntity(points);
		return pe;
	}
	
}
