package extendedrenderer.particle;

import extendedrenderer.shadertest.gametest.InstancedMesh;
import extendedrenderer.shadertest.gametest.Mesh;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by corosus on 25/05/17.
 */
public class ParticleMeshBufferManager {

    //for prebuffering allowed max
    public static int numInstances = 100000;

    private static HashMap<TextureAtlasSprite, InstancedMesh> lookupParticleToMesh = new HashMap<>();

    public static void setupMeshForParticle(TextureAtlasSprite sprite) {

        //drawn in order of a U shape starting top left
        float[] positions = null;

        if (sprite == ParticleRegistry.rain_white_2) {
            float sizeMax = 16;
            float sizeX = 1;
            float sizeY = 15;
            float facX = 1F / sizeMax * sizeX;
            float facY = 1F / sizeMax * sizeY;

            positions = new float[]{
                    -0.5f * facX, 0.5f * facY, 0.0f,
                    -0.5f * facX, -0.5f * facY, 0.0f,
                    0.5f * facX, -0.5f * facY, 0.0f,
                    0.5f * facX, 0.5f * facY, 0.0f
            };
        } else {
            positions = new float[]{
                    -0.5f, 0.5f, 0.0f,
                    -0.5f, -0.5f, 0.0f,
                    0.5f, -0.5f, 0.0f,
                    0.5f, 0.5f, 0.0f
            };
        }

        float[] texCoords = null;

        //testing rendering just the populated pixel area for the texture
        if (sprite == ParticleRegistry.rain_white_2) {

            //rain is 1x15

            /*float pixel = 0.0624375F;
            float minU = sprite.getMinU();
            float minV = sprite.getMinV();
            float maxU = minU + pixel;
            float maxV = minV + (pixel * 15F);*/

            float minU = sprite.getInterpolatedU(0);
            float minV = sprite.getInterpolatedV(0);
            float maxU = sprite.getInterpolatedU(1);
            float maxV = sprite.getInterpolatedV(15);

            texCoords = new float[]{
                    minU, minV,
                    minU, maxV,
                    maxU, maxV,
                    maxU, minV
            };

            /*texCoords = new float[]{
                    sprite.getMinU(), sprite.getMinV(),
                    sprite.getMinU(), sprite.getMaxV(),
                    sprite.getMaxU(), sprite.getMaxV(),
                    sprite.getMaxU(), sprite.getMinV()
            };*/
        } else {
            texCoords = new float[]{
                    sprite.getMinU(), sprite.getMinV(),
                    sprite.getMinU(), sprite.getMaxV(),
                    sprite.getMaxU(), sprite.getMaxV(),
                    sprite.getMaxU(), sprite.getMinV()
            };
        }

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

    public static InstancedMesh getMesh(TextureAtlasSprite sprite) {
        return lookupParticleToMesh.get(sprite);
    }
}
