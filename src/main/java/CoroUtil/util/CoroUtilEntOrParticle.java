package CoroUtil.util;

import java.lang.reflect.Field;

import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;

public class CoroUtilEntOrParticle {

	private static Field fieldPosX = null;
	private static Field fieldPosY = null;
	private static Field fieldPosZ = null;
	
	private static Field fieldMotionX = null;
	private static Field fieldMotionY = null;
	private static Field fieldMotionZ = null;
	
	public static double getPosX(Object obj) {
		if (obj instanceof Particle) {
			checkFields();
			return get(fieldPosX, obj);
		} else {
			return ((Entity)obj).posX;
		}
	}
	
	public static double getPosY(Object obj) {
		if (obj instanceof Particle) {
			checkFields();
			return get(fieldPosY, obj);
		} else {
			return ((Entity)obj).posY;
		}
	}
	
	public static double getPosZ(Object obj) {
		if (obj instanceof Particle) {
			checkFields();
			return get(fieldPosZ, obj);
		} else {
			return ((Entity)obj).posZ;
		}
	}
	
	public static double getMotionX(Object obj) {
		if (obj instanceof Particle) {
			checkFields();
			return get(fieldMotionX, obj);
		} else {
			return ((Entity)obj).motionX;
		}
	}
	
	public static double getMotionY(Object obj) {
		if (obj instanceof Particle) {
			checkFields();
			return get(fieldMotionY, obj);
		} else {
			return ((Entity)obj).motionY;
		}
	}
	
	public static double getMotionZ(Object obj) {
		if (obj instanceof Particle) {
			checkFields();
			return get(fieldMotionZ, obj);
		} else {
			return ((Entity)obj).motionZ;
		}
	}
	
	public static void setMotionX(Object obj, double val) {
		if (obj instanceof Particle) {
			checkFields();
			set(fieldMotionX, obj, val);
		} else {
			((Entity)obj).motionX = val;
		}
	}
	
	public static void setMotionY(Object obj, double val) {
		if (obj instanceof Particle) {
			checkFields();
			set(fieldMotionY, obj, val);
		} else {
			((Entity)obj).motionY = val;
		}
	}
	
	public static void setMotionZ(Object obj, double val) {
		if (obj instanceof Particle) {
			checkFields();
			set(fieldMotionZ, obj, val);
		} else {
			((Entity)obj).motionZ = val;
		}
	}
	
	public static void checkFields() {
		if (fieldPosX == null) {
			try {
				fieldPosX = Particle.class.getDeclaredField("field_187126_f");
				fieldPosY = Particle.class.getDeclaredField("field_187127_g");
				fieldPosZ = Particle.class.getDeclaredField("field_187128_h");
				fieldMotionX = Particle.class.getDeclaredField("field_187129_i");
				fieldMotionY = Particle.class.getDeclaredField("field_187130_j");
				fieldMotionZ = Particle.class.getDeclaredField("field_187131_k");
			} catch (Exception e) {
				try {
					fieldPosX = Particle.class.getDeclaredField("posX");
					fieldPosY = Particle.class.getDeclaredField("posY");
					fieldPosZ = Particle.class.getDeclaredField("posZ");
					fieldMotionX = Particle.class.getDeclaredField("motionX");
					fieldMotionY = Particle.class.getDeclaredField("motionY");
					fieldMotionZ = Particle.class.getDeclaredField("motionZ");
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			} finally {
				fieldPosX.setAccessible(true);
				fieldPosY.setAccessible(true);
				fieldPosZ.setAccessible(true);
				fieldMotionX.setAccessible(true);
				fieldMotionY.setAccessible(true);
				fieldMotionZ.setAccessible(true);
			}
		}
	}
	
	private static double get(Field field, Object obj) {
		try {
			return field.getDouble(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	private static void set(Field field, Object obj, double val) {
		try {
			field.setDouble(obj, val);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
