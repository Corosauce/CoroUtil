package CoroUtil.util;

import java.util.Random;


public class CoroUtilParticle {

    public static Vec3[] rainPositions;
    public static int maxRainDrops = 200;
    
    public static Random rand = new Random();
    
    static {
    	rainPositions = new Vec3[maxRainDrops];
        
        float range = 20F;
        
        for (int i = 0; i < maxRainDrops; i++) {
        	rainPositions[i] = new Vec3((rand.nextFloat() * range) - (range/2), (rand.nextFloat() * range/16) - (range/32), (rand.nextFloat() * range) - (range/2));
        }
    }
	
}
