package com.corosus.coroutil.util;

import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;

public class CoroUtilBlock {
	
	public static boolean isAir(Block parBlock) {
		return parBlock.defaultBlockState().isAir();
	}

	public static BlockPos blockPos(double x, double y, double z) {
		return new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
	}

	public static BlockPos blockPos(Vector3f vec) {
		return new BlockPos(Mth.floor(vec.x()), Mth.floor(vec.y()), Mth.floor(vec.z()));
	}
	
}
