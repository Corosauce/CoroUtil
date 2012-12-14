package CoroAI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.minecraft.src.Entity;

import CoroAI.*;

public abstract interface c_IEnhAI
{
	public float getMoveSpeed();
	//public int health = 0;
	public boolean canUseLadders();
	
	//public boolean isPathableBlock(c_IEnhAI ent, int id, int meta, int x, int y, int z);
	//public void pfComplete(PathEntityEx pathEx);
	
	public int overrideBlockPathOffset(c_IEnhAI ent, int id, int meta, int x, int y, int z);
	
}