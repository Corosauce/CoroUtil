package CoroUtil.util;

import java.util.Iterator;
import java.util.UUID;

import CoroUtil.config.ConfigCoroUtilAdvanced;
import CoroUtil.difficulty.UtilEntityBuffs;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class CoroUtilEntity {

	public static boolean canCoordBeSeen(LivingEntity ent, int x, int y, int z)
    {
        return ent.world.rayTraceBlocks(new Vec3d(ent.posX, ent.posY + (double)ent.getEyeHeight(), ent.posZ), new Vec3d(x, y, z)) == null;
    }
    
    public static boolean canCoordBeSeenFromFeet(LivingEntity ent, int x, int y, int z)
    {
        return ent.world.rayTraceBlocks(new Vec3d(ent.posX, ent.getBoundingBox().minY+0.15, ent.posZ), new Vec3d(x, y, z)) == null;
    }
    
    public static double getDistance(Entity ent, BlockCoord coords)
    {
        double d3 = ent.posX - coords.posX;
        double d4 = ent.posY - coords.posY;
        double d5 = ent.posZ - coords.posZ;
        return (double)MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
    }
	
	public static double getDistance(Entity ent, TileEntity tEnt)
    {
        double d3 = ent.posX - tEnt.getPos().getX();
        double d4 = ent.posY - tEnt.getPos().getY();
        double d5 = ent.posZ - tEnt.getPos().getZ();
        return (double)MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
    }
	
	public static Vec3 getTargetVector(LivingEntity parEnt, LivingEntity target) {
    	double vecX = target.posX - parEnt.posX;
    	double vecY = target.posY - parEnt.posY;
    	double vecZ = target.posZ - parEnt.posZ;
    	double dist = Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
    	Vec3 vec3 = new Vec3(vecX / dist, vecY / dist, vecZ / dist);
    	return vec3;
    }
	
	public static void moveTowards(Entity ent, Entity targ, float speed) {
		double vecX = targ.posX - ent.posX;
		double vecY = targ.posY - ent.posY;
		double vecZ = targ.posZ - ent.posZ;

		double dist2 = (double)Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
		ent.motionX += vecX / dist2 * speed;
		ent.motionY += vecY / dist2 * speed;
		ent.motionZ += vecZ / dist2 * speed;
	}
	
	public static String getName(Entity ent) {
		return ent != null ? ent.getName() : "nullObject";
	}
	
	public static PlayerEntity getPlayerByUUID(UUID uuid) {
		Iterator iterator = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers().iterator();
        ServerPlayerEntity entityplayermp;
        
        while (iterator.hasNext()) {
        	entityplayermp = (ServerPlayerEntity) iterator.next();
        	
        	if (entityplayermp.getGameProfile().getId().equals(uuid)) {
        		return entityplayermp;
        	}
        }
        
        return null;
	}
	
	/**
     * Returns the closest vulnerable player to this entity within the given radius, or null if none is found
     */
    public static PlayerEntity getClosestVulnerablePlayerToEntity(World world, Entity p_72856_1_, double p_72856_2_)
    {
        return getClosestVulnerablePlayer(world, p_72856_1_.posX, p_72856_1_.posY, p_72856_1_.posZ, p_72856_2_);
    }

    /**
     * Returns the closest vulnerable player within the given radius, or null if none is found.
     */
    public static PlayerEntity getClosestVulnerablePlayer(World world, double p_72846_1_, double p_72846_3_, double p_72846_5_, double p_72846_7_)
    {
        double d4 = -1.0D;
        PlayerEntity entityplayer = null;

        for (int i = 0; i < world.playerEntities.size(); ++i)
        {
            PlayerEntity entityplayer1 = (PlayerEntity)world.playerEntities.get(i);

            if (!entityplayer1.capabilities.disableDamage && entityplayer1.isAlive())
            {
                double d5 = entityplayer1.getDistanceSq(p_72846_1_, p_72846_3_, p_72846_5_);
                double d6 = p_72846_7_;

                if (entityplayer1.isSneaking())
                {
                    d6 = p_72846_7_ * 0.800000011920929D;
                }

                if (entityplayer1.isInvisible())
                {
                    float f = entityplayer1.getArmorVisibility();

                    if (f < 0.1F)
                    {
                        f = 0.1F;
                    }

                    d6 *= (double)(0.7F * f);
                }

                if ((p_72846_7_ < 0.0D || d5 < d6 * d6) && (d4 == -1.0D || d5 < d4))
                {
                    d4 = d5;
                    entityplayer = entityplayer1;
                }
            }
        }

        return entityplayer;
    }

    public static boolean canProcessForList(String playerName, String list, boolean whitelistMode) {
        if (whitelistMode) {
            if (!list.contains(playerName)) {
                return false;
            }
        } else {
            if (list.contains(playerName)) {
                return false;
            }
        }
        return true;
    }

    public static Class getClassFromRegistry(String name) {
        //Class clazz = EntityList.NAME_TO_CLASS.get(name);
        Class clazz = EntityList.getClass(new ResourceLocation(name));
        //dont think this will be needed for proper registered entity names
        /*if (clazz == null) {
            clazz = EntityList.NAME_TO_CLASS.get(name.replace("minecraft:", "").replace("minecraft.", ""));
        }*/
        return clazz;
    }

    /**
     * Mimicing some of vanillas rules for spawning in a mob
     *
     * x y z coords are expected to be the ground the mob is going to spawn on
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static boolean canSpawnMobOnGround(World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = world.getBlockState(pos);
        Block block = state.getOwner();
        if (CoroUtilBlock.isAir(block) || !block.canCreatureSpawn(state, world, pos, MobEntity.SpawnPlacementType.ON_GROUND)) {
            return false;
        }
        return true;
    }

    public static boolean isInDarkCave(World world, int x, int y, int z, boolean checkSpaceToSpawn, boolean skipLightCheck) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockPos posAir = new BlockPos(x, y + 1, z);
        BlockState state = world.getBlockState(pos);
        Block block = state.getOwner();
        if (!world.canSeeSky(posAir) && (skipLightCheck || world.getLightFromNeighbors(posAir) < 5)) {
            if (!CoroUtilBlock.isAir(block) && state.getMaterial() == Material.ROCK/*(block != Blocks.grass || block.getMaterial() != Material.grass)*/) {

                if (!checkSpaceToSpawn) {
                    return true;
                } else {
                    if (world.isAirBlock(posAir) && world.isAirBlock(pos.up(2))) {
                        return true;
                    }

                }
            }
        }
        return false;
    }

    public static boolean attackEntityAsMobForPassives(LivingEntity source, Entity entityIn)
    {

        float f;
        if (source.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
            f = (float)source.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).get();
        } else {
            //use default zombie damage of 3
            f = 3;
        }

        int i = 0;

        if (entityIn instanceof LivingEntity)
        {
            f += EnchantmentHelper.getModifierForCreature(source.getHeldItemMainhand(), ((LivingEntity)entityIn).getCreatureAttribute());
            i += EnchantmentHelper.getKnockbackModifier(source);
        }

        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(source), f);

        if (flag)
        {
            if (i > 0 && entityIn instanceof LivingEntity)
            {
                ((LivingEntity)entityIn).knockBack(source, (float)i * 0.5F, (double)MathHelper.sin(source.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(source.rotationYaw * 0.017453292F)));
                source.motionX *= 0.6D;
                source.motionZ *= 0.6D;
            }

            int j = EnchantmentHelper.getFireAspectModifier(source);

            if (j > 0)
            {
                entityIn.setFire(j * 4);
            }

            if (entityIn instanceof PlayerEntity)
            {
                PlayerEntity entityplayer = (PlayerEntity)entityIn;
                ItemStack itemstack = source.getHeldItemMainhand();
                ItemStack itemstack1 = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : ItemStack.EMPTY;

                if (!itemstack.isEmpty() && !itemstack1.isEmpty() && itemstack.getItem().canDisableShield(itemstack, itemstack1, entityplayer, source) && itemstack1.getItem().isShield(itemstack1, entityplayer))
                {
                    float f1 = 0.25F + (float)EnchantmentHelper.getEfficiencyModifier(source) * 0.05F;

                    if (source.world.rand.nextFloat() < f1)
                    {
                        entityplayer.getCooldownTracker().setCooldown(itemstack1.getItem(), 100);
                        source.world.setEntityState(entityplayer, (byte)30);
                    }
                }
            }

            //protected access, disable or use AT
            //source.applyEnchantments(source, entityIn);
        }

        return flag;
    }

    public static boolean canPathfindLongDist(CreatureEntity ent) {
        long lastPathTime = ent.getEntityData().getLong(UtilEntityBuffs.dataEntityBuffed_LastTimePathfindLongDist);
        if (ent.world.getGameTime() > lastPathTime + ConfigCoroUtilAdvanced.worldTimeDelayBetweenLongDistancePathfindTries) {
            return true;
        }
        return false;
    }

    public static void updateLastTimeLongDistPathfinded(CreatureEntity ent) {
        ent.getEntityData().putLong(UtilEntityBuffs.dataEntityBuffed_LastTimePathfindLongDist, ent.world.getGameTime() + (ent.getEntityId() % 20));
    }
}

