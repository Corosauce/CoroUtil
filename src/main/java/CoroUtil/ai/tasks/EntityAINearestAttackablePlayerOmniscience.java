package CoroUtil.ai.tasks;

import CoroUtil.ai.IInvasionControlledTask;
import CoroUtil.ai.ITaskInitializer;
import CoroUtil.forge.CULog;
import com.google.common.base.Predicate;
import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntitySelectors;

public class EntityAINearestAttackablePlayerOmniscience<T extends EntityLivingBase> extends EntityAITargetBetter implements ITaskInitializer, IInvasionControlledTask
{
    protected Class<EntityPlayer> targetClass;
    private int targetChance;
    /** Instance of EntityAINearestAttackableTargetSorter. */
    protected EntityAINearestAttackablePlayerOmniscience.Sorter sorter;
    protected Predicate <? super T > targetEntitySelector;
    protected EntityPlayer targetEntity;

    private boolean disableAtSunrise = true;

    public EntityAINearestAttackablePlayerOmniscience() {
        shouldCheckSight = false;
        nearbyOnly = false;
        this.targetClass = EntityPlayer.class;
        this.targetChance = 40;
        this.setMutexBits(0);
        this.targetEntitySelector = new Predicate<T>()
        {
            public boolean apply(@Nullable T p_apply_1_)
            {
                if (p_apply_1_ == null)
                {
                    return false;
                }
                else
                {
                    return !EntitySelectors.NOT_SPECTATING.apply(p_apply_1_) ? false : true;
                }
            }
        };
    }

    public EntityAINearestAttackablePlayerOmniscience(EntityCreature creature, boolean checkSight)
    {
        this(creature, checkSight, false);
    }

    public EntityAINearestAttackablePlayerOmniscience(EntityCreature creature, boolean checkSight, boolean onlyNearby)
    {
        this(creature, 10, checkSight, onlyNearby, (Predicate)null);
    }

    public EntityAINearestAttackablePlayerOmniscience(EntityCreature creature, int chance, boolean checkSight, boolean onlyNearby, @Nullable final Predicate <? super T > targetSelector)
    {
        super(creature, checkSight, onlyNearby);
        this.targetClass = EntityPlayer.class;
        this.targetChance = chance;
        this.sorter = new EntityAINearestAttackablePlayerOmniscience.Sorter(creature);
        this.setMutexBits(1);
        this.targetEntitySelector = new Predicate<T>()
        {
            public boolean apply(@Nullable T p_apply_1_)
            {
                if (p_apply_1_ == null)
                {
                    return false;
                }
                else if (targetSelector != null && !targetSelector.apply(p_apply_1_))
                {
                    return false;
                }
                else
                {
                    return !EntitySelectors.NOT_SPECTATING.apply(p_apply_1_) ? false : true/*EntityAINearestAttackablePlayerOmniscience.this.isSuitableTarget(p_apply_1_, false)*/;
                }
            }
        };
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute()
    {

        if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0)
        {
            return false;
        }
        else
        {
            this.targetEntity = this.taskOwner.world.getNearestAttackablePlayer(
                    this.taskOwner.posX, this.taskOwner.posY + (double)this.taskOwner.getEyeHeight(), this.taskOwner.posZ,
                    this.getTargetDistance(), this.getTargetDistance(), null, (Predicate<EntityPlayer>)this.targetEntitySelector);
            return this.targetEntity != null;
        }
    }

    @Override
    protected double getTargetDistance() {
        return 9999;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting()
    {
        this.taskOwner.setAttackTarget(this.targetEntity);
        super.startExecuting();
    }

    @Override
    public void setEntity(EntityCreature creature) {
        this.taskOwner = creature;
        this.sorter = new EntityAINearestAttackablePlayerOmniscience.Sorter(creature);
    }

    @Override
    public boolean shouldBeRemoved() {
        if (disableAtSunrise) {
            //once its day, disable forever
            if (this.taskOwner.world.isDaytime()) {
                CULog.dbg("removing omniscience");
                //also detarget
                if (this.taskOwner.getAttackTarget() instanceof EntityPlayer) {
                    this.taskOwner.setAttackTarget(null);
                }
                return true;
                //taskActive = false;
            }
        }

        return false;
    }

    public static class Sorter implements Comparator<Entity>
        {
            private final Entity entity;

            public Sorter(Entity entityIn)
            {
                this.entity = entityIn;
            }

            public int compare(Entity p_compare_1_, Entity p_compare_2_)
            {
                double d0 = this.entity.getDistanceSqToEntity(p_compare_1_);
                double d1 = this.entity.getDistanceSqToEntity(p_compare_2_);

                if (d0 < d1)
                {
                    return -1;
                }
                else
                {
                    return d0 > d1 ? 1 : 0;
                }
            }
        }
}