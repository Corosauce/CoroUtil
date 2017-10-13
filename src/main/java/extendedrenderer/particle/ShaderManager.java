package extendedrenderer.particle;

import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.GL_SHADING_LANGUAGE_VERSION;

/**
 * Created by corosus on 25/05/17.
 *
 * Manages checking support for instanced rendering
 *
 * Minimum opengl version required for core method use is 3.3 due to use of glVertexAttribDivisor which is part of Instanced Arrays feature
 * Otherwise will attempt to use ARB versions of methods if available
 */
public class ShaderManager {

    private static boolean check = true;

    private static boolean canUseShaders = false;
    private static boolean canUseShadersInstancedRendering = false;

    private static boolean useARBInstancedRendering = false;
    private static boolean useARBShaders = false;
    private static boolean useARBVBO = false;
    private static boolean useARBInstancedArrays = false;

    public static boolean canUseShadersInstancedRendering() {
        if (check) {
            check = false;
            queryGLCaps();
        }
        return canUseShadersInstancedRendering;
    }

    public static void disableShaders() {
        canUseShaders = false;
        canUseShadersInstancedRendering = false;
    }

    public static void queryGLCaps() {
        ContextCapabilities contextcapabilities = GLContext.getCapabilities();

        System.out.println("Extended Renderer: detected GLSL version: " + GL11.glGetString(GL_SHADING_LANGUAGE_VERSION));

        useARBVBO = !contextcapabilities.OpenGL15 && contextcapabilities.GL_ARB_vertex_buffer_object;

        if (contextcapabilities.OpenGL21 || contextcapabilities.GL_ARB_vertex_shader && contextcapabilities.GL_ARB_fragment_shader && contextcapabilities.GL_ARB_shader_objects) {
            canUseShaders = true;

            if (contextcapabilities.OpenGL21) {
                useARBShaders = false;
            } else {
                useARBShaders = true;
            }

            if (contextcapabilities.OpenGL33 || (contextcapabilities.GL_ARB_draw_instanced && contextcapabilities.GL_ARB_instanced_arrays)) {
                canUseShadersInstancedRendering = true;

                if (contextcapabilities.OpenGL33) {
                    useARBInstancedRendering = false;
                    useARBInstancedArrays = false;
                } else {
                    useARBInstancedRendering = true;
                    useARBInstancedArrays = true;
                }
            } else {
                System.out.println("Extended Renderer WARNING: Unable to use instanced rendering shaders, OpenGL33: " + contextcapabilities.OpenGL33 + ", (" +
                        "GL_ARB_draw_instanced: " + contextcapabilities.GL_ARB_draw_instanced + ", " +
                        "GL_ARB_instanced_arrays: " + contextcapabilities.GL_ARB_instanced_arrays + ")");
                canUseShadersInstancedRendering = false;
            }
        } else {
            System.out.println("Extended Renderer WARNING: Unable to use shaders, OpenGL21: " + contextcapabilities.OpenGL21 + ", (" +
                    "GL_ARB_vertex_shader: " + contextcapabilities.GL_ARB_vertex_shader + ", " +
                    "GL_ARB_fragment_shader: " + contextcapabilities.GL_ARB_fragment_shader + ", " +
                    "GL_ARB_shader_objects: " + contextcapabilities.GL_ARB_shader_objects + ")");
            canUseShadersInstancedRendering = false;
        }
    }

    public static void glDrawElementsInstanced(int mode, int indices_count, int type, long indices_buffer_offset, int primcount) {
        if (useARBInstancedRendering) {
            ARBDrawInstanced.glDrawElementsInstancedARB(mode, indices_count, type, indices_buffer_offset, primcount);
        } else {
            GL31.glDrawElementsInstanced(mode, indices_count, type, indices_buffer_offset, primcount);
        }

    }

    public static void glShaderSource(int shader, CharSequence string) {
        if (useARBShaders) {
            ARBShaderObjects.glShaderSourceARB(shader, string);
        } else {
            GL20.glShaderSource(shader, string);
        }
    }

    public static void glBindAttribLocation(int program, int index, CharSequence name) {
        if (useARBShaders) {
            ARBVertexShader.glBindAttribLocationARB(program, index, name);
        } else {
            GL20.glBindAttribLocation(program, index, name);
        }
    }

    public static void glBufferData(int target, FloatBuffer data, int usage) {
        if (useARBVBO) {
            ARBVertexBufferObject.glBufferDataARB(target, data, usage);
        } else {
            GL15.glBufferData(target, data, usage);
        }
    }

    public static void glBufferData(int target, IntBuffer data, int usage) {
        if (useARBVBO) {
            ARBVertexBufferObject.glBufferDataARB(target, data, usage);
        } else {
            GL15.glBufferData(target, data, usage);
        }
    }

    public static void glVertexAttribDivisor(int index, int divisor) {
        if (useARBInstancedArrays) {
            ARBInstancedArrays.glVertexAttribDivisorARB(index, divisor);
        } else {
            GL33.glVertexAttribDivisor(index, divisor);
        }
    }

    public static void glBindVertexArray(int array) {
        if (useARBVBO) {
            ARBVertexArrayObject.glBindVertexArray(array);
        } else {
            GL30.glBindVertexArray(array);
        }
    }

    public static void glDeleteVertexArrays(int array) {
        if (useARBVBO) {
            ARBVertexArrayObject.glDeleteVertexArrays(array);
        } else {
            GL30.glDeleteVertexArrays(array);
        }
    }

}
