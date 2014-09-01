package CoroUtil.entity.render;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.client.model.ModelFormatException;

import org.lwjgl.opengl.GL11;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import CoroUtil.bt.IBTAgent;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Techne model importer, based on iChun's Hats importer
 */
@SideOnly(Side.CLIENT)
public class TechneModelCoroAI extends ModelBase implements IModelCustom {
	
	//Idea:
	//Techne to MC Model Tree
	
	//Info:
	//ModelRenderer has list of cubes and ModelRenderer children
	//techne top and techne piece is a new ModelRenderer
	
	//Test:
	
	//method 1: 
	//- shape is an addition to parent ModelRenderer, hopefully UV still works as it should
	//- added to ModelBox based cubeList in parent ModelRenderer
	
	//method 2:
	//- shape is a new ModelRenderer
	//- added as a child for sake of rendering 1 box
	
	//results:
	
	//techne bug disallows 'piece' parents to rotate at all
	
	//method 1 fails for now due to bug, also ModelBox needs custom constructor call for UV offsets not used from parent ModelRenderer (should work)
	
	//method 2 can work with adjustment
	//- techne folder signifies new childentry into current context, a ModelRenderer with no boxes/cubes
	//- each shape is a full ModelRenderer because techne lets all pieces rotate freely in folders
	
	//this should allow for us to work out local rotation rules since a bare ModelRenderer exists to use as the master rotator over its child nodes
	
	//Shoulder has arm
	//Shoulder has child entry elbow
	//Elbow has armLower
	//Elbow could have child entry wrist
	
	//Shoulder -> arm, other entries, elbow -> armLower
	
	//Is there any way to fix this to be changed to:
	
	//Shoulder -> arm -> elbow -> armLower
	
	//Name folders with "parent-child" marking, ie: "body" piece, "body-rightarm" folder with "rightarm" in it
	//Since parent and child can have any number of freely rotating pieces in it, this gives us a link to the right part for both
	//Requires relevant parent piece to be loaded before folder? (techne wont let me do this for stuff in folders already fffffff) - 2 pass scan per tree layer maybe
	//With this data we should be able to cleanly connect pieces better, helps rel pos fix issue?
	//dont need child marking, just do "body" instead of "body-rightarm"
	
	
	//Notes on position values:
	//Shapes had them relative in the XML i believe
	//Folders dont effect anything other than visual structure of tree, so its 'child pieces' use absolute values
	
	//If the data is to be usable for local rotations, it must be relative position
	
	//Post load parent child rel pos calculations ? new vars ? using above folder marking, should be able to help
	
	//!!! Theres a good chance we need to fix these pos before we create the ModelBox, recursive render might make it do a double translation otherwise, and more position bugs
	
    public static final List<String> cubeTypes = Arrays.asList(
            "d9e621f7-957f-4b77-b1ae-20dcd0da7751",
            "de81aa14-bd60-4228-8d8d-5238bcd3caaa"
            );
    
    private String fileName;
    private Map<String, byte[]> zipContents = new HashMap<String, byte[]>();
    
    public Map<String, ModelRendererBones> parts = new LinkedHashMap<String, ModelRendererBones>();
    public Map<String, ModelRendererBones> partsAllChildren = new LinkedHashMap<String, ModelRendererBones>();
    public String textureName = null;
    public int textureWidth = 64;
    public int textureHeight = 32;
    private boolean textureNameSet = false;
    
    public boolean flattenModelTree = false;
    
    public float partialTickCache = 1F;

    public TechneModelCoroAI(String fileName, URL resource) throws ModelFormatException
    {
        this.fileName = fileName;
        loadTechneModel(resource);
    }
    
