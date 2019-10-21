package extendedrenderer.shader;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by corosus on 25/05/17.
 */
public class MeshBufferManagerFoliage {

    //for prebuffering allowed max
    public static int numInstances = 10000;
    //public static int numInstances = 30000;

    public static HashMap<TextureAtlasSprite, InstancedMeshFoliage> lookupParticleToMesh = new HashMap<>();

    public static void setupMeshForParticle(TextureAtlasSprite sprite) {

        //drawn in order of a U shape starting top left
        float[] positions = null;

        positions = new float[]{
                -0.5f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.5f, 0.5f, 0.0f
        };

        float[] texCoords = null;

        texCoords = new float[]{
                sprite.getMinU(), sprite.getMinV(),
                sprite.getMinU(), sprite.getMaxV(),
                sprite.getMaxU(), sprite.getMaxV(),
                sprite.getMaxU(), sprite.getMinV()
        };


        int[] indices = new int[] {
                0, 1, 3, 3, 1, 2
        };

        InstancedMeshFoliage mesh = new InstancedMeshFoliage(positions, texCoords, indices, numInstances);

        if (!lookupParticleToMesh.containsKey(sprite)) {
            lookupParticleToMesh.put(sprite, mesh);
        } else {
            System.out.println("WARNING: duplicate entry attempt for particle sprite: " + sprite);
        }
    }

    public static void cleanup() {
        for (Map.Entry<TextureAtlasSprite, InstancedMeshFoliage> entry : lookupParticleToMesh.entrySet()) {
            entry.get().cleanup();
        }
        lookupParticleToMesh.clear();
    }

    public static InstancedMeshFoliage getMesh(TextureAtlasSprite sprite) {
        return lookupParticleToMesh.get(sprite);
    }

    public static void setupMeshIfMissing(TextureAtlasSprite sprite) {
        if (!lookupParticleToMesh.containsKey(sprite)) {
            setupMeshForParticle(sprite);
        }
    }
}

