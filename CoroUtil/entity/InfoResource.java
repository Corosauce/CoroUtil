package CoroUtil.entity;


public class InfoResource extends InfoArea
{
	public boolean mined;
	public EnumResource type;
	
	public InfoResource(int x, int y, int z, int id, EnumResource parType) {
		super(x, y, z, id);
		this.type = parType;
	}
	
	public void mine() {
		mined = true;
		blockID = 0;
	}
	
}
