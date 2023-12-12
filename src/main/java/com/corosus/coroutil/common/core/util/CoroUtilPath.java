package com.corosus.coroutil.common.core.util;

import com.corosus.coroutil.common.core.config.ConfigCoroUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.PathComputationType;

import java.util.Random;

public class CoroUtilPath {
	
	public static boolean tryMoveToEntityLivingLongDist(Mob entSource, Entity entityTo, double moveSpeedAmp) {
		return tryMoveToXYZLongDist(entSource, entityTo.getX(), entityTo.getBoundingBox().minY, entityTo.getZ(), moveSpeedAmp);
	}
	
	/**
	 * If close enough, paths to coords, if too far based on attribute, tries to find best spot towards target to pathfind to
	 * 
	 * @param ent
	 * @param x
	 * @param y
	 * @param z
	 * @param moveSpeedAmp
	 * @return
	 */
	public static boolean tryMoveToXYZLongDist(Mob ent, double x, double y, double z, double moveSpeedAmp) {
		
		Level world = ent.level();
		
		boolean success = false;

		try {
			if (ent.getNavigation().isDone()) {

				double distToPlayer = Math.sqrt(ent.distanceToSqr(x, y, z));//ent.getDistanceToEntity(player);

				double followDist = ent.getAttribute(Attributes.FOLLOW_RANGE).getValue();

				if (distToPlayer <= followDist) {
					//boolean success = ent.getNavigator().tryMoveToEntityLiving(player, moveSpeedAmp);
					//success = ent.getNavigator().tryMoveToXYZ(x, y, z, moveSpeedAmp);
					success = CoroUtilCompatibility.tryPathToXYZModCompat(ent, Mth.floor(x), Mth.floor(y), Mth.floor(z), moveSpeedAmp);
					//System.out.println("success? " + success + "- move to player: " + ent + " -> " + player);
				} else {
					/*int x = MathHelper.floor(player.posX);
					int y = MathHelper.floor(player.posY);
					int z = MathHelper.floor(player.posZ);*/

					double d = x + 0.5F - ent.getX();
					double d2 = z + 0.5F - ent.getZ();
					double d1;
					d1 = y + 0.5F - (ent.getY() + (double) ent.getEyeHeight());

					double d3 = Mth.sqrt((float) (d * d + d2 * d2));
					float f2 = (float) ((Math.atan2(d2, d) * 180D) / 3.1415927410125732D) - 90F;
					float f3 = (float) (-((Math.atan2(d1, d3) * 180D) / 3.1415927410125732D));
					float rotationPitch = -f3;//-ent.updateRotation(rotationPitch, f3, 180D);
					float rotationYaw = f2;//updateRotation(rotationYaw, f2, 180D);

					Mob center = ent;

					Random rand = new Random();

					float randLook = rand.nextInt(90) - 45;
					//int height = 10;
					double dist = (followDist * 0.75D) + rand.nextInt((int) followDist / 2);//rand.nextInt(26)+(queue.get(0).retryState * 6);
					int gatherX = (int) Math.floor(center.getX() + ((double) (-Math.sin((rotationYaw + randLook) / 180.0F * 3.1415927F)/* * Math.cos(center.rotationPitch / 180.0F * 3.1415927F)*/) * dist));
					int gatherY = (int) center.getY();//Math.floor(center.posY-0.5 + (double)(-MathHelper.sin(center.rotationPitch / 180.0F * 3.1415927F) * dist) - 0D); //center.posY - 0D;
					int gatherZ = (int) Math.floor(center.getZ() + ((double) (Math.cos((rotationYaw + randLook) / 180.0F * 3.1415927F)/* * Math.cos(center.rotationPitch / 180.0F * 3.1415927F)*/) * dist));

					BlockPos pos = new BlockPos(gatherX, gatherY, gatherZ);

					if (!world.isLoaded(pos)) return false;

					BlockState state = world.getBlockState(pos);
					//Block block = state.getBlock();
					int tries = 0;
					if (!CoroUtilBlock.isAir(state.getBlock())) {
						int offset = -5;

						while (tries < 30) {
							if (CoroUtilBlock.isAir(state.getBlock()) || !state.isPathfindable(ent.level(), pos, PathComputationType.LAND)) {
								break;
							}
							gatherY += offset++;
							pos = new BlockPos(gatherX, gatherY, gatherZ);
							state = world.getBlockState(pos);
							tries++;
						}
					} else {
						//int offset = 0;
						while (tries < 30) {
							if (!CoroUtilBlock.isAir(state.getBlock()) && state.isPathfindable(ent.level(), pos, PathComputationType.LAND)) {
								break;
							}
							gatherY -= 1;//offset++;
							pos = new BlockPos(gatherX, gatherY, gatherZ);
							state = world.getBlockState(pos);
							tries++;
						}
					}

					if (tries < 30) {
						//success = ent.getNavigator().tryMoveToXYZ(gatherX, gatherY, gatherZ, moveSpeedAmp);
						success = CoroUtilCompatibility.tryPathToXYZModCompat(ent, gatherX, gatherY, gatherZ, moveSpeedAmp);
						//System.out.println("pp success? " + success + "- move to player: " + ent + " -> " + player);
					} else {
						//fallback for extreme y differences, just path to topmost block, hopefully wont break much for inside structures etc

						pos = new BlockPos(pos.getX(), world.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()), pos.getZ()).below();

						if (!world.isLoaded(pos)) return false;

						state = world.getBlockState(pos);
						if (state.isPathfindable(ent.level(), pos, PathComputationType.LAND)) {
							success = CoroUtilCompatibility.tryPathToXYZModCompat(ent, pos.getX(), pos.getY(), pos.getZ(), moveSpeedAmp);
						}
					}
				}
			}
		} catch (Exception ex) {
			//theres is 1 case where this happens: https://github.com/Corosauce/ZombieAwareness/issues/27
			//tracked it down to possibly a null AABB on a block
			CULog.err("Exception trying to pathfind");
			if (ConfigCoroUtil.useLoggingError) {
				ex.printStackTrace();
			}
		}
		
		return success;
	}
	
}
