package CoroAI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.minecraft.src.PathEntity;



public abstract interface c_IEnhPF 
{

	public void setPathExToEntity(PathEntityEx pathentity);
	public void setPathToEntity(PathEntity pathentity);
	
	public PathEntityEx getPath();
	public boolean hasPath();
	public void faceCoord(int x, int y, int z, float f, float f1);
	public void noMoveTriggerCallback();
	
	
}