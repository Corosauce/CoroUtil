package com.corosus.coroutil.common.core.util;

import java.util.Random;

public class CoroUtilMisc {

	public static Random random = new Random();

	public static float adjVal(float source, float target, float adj) {
		if (source < target) {
			source += adj;
			//fix over adjust
			if (source > target) {
				source = target;
			}
		} else if (source > target) {
			source -= adj;
			//fix over adjust
			if (source < target) {
				source = target;
			}
		}
		return source;
	}

	public static Random random() {
		return random;
	}
}
