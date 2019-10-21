package CoroUtil.ai.tasks;

import CoroUtil.ai.IInvasionControlledTask;
import CoroUtil.ai.ITaskInitializer;
import CoroUtil.config.ConfigCoroUtilAdvanced;
import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.forge.CULog;
import com.google.common.base.Predicate;
import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.EntityPredicates;

public class EntityAINearestAttackablePlayerOmniscience<T extends LivingEntity> extends EntityAITargetBetter implements ITaskInitializer, IInvasionControlledTask
{
    protected Class<PlayerEntity> targetClass;
    private int targetChance;
    /** Instance of EntityAINearestAttackableTargetSorter. */
    protected EntityAINearestAttackablePlayerOmniscience.Sorter sorter;
    protected Predicate <? super T > targetEntitySelector;
    protected PlayerEntity targetEntity;

    private boolean disableAtSunrise = true;

    //needed for generic instantiation
    public EntityAINearestAttackablePlayerOmniscience() {
        shouldCheckSight = false;
        nearbyOnly = false;
        this.targetClass = PlayerEntity.class;
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
                    return !EntityPredicates.NOT_SPECTATING.apply(p_apply_1_) ? false : EntityAINearestAttackablePlayerOmniscience.this.isPlayerItSpawnedForOrBlank(p_apply_1_);
                }
            }
        };
    }

    /**
     * A method used to see if an entity is a suitable target through a number of checks. Args : entity,
     * canTargetInvinciblePlayer
     */
    protected boolean isPlayerItSpawnedForOrBlank(@Nullable LivingEntity target)
    {
        if (target instanceof PlayerEntity) {
            if (this.taskOwner.getEntityData().hasKey(UtilEntityBuffs.dataEntityBuffed_PlayerSpawnedFor)) {
                String spawnName = this.taskOwner.getEntityData().getString(UtilEntityBuffs.dataEntityBuffed_PlayerSpawnedFor);
                if (spawnName != null && target.getName().equals(spawnName)) {
                    return true;
                }
            } else {
                //if for some reason no name set, allow it to target any player
                return true;
            }
        }
        return false;
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
                    this.getTargetDistance(), this.getTargetDistance(), null, (Predicate<PlayerEntity>)this.targetEntitySelector);
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
    public void setEntity(CreatureEntity creature) {
        this.taskOwner = creature;
        this.sorter = new EntityAINearestAttackablePlayerOmniscience.Sorter(creature);
    }

    @Override
    public boolean shouldBeRemoved() {
        if (disableAtSunrise && ConfigCoroUtilAdvanced.removeInvasionAIWhenInvasionDone) {
            //once its day, disable forever
            if (this.taskOwner.world.isDaytime()) {
                CULog.dbg("removing omniscience from " + this.taskOwner.getName());
                //also detarget
                if (this.taskOwner.getAttackTarget() instanceof PlayerEntity) {
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