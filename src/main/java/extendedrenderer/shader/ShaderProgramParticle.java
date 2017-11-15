package extendedrenderer.shader;

import extendedrenderer.particle.ShaderManager;

public class ShaderProgramParticle extends ShaderProgram {

    private int vertexShaderAttributeIndexPosition = 0;
    private int vertexShaderAttributeTexCoord = 1;
    //private int vertexShaderAttributeVertexNormal = 2;
    private int vertexShaderAttributeModelViewMatrix = InstancedMeshParticle.vboSizeMesh;//5;
    private int vertexShaderAttributeBrightness = InstancedMeshParticle.vboSizeMesh + 4;//9;
    //private int vertexShaderAttributeRGBA = InstancedMesh.vboSizeMesh + 5;//10;
    private int vertexShaderAttributeRGBATest = InstancedMeshParticle.vboSizeMesh + 5;//10;
    //private int vertexShaderAttributeTexOffset = 13;

    public ShaderProgramParticle(String name) throws Exception {
        super(name);
    }

    @Override
    public void setupAttribLocations() {
        ShaderManager.glBindAttribLocation(getProgramId(), vertexShaderAttributeIndexPosition, "position");
        ShaderManager.glBindAttribLocation(getProgramId(), vertexShaderAttributeTexCoord, "texCoord");
        //ShaderManager.glBindAttribLocation(programId, vertexShaderAttributeVertexNormal, "vertexNormal");
        ShaderManager.glBindAttribLocation(getProgramId(), vertexShaderAttributeModelViewMatrix, "modelViewMatrix");
        ShaderManager.glBindAttribLocation(getProgramId(), vertexShaderAttributeBrightness, "brightness");
        //ShaderManager.glBindAttribLocation(programId, vertexShaderAttributeRGBA, "rgba");
        ShaderManager.glBindAttribLocation(getProgramId(), vertexShaderAttributeRGBATest, "rgbaTest");
    }
}
