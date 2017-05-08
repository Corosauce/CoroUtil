package extendedrenderer.shadertest.gametest;

import extendedrenderer.shadertest.Renderer;

public class DummyGame implements IGameLogic {

    private int direction = 0;

    private float color = 0.0f;

    public final Renderer renderer;

    private Mesh mesh;

    public Mesh getMesh() {
        return mesh;
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public DummyGame() {
        renderer = new Renderer();
    }
    
    @Override
    public void init() throws Exception {
        renderer.init();

        float[] vertices = new float[]{
                -0.5f,  0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.5f,  0.5f, 0.0f
        };

        int[] indices = new int[] {
                0, 1, 3, 3, 1, 2
        };

        mesh = new Mesh(vertices, indices);
    }

    @Override
    public void input(Window window) {
        /*if (window.isKeyPressed(GLFW_KEY_UP)) {
            direction = 1;
        } else if (window.isKeyPressed(GLFW_KEY_DOWN)) {
            direction = -1;
        } else {
            direction = 0;
        }*/
    }

    @Override
    public void update(float interval) {
        color += direction * 0.01f;
        if (color > 1) {
            color = 1.0f;
        } else if (color < 0) {
            color = 0.0f;
        }
    }

    @Override
    public void render(Window window) {
        if (window != null) window.setClearColor(color, color, color, 0.0f);
        renderer.render(window, mesh);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
    }

}