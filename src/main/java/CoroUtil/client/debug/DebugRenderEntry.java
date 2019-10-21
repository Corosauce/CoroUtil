package CoroUtil.client.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DebugRenderEntry {

    public BlockPos pos;
    public long renderUntilTime = -1;
    public int color;
    public AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 1, 1, 1);

    public DebugRenderEntry(BlockPos pos, long renderUntilTime, int color) {
        this.pos = pos;
        this.renderUntilTime = renderUntilTime;
        this.color = color;
    }

    public void addToBuffer(BufferBuilder buffer) {
        /**
         * Using:
         * DefaultVertexFormats.BLOCK:
         * BLOCK.addElement(POSITION_3F);
         * BLOCK.addElement(COLOR_4UB);
         * BLOCK.addElement(TEX_2F);
         * BLOCK.addElement(TEX_2S); - lightmap
         */

        /*int r = 255;
        int g = 0;
        int b = 0;*/
        int a = 50;

        int r = (color >> 16) & 0xff;
        int g = (color >> 8) & 0xff;
        int b = color & 0xff;

        //TextureAtlasSprite tas = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(Blocks.ICE.getDefaultState()).getParticleTexture();

        double u = 0;//tas.getMinU();
        double v = 0;//tas.getMinV();

        /*double f = tas.getMinU();
        double f1 = tas.getMaxU();
        double f2 = tas.getMinV();
        double f3 = tas.getMaxV();*/

        double f = 0;
        double f1 = 0;
        double f2 = 0;
        double f3 = 0;

        EntityRendererManager rm = Minecraft.getMinecraft().getRenderManager();

        buffer.setTranslation(pos.getX() + 0.5F - rm.renderPosX, pos.getY() + 0.5F - rm.renderPosY, pos.getZ() + 0.5F - rm.renderPosZ);

        //buffer.setTranslation(-127 + 0.5F - rm.renderPosX, 64 + 0.5F - rm.renderPosY, 241 + 0.5F - rm.renderPosZ);

        //buffer.setTranslation(2, 1, 0);

        float sizeRadius = 0.501F;

        /**
         * render order for front facing quads (working north used with face culling on):
         * B----C
         * |    |
         * A    D
         *
         */

        //south face
        buffer.pos(-sizeRadius, sizeRadius, sizeRadius).color(r, g, b, a).tex(f1, f2).lightmap(0, 0).endVertex();
        buffer.pos(-sizeRadius, -sizeRadius, sizeRadius).color(r, g, b, a).tex(f1, f3).lightmap(0, 0).endVertex();
        buffer.pos(sizeRadius, -sizeRadius, sizeRadius).color(r, g, b, a).tex(f, f3).lightmap(0, 0).endVertex();
        buffer.pos(sizeRadius, sizeRadius, sizeRadius).color(r, g, b, a).tex(f, f2).lightmap(0, 0).endVertex();

        //north face
        buffer.pos(-sizeRadius, -sizeRadius, -sizeRadius).color(r, g, b, a).tex(f1, f3).lightmap(0, 0).endVertex();
        buffer.pos(-sizeRadius, sizeRadius, -sizeRadius).color(r, g, b, a).tex(f1, f2).lightmap(0, 0).endVertex();
        buffer.pos(sizeRadius, sizeRadius, -sizeRadius).color(r, g, b, a).tex(f, f2).lightmap(0, 0).endVertex();
        buffer.pos(sizeRadius, -sizeRadius, -sizeRadius).color(r, g, b, a).tex(f, f3).lightmap(0, 0).endVertex();

        //east face
        buffer.pos(sizeRadius, -sizeRadius, -sizeRadius).color(r, g, b, a).tex(f1, f3).lightmap(0, 0).endVertex();
        buffer.pos(sizeRadius, sizeRadius, -sizeRadius).color(r, g, b, a).tex(f1, f2).lightmap(0, 0).endVertex();
        buffer.pos(sizeRadius, sizeRadius, sizeRadius).color(r, g, b, a).tex(f, f2).lightmap(0, 0).endVertex();
        buffer.pos(sizeRadius, -sizeRadius, sizeRadius).color(r, g, b, a).tex(f, f3).lightmap(0, 0).endVertex();

        //west face
        buffer.pos(-sizeRadius, sizeRadius, -sizeRadius).color(r, g, b, a).tex(f1, f2).lightmap(0, 0).endVertex();
        buffer.pos(-sizeRadius, -sizeRadius, -sizeRadius).color(r, g, b, a).tex(f1, f3).lightmap(0, 0).endVertex();
        buffer.pos(-sizeRadius, -sizeRadius, sizeRadius).color(r, g, b, a).tex(f, f3).lightmap(0, 0).endVertex();
        buffer.pos(-sizeRadius, sizeRadius, sizeRadius).color(r, g, b, a).tex(f, f2).lightmap(0, 0).endVertex();

        //top face
        buffer.pos(sizeRadius, sizeRadius, -sizeRadius).color(r, g, b, a).tex(f1, f2).lightmap(0, 0).endVertex();
        buffer.pos(-sizeRadius, sizeRadius, -sizeRadius).color(r, g, b, a).tex(f1, f3).lightmap(0, 0).endVertex();
        buffer.pos(-sizeRadius, sizeRadius, sizeRadius).color(r, g, b, a).tex(f, f3).lightmap(0, 0).endVertex();
        buffer.pos(sizeRadius, sizeRadius, sizeRadius).color(r, g, b, a).tex(f, f2).lightmap(0, 0).endVertex();

        //bottom face
        buffer.pos(-sizeRadius, -sizeRadius, -sizeRadius).color(r, g, b, a).tex(f1, f3).lightmap(0, 0).endVertex();
        buffer.pos(sizeRadius, -sizeRadius, -sizeRadius).color(r, g, b, a).tex(f1, f2).lightmap(0, 0).endVertex();
        buffer.pos(sizeRadius, -sizeRadius, sizeRadius).color(r, g, b, a).tex(f, f2).lightmap(0, 0).endVertex();
        buffer.pos(-sizeRadius, -sizeRadius, sizeRadius).color(r, g, b, a).tex(f, f3).lightmap(0, 0).endVertex();

        /*buffer.pos(-1, -1, 0).color(r, g, b, a).tex((double)f1, (double)f3).lightmap(255, 255).endVertex();
        buffer.pos(-1, 1, 0).color(r, g, b, a).tex((double)f1, (double)f2).lightmap(0, 0).endVertex();
        buffer.pos(1, 1, 0).color(r, g, b, a).tex((double)f, (double)f2).lightmap(0, 0).endVertex();
        buffer.pos(1, -1, 0).color(r, g, b, a).tex((double)f, (double)f3).lightmap(0, 0).endVertex();*/

        buffer.setTranslation(0, 0, 0);
    }

    public void tick() {
        World world = Minecraft.getMinecraft().world;

        if (world == null) return;

        if (world.getTotalWorldTime() % 10 == 0) {
            world.spawnParticle(ParticleTypes.REDSTONE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0D, 0.0D, 0.0D);
        }
    }

    public boolean isExpired(World world) {
        return renderUntilTime < world.getTotalWorldTime();
    }
}
