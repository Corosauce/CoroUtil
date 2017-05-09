package extendedrenderer.shadertest.gametest;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;

/**
 * Created by corosus on 08/05/17.
 */
public class Matrix4fe extends Matrix4f {

    byte properties;

    public Matrix4fe() {
        this.m00 = 1.0F;
        this.m11 = 1.0F;
        this.m22 = 1.0F;
        this.m33 = 1.0F;
    }

    void _properties(int properties) {
        //this.properties = (byte)properties;
        //force all the caches off
        this.properties = 0;
    }

    void _m00(float m00) {
        this.m00 = m00;
    }

    void _m01(float m01) {
        this.m01 = m01;
    }

    void _m02(float m02) {
        this.m02 = m02;
    }

    void _m03(float m03) {
        this.m03 = m03;
    }

    void _m10(float m10) {
        this.m10 = m10;
    }

    void _m11(float m11) {
        this.m11 = m11;
    }

    void _m12(float m12) {
        this.m12 = m12;
    }

    void _m13(float m13) {
        this.m13 = m13;
    }

    void _m20(float m20) {
        this.m20 = m20;
    }

    void _m21(float m21) {
        this.m21 = m21;
    }

    void _m22(float m22) {
        this.m22 = m22;
    }

    void _m23(float m23) {
        this.m23 = m23;
    }

    void _m30(float m30) {
        this.m30 = m30;
    }

    void _m31(float m31) {
        this.m31 = m31;
    }

    void _m32(float m32) {
        this.m32 = m32;
    }

    void _m33(float m33) {
        this.m33 = m33;
    }

    public Matrix4fe setPerspective(float fovy, float aspect, float zNear, float zFar) {
        return this.setPerspective(fovy, aspect, zNear, zFar, false);
    }

    public Matrix4fe setPerspective(float fovy, float aspect, float zNear, float zFar, boolean zZeroToOne) {
        //MemUtil.INSTANCE.zero(this);
        float h = (float)Math.tan((double)(fovy * 0.5F));
        this._m00(1.0F / (h * aspect));
        this._m11(1.0F / h);
        boolean farInf = zFar > 0.0F && Float.isInfinite(zFar);
        boolean nearInf = zNear > 0.0F && Float.isInfinite(zNear);
        float e;
        if(farInf) {
            e = 1.0E-6F;
            this._m22(e - 1.0F);
            this._m32((e - (zZeroToOne?1.0F:2.0F)) * zNear);
        } else if(nearInf) {
            e = 1.0E-6F;
            this._m22((zZeroToOne?0.0F:1.0F) - e);
            this._m32(((zZeroToOne?1.0F:2.0F) - e) * zFar);
        } else {
            this._m22((zZeroToOne?zFar:zFar + zNear) / (zNear - zFar));
            this._m32((zZeroToOne?zFar:zFar + zFar) * zNear / (zNear - zFar));
        }

        this._m23(-1.0F);
        //this._properties(1);
        return this;
    }

    public Matrix4fe rotateX(float ang, Matrix4fe dest) {
        if((this.properties & 4) != 0) {
            return this;//dest.rotationX(ang);
        } else {
            float sin = (float)Math.sin((double)ang);
            float cos = (float)cosFromSin((double)sin, (double)ang);
            float rm21 = -sin;
            float nm10 = this.m10 * cos + this.m20 * sin;
            float nm11 = this.m11 * cos + this.m21 * sin;
            float nm12 = this.m12 * cos + this.m22 * sin;
            float nm13 = this.m13 * cos + this.m23 * sin;
            dest._m20(this.m10 * rm21 + this.m20 * cos);
            dest._m21(this.m11 * rm21 + this.m21 * cos);
            dest._m22(this.m12 * rm21 + this.m22 * cos);
            dest._m23(this.m13 * rm21 + this.m23 * cos);
            dest._m10(nm10);
            dest._m11(nm11);
            dest._m12(nm12);
            dest._m13(nm13);
            dest._m00(this.m00);
            dest._m01(this.m01);
            dest._m02(this.m02);
            dest._m03(this.m03);
            dest._m30(this.m30);
            dest._m31(this.m31);
            dest._m32(this.m32);
            dest._m33(this.m33);
            dest._properties((byte)(this.properties & -14));
            return dest;
        }
    }

