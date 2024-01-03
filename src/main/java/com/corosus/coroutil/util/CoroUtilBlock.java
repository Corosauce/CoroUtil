package com.corosus.coroutil.util;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class CoroUtilBlock {
	
	public static boolean isAir(Block parBlock) {
		return parBlock.defaultBlockState().isAir();
	}

	public static BlockPos blockPos(double x, double y, double z) {
		return new BlockPos(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
	}

	public static BlockPos blockPos(Vector3f vec) {
		return new BlockPos(MathHelper.floor(vec.x()), MathHelper.floor(vec.y()), MathHelper.floor(vec.z()));
	}
	
}
