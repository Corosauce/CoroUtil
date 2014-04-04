package CoroUtil.entity.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;

import org.lwjgl.opengl.GL11;

import CoroUtil.bt.IBTAgent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModelRendererBones
{
    /** The size of the texture file's width in pixels. */
    public float textureWidth;

    /** The size of the texture file's height in pixels. */
    public float textureHeight;

    /** The X offset into the texture used for displaying this model */
    private int textureOffsetX;

    /** The Y offset into the texture used for displaying this model */
    private int textureOffsetY;
    
    //these are always absolute
    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    
    //new relative additions, calculated on model creation from parent data for child nodes
    public float rotationPointXRel;
    public float rotationPointYRel;
    public float rotationPointZRel;
    
    //default resting state of model - used just so it can be read from animation helper
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
    
    private boolean compiled;

    /** The GL display list rendered by the Tessellator for this model */
    private int displayList;
    public boolean mirror;
    public boolean showModel;

    /** Hides the model. */
    public boolean isHidden;
    public List cubeList;
    public List childModels;
    public final String boxName;
    private ModelBase baseModel;
    public float offsetX;
    public float offsetY;
    public float offsetZ;
    
    //new item render stuff
    public int inventoryRenderType = -1;

    public ModelRendererBones(ModelBase par1ModelBase, String par2Str)
    {
        this.textureWidth = 64.0F;
        this.textureHeight = 32.0F;
        this.showModel = true;
        this.cubeList = new ArrayList();
        this.baseModel = par1ModelBase;
        par1ModelBase.boxList.add(this);
        this.boxName = par2Str;
        this.setTextureSize(par1ModelBase.textureWidth, par1ModelBase.textureHeight);
    }

    public ModelRendererBones(ModelBase par1ModelBase)
    {
        this(par1ModelBase, (String)null);
    }

    public ModelRendererBones(ModelBase par1ModelBase, int par2, int par3)
    {
        this(par1ModelBase);
        this.setTextureOffset(par2, par3);
    }

    /**
     * Sets the current box's rotation points and rotation angles to another box.
     */
    public void addChild(ModelRendererBones par1ModelRenderer)
    {
        if (this.childModels == null)
        {
            this.childModels = new ArrayList();
        }

        this.childModels.add(par1ModelRenderer);
    }

    public ModelRendererBones setTextureOffset(int par1, int par2)
    {
        this.textureOffsetX = par1;
        this.textureOffsetY = par2;
        return this;
    }

    public ModelRendererBones addBox(String par1Str, float par2, float par3, float par4, int par5, int par6, int par7)
    {
        par1Str = this.boxName + "." + par1Str;
        TextureOffset textureoffset = this.baseModel.getTextureOffset(par1Str);
        this.setTextureOffset(textureoffset.textureOffsetX, textureoffset.textureOffsetY);
        this.cubeList.add((new ModelBox(this, this.textureOffsetX, this.textureOffsetY, par2, par3, par4, par5, par6, par7, 0.0F)).func_78244_a(par1Str));
        return this;
    }

    public ModelRendererBones addBox(float par1, float par2, float par3, int par4, int par5, int par6)
    {
        this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, par1, par2, par3, par4, par5, par6, 0.0F));
        return this;
    }

    /**
     * Creates a textured box. Args: originX, originY, originZ, width, height, depth, scaleFactor.
     */
    public void addBox(float par1, float par2, float par3, int par4, int par5, int par6, float par7)
    {
        this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, par1, par2, par3, par4, par5, par6, par7));
    }

    public void setRotationPoint(float par1, float par2, float par3)
    {
        this.rotationPointX = par1;
        this.rotationPointY = par2;
        this.rotationPointZ = par3;
    }
    
    @SideOnly(Side.CLIENT)
    public void render(IBTAgent agent, float scale, float partialTicks)
    {
    	
    	//issues with rebinding texture lighting, hit damage overlay etc, should do items in another pass
    	
    	AnimationStateObject animData = agent.getAIBTAgent().profile.animationData.get(boxName);
    	RenderEntityCoroAI render = (RenderEntityCoroAI) RenderManager.instance.entityRenderMap.get(agent.getClass());
    	
    	//top part is null :/
    	/*if (animData == null) {
    		animData = new AnimationStateObject("top"); 
    	}*/
    	
    	if (animData != null) {
	        if (!this.isHidden)
	        {
	            if (this.showModel)
	            {
	                if (!this.compiled)
	                {
	                    this.compileDisplayList(scale);
	                }
	
	                //GL11.glTranslatef(this.offsetX, this.offsetY, this.offsetZ);
	                int i;
	
	                if (animData.rotateAngleX == 0.0F && animData.rotateAngleY == 0.0F && animData.rotateAngleZ == 0.0F)
	                {
	                    if (this.rotationPointXRel == 0.0F && this.rotationPointYRel == 0.0F && this.rotationPointZRel == 0.0F)
	                    {
	                        GL11.glCallList(this.displayList);
	
	                        if (inventoryRenderType == 0) {
	                        	RenderEntityCoroAI.renderEquipment(agent.getAIBTAgent().ent, scale);
	    	                } else if (inventoryRenderType == 1) {
	    	                	//RenderEntityCoroAI.renderEquipment(agent.getAIBTAgent().ent, scale);
	    	                }
	                        ((RenderEntityCoroAI)render).bindEntityTexture(agent.getAIBTAgent().ent);
	                        
	                        if (this.childModels != null)
	                        {
	                            for (i = 0; i < this.childModels.size(); ++i)
	                            {
	                                ((ModelRendererBones)this.childModels.get(i)).render(agent, scale, partialTicks);
	                            }
	                        }
	                    }
	                    else
	                    {
	                        GL11.glTranslatef(this.rotationPointXRel * scale, this.rotationPointYRel * scale, this.rotationPointZRel * scale);
	                        GL11.glCallList(this.displayList);
	
	                        if (inventoryRenderType == 0) {
	                        	RenderEntityCoroAI.renderEquipment(agent.getAIBTAgent().ent, scale);
	    	                } else if (inventoryRenderType == 1) {
	    	                	//RenderEntityCoroAI.renderEquipment(agent.getAIBTAgent().ent, scale);
	    	                }
	                        ((RenderEntityCoroAI)render).bindEntityTexture(agent.getAIBTAgent().ent);
	                        
	                        if (this.childModels != null)
	                        {
	                            for (i = 0; i < this.childModels.size(); ++i)
	                            {
	                                ((ModelRendererBones)this.childModels.get(i)).render(agent, scale, partialTicks);
	                            }
	                        }
	
	                        GL11.glTranslatef(-this.rotationPointXRel * scale, -this.rotationPointYRel * scale, -this.rotationPointZRel * scale);
	                    }
	                }
	                else
	                {
	                    GL11.glPushMatrix();
	                    GL11.glTranslatef(this.rotationPointXRel * scale, this.rotationPointYRel * scale, this.rotationPointZRel * scale);
	
	                    if (animData.rotateAngleZ != 0.0F)
	                    {
	                        GL11.glRotatef((animData.rotateAngleZPrev + (animData.rotateAngleZ - animData.rotateAngleZPrev) * scale) * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
	                    }
	
	                    if (animData.rotateAngleY != 0.0F)
	                    {
	                        GL11.glRotatef((animData.rotateAngleYPrev + (animData.rotateAngleY - animData.rotateAngleYPrev) * scale) * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
	                    }
	
	                    if (animData.rotateAngleX != 0.0F)
	                    {
	                    	//System.out.println(partialTicks);
	                    	//System.out.println((animData.rotateAngleXPrev + (animData.rotateAngleX - animData.rotateAngleXPrev) * par1));
	                        GL11.glRotatef((animData.rotateAngleXPrev + (animData.rotateAngleX - animData.rotateAngleXPrev) * partialTicks) * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
	                    }
	
	                    GL11.glCallList(this.displayList);
	                    
	                    if (inventoryRenderType == 0) {
	                    	RenderEntityCoroAI.renderEquipment(agent.getAIBTAgent().ent, scale);
		                } else if (inventoryRenderType == 1) {
		                	//RenderEntityCoroAI.renderEquipment(agent.getAIBTAgent().ent, scale);
    	                }
	                    ((RenderEntityCoroAI)render).bindEntityTexture(agent.getAIBTAgent().ent);
	                    
	                    if (this.childModels != null)
	                    {
	                        for (i = 0; i < this.childModels.size(); ++i)
	                        {
	                            ((ModelRendererBones)this.childModels.get(i)).render(agent, scale, partialTicks);
	                        }
	                    }
	
	                    GL11.glPopMatrix();
	                }
	                
	                
	
	                //GL11.glTranslatef(-this.offsetX, -this.offsetY, -this.offsetZ);
	            }
	        }
    	}
    }

    /*@SideOnly(Side.CLIENT)
    public void renderWithRotation(float par1)
    {
        if (!this.isHidden)
        {
            if (this.showModel)
            {
                if (!this.compiled)
                {
                    this.compileDisplayList(par1);
                }

                GL11.glPushMatrix();
                GL11.glTranslatef(this.rotationPointX * par1, this.rotationPointY * par1, this.rotationPointZ * par1);

                if (this.rotateAngleY != 0.0F)
                {
                    GL11.glRotatef(this.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
                }

                if (this.rotateAngleX != 0.0F)
                {
                    GL11.glRotatef(this.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
                }

                if (this.rotateAngleZ != 0.0F)
                {
                    GL11.glRotatef(this.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
                }

                GL11.glCallList(this.displayList);
                GL11.glPopMatrix();
            }
        }
    }*/

    /**
     * Allows the changing of Angles after a box has been rendered
     */
    /*@SideOnly(Side.CLIENT)
    public void postRender(float par1)
    {
        if (!this.isHidden)
        {
            if (this.showModel)
            {
                if (!this.compiled)
                {
                    this.compileDisplayList(par1);
                }

                if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F)
                {
                    if (this.rotationPointX != 0.0F || this.rotationPointY != 0.0F || this.rotationPointZ != 0.0F)
                    {
                        GL11.glTranslatef(this.rotationPointX * par1, this.rotationPointY * par1, this.rotationPointZ * par1);
                    }
                }
                else
                {
                    GL11.glTranslatef(this.rotationPointX * par1, this.rotationPointY * par1, this.rotationPointZ * par1);

                    if (this.rotateAngleZ != 0.0F)
                    {
                        GL11.glRotatef(this.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
                    }

                    if (this.rotateAngleY != 0.0F)
                    {
                        GL11.glRotatef(this.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
                    }

                    if (this.rotateAngleX != 0.0F)
                    {
                        GL11.glRotatef(this.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
                    }
                }
            }
        }
    }*/

    /**
     * Compiles a GL display list for this model
     */
    @SideOnly(Side.CLIENT)
    private void compileDisplayList(float par1)
    {
        this.displayList = GLAllocation.generateDisplayLists(1);
        GL11.glNewList(this.displayList, GL11.GL_COMPILE);
        Tessellator tessellator = Tessellator.instance;

        for (int i = 0; i < this.cubeList.size(); ++i)
        {
            ((ModelBox)this.cubeList.get(i)).render(tessellator, par1);
        }

        GL11.glEndList();
        this.compiled = true;
    }

    /**
     * Returns the model renderer with the new texture parameters.
     */
    public ModelRendererBones setTextureSize(int par1, int par2)
    {
        this.textureWidth = (float)par1;
        this.textureHeight = (float)par2;
        return this;
    }
}
