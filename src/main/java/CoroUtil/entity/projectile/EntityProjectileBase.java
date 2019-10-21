package CoroUtil.entity.projectile;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import CoroUtil.entity.EntityThrowableUsefull;
import extendedrenderer.particle.behavior.ParticleBehaviors;

public class EntityProjectileBase extends EntityThrowableUsefull implements IEntityAdditionalSpawnData {

	public int projectileType = 0;
	
	public static int PRJTYPE_FIREBALL = 0;
	public static int PRJTYPE_ICEBALL = 1;
	public static int PRJTYPE_ = 2;
	
	@OnlyIn(Dist.CLIENT)
	public ParticleBehaviors particleBehavior;
	
	public boolean tickParticleBehaviorList = true;
	
	public EntityProjectileBase(World world)
	{
		super(world);
	}
	
	public EntityProjectileBase(World world, LivingEntity entityliving, double parSpeed)
	{
		super(world, entityliving, parSpeed);
	}

	public EntityProjectileBase(World par1World, LivingEntity par2EntityLivingBase, LivingEntity target, double parSpeed)
    {
		super(par1World, par2EntityLivingBase, target, parSpeed);
    }

	public EntityProjectileBase(World world, double d, double d1, double d2)
	{
		super(world, d, d1, d2);
	}

	@Override
	protected void onImpact(RayTraceResult movingobjectposition) {
		super.onImpact(movingobjectposition);

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
	public void tick() {
		
		if (tickParticleBehaviorList) {
			if (particleBehavior != null) {
				particleBehavior.tickUpdateList();
			}
		}
		
		super.tick();
	}
	
	@Override
	public void readAdditional(CompoundNBT par1nbtTagCompound) {
		super.readAdditional(par1nbtTagCompound);
		
		//kill on reload
		remove();
	}

}