    public Matrix4fe rotateX(float ang) {
        return this.rotateX(ang, this);
    }

    public Matrix4fe rotateY(float ang, Matrix4fe dest) {
        if((this.properties & 4) != 0) {
            return this;//dest.rotationY(ang);
        } else {
            float sin = (float)Math.sin((double)ang);
            float cos = (float)cosFromSin((double)sin, (double)ang);
            float rm02 = -sin;
            float nm00 = this.m00 * cos + this.m20 * rm02;
            float nm01 = this.m01 * cos + this.m21 * rm02;
            float nm02 = this.m02 * cos + this.m22 * rm02;
            float nm03 = this.m03 * cos + this.m23 * rm02;
            dest._m20(this.m00 * sin + this.m20 * cos);
            dest._m21(this.m01 * sin + this.m21 * cos);
            dest._m22(this.m02 * sin + this.m22 * cos);
            dest._m23(this.m03 * sin + this.m23 * cos);
            dest._m00(nm00);
            dest._m01(nm01);
            dest._m02(nm02);
            dest._m03(nm03);
            dest._m10(this.m10);
            dest._m11(this.m11);
            dest._m12(this.m12);
            dest._m13(this.m13);
            dest._m30(this.m30);
            dest._m31(this.m31);
            dest._m32(this.m32);
            dest._m33(this.m33);
            dest._properties((byte)(this.properties & -14));
            return dest;
        }
    }

    public Matrix4fe rotateY(float ang) {
        return this.rotateY(ang, this);
    }

    public Matrix4fe rotateZ(float ang, Matrix4fe dest) {
        if((this.properties & 4) != 0) {
            return this;//dest.rotationZ(ang);
        } else {
            float sin = (float)Math.sin((double)ang);
            float cos = (float)cosFromSin((double)sin, (double)ang);
            float rm10 = -sin;
            float nm00 = this.m00 * cos + this.m10 * sin;
            float nm01 = this.m01 * cos + this.m11 * sin;
            float nm02 = this.m02 * cos + this.m12 * sin;
            float nm03 = this.m03 * cos + this.m13 * sin;
            dest._m10(this.m00 * rm10 + this.m10 * cos);
            dest._m11(this.m01 * rm10 + this.m11 * cos);
            dest._m12(this.m02 * rm10 + this.m12 * cos);
            dest._m13(this.m03 * rm10 + this.m13 * cos);
            dest._m00(nm00);
            dest._m01(nm01);
            dest._m02(nm02);
            dest._m03(nm03);
            dest._m20(this.m20);
            dest._m21(this.m21);
            dest._m22(this.m22);
            dest._m23(this.m23);
            dest._m30(this.m30);
            dest._m31(this.m31);
            dest._m32(this.m32);
            dest._m33(this.m33);
            dest._properties((byte)(this.properties & -14));
            return dest;
        }
    }

    public Matrix4fe rotateZ(float ang) {
        return this.rotateZ(ang, this);
    }

    public static double cosFromSin(double sin, double angle) {
        double cos = Math.sqrt(1.0D - sin * sin);
        double a = angle + 1.5707963267948966D;
        double b = a - (double)((int)(a / 6.283185307179586D)) * 6.283185307179586D;
        if(b < 0.0D) {
            b += 6.283185307179586D;
        }

        return b >= 3.141592653589793D?-cos:cos;
    }

    public Matrix4fe scale(float xyz) {
        return this.scale(xyz, xyz, xyz);
    }

    public Matrix4fe scale(float x, float y, float z) {
        return this.scale(x, y, z, this);
    }

    public Matrix4fe scale(float x, float y, float z, Matrix4fe dest) {
        return (this.properties & 4) != 0?dest.scaling(x, y, z):this.scaleGeneric(x, y, z, dest);
    }

    public Matrix4fe scaling(float x, float y, float z) {
        //MemUtil.INSTANCE.identity(this);
        identity(this);
        this._m00(x);
        this._m11(y);
        this._m22(z);
        this._properties(2);
        return this;
    }

