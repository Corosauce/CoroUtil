package CoroUtil.util;

import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class CoroUtilEntOrParticle {
	
	public static double getPosX(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).posX;
		} else {
			return getPosXParticle(obj);
		}
	}
	
	public static double getPosXParticle(Object obj) {
		return ((Particle)obj).posX;
	}
	
	public static double getPosY(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).posY;
		} else {
			return getPosYParticle(obj);
		}
	}
	
	public static double getPosYParticle(Object obj) {
		return ((Particle)obj).posY;
	}
	
	public static double getPosZ(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).posZ;
		} else {
			return getPosZParticle(obj);
		}
	}
	
	public static double getPosZParticle(Object obj) {
		return ((Particle)obj).posZ;
	}
	
	public static double getMotionX(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).motionX;
		} else {
			return getMotionXParticle(obj);
		}
	}
	
	public static double getMotionXParticle(Object obj) {
		return ((Particle)obj).motionX;
	}
	
	public static double getMotionY(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).motionY;
		} else {
			return getMotionYParticle(obj);
		}
	}
	
	public static double getMotionYParticle(Object obj) {
		return ((Particle)obj).motionY;
	}
	
	public static double getMotionZ(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).motionZ;
		} else {
			return getMotionZParticle(obj);
		}
	}
	
	public static double getMotionZParticle(Object obj) {
		return ((Particle)obj).motionZ;
	}
	
	public static void setMotionX(Object obj, double val) {
		if (obj instanceof Entity) {
			((Entity)obj).motionX = val;
		} else {
			setMotionXParticle(obj, val);
		}
	}
	
	public static void setMotionXParticle(Object obj, double val) {
		((Particle)obj).motionX = val;
	}
	
	public static void setMotionY(Object obj, double val) {
		if (obj instanceof Entity) {
			((Entity)obj).motionY = val;
		} else {
			setMotionYParticle(obj, val);
		}
	}
	
	public static void setMotionYParticle(Object obj, double val) {
		((Particle)obj).motionY = val;
	}
	
	public static void setMotionZ(Object obj, double val) {
		if (obj instanceof Entity) {
			((Entity)obj).motionZ = val;
		} else {
			setMotionZParticle(obj, val);
		}
	}
	
	public static void setMotionZParticle(Object obj, double val) {
		((Particle)obj).motionZ = val;
	}
	
	public static World getWorld(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).worldObj;
		} else {
			return getWorldParticle(obj);
		}
	}
	
	public static World getWorldParticle(Object obj) {
		return ((Particle)obj).worldObj;
	}
	
}
