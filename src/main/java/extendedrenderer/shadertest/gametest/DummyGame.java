package extendedrenderer.shadertest.gametest;

import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.shadertest.Renderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.ArrayList;
import java.util.List;

public class DummyGame implements IGameLogic {

    private int direction = 0;

    private float color = 0.0f;

    public final Renderer renderer;

    public final Camera camera;

    public List<GameItem> gameItems;

    public DummyGame() {
        renderer = new Renderer();
        camera = new Camera();
        gameItems = new ArrayList<>();
    }
    
    @Override
    public void init() throws Exception {
        renderer.init();

        float[] positions = new float[]{
                -0.5f,  0.5f,  0.0f,
                -0.5f, -0.5f,  0.0f,
                0.5f, -0.5f,  0.0f,
                0.5f,  0.5f,  0.0f
        };

        /*float[] texCoords = new float[]{
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };*/

        TextureAtlasSprite ta = ParticleRegistry.rain_white;

        float[] texCoords = new float[]{
                ta.getMinU(), ta.getMinV(),
                ta.getMinU(), ta.getMaxV(),
                ta.getMaxU(), ta.getMaxV(),
                ta.getMaxU(), ta.getMinV()
        };


        int[] indices = new int[] {
                0, 1, 3, 3, 1, 2
        };

        int instances = 300;
        int instancesRender = instances * Mesh.extraRenders;

        Mesh mesh = new InstancedMesh(positions, texCoords, indices, instancesRender);
        for (int i = 0; i < instances; i++) {
            GameItem gameItem = new GameItem(mesh);
            gameItem.setPosition(0, 0, -2);
            gameItems.add(gameItem);
        }

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
        renderer.render(window, camera, gameItems);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
    }

}