package extendedrenderer.shadertest;


import com.google.common.base.Charsets;
import com.google.common.io.Files;
import extendedrenderer.shadertest.gametest.Mesh;
import extendedrenderer.shadertest.gametest.Window;
import org.lwjgl.BufferUtils;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    private ShaderProgram shaderProgram;

    public Renderer() {
    }

    public void init() throws Exception {
        shaderProgram = new ShaderProgram();

        String folderShaders = "/mnt/e/git/CoroUtil_1.10.2/src/main/resources/assets/coroutil/shaders/";
        String vertex = Files.toString(new File(folderShaders + "/" + "vertex.vs"), Charsets.UTF_8);
        String fragment = Files.toString(new File(folderShaders + "/" + "fragment.fs"), Charsets.UTF_8);

        /*String vertex = "#version 120\n" +
                "\n" +
                "in vec3 position;\n" +
                "uniform mat4 projectionMatrix;\n" +
                "\n" +
                "void main()\n" +
                "{\n" +
                "\tgl_Position = vec4(position, 1.0);\n" +
                "}";
        String fragment = "#version 120\n" +
                "\n" +
                "varying out vec4 fragColor;\n" +
                "\n" +
                "void main()\n" +
                "{\n" +
                "\tfragColor = vec4(0.0, 0.5, 0.5, 1.0);\n" +
                "}";*/
        shaderProgram.createVertexShader(vertex);
        shaderProgram.createFragmentShader(fragment);
        shaderProgram.link();


    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(Window window, Mesh mesh) {
        //clear();

        if (window != null && window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        shaderProgram.bind();

        // Bind to the VAO
        glBindVertexArray(mesh.getVaoId());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        // Draw the vertices
        //glDrawArrays(GL_TRIANGLES, 0, mesh.getVertexCount());
        glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);

        // Restore state
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);

        shaderProgram.unbind();
    }

    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }


    }
}
