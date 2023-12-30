package com.corosus.coroutil.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public class CoroUtilBlock {
	
	public static boolean isAir(Block parBlock) {
		return parBlock.defaultBlockState().isAir();
	}

	public static BlockPos blockPos(double x, double y, double z) {
		return new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
	}

	public static BlockPos blockPos(Vec3 vec) {
		return new BlockPos(Mth.floor(vec.x), Mth.floor(vec.y), Mth.floor(vec.z));
	}
	
}
