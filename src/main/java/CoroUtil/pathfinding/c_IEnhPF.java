package CoroUtil.pathfinding;




public abstract interface c_IEnhPF 
{

	public void setPathExToEntity(PathEntityEx pathentity);
	//public void setPathToEntity(PathEntity pathentity);
	
	public PathEntityEx getPath();
	public boolean hasPath();
	public void faceCoord(int x, int y, int z, float f, float f1);
	public void noMoveTriggerCallback();
	
	
}