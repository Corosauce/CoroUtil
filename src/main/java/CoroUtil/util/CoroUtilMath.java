package CoroUtil.util;

import org.lwjgl.util.vector.Quaternion;

/**
 * Created by corosus on 29/05/17.
 */
public class CoroUtilMath {

    public static Quaternion rotation(Quaternion q, float angleX, float angleY, float angleZ) {
        double thetaX = (double)angleX * 0.5D;
        double thetaY = (double)angleY * 0.5D;
        double thetaZ = (double)angleZ * 0.5D;
        double thetaMagSq = thetaX * thetaX + thetaY * thetaY + thetaZ * thetaZ;
        double s;
        if(thetaMagSq * thetaMagSq / 24.0D < 9.99999993922529E-9D) {
            q.w = (float)(1.0D - thetaMagSq / 2.0D);
            s = 1.0D - thetaMagSq / 6.0D;
        } else {
            double thetaMag = Math.sqrt(thetaMagSq);
            double sin = Math.sin(thetaMag);
            s = sin / thetaMag;
            q.w = (float)cosFromSin(sin, thetaMag);
        }

        q.x = (float)(thetaX * s);
        q.y = (float)(thetaY * s);
        q.z = (float)(thetaZ * s);
        return q;
    }

    public static double cosFromSin(double sin, double angle) {
        return Math.sin(angle + 1.5707963267948966D);
        /*if(Options.FASTMATH) {
            return Math.sin(angle + 1.5707963267948966D);
        } else {
            double cos = sqrt(1.0D - sin * sin);
            double a = angle + 1.5707963267948966D;
            double b = a - (double)((int)(a / 6.283185307179586D)) * 6.283185307179586D;
            if(b < 0.0D) {
                b += 6.283185307179586D;
            }

            return b >= 3.141592653589793D?-cos:cos;
        }*/
    }

}
