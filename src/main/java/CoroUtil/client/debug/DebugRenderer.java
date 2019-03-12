package CoroUtil.client.debug;

import CoroUtil.forge.CULog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DebugRenderer {

    private static List<DebugRenderEntry> listRenderables = new ArrayList<>();

    public static void addRenderable(DebugRenderEntry entry) {
        listRenderables.add(entry);
        CULog.dbg("add renderable, new size: " + listRenderables.size());
    }

    public static void tickClient() {
        World world = Minecraft.getMinecraft().world;

        if (world == null) return;

        //filter out expired renders
        Iterator<DebugRenderEntry> it = listRenderables.listIterator();
        while (it.hasNext()) {
            DebugRenderEntry entry = it.next();
            if (entry.isExpired(world)) {
                CULog.dbg("remove expired renderable");
                it.remove();
            }
        }

        if (listRenderables.size() > 0) {
            it = listRenderables.listIterator();
            while (it.hasNext()) {
                DebugRenderEntry entry = it.next();

                entry.tick();
            }
        }
    }

    public static void renderDebug(RenderWorldLastEvent event) {

        World world = Minecraft.getMinecraft().world;

        if (world == null) return;

        if (listRenderables.size() > 0) {

            //CULog.dbg("renderables: " + listRenderables.size());

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            bufferBuilder.begin(org.lwjgl.opengl.GL11.GL_QUADS, net.minecraft.client.renderer.vertex.DefaultVertexFormats.BLOCK);

            Iterator<DebugRenderEntry> it = listRenderables.listIterator();
            while (it.hasNext()) {
                DebugRenderEntry entry = it.next();

                entry.addToBuffer(bufferBuilder);
            }

            renderBatch(tessellator, bufferBuilder);
        }
    }

    public static void renderBatch(Tessellator tessellator, BufferBuilder bufferBuilder) {

        //Minecraft.getMinecraft().renderEngine.bindTexture(net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("textures/particle/particles.png"));
        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();

        if (net.minecraft.client.Minecraft.isAmbientOcclusionEnabled())
        {
            GlStateManager.shadeModel(org.lwjgl.opengl.GL11.GL_SMOOTH);
        }
        else
        {
            GlStateManager.shadeModel(org.lwjgl.opengl.GL11.GL_FLAT);
        }

        /*if(pass > 0)
        {
            tessellator.getBuffer().sortVertexData(0, 0, 0);
        }*/
        tessellator.draw();

        net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
        //drawingBatch = false;
    }

}