    private Matrix4fe scaleGeneric(float x, float y, float z, Matrix4fe dest) {
        dest._m00(this.m00 * x);
        dest._m01(this.m01 * x);
        dest._m02(this.m02 * x);
        dest._m03(this.m03 * x);
        dest._m10(this.m10 * y);
        dest._m11(this.m11 * y);
        dest._m12(this.m12 * y);
        dest._m13(this.m13 * y);
        dest._m20(this.m20 * z);
        dest._m21(this.m21 * z);
        dest._m22(this.m22 * z);
        dest._m23(this.m23 * z);
        dest._m30(this.m30);
        dest._m31(this.m31);
        dest._m32(this.m32);
        dest._m33(this.m33);
        dest._properties((byte)(this.properties & -14));
        return dest;
    }

    public final Matrix4fe identity(Matrix4fe dest) {
        dest.m00 = 1.0F;
        dest.m01 = 0.0F;
        dest.m02 = 0.0F;
        dest.m03 = 0.0F;
        dest.m10 = 0.0F;
        dest.m11 = 1.0F;
        dest.m12 = 0.0F;
        dest.m13 = 0.0F;
        dest.m20 = 0.0F;
        dest.m21 = 0.0F;
        dest.m22 = 1.0F;
        dest.m23 = 0.0F;
        dest.m30 = 0.0F;
        dest.m31 = 0.0F;
        dest.m32 = 0.0F;
        dest.m33 = 1.0F;
        return dest;
    }

    public final void zero(Matrix4fe dest) {
        dest.m00 = 0.0F;
        dest.m01 = 0.0F;
        dest.m02 = 0.0F;
        dest.m03 = 0.0F;
        dest.m10 = 0.0F;
        dest.m11 = 0.0F;
        dest.m12 = 0.0F;
        dest.m13 = 0.0F;
        dest.m20 = 0.0F;
        dest.m21 = 0.0F;
        dest.m22 = 0.0F;
        dest.m23 = 0.0F;
        dest.m30 = 0.0F;
        dest.m31 = 0.0F;
        dest.m32 = 0.0F;
        dest.m33 = 0.0F;
    }

    public FloatBuffer get(FloatBuffer buffer) {
        return this.get(buffer.position(), buffer);
    }

    public FloatBuffer get(int index, FloatBuffer buffer) {
        //MemUtil.INSTANCE.put(this, index, buffer);
        put0(this, buffer);
        return buffer;
    }

    private void put0(Matrix4f m, FloatBuffer dest) {
        dest.put(0, m.m00);
        dest.put(1, m.m01);
        dest.put(2, m.m02);
        dest.put(3, m.m03);
        dest.put(4, m.m10);
        dest.put(5, m.m11);
        dest.put(6, m.m12);
        dest.put(7, m.m13);
        dest.put(8, m.m20);
        dest.put(9, m.m21);
        dest.put(10, m.m22);
        dest.put(11, m.m23);
        dest.put(12, m.m30);
        dest.put(13, m.m31);
        dest.put(14, m.m32);
        dest.put(15, m.m33);
    }

    public Matrix4fe translate(Vector3f offset) {
        return this.translate(offset.x, offset.y, offset.z);
    }

    public Matrix4fe translate(float x, float y, float z) {
        if((this.properties & 4) != 0) {
            return this.translation(x, y, z);
        } else {
            this._m30(this.m00 * x + this.m10 * y + this.m20 * z + this.m30);
            this._m31(this.m01 * x + this.m11 * y + this.m21 * z + this.m31);
            this._m32(this.m02 * x + this.m12 * y + this.m22 * z + this.m32);
            this._m33(this.m03 * x + this.m13 * y + this.m23 * z + this.m33);
            this.properties &= -6;
            return this;
        }
    }

    public Matrix4fe translation(float x, float y, float z) {
        //MemUtil.INSTANCE.identity(this);
        identity(this);
        this._m30(x);
        this._m31(y);
        this._m32(z);
        this._properties(10);
        return this;
    }
}
