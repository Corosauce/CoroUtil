package CoroUtil.entity.render.projectile;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;

import org.lwjgl.opengl.GL11;

import CoroUtil.entity.EntityThrowableUsefull;
import CoroUtil.entity.projectile.EntityProjectileBase;

public class RenderProjectileElementalBall extends RenderProjectileBase {

	@Override
	public void doRender(Entity var1, double var2, double var4, double var6,
			float var8, float var9) {
		super.doRender(var1, var2, var4, var6, var8, var9);
		
		this.bindEntityTexture(var1);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_FOG);
        
        //MovingBlock entBlock = ((MovingBlock)var1);
        float size = 0.4F;//entBlock.scale;
        
        GL11.glTranslatef((float)var2, (float)var4, (float)var6);
                
        GL11.glRotatef((float)(((EntityThrowableUsefull)var1).ticksInAir/* * entBlock.blockNum*/ * 0.2F * 180.0D / 12.566370964050293D - 0.0D), 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((float)(((EntityThrowableUsefull)var1).ticksInAir/* * entBlock.blockNum*/ * 0.2F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 1.0F, 0.0F);
        GL11.glRotatef((float)(((EntityThrowableUsefull)var1).ticksInAir/* * entBlock.blockNum*/ * 0.2F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 0.0F, 1.0F);
        
        GL11.glScalef(size, size, size);
        
        //this.loadTexture("/terrain.png");
        Block block = Blocks.stone;
        int projectileType = ((EntityProjectileBase)var1).projectileType;
        
        if (projectileType == EntityProjectileBase.PRJTYPE_FIREBALL) {
        	block = Blocks.lava;
        } else if (projectileType == EntityProjectileBase.PRJTYPE_ICEBALL) {
        	block = Blocks.ice;
        }
        
        //block = Block.lavaStill;
        //block = Block.ice;
        //block = Block.sand;
        //block = Block.glass;
        
        itemRender = false;
        
        if (block != null) {
	        if (itemRender) {
		        RenderBlocks rb = new RenderBlocks(var1.worldObj);
		        rb.renderBlockAsItem(block, 0, 0.8F);
	        } else {
	        	GL11.glDisable(GL11.GL_LIGHTING);
	        	this.renderFallingCube(var1, block, var1.worldObj, MathHelper.floor_double(var1.posX), MathHelper.floor_double(var1.posY), MathHelper.floor_double(var1.posZ), 0);
	        }
        } else {
        	System.out.println("moving block has no blockID set for render");
        }
        
        GL11.glEnable(GL11.GL_FOG);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
	}
	
}
