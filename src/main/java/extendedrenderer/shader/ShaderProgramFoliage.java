package extendedrenderer.shader;

import extendedrenderer.particle.ShaderManager;

public class ShaderProgramFoliage extends ShaderProgram {

    public ShaderProgramFoliage(String name) throws Exception {
        super(name);
    }

    @Override
    public void setupAttribLocations() {
        int index = 0;
        ShaderManager.glBindAttribLocation(getProgramId(), index++, "position");
        ShaderManager.glBindAttribLocation(getProgramId(), index++, "texCoord");
        ShaderManager.glBindAttribLocation(getProgramId(), index++, "alphaBrightness");
        ShaderManager.glBindAttribLocation(getProgramId(), index, "modelMatrix");
        index+=4;
        ShaderManager.glBindAttribLocation(getProgramId(), index++, "rgba");
        ShaderManager.glBindAttribLocation(getProgramId(), index++, "meta");
    }
}
