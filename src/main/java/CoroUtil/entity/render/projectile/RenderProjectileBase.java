package CoroUtil.entity.render.projectile;

import java.nio.FloatBuffer;

import javax.vecmath.Matrix3f;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.BufferUtils;

@SideOnly(Side.CLIENT)
public class RenderProjectileBase extends Render
{
	
	boolean itemRender = false;
	
    public RenderProjectileBase()
    {
    	
    }

	@Override

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(Entity entity) {
		return TextureMap.locationBlocksTexture;
	}

    @Override
    public void doRender(Entity var1, double var2, double var4, double var6, float var8, float var9)
    {
    	
    }
    
    public static FloatBuffer writeMatrixToBuffer(Matrix3f matrix, FloatBuffer buffer) {
		if (buffer == null)
		{
			buffer = BufferUtils.createFloatBuffer(9);
		}
		int oldPosition = buffer.position();
		buffer.put(matrix.m00);
		buffer.put(matrix.m10);
		buffer.put(matrix.m20);
		buffer.put(matrix.m01);
		buffer.put(matrix.m11);
		buffer.put(matrix.m21);
		buffer.put(matrix.m02);
		buffer.put(matrix.m12);
		buffer.put(matrix.m22);
		buffer.position(oldPosition);
		return buffer;
    }
    
    public void renderFallingCube(Entity var1, Block var2, World var3, int var4, int var5, int var6, int var7)
    {
    	RenderBlocks a = new RenderBlocks(var1.worldObj);
    	
        float var8 = 0.5F;
        float var9 = 1.0F;
        float var10 = 0.8F;
        float var11 = 0.6F;
        Tessellator var12 = Tessellator.getInstance();
        var12.startDrawingQuads();
        //float var13 = var2.getBlockBrightness(var3, var4, var5, var6);
        //float var14 = var2.getBlockBrightness(var3, var4, var5 - 1, var6);
        var12.setBrightness(var2.getMixedBrightnessForBlock(var3, var4, var5 + 1, var6));
        //var12.setBrightness(15728704);
        var12.setBrightness(15728880); // aka F000F0 - seems to be max

        float var13 = 0.8F;
        float var14 = 0.8F;
        
        //var13 = (float) (var13 + Math.cos((var1.worldObj.getWorldTime() * 0.3F) - (/*var1.blockRow * */0.5F)) * 0.15F);
        var14 = var13;
        
        float var15 = 1.0F;
        float var16 = 1.0F;
        float var17 = 1.0F;

        if (var2 == Blocks.leaves)
        {
            int var18 = var2.colorMultiplier(var3, (int)var1.posX, (int)var1.posY, (int)var1.posZ);
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
        a.renderFaceYNeg(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(0, var7));

        if (var14 < var13)
        {
            var14 = var13;
        }

        var12.setColorOpaque_F(var15 * var9 * var14, var16 * var9 * var14, var17 * var9 * var14);
        a.renderFaceYPos(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(1, var7));

        if (var14 < var13)
        {
            var14 = var13;
        }

        var12.setColorOpaque_F(var15 * var10 * var14, var16 * var10 * var14, var17 * var10 * var14);
        a.renderFaceZNeg(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(2, var7));

        if (var14 < var13)
        {
            var14 = var13;
        }

        var12.setColorOpaque_F(var15 * var10 * var14, var16 * var10 * var14, var17 * var10 * var14);
        a.renderFaceZPos(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(3, var7));

        if (var14 < var13)
        {
            var14 = var13;
        }

        var12.setColorOpaque_F(var15 * var11 * var14, var16 * var11 * var14, var17 * var11 * var14);
        a.renderFaceXNeg(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(4, var7));

        if (var14 < var13)
        {
            var14 = var13;
        }

        var12.setColorOpaque_F(var15 * var11 * var14, var16 * var11 * var14, var17 * var11 * var14);
        a.renderFaceXPos(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(5, var7));
        var12.draw();
    }
}
