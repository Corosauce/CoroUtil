package extendedrenderer.particle;

import extendedrenderer.shadertest.gametest.InstancedMesh;
import extendedrenderer.shadertest.gametest.Mesh;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by corosus on 25/05/17.
 */
public class ParticleMeshBufferManager {

    //for prebuffering allowed max
    public static int numInstances = 20000;

    private static HashMap<TextureAtlasSprite, InstancedMesh> lookupParticleToMesh = new HashMap<>();

    public static void setupMeshForParticle(TextureAtlasSprite sprite) {
        float[] positions = new float[]{
                -0.5f,  0.5f,  0.0f,
                -0.5f, -0.5f,  0.0f,
                0.5f, -0.5f,  0.0f,
                0.5f,  0.5f,  0.0f
        };

        float[] texCoords = new float[]{
                sprite.getMinU(), sprite.getMinV(),
                sprite.getMinU(), sprite.getMaxV(),
                sprite.getMaxU(), sprite.getMaxV(),
                sprite.getMaxU(), sprite.getMinV()
        };

        int[] indices = new int[] {
                0, 1, 3, 3, 1, 2
        };

        InstancedMesh mesh = new InstancedMesh(positions, texCoords, indices, numInstances);

        if (!lookupParticleToMesh.containsKey(sprite)) {
            lookupParticleToMesh.put(sprite, mesh);
        } else {
            System.out.println("WARNING: duplicate entry attempt for particle sprite: " + sprite);
        }
    }

    public static void cleanup() {
        for (Map.Entry<TextureAtlasSprite, InstancedMesh> entry : lookupParticleToMesh.entrySet()) {
            entry.getValue().cleanup();
        }
        lookupParticleToMesh.clear();
    }
}
