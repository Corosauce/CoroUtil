package extendedrenderer.shadertest.gametest;


import javax.vecmath.Vector3f;

public class Transformation {

    private final Matrix4fe projectionMatrix;

    private final Matrix4fe worldMatrix;
    
    public Transformation() {
        worldMatrix = new Matrix4fe();
        projectionMatrix = new Matrix4fe();
    }

    public final Matrix4fe getProjectionMatrix(float fov, float aspectRatio, float zNear, float zFar) {
        projectionMatrix.identity(projectionMatrix);
        projectionMatrix.setPerspective(fov, aspectRatio, zNear, zFar);
        return projectionMatrix;
    }
    
    public Matrix4fe getWorldMatrix(Vector3f offset, Vector3f rotation, float scale) {
        worldMatrix.identity(worldMatrix)
                .translate(offset).
                rotateX((float)Math.toRadians(rotation.x)).
                rotateY((float)Math.toRadians(rotation.y)).
                rotateZ((float)Math.toRadians(rotation.z)).
                scale(scale);
        return worldMatrix;
    }
}
