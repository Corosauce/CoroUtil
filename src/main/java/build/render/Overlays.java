package build.render;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import CoroUtil.util.CoroUtilBlock;
import build.world.Build;
import build.world.BuildManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Overlays {

	public static void renderBuildPreview(Build b, Vec3 pos, Vec3 angle) {
		
		Tessellator tess = Tessellator.instance;
		float scale = 1F;
		GL11.glPushMatrix();

		Minecraft mc = Minecraft.getMinecraft();
		
		Vec3 posPlayer = Vec3.createVectorHelper(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
		GL11.glTranslated(-posPlayer.xCoord, -posPlayer.yCoord, -posPlayer.zCoord);
		GL11.glTranslated(pos.xCoord-0.5D, pos.yCoord+0.5D, pos.zCoord-0.5D);
		
		for (int x = 0; x < b.map_sizeX; x++) {
			for (int y = 0; y < b.map_sizeY; y++) {
				for (int z = 0; z < b.map_sizeZ; z++) {
		    		
					Block block = b.build_blockIDArr[x][y][z];
		    		int meta = b.build_blockMetaArr[x][y][z];
		    		
		    		if (!CoroUtilBlock.isAir(block)) {
		    			//Block block = Block.blocksList[id];
		    			
		    			if (block != null) {
		    				
		    				GL11.glPushMatrix();
		    				
		    				GL11.glTranslated(x * scale, y * scale, z * scale);
		    				
		    				renderBlock(block, meta, 0, 0, 0);
		    				
		    				GL11.glPopMatrix();
		    			}
		    		}					
				}
			}
		}
		
		GL11.glPopMatrix();
	}
	
	public static void renderBuildOutline(Build b, int dir) {
		float x = b.map_coord_minX;// + 0.5F;
		float y = b.map_coord_minY;// + 0.5F;
		float z = b.map_coord_minZ;// + 0.5F;
		float x1 = b.map_sizeX + x - 1F;
		float y1 = b.map_sizeY + y - 1F;
		float z1 = b.map_sizeZ + z - 1F;
		
		ChunkCoordinates c1 = BuildManager.rotate(new ChunkCoordinates((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z)), dir, Vec3.createVectorHelper(b.map_coord_minX, b.map_coord_minY, b.map_coord_minZ), Vec3.createVectorHelper(b.map_sizeX, b.map_sizeY, b.map_sizeZ));
		ChunkCoordinates c2 = BuildManager.rotate(new ChunkCoordinates((int)Math.floor(x1), (int)Math.floor(y1), (int)Math.floor(z1)), dir, Vec3.createVectorHelper(b.map_coord_minX, b.map_coord_minY, b.map_coord_minZ), Vec3.createVectorHelper(b.map_sizeX, b.map_sizeY, b.map_sizeZ));
		
		renderBuildOutline(c1.posX, c1.posY, c1.posZ, c2.posX, c2.posY, c2.posZ);
	}
	
	public static void renderDirectionArrow(Build b, int dir) {
		float x = (float)b.map_coord_minX + 0.5F + (float)b.map_sizeX / 2F;// + 0.5F;
		float y = b.map_coord_minY;
		float z = (float)b.map_coord_minZ + (float)b.map_sizeZ / 2F;// + 0.5F;
		float x1 = (float)b.map_coord_minX + (float)b.map_sizeX / 2F;// + 0.5F;
		float y1 = b.map_coord_minY;
		float z1 = (float)b.map_coord_minZ + (float)b.map_sizeZ / 2F;// + 0.5F;
		float x2 = b.map_sizeX/2 + x - 1F;
		float y2 = b.map_sizeY + y - 1F;
		float z2 = b.map_sizeZ/2 + z - 1F;
		
		ChunkCoordinates c1 = BuildManager.rotate(new ChunkCoordinates((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z)), dir, Vec3.createVectorHelper(b.map_coord_minX, b.map_coord_minY, b.map_coord_minZ), Vec3.createVectorHelper(b.map_sizeX, b.map_sizeY, b.map_sizeZ));
		ChunkCoordinates c2 = BuildManager.rotate(new ChunkCoordinates((int)Math.floor(x1), (int)Math.floor(y1), (int)Math.floor(z1)), dir, Vec3.createVectorHelper(b.map_coord_minX, b.map_coord_minY, b.map_coord_minZ), Vec3.createVectorHelper(b.map_sizeX, b.map_sizeY, b.map_sizeZ));
		
		renderLineFromToBlock(c1.posX + 0.5, y + 3.5, c1.posZ + 0.5, c2.posX + 0.5, y + 3.5, c2.posZ + 0.5, 0x00FF00);
	}
	
	/*public static void renderDirectionArrow(Vec3 pos, int dir) {
		
		//double vecX = 
		double arrowSize = 4;
		ChunkCoordinates c1 = BuildManager.rotate(new ChunkCoordinates((int)pos.xCoord, (int)pos.yCoord, (int)pos.zCoord), dir, Vec3.createVectorHelper(pos.xCoord - arrowSize/2, pos.yCoord - arrowSize/2, pos.zCoord - arrowSize/2), Vec3.createVectorHelper(arrowSize, arrowSize, arrowSize));
		ChunkCoordinates c2 = BuildManager.rotate(new ChunkCoordinates((int)pos.xCoord + 1, (int)pos.yCoord, (int)pos.zCoord), dir, Vec3.createVectorHelper(pos.xCoord - arrowSize/2, pos.yCoord - arrowSize/2, pos.zCoord - arrowSize/2), Vec3.createVectorHelper(arrowSize, arrowSize, arrowSize));
		
		renderLineFromToBlock(c1.posX, c1.posY, c1.posZ, c2.posX, c2.posY, c2.posZ, 0x00FF00);
	}*/
	
	public static void renderAABB(AxisAlignedBB aabb, int color) {
		
		double x = aabb.maxX;
		double y = aabb.maxY;
		double z = aabb.maxZ;
		double x1 = aabb.minX;
		double y1 = aabb.minY;
		double z1 = aabb.minZ;
		
		//cross intersects
		renderLineFromToBlock(x, y, z, x1, y1, z1, color);
		renderLineFromToBlock(x1, y, z1, x, y1, z, color);
		renderLineFromToBlock(x, y, z1, x1, y1, z, color);
		renderLineFromToBlock(x1, y, z, x, y1, z1, color);
		
		//corner 0
		renderLineFromToBlock(x, y, z, x, y, z1, color);
		renderLineFromToBlock(x, y, z, x, y1, z, color);
		renderLineFromToBlock(x, y, z, x1, y, z, color);
		
		//corner 1
		renderLineFromToBlock(x1, y, z, x1, y1, z, color);
		renderLineFromToBlock(x1, y, z, x1, y, z1, color);
		
		//corner 2
		renderLineFromToBlock(x1, y, z1, x1, y1, z1, color);
		renderLineFromToBlock(x1, y, z1, x, y, z1, color);
		
		//corner 3
		renderLineFromToBlock(x, y, z1, x, y1, z1, color);
		//renderLineFromToBlock(x1, y, z1, x, y, z1);
		
		//top parts
		renderLineFromToBlock(x, y1, z, x1, y1, z, color);
		renderLineFromToBlock(x1, y1, z, x1, y1, z1, color);
		renderLineFromToBlock(x1, y1, z1, x, y1, z1, color);
		renderLineFromToBlock(x, y1, z1, x, y1, z, color);
	}
	
	public static void renderBuildOutline(float x, float y, float z, float x1, float y1, float z1) {
		renderBuildOutline(x, y, z, x1, y1, z1, true, 0xFF0000);
	}
	
	public static void renderBuildOutline(double x, double y, double z, double x1, double y1, double z1, boolean center, int parColor) {
		renderBuildOutline((float)x, (float)y, (float)z, (float)x1, (float)y1, (float)z1, center, parColor);
	}
	
	public static void renderBuildOutline(float x, float y, float z, float x1, float y1, float z1, boolean center, int parColor) {
		
		if (center) {
			x += 0.5F;
			y += 0.5F;
			z += 0.5F;
			x1 += 0.5F;
			y1 += 0.5F;
			z1 += 0.5F;
		}
		
		//x += (mod_ZombieCraft.worldRef.rand.nextFloat() * 0.3F);
		
		int color = parColor;
		
		//cross intersects
		renderLineFromToBlock(x, y, z, x1, y1, z1, color);
		renderLineFromToBlock(x1, y, z1, x, y1, z, color);
		renderLineFromToBlock(x, y, z1, x1, y1, z, color);
		renderLineFromToBlock(x1, y, z, x, y1, z1, color);
		
		//corner 0
		renderLineFromToBlock(x, y, z, x, y, z1, color);
		renderLineFromToBlock(x, y, z, x, y1, z, color);
		renderLineFromToBlock(x, y, z, x1, y, z, color);
		
		//corner 1
		renderLineFromToBlock(x1, y, z, x1, y1, z, color);
		renderLineFromToBlock(x1, y, z, x1, y, z1, color);
		
		//corner 2
		renderLineFromToBlock(x1, y, z1, x1, y1, z1, color);
		renderLineFromToBlock(x1, y, z1, x, y, z1, color);
		
		//corner 3
		renderLineFromToBlock(x, y, z1, x, y1, z1, color);
		//renderLineFromToBlock(x1, y, z1, x, y, z1);
		
		//top parts
		renderLineFromToBlock(x, y1, z, x1, y1, z, color);
		renderLineFromToBlock(x1, y1, z, x1, y1, z1, color);
		renderLineFromToBlock(x1, y1, z1, x, y1, z1, color);
		renderLineFromToBlock(x, y1, z1, x, y1, z, color);
		
		//renderLineFromToBlock(x1, y, z, x1, y1, z1);
		
	}
	
	/*public static void renderRectangle(Vector3f parStart, Vector3f parEnd, float angle, float radius, float height) {
		float vecX = parEnd.x - parStart.x;
		float vecZ = parEnd.z - parStart.z;
		double dist = (double)Math.sqrt(vecX * vecX + vecZ * vecZ);
		
		//float angle = line.getAngle();
		//float radius = line.thickness/2;
		
		for (int i = 0; i < 2; i++) {
			float offset = 0.5F+i*height;
			Vector3f vec = new Vector3f((float)-Math.sin(Math.toRadians(angle)) * radius, 0, (float)Math.cos(Math.toRadians(angle)) * radius);
			
			Vector3f start = new Vector3f(parStart.x + vec.x, parStart.y + offset, parStart.z + vec.z);
			Vector3f end = new Vector3f(parEnd.x + vec.x, parEnd.y + offset, parEnd.z + vec.z);
			renderLineFromToBlock(start.x, start.y, start.z, end.x, end.y, end.z, 0xFFFFFF);
			angle -= 180;
			vec = new Vector3f((float)-Math.sin(Math.toRadians(angle)) * radius, 0, (float)Math.cos(Math.toRadians(angle)) * radius);
			start = new Vector3f(parStart.x + vec.x, parStart.y + offset, parStart.z + vec.z);
			end = new Vector3f(parEnd.x + vec.x, parEnd.y + offset, parEnd.z + vec.z);
			renderLineFromToBlock(start.x, start.y, start.z, end.x, end.y, end.z, 0xFFFFFF);
			
			angle -= 180;
			vec = new Vector3f((float)-Math.sin(Math.toRadians(angle)) * radius, 0, (float)Math.cos(Math.toRadians(angle)) * radius);
			start = new Vector3f(parStart.x + vec.x, parStart.y + offset, parStart.z + vec.z);
			angle -= 180;
			vec = new Vector3f((float)-Math.sin(Math.toRadians(angle)) * radius, 0, (float)Math.cos(Math.toRadians(angle)) * radius);
			end = new Vector3f(parStart.x + vec.x, parStart.y + offset, parStart.z + vec.z);
			renderLineFromToBlock(start.x, start.y, start.z, end.x, end.y, end.z, 0xFFFFFF);
			
			vec = new Vector3f((float)-Math.sin(Math.toRadians(angle)) * radius, 0, (float)Math.cos(Math.toRadians(angle)) * radius);
			start = new Vector3f(parEnd.x + vec.x, parEnd.y + offset, parEnd.z + vec.z);
			angle -= 180;
			vec = new Vector3f((float)-Math.sin(Math.toRadians(angle)) * radius, 0, (float)Math.cos(Math.toRadians(angle)) * radius);
			end = new Vector3f(parEnd.x + vec.x, parEnd.y + offset, parEnd.z + vec.z);
			renderLineFromToBlock(start.x, start.y, start.z, end.x, end.y, end.z, 0xFFFFFF);
		}
		
		Vector3f vec = new Vector3f((float)-Math.sin(Math.toRadians(angle)) * radius, 0, (float)Math.cos(Math.toRadians(angle)) * radius);
		Vector3f start = new Vector3f(parStart.x + vec.x, parStart.y + 0.5F, parStart.z + vec.z);
		Vector3f end = new Vector3f(parStart.x + vec.x, parStart.y + 0.5F + height, parStart.z + vec.z);
		renderLineFromToBlock(start.x, start.y, start.z, end.x, end.y, end.z, 0xFFFFFF);
		
		angle -= 180;
		vec = new Vector3f((float)-Math.sin(Math.toRadians(angle)) * radius, 0, (float)Math.cos(Math.toRadians(angle)) * radius);
		start = new Vector3f(parStart.x + vec.x, parStart.y + 0.5F, parStart.z + vec.z);
		end = new Vector3f(parStart.x + vec.x, parStart.y + 0.5F + height, parStart.z + vec.z);
		renderLineFromToBlock(start.x, start.y, start.z, end.x, end.y, end.z, 0xFFFFFF);
		
		vec = new Vector3f((float)-Math.sin(Math.toRadians(angle)) * radius, 0, (float)Math.cos(Math.toRadians(angle)) * radius);
		start = new Vector3f(parEnd.x + vec.x, parEnd.y + 0.5F, parEnd.z + vec.z);
		end = new Vector3f(parEnd.x + vec.x, parEnd.y + 0.5F + height, parEnd.z + vec.z);
		renderLineFromToBlock(start.x, start.y, start.z, end.x, end.y, end.z, 0xFFFFFF);
		
		angle -= 180;
		vec = new Vector3f((float)-Math.sin(Math.toRadians(angle)) * radius, 0, (float)Math.cos(Math.toRadians(angle)) * radius);
		start = new Vector3f(parEnd.x + vec.x, parEnd.y + 0.5F, parEnd.z + vec.z);
		end = new Vector3f(parEnd.x + vec.x, parEnd.y + 0.5F + height, parEnd.z + vec.z);
		renderLineFromToBlock(start.x, start.y, start.z, end.x, end.y, end.z, 0xFFFFFF);
	}*/

	public static void renderLine(PathPoint ppx, PathPoint ppx2, double d, double d1, double d2, float f, float f1, int stringColor) {
		renderLineFromToBlock(ppx.xCoord, ppx.yCoord, ppx.zCoord, ppx2.xCoord, ppx2.yCoord, ppx2.zCoord, stringColor);
	}
	
	public static void renderLineFromToBlockCenter(double x1, double y1, double z1, double x2, double y2, double z2, int stringColor) {
		renderLineFromToBlock(x1+0.5D, y1+0.5D, z1+0.5D, x2+0.5D, y2+0.5D, z2+0.5D, stringColor);
	}
	
	public static void renderLineFromToBlock(double x1, double y1, double z1, double x2, double y2, double z2, int stringColor) {
	    Tessellator tessellator = Tessellator.instance;
	    RenderManager rm = RenderManager.instance;
	    
	    float castProgress = 1.0F;
	
	    float f10 = 0F;
	    double d4 = MathHelper.sin(f10);
	    double d6 = MathHelper.cos(f10);
	
	    double pirateX = x1;
	    double pirateY = y1;
	    double pirateZ = z1;
	    double entX = x2;
	    double entY = y2;
	    double entZ = z2;
	
	    double fishX = castProgress*(entX - pirateX);
	    double fishY = castProgress*(entY - pirateY);
	    double fishZ = castProgress*(entZ - pirateZ);
	    GL11.glDisable(GL11.GL_TEXTURE_2D);
	    //GL11.glDisable(GL11.GL_LIGHTING);
	    tessellator.startDrawing(GL11.GL_LINE_STRIP);
	    //GL11.GL_LINE_WIDTH
	    //int stringColor = 0x888888;
	    //if (((EntityNode)entitypirate).render) {
	    	//stringColor = 0x880000;
	    //} else {
	    	//stringColor = 0xEF4034;
		//}
	    tessellator.setColorOpaque_I(stringColor);
	    int steps = 1;
	
	    for (int i = 0; i <= steps; ++i) {
	        float f4 = i/(float)steps;
	        tessellator.addVertex(
	            pirateX - rm.renderPosX + fishX * f4,//(f4 * f4 + f4) * 0.5D + 0.25D,
	            pirateY - rm.renderPosY + fishY * f4,//(f4 * f4 + f4) * 0.5D + 0.25D,
	            pirateZ - rm.renderPosZ + fishZ * f4);
	    }
	    
	    tessellator.draw();
	    //GL11.glEnable(GL11.GL_LIGHTING);
	    GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	public static void renderBlock(Block var2, int meta, int x, int y, int z)
    {
		World world = Minecraft.getMinecraft().theWorld;
    	RenderBlocks a = new RenderBlocks(world);
    	
    	Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
    	
        float var8 = 0.5F;
        float var9 = 1.0F;
        float var10 = 0.8F;
        float var11 = 0.6F;
        Tessellator var12 = Tessellator.instance;
        var12.startDrawingQuads();
        //float var13 = var2.getBlockBrightness(var3, var4, var5, var6);
        //float var14 = var2.getBlockBrightness(var3, var4, var5 - 1, var6);
        var12.setBrightness(var2.getMixedBrightnessForBlock(world, x, y, z));

        float var13 = 0.8F;
        float var14 = 0.8F;
        
        var13 = 1F;//(float) (var13 + Math.cos((var1.worldObj.getWorldTime() * 0.3F) - (var1.blockRow * 0.5F)) * 0.15F);
        var14 = var13;
        
        float var15 = 1.0F;
        float var16 = 1.0F;
        float var17 = 1.0F;

        if (var2 == Blocks.leaves)
        {
            int var18 = var2.colorMultiplier(world, x, y, z);
            var15 = (float)(var18 >> 16 & 255) / 255.0F;
            var16 = (float)(var18 >> 8 & 255) / 255.0F;
            var17 = (float)(var18 & 255) / 255.0F;

            if (EntityRenderer.anaglyphEnable)
            {
                float var19 = (var15 * 30.0F + var16 * 59.0F + var17 * 11.0F) / 100.0F;
                float var20 = (var15 * 30.0F + var16 * 70.0F) / 100.0F;
                float var21 = (var15 * 30.0F + var17 * 70.0F) / 100.0F;
                var15 = var19;
                var16 = var20;
                var17 = var21;
            }
        }
        
        //NEW! - set block render size
        a.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
        //a.setRenderMinMax(0.25D, 0.25D, 0.25D, 0.75D, 0.75D, 0.75D);

        var12.setColorOpaque_F(var15 * var8 * var14, var16 * var8 * var14, var17 * var8 * var14);
        a.renderFaceYNeg(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(0, meta));

        if (var14 < var13)
        {
            var14 = var13;
        }

        var12.setColorOpaque_F(var15 * var9 * var14, var16 * var9 * var14, var17 * var9 * var14);
        a.renderFaceYPos(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(1, meta));

        if (var14 < var13)
        {
            var14 = var13;
        }

        var12.setColorOpaque_F(var15 * var10 * var14, var16 * var10 * var14, var17 * var10 * var14);
        a.renderFaceZNeg(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(2, meta));

        if (var14 < var13)
        {
            var14 = var13;
        }

        var12.setColorOpaque_F(var15 * var10 * var14, var16 * var10 * var14, var17 * var10 * var14);
        a.renderFaceZPos(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(3, meta));

        if (var14 < var13)
        {
            var14 = var13;
        }

        var12.setColorOpaque_F(var15 * var11 * var14, var16 * var11 * var14, var17 * var11 * var14);
        a.renderFaceXNeg(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(4, meta));

        if (var14 < var13)
        {
            var14 = var13;
        }

        var12.setColorOpaque_F(var15 * var11 * var14, var16 * var11 * var14, var17 * var11 * var14);
        a.renderFaceXPos(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(5, meta));
        var12.draw();
    }
	
}
