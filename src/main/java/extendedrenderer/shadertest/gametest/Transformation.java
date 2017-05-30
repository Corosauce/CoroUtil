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

    public Matrix4fe getViewMatrix(Camera camera) {
        Vector3f cameraPos = camera.getPosition();
        Vector3f rotation = camera.getRotation();

        viewMatrix.identity();

        //first person mc code adjustment, kinda temp for now
        viewMatrix.translate(0, 0, 0.05F);

        // First do the rotation so camera rotates over its position
        /**
         * viewMatrix.rotate((float)Math.toRadians(rotation.x), new Vector3f(1, 0, 0))
         .rotate((float)Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
         */
        viewMatrix.rotate((float)Math.toRadians(rotation.x), new Vector3f(1, 0, 0))
                .rotate((float)Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
        // Then do the translation
        //was originally viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        //viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        viewMatrix.translate(cameraPos.x, cameraPos.y, cameraPos.z);
        return viewMatrix;
    }

    public Matrix4fe buildModelViewMatrix(Matrix4fe modelMatrix, Matrix4fe viewMatrix) {
        /*Matrix4fe viewCurr = new Matrix4fe(viewMatrix);
        return viewCurr.mul(modelMatrix);*/
        return viewMatrix.mulAffine(modelMatrix, modelViewMatrix);
    }

    public Matrix4fe buildModelMatrix(GameItem gameItem) {
        return buildModelMatrix(gameItem, null);
    }

    public Matrix4fe buildModelMatrix(GameItem gameItem, Vector3f posCustom) {
        Quaternion q = gameItem.getRotation();

        boolean quat = true;

        if (!quat) {
            return modelMatrix.identity().translate(posCustom != null ? posCustom : gameItem.getPosition()).
                    rotateX((float)Math.toRadians(-45)).
                    rotateY((float)Math.toRadians(-0)).
                    rotateZ((float)Math.toRadians(-0)).
                    scale(gameItem.getScale());
        } else {

            //using translationRotateScale instead of above per call code doesnt seem to show higher fps, but cant hurt anyways
            //upon further testing, this doesnt actually work by just forcing 0 or 1 for w param, needs actual quaternion handled rotation
            //Quaternion q = new Quaternion();
            //q.set((float)Math.toRadians(-rotation.x), (float)Math.toRadians(-rotation.y), (float)Math.toRadians(-rotation.z));
            Vector3f vecPos = posCustom != null ? posCustom : gameItem.getPosition();
            return modelMatrix.translationRotateScale(
                    vecPos.x, vecPos.y, vecPos.z,
                    q.x, q.y, q.z, q.w/*rotation.w*/,
                    gameItem.getScale(), gameItem.getScale(), gameItem.getScale());
        }
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
