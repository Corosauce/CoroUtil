package extendedrenderer.shadertest.gametest;


import extendedrenderer.particle.entity.EntityRotFX;
import org.lwjgl.util.vector.Quaternion;

import javax.vecmath.Vector3f;

public class Transformation {

    private Matrix4fe projectionMatrix;

    private Matrix4fe modelViewMatrix;

    private Matrix4fe viewMatrix;

    public Matrix4fe modelMatrix;
    
    public Transformation() {
        modelViewMatrix = new Matrix4fe();
        projectionMatrix = new Matrix4fe();
        viewMatrix = new Matrix4fe();
        modelMatrix = new Matrix4fe();
    }

    public final Matrix4fe getProjectionMatrix(float fov, float aspectRatio, float zNear, float zFar) {
        projectionMatrix.identity(projectionMatrix);
        projectionMatrix.setPerspective(fov, aspectRatio, zNear, zFar);
        return projectionMatrix;
    }

    public Matrix4fe buildModelViewMatrix(Matrix4fe modelMatrix, Matrix4fe viewMatrix) {
        /*Matrix4fe viewCurr = new Matrix4fe(viewMatrix);
        return viewCurr.mul(modelMatrix);*/
        return viewMatrix.mulAffine(modelMatrix, modelViewMatrix);
    }

    public Matrix4fe buildModelMatrix(EntityRotFX gameItem, Vector3f posCustom) {
        Quaternion q = gameItem.rotation;

        float scaleAdj = gameItem.getScale();

        scaleAdj *= 0.2F;

        Vector3f vecPos = posCustom != null ? posCustom : new Vector3f((float)gameItem.posX, (float)gameItem.posY, (float)gameItem.posZ);

        //moved - its eyeheight
        //vecPos.y -= 1.62F;

        boolean quat = true;

        if (quat) {
            return modelMatrix.translationRotateScale(
                    vecPos.x, vecPos.y, vecPos.z,
                    q.x, q.y, q.z, q.w,
                    scaleAdj, scaleAdj, scaleAdj);
        } else {
            return modelMatrix.identity()
                    .translate(vecPos)
                    .rotateY((float)Math.toRadians(-gameItem.rotationYaw))
                    .rotateX((float)Math.toRadians(-gameItem.rotationPitch))
                    /*.rotateZ((float)Math.toRadians(0))*/
                    .scale(scaleAdj);
        }
    }
}