    private void loadTechneModel(URL fileURL) throws ModelFormatException
    {
    	
        try
        {
            ZipInputStream zipInput = new ZipInputStream(fileURL.openStream());
            
            ZipEntry entry;
            while ((entry = zipInput.getNextEntry()) != null)
            {
                byte[] data = new byte[(int) entry.getSize()];
                // For some reason, using read(byte[]) makes reading stall upon reaching a 0x1E byte
                int i = 0;
                while (zipInput.available() > 0 && i < data.length)
                {
                    data[i++] = (byte)zipInput.read();
                }
                zipContents.put(entry.getName(), data);
            }
            
            byte[] modelXml = zipContents.get("model.xml");
            if (modelXml == null)
            {
                throw new ModelFormatException("Model " + fileName + " contains no model.xml file");
            }
            
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new ByteArrayInputStream(modelXml));
            
            NodeList nodeListTechne = document.getElementsByTagName("Techne");
            if (nodeListTechne.getLength() < 1)
            {
                throw new ModelFormatException("Model " + fileName + " contains no Techne tag");
            }
            
            NodeList nodeListModel = document.getElementsByTagName("Model");
            if (nodeListModel.getLength() < 1)
            {
                throw new ModelFormatException("Model " + fileName + " contains no Model tag");
            }
            
            NamedNodeMap modelAttributes = nodeListModel.item(0).getAttributes();
            if (modelAttributes == null)
            {
                throw new ModelFormatException("Model " + fileName + " contains a Model tag with no attributes");
            }
            
            Node modelTexture = modelAttributes.getNamedItem("texture");
            if (modelTexture != null)
            {
                textureName = modelTexture.getTextContent();
            }
            
            NodeList textureRes = document.getElementsByTagName("TextureSize");
            if (textureRes != null) {
            	String[] textureOffset = new String[2];
            	Node node = textureRes.item(0);
            	textureOffset = node.getTextContent().split(",");
            	textureWidth = Integer.parseInt(textureOffset[0]);
            	textureHeight = Integer.parseInt(textureOffset[1]);
            }
            
            NodeList shapes = null;
            if (flattenModelTree) {
                shapes = document.getElementsByTagName("Shape");
            } else {
            	//Setup top node, dig down to the top of the Geometry model tree
            	ModelRendererBones cube = new ModelRendererBones(this, "top");
            	cube.textureWidth = this.textureWidth;
            	cube.textureHeight = this.textureHeight;
            	parts.put("top", cube); //add top piece only
            	
            	NodeList temp = document.getElementsByTagName("Geometry");
            	Node node = temp.item(0);
            	shapes = node.getChildNodes();
            	
            	processListRecursive(cube, shapes);
            	
            	int finished = 0;
            }
            
            
            
            
            
            
            
        }
        catch (ZipException e)
        {
            throw new ModelFormatException("Model " + fileName + " is not a valid zip file");
        }
        catch (IOException e)
        {
            throw new ModelFormatException("Model " + fileName + " could not be read", e);
        }
        catch (ParserConfigurationException e)
        {
            // hush
        }
        catch (SAXException e)
        {
            throw new ModelFormatException("Model " + fileName + " contains invalid XML", e);
        }
    }
    
    public void processListRecursive(ModelRendererBones parent, NodeList nodes) {
    	
    	//test thuroughly!
    	
    	//2 passes required since the folder might be above the shape it needs to link from
    	
    	//Shapes will add a new ModelRendererBones to parent childModels list, Folders are just markers to tell me what Shape to add children to and process deeper on
    	
    	//first pass does all pieces and adds to parent childModels just so we can add a single ModelRendererBones with a single entry in cubelist from processItem call
    	//second pass does all folders, using name it finds the relevant _EXISTING_ shapes ModelRendererBones, and goes next level down, BUT, 
    	//"body-rightarm" does the rightarm need anything special done on it other than relative fixes?
    	//cant think of anything...
    	//changing rule, "body-rightarm" to "body"
    	
    	//First pass
    	for (int i = 0; i < nodes.getLength(); i++)
        {
    		Node shape = nodes.item(i);
    		
            if (shape.getNodeName().equals("Shape")) {
            	String childNodeName = getNodeName(shape);
            	System.out.println("creating shape: " + childNodeName);
            	ModelRendererBones cube = new ModelRendererBones(this, childNodeName);
            	cube.textureWidth = this.textureWidth;
            	cube.textureHeight = this.textureHeight;
            	//add to parent
            	parent.addChild(cube);
            	processItem(cube, shape);
            	
            	cube.rotationPointXRel = cube.rotationPointX - parent.rotationPointX;
            	cube.rotationPointYRel = cube.rotationPointY - parent.rotationPointY;
            	cube.rotationPointZRel = cube.rotationPointZ - parent.rotationPointZ;
            }
        }
    	
    	//Second pass
    	for (int i = 0; i < nodes.getLength(); i++)
        {
            Node shape = nodes.item(i);
        	//String childNodeName = shape.getNodeValue();
        	//just get left side 
            
            if (shape.getNodeName().equals("Folder")) {
            	String childNodeName = getNodeName(shape);
            	ModelRendererBones lookupParent = partsAllChildren.get(childNodeName);
            	if (lookupParent != null) {
            		System.out.println("found folder and its shape modelrenderer: " + childNodeName);
            		//go down next level with relevant parent and node collection from the folder
            		processListRecursive(lookupParent, shape.getChildNodes());
            	} else {
            		System.out.println("TechneModelEpoch critical error: failed to find piece with name: " + childNodeName + " to link this folder to");
            	}
            }
            
        }
    }
    
    public String getNodeName(Node shape) {
    	NamedNodeMap shapeAttributes = shape.getAttributes();
        if (shapeAttributes == null)
        {
            throw new ModelFormatException("Shape in " + fileName + " has no attributes");
        }

        String childNodeName = null;
        Node name = shapeAttributes.getNamedItem("name");
        if (name == null) {
        	name = shapeAttributes.getNamedItem("Name"); //fix for folders
        }
        if (name != null)
        {
        	childNodeName = name.getNodeValue();
        }
        if (childNodeName == null)
        {
        	childNodeName = "Shape ???";
        }
        return childNodeName;
    }
    
    public void processItem(ModelRendererBones owner, Node shape) {
    	
    	
        NamedNodeMap shapeAttributes = shape.getAttributes();
        if (shapeAttributes == null)
        {
            throw new ModelFormatException("Shape in " + fileName + " has no attributes");
        }
        
        Node name = shapeAttributes.getNamedItem("name");
        String shapeName = null;
        if (name != null)
        {
            shapeName = name.getNodeValue();
        }
        if (shapeName == null)
        {
            shapeName = "Shape ";
        }
        
        String shapeType = null;
        Node type = shapeAttributes.getNamedItem("type");
        if (type != null)
        {
            shapeType = type.getNodeValue();
        }
        if (shapeType != null && !cubeTypes.contains(shapeType))
        {
            FMLLog.warning("Model shape [" + shapeName + "] in " + fileName + " is not a cube, ignoring");
            return;
        }
        
        try
        {
            boolean mirrored = false;
            String[] offset = new String[3];
            String[] position = new String[3];
            String[] rotation = new String[3];
            String[] size = new String[3];
            String[] textureOffset = new String[2];
            
            NodeList shapeChildren = shape.getChildNodes();
            for (int j = 0; j < shapeChildren.getLength(); j++)
            {
                Node shapeChild = shapeChildren.item(j);
                
                String shapeChildName = shapeChild.getNodeName();
                String shapeChildValue = shapeChild.getTextContent();
                if (shapeChildValue != null)
                {
                    shapeChildValue = shapeChildValue.trim();
                    
                    if (shapeChildName.equals("IsMirrored"))
                    {
                        mirrored = !shapeChildValue.equals("False");
                    }
                    else if (shapeChildName.equals("Offset"))
                    {
                        offset = shapeChildValue.split(",");
                    }
                    else if (shapeChildName.equals("Position"))
                    {
                        position = shapeChildValue.split(",");
                    }
                    else if (shapeChildName.equals("Rotation"))
                    {
                        rotation = shapeChildValue.split(",");
                    }
                    else if (shapeChildName.equals("Size"))
                    {
                        size = shapeChildValue.split(",");
                    }
                    else if (shapeChildName.equals("TextureOffset"))
                    {
                        textureOffset = shapeChildValue.split(",");
                    }
                }
            }
                        
            // That's what the ModelBase subclassing is needed for
            owner.setTextureOffset(Integer.parseInt(textureOffset[0]), Integer.parseInt(textureOffset[1]));
            owner.mirror = mirrored;
            
            owner.addBox(Float.parseFloat(offset[0]), Float.parseFloat(offset[1]), Float.parseFloat(offset[2]), Integer.parseInt(size[0]), Integer.parseInt(size[1]), Integer.parseInt(size[2]));
            
            float hatOffset = 23.4F;
            hatOffset = 0;
            
            owner.setRotationPoint(Float.parseFloat(position[0]), Float.parseFloat(position[1]) - hatOffset, Float.parseFloat(position[2]));

            owner.rotateAngleX = (float)Math.toRadians(Float.parseFloat(rotation[0]));
            owner.rotateAngleY = (float)Math.toRadians(Float.parseFloat(rotation[1]));
            owner.rotateAngleZ = (float)Math.toRadians(Float.parseFloat(rotation[2]));
            
            //Added, ModelRenderer makes setting this a problem...
            owner.offsetX = Float.parseFloat(offset[0]);
            owner.offsetY = Float.parseFloat(offset[1]);
            owner.offsetZ = Float.parseFloat(offset[2]);
            
            //Semi custom inventory using code
            if (shapeName.equals("rightarmlower")) {
            	owner.inventoryRenderType = 0;
            } else if (shapeName.equals("leftarmlower")) {
            	owner.inventoryRenderType = 1;
            }

            //dont add all modelrenderers to parts, since rendering is recursive we think
            partsAllChildren.put(shapeName, owner);
            //parts.put(shapeName, cube);
        }
        catch (NumberFormatException e)
        {
            FMLLog.warning("Model shape [" + shapeName + "] in " + fileName + " contains malformed integers within its data, ignoring");
            e.printStackTrace();
        }
        
    }
    
    private void bindTexture()
    {
        /* TODO: Update to 1.6
        if (texture != null)
        {
            if (!textureNameSet)
            {
                try
                {
                    byte[] textureEntry = zipContents.get(texture);
                    if (textureEntry == null)
                    {
                        throw new ModelFormatException("Model " + fileName + " has no such texture " + texture);
                    }
                    
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(textureEntry));
                    textureName = Minecraft.getMinecraft().renderEngine.allocateAndSetupTexture(image);
                    textureNameSet = true;
                }
                catch (ZipException e)
                {
                    throw new ModelFormatException("Model " + fileName + " is not a valid zip file");
                }
                catch (IOException e)
                {
                    throw new ModelFormatException("Texture for model " + fileName + " could not be read", e);
                }
            }
            
            if (textureNameSet)
            {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureName);
                Minecraft.getMinecraft().renderEngine.resetBoundTexture();
            }
        }
        */
    }
    
    @Override
    public String getType()
    {
        return "tcn";
    }
    
    @Override
    public void render(Entity par1Entity, float par2, float par3, float par4,
    		float par5, float par6, float par7) {
    	super.render(par1Entity, par2, par3, par4, par5, par6, par7);
    	
    	//in techne, the bottom of the head is y=0
    	//in code, the bottom of the model (feet) is y=0
    	
    	//for both: up is y--, down is y++
    	
    	try {
    		/*float offset = -20;
    		float offset2 = 25;
    		
    		float rate = 12;
    		float ampArm = 1.2F;
    		float ampLeg = 1.6F;
    		
    		float lean = -10+(float) Math.toDegrees(Math.cos(Math.toRadians(((par1Entity.worldObj.getTotalWorldTime() * rate * 0.4F) % 360)))) * 0.6F * 0.3F;
    		
    		float armRot = (float) Math.toDegrees(Math.cos(Math.toRadians(((par1Entity.worldObj.getTotalWorldTime() * rate) % 360)))) * ampArm;
    		float armRotBottom = (float) Math.toDegrees(Math.sin(Math.toRadians(((par1Entity.worldObj.getTotalWorldTime() * rate) % 360)))) * ampArm;
    		
    		float legRot = (float) Math.toDegrees(Math.cos(Math.toRadians(((par1Entity.worldObj.getTotalWorldTime() * rate) % 360)))) * ampLeg;
    		float legRotBottom = (float) Math.toDegrees(Math.sin(Math.toRadians(((par1Entity.worldObj.getTotalWorldTime() * rate) % 360)))) * ampLeg;
    		
    		float bob = (float) Math.toDegrees(Math.sin(Math.toRadians(((par1Entity.worldObj.getTotalWorldTime() * rate * 2F) % 360)))) * ampLeg;
    		
    		float headRot = (float) Math.toDegrees(Math.cos(Math.toRadians(((par1Entity.worldObj.getTotalWorldTime() * rate * 2F) % 360)))) * 0.6F * 0.4F;
    		
    		//this.parts.get("top").rotationPointYRel = (float)Math.toRadians(bob * 1F);
    		
    		this.partsAllChildren.get("body").rotateAngleXDesired = (float)Math.toRadians(lean);
    		
    		this.partsAllChildren.get("head").rotateAngleXDesired = (float)Math.toRadians(-headRot * 1F);
    		this.partsAllChildren.get("head").rotateAngleYDesired = (float)Math.toRadians(-armRot * 1F);
    		
    		this.partsAllChildren.get("rightarm").rotateAngleXDesired = (float)Math.toRadians(armRot);
    		partsAllChildren.get("rightarmlower").rotateAngleXDesired = (float)Math.toRadians(armRot-45);
    		this.partsAllChildren.get("leftarm").rotateAngleXDesired = (float)-Math.toRadians(armRot);
    		partsAllChildren.get("leftarmlower").rotateAngleXDesired = (float)-Math.toRadians(armRot+45);
    		
    		partsAllChildren.get("rightleg").rotateAngleXDesired = (float)Math.toRadians(legRot-offset);
    		partsAllChildren.get("rightleglower").rotateAngleXDesired = (float)Math.toRadians(legRotBottom+offset2);
    		partsAllChildren.get("leftleg").rotateAngleXDesired = (float)-Math.toRadians(legRot+offset);
    		partsAllChildren.get("leftleglower").rotateAngleXDesired = (float)-Math.toRadians(legRotBottom-offset2);*/
    		
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	
    	
    	
    	if (par1Entity instanceof IBTAgent) {
        	((IBTAgent) par1Entity).getAIBTAgent().profile.tickAbilitiesRenderModel(this);
        }
    	
    	//temp
    	//GL11.glTranslatef(0, -5.5F, 0);
    	
    	GL11.glPushMatrix();
    	float fixScale = 1F / 16F;
    	//GL11.glTranslatef(0, 1.5F, 0);
    	GL11.glScalef(fixScale, fixScale, fixScale);
    	//renderAll();
    	
    	
    	
    	//bindTexture();
        
        for (ModelRendererBones part : parts.values())
        {
            part.render(((IBTAgent) par1Entity), 1F, partialTickCache);
        }
        
        //if (par1Entity instanceof EntityLiving) RenderCoroAIEntity.renderEquipment((EntityLiving)par1Entity, par7);
    	GL11.glPopMatrix();
    }

    @Override
    public void renderAll()
    {
        /*bindTexture();
        
        for (ModelRendererBones part : parts.values())
        {
            part.render(1.0F);
        }*/
    }

    @Override
    public void renderPart(String partName)
    {        
        /*ModelRendererBones part = parts.get(partName);
        if (part != null)
        {
            bindTexture();
            
            part.render(1.0F);
        }*/
    }

    @Override
    public void renderOnly(String... groupNames)
    {
        /*bindTexture();
        for (ModelRendererBones part : parts.values())
        {
            for (String groupName : groupNames)
            {
                if (groupName.equalsIgnoreCase(part.boxName))
                {
                    part.render(1.0f);
                }
            }
        }*/
    }

    @Override
    public void renderAllExcept(String... excludedGroupNames)
    {
        /*for (ModelRendererBones part : parts.values())
        {
            boolean skipPart=false;
            for (String excludedGroupName : excludedGroupNames)
            {
                if (excludedGroupName.equalsIgnoreCase(part.boxName))
                {
                    skipPart=true;
                }
            }
            if(!skipPart)
            {
                part.render(1.0f);
            }
        }*/
    }
    
    @Override
    public void setLivingAnimations(EntityLivingBase par1EntityLivingBase, float par2, float par3, float par4) {
    	super.setLivingAnimations(par1EntityLivingBase, par2, par3, par4);
    	partialTickCache = par4;
    }
}
