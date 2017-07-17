package CoroUtil.pathfinding;

import net.minecraft.util.math.MathHelper;

public class PathPointEx
{
    public final int xCoord;
    public final int yCoord;
    public final int zCoord;
    public final int hash;
    public int index;
    public float totalPathDistance;
    public float distanceToNext;
    public float distanceToTarget;
    public PathPointEx previous;
    public boolean isFirst;

    public PathPointEx(int i, int j, int k)
    {
        index = -1;
        isFirst = false;
        xCoord = i;
        yCoord = j;
        zCoord = k;
        hash = makeHash(i, j, k);
    }

    public static int makeHash(int i, int j, int k)
    {
        return j & 0xff | (i & 0x7fff) << 8 | (k & 0x7fff) << 24 | (i >= 0 ? 0 : 0x80000000) | (k >= 0 ? 0 : 0x8000);
    }

    public float distanceTo(PathPointEx pathpoint)
    {
        float f = pathpoint.xCoord - xCoord;
        float f1 = pathpoint.yCoord - yCoord;
        float f2 = pathpoint.zCoord - zCoord;
        return MathHelper.sqrt(f * f + f1 * f1 + f2 * f2);
    }

    public boolean equals(Object obj)
    {
        if (obj instanceof PathPointEx)
        {
            PathPointEx pathpoint = (PathPointEx)obj;
            return hash == pathpoint.hash && xCoord == pathpoint.xCoord && yCoord == pathpoint.yCoord && zCoord == pathpoint.zCoord;
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return hash;
    }

    public boolean isAssigned()
    {
        return index >= 0;
    }

    public String toString()
    {
        return (new StringBuilder()).append(xCoord).append(", ").append(yCoord).append(", ").append(zCoord).toString();
    }
}
