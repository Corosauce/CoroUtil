package extendedrenderer.shadertest.gametest;


import javax.vecmath.Vector3f;

public class Transformation {

    private Matrix4fe projectionMatrix;

    private Matrix4fe modelViewMatrix;

    private Matrix4fe viewMatrix;
    
    public Transformation() {
        modelViewMatrix = new Matrix4fe();
        projectionMatrix = new Matrix4fe();
        viewMatrix = new Matrix4fe();
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
    
    public Matrix4fe getWorldMatrix(Vector3f offset, Vector3f rotation, float scale) {
        modelViewMatrix.identity(modelViewMatrix)
                .translate(offset).
                rotateX((float)Math.toRadians(rotation.x)).
                rotateY((float)Math.toRadians(rotation.y)).
                rotateZ((float)Math.toRadians(rotation.z)).
                scale(scale);
        return modelViewMatrix;
    }
}
