package CoroUtil.entity.projectile;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import CoroUtil.entity.EntityThrowableUsefull;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extendedrenderer.particle.behavior.ParticleBehaviors;

public class EntityProjectileBase extends EntityThrowableUsefull implements IEntityAdditionalSpawnData {

	public int projectileType = 0;
	
	public static int PRJTYPE_FIREBALL = 0;
	public static int PRJTYPE_ICEBALL = 1;
	public static int PRJTYPE_ = 2;
	
	@SideOnly(Side.CLIENT)
	public ParticleBehaviors particleBehavior;
	
	public boolean tickParticleBehaviorList = true;
	
	public EntityProjectileBase(World world)
	{
		super(world);
	}
	
	public EntityProjectileBase(World world, EntityLivingBase entityliving, double parSpeed)
	{
		super(world, entityliving, parSpeed);
	}

	public EntityProjectileBase(World par1World, EntityLivingBase par2EntityLivingBase, EntityLivingBase target, double parSpeed)
    {
		super(par1World, par2EntityLivingBase, target, parSpeed);
    }

	public EntityProjectileBase(World world, double d, double d1, double d2)
	{
		super(world, d, d1, d2);
	}

	@Override
	protected void onImpact(MovingObjectPosition movingobjectposition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeSpawnData(ByteBuf data) {
		data.writeInt(projectileType);
	}

	@Override
	public void readSpawnData(ByteBuf data) {
		projectileType = data.readInt();
	}
	
	@Override
	public void onUpdate() {
		
		if (tickParticleBehaviorList) {
			if (particleBehavior != null) {
				particleBehavior.tickUpdateList();
			}
		}
		
		super.onUpdate();
	}

}
