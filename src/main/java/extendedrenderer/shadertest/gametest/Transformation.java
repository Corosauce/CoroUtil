package extendedrenderer.shadertest.gametest;


import org.lwjgl.util.vector.Quaternion;

import javax.vecmath.Vector3f;

public class Transformation {

    private Matrix4fe projectionMatrix;

    private Matrix4fe modelViewMatrix;

    private Matrix4fe viewMatrix;

    private Matrix4fe modelMatrix;
    
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

    public Matrix4fe getModelViewMatrix(GameItem gameItem, Matrix4fe viewMatrix) {
        Vector3f rotation = gameItem.getRotation();
        modelViewMatrix.identity().translate(gameItem.getPosition()).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(gameItem.getScale());
        Matrix4fe viewCurr = new Matrix4fe(viewMatrix);
        return viewCurr.mul(modelViewMatrix);
    }

    public Matrix4fe buildModelViewMatrix(Matrix4fe modelMatrix, Matrix4fe viewMatrix) {
        /*Matrix4fe viewCurr = new Matrix4fe(viewMatrix);
        return viewCurr.mul(modelMatrix);*/
        return viewMatrix.mulAffine(modelMatrix, modelViewMatrix);
    }

    public Matrix4fe getModelViewMatrixOffset(GameItem gameItem, Matrix4fe viewMatrix, Matrix4fe offsetMatrix) {
        Vector3f rotation = gameItem.getRotation();
        modelViewMatrix.identity().translate(gameItem.getPosition()).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(gameItem.getScale());
        Matrix4fe viewCurr = new Matrix4fe(viewMatrix);
        Matrix4fe mat2 = new Matrix4fe(offsetMatrix);
        return viewCurr.mul(mat2).mul(modelViewMatrix);
    }

    public Matrix4fe getModelViewMatrixMC(GameItem gameItem) {
        Vector3f rotation = gameItem.getRotation();
        modelViewMatrix.identity().translate(gameItem.getPosition()).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(gameItem.getScale());
        //Matrix4fe viewCurr = new Matrix4fe(viewMatrix);
        return modelViewMatrix;//viewCurr.mul(modelViewMatrix);
    }
    
    public Matrix4fe getWorldMatrix(Vector3f offset, Vector3f rotation, float scale) {
        modelViewMatrix.identity(modelViewMatrix)
                .translate(offset).
                rotateX((float)Math.toRadians(rotation.x)).
                rotateY((float)Math.toRadians(rotation.y)).
                rotateZ((float)Math.toRadians(rotation.z)).
                scale(scale);
        return modelViewMatrix;
    }

    public Matrix4fe buildModelMatrix(GameItem gameItem) {
        return buildModelMatrix(gameItem, null);
    }

    public Matrix4fe buildModelMatrix(GameItem gameItem, Vector3f posCustom) {
        Vector3f rotation = gameItem.getRotation();

        //TODO: quaternion usage to make rotation math faster

        return modelMatrix.identity().translate(posCustom != null ? posCustom : gameItem.getPosition()).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(gameItem.getScale());

        //using translationRotateScale instead of above per call code doesnt seem to show higher fps, but cant hurt anyways
        //upon further testing, this doesnt actually work by just forcing 0 or 1 for w param, needs actual quaternion handled rotation
        /*return modelMatrix.translationRotateScale(
                gameItem.getPosition().x, gameItem.getPosition().y, gameItem.getPosition().z,
                rotation.x, rotation.y, rotation.z, 1*//*rotation.w*//*,
                gameItem.getScale(), gameItem.getScale(), gameItem.getScale());*/
    }

    public Matrix4fe buildModelMatrixPhase1Translate(GameItem gameItem) {
        return modelMatrix.identity().translate(gameItem.getPosition());
    }

    public Matrix4fe buildModelMatrixPhase2RotateScale(GameItem gameItem) {
        Vector3f rotation = gameItem.getRotation();

        return modelMatrix.
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(gameItem.getScale());
    }
}
