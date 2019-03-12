package CoroUtil.client.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
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

        //TODO: broken for some unknown reason

        int r = 255;
        int g = 0;
        int b = 0;
        int a = 150;

        TextureAtlasSprite tas = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(Blocks.ICE.getDefaultState()).getParticleTexture();

        double u = tas.getMinU();
        double v = tas.getMinV();

        double f = tas.getMinU();
        double f1 = tas.getMaxU();
        double f2 = tas.getMinV();
        double f3 = tas.getMaxV();

        //buffer.setTranslation(pos.getX(), pos.getY(), pos.getZ());

        buffer.setTranslation(2, 1, 0);

        //temp
        /*buffer.pos(-1, -1, 0).color(r, g, b, a).tex(u, v).lightmap(255, 255).endVertex();
        buffer.pos(-1, 1, 0).color(r, g, b, a).tex(u, v).lightmap(0, 0).endVertex();
        buffer.pos(1, 1, 0).color(r, g, b, a).tex(u, v).lightmap(0, 0).endVertex();
        buffer.pos(1, -1, 0).color(r, g, b, a).tex(u, v).lightmap(0, 0).endVertex();*/

        buffer.pos(-1, -1, 0).color(r, g, b, a).tex((double)f1, (double)f3).lightmap(255, 255).endVertex();
        buffer.pos(-1, 1, 0).color(r, g, b, a).tex((double)f1, (double)f2).lightmap(0, 0).endVertex();
        buffer.pos(1, 1, 0).color(r, g, b, a).tex((double)f, (double)f2).lightmap(0, 0).endVertex();
        buffer.pos(1, -1, 0).color(r, g, b, a).tex((double)f, (double)f3).lightmap(0, 0).endVertex();

        buffer.setTranslation(0, 0, 0);
    }

    public void tick() {
        World world = Minecraft.getMinecraft().world;

        if (world == null) return;

        if (world.getTotalWorldTime() % 10 == 0) {
            world.spawnParticle(EnumParticleTypes.REDSTONE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0D, 0.0D, 0.0D);
        }
    }

    public boolean isExpired(World world) {
        return renderUntilTime < world.getTotalWorldTime();
    }
}
