package extendedrenderer.shader;

import extendedrenderer.particle.ShaderManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public abstract class ShaderProgram {

    private String name;

    private final int programId;

    private int vertexShaderId;

    private int fragmentShaderId;

    public Map<String, Integer> uniforms;

    public ShaderProgram(String name) throws Exception {
        this.name = name;
        programId = OpenGlHelper.glCreateProgram();
        if (programId == 0) {
            throw new Exception("Could not create Shader");
        }
        uniforms = new HashMap<>();
    }

    public void createUniform(String uniformName) throws Exception {
        int uniformLocation = OpenGlHelper.glGetUniformLocation(programId, uniformName);
        if (uniformLocation < 0) {
            System.out.println("!!!!!!!! GLSL OPTIMIZATION WARNING MAYBE: " + "Could not find uniform:" + uniformName);
            //throw new Exception("Could not find uniform:" + uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }

    public void setUniform(String uniformName, Matrix4fe value) {
        /*try (MemoryStack stack = MemoryStack.stackPush()) {
            // Dump the matrix into a float buffer
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
        }*/
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        value.get(fb);
        OpenGlHelper.glUniformMatrix4(uniforms.get(uniformName), false, fb);
    }

    public void setUniformEfficient(String uniformName, Matrix4fe value, FloatBuffer buffer) {
        /*try (MemoryStack stack = MemoryStack.stackPush()) {
            // Dump the matrix into a float buffer
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
        }*/
        //FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        value.get(buffer);
        OpenGlHelper.glUniformMatrix4(uniforms.get(uniformName), false, buffer);
    }

    public void setUniform(String uniformName, int value) {
        OpenGlHelper.glUniform1i(uniforms.get(uniformName), value);
    }

    public void setUniform(String uniformName, float value) {
        GL20.glUniform1f(uniforms.get(uniformName), value);
        //OpenGlHelper.glUniform(uniforms.get(uniformName), value);
    }

    public void createVertexShader(String shaderCode) throws Exception {
        vertexShaderId = createShader(shaderCode, GL20.GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String shaderCode) throws Exception {
        fragmentShaderId = createShader(shaderCode, GL20.GL_FRAGMENT_SHADER);
    }

    protected int createShader(String shaderCode, int shaderType) throws Exception {
        int shaderId = OpenGlHelper.glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new Exception("Error creating shader. Type: " + shaderType);
        }

        ShaderManager.glShaderSource(shaderId, shaderCode);
        OpenGlHelper.glCompileShader(shaderId);

        if (OpenGlHelper.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0) {
            throw new Exception("Error compiling Shader code: " + OpenGlHelper.glGetShaderInfoLog(shaderId, 1024));
        }

        OpenGlHelper.glAttachShader(programId, shaderId);

        //replaces use of "layout (location =0) " type indexing in shader, since that isnt supported for GLSL 120
        if (shaderType == GL20.GL_VERTEX_SHADER) {
            setupAttribLocations();
        }

        return shaderId;
    }

    public abstract void setupAttribLocations();

    public void link() throws Exception {
        OpenGlHelper.glLinkProgram(programId);
        if (OpenGlHelper.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + OpenGlHelper.glGetProgramInfoLog(programId, 1024));
        }

        if (vertexShaderId != 0) {
            GL20.glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            GL20.glDetachShader(programId, fragmentShaderId);
        }

        //standard practice suggests this be removed for release builds, is also probably why OpenGlHelper doesnt have it for ARB check
        GL20.glValidateProgram(programId);
        if (OpenGlHelper.glGetProgrami(programId, GL20.GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + OpenGlHelper.glGetProgramInfoLog(programId, 1024));
        }

    }

    public void bind() {
        OpenGlHelper.glUseProgram(programId);
    }

    public void unbind() {
        OpenGlHelper.glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if (programId != 0) {
            OpenGlHelper.glDeleteProgram(programId);
        }
    }

    public String getName() {
        return this.name;
    }

    public int getProgramId() {
        return programId;
    }
}
