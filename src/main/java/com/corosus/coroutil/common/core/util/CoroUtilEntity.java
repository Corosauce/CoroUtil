package com.corosus.coroutil.common.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CoroUtilEntity {

    public static String getName(Entity ent) {
        return ent != null ? ent.getName().getString() : "nullObject";
    }

    public static boolean canSee(Entity p_70685_1_, BlockPos pos) {
        Vec3 vector3d = new Vec3(p_70685_1_.getX(), p_70685_1_.getEyeY(), p_70685_1_.getZ());
        Vec3 vector3d1 = new Vec3(pos.getX(), pos.getY(), pos.getZ());
        if (p_70685_1_.level() != p_70685_1_.level() || vector3d1.distanceToSqr(vector3d) > 128.0D * 128.0D) return false;
        return p_70685_1_.level().clip(new ClipContext(vector3d, vector3d1, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p_70685_1_)).getType() == HitResult.Type.MISS;
    }
}
