package CoroUtil.client.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DebugRenderer {

    private static List<DebugRenderEntry> listRenderables = new ArrayList<>();

    public static void addRenderable(DebugRenderEntry entry) {
        listRenderables.add(entry);
        //CULog.dbg("add renderable, new size: " + listRenderables.size());
    }

    public static void tickClient() {
        World world = Minecraft.getInstance().world;

        if (world == null) return;

        //filter out expired renders
        Iterator<DebugRenderEntry> it = listRenderables.listIterator();
        while (it.hasNext()) {
            DebugRenderEntry entry = it.next();
            if (entry.isExpired(world)) {
                //CULog.dbg("remove expired renderable");
                it.remove();
            }
        }

        if (listRenderables.size() > 0) {
            it = listRenderables.listIterator();
            while (it.hasNext()) {
                DebugRenderEntry entry = it.next();

                //entry.tick();
            }
        }
    }

    public static void renderDebug(RenderWorldLastEvent event) {

        World world = Minecraft.getInstance().world;

        if (world == null) return;

        if (listRenderables.size() > 0) {

            //CULog.dbg("renderables: " + listRenderables.size());

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();

            bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

            Iterator<DebugRenderEntry> it = listRenderables.listIterator();
            while (it.hasNext()) {
                DebugRenderEntry entry = it.next();

                entry.addToBuffer(bufferBuilder);
            }

            renderBatch(tessellator, bufferBuilder);

            it = listRenderables.listIterator();
            while (it.hasNext()) {
                DebugRenderEntry entry = it.next();

                //for testing
                //entry.renderImmediate();
            }
        }
    }

    public static void renderBatch(Tessellator tessellator, BufferBuilder bufferBuilder) {

        //Minecraft.getInstance().renderEngine.bindTexture(net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE);
        //Minecraft.getInstance().renderEngine.bindTexture(new ResourceLocation("textures/particle/particles.png"));
        RenderHelper.disableStandardItemLighting();

        boolean translucency = true;
        if (translucency) {
            //GlStateManager.disableDepth();
            GlStateManager.blendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableBlend();
        } else {
            GlStateManager.disableBlend();
            //GlStateManager.enableDepth();
            GlStateManager.depthMask(false);
        }

        //GlStateManager.disableCull();
        GlStateManager.disableTexture2D();
        //GlStateManager.disableDepth();

        if (Minecraft.isAmbientOcclusionEnabled())
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
        EntityRendererManager rm = Minecraft.getInstance().getRenderManager();
        //tessellator.getBuffer().sortVertexData((float) rm.renderPosX, (float) rm.renderPosY, (float) rm.renderPosZ);
        tessellator.draw();

        //GlStateManager.enableDepth();
        if (translucency) {

        } else {
            GlStateManager.enableBlend();
            //GlStateManager.disableDepth();
        }

        GlStateManager.enableDepth();

        GlStateManager.enableTexture2D();
        RenderHelper.enableStandardItemLighting();
        //drawingBatch = false;
    }

    public static void debugPathfinding(CreatureEntity ent) {
        if (!ent.world.isRemote) {
            if (!ent.getNavigator().noPath()) {
                for (int k = 0; k < ent.getNavigator().getPath().getCurrentPathLength(); ++k)
                {
                    PathPoint pathpoint2 = ent.getNavigator().getPath().getPathPointFromIndex(k);

                    CoroUtil.client.debug.DebugRenderer.addRenderable(new DebugRenderEntry(new BlockPos(pathpoint2.x, pathpoint2.y, pathpoint2.z), ent.world.getGameTime() + 100, 0x00FF00));
                }
            }
        }
    }

    public static void clearRenderables() {
        listRenderables.clear();
    }

}

