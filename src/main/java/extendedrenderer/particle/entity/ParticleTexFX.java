package extendedrenderer.particle.entity;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;

public class ParticleTexFX extends EntityRotFX {

	public ParticleTexFX(World worldIn, double posXIn, double posYIn, double posZIn, double mX, double mY, double mZ, TextureAtlasSprite par8Item)
    {
        super(worldIn, posXIn, posYIn, posZIn, mX, mY-0.5, mZ);
        this.setParticleTexture(par8Item);
        //this.setParticleTexture(Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(Items.IRON_AXE, 0));
        this.particleRed = 1.0F;
        this.particleGreen = 1.0F;
        this.particleBlue = 1.0F;
        this.particleGravity = 1F;
        this.particleScale = 1F;
        this.setMaxAge(100);
        this.setCanCollide(false);
    }

    @Override
    public int getFXLayer()
    {
        return 1;
    }
}
