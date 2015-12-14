package extendedrenderer.particle;

import extendedrenderer.ExtendedRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

public class ParticleRegistry {

	/*public static TextureAtlasSprite squareGrey;
	public static IIcon smoke;
	public static IIcon cloud;
	public static IIcon cloud256;
	public static IIcon cloud256dark;
	public static IIcon cloudDownfall;*/
	
	public static void init(TextureStitchEvent event) {
		//type is gone now afaik, since items texture sheet doesnt exist now
		//if (event.map.getTextureType() == 1) {
		
			ResourceLocation res;
		
			res = new ResourceLocation(ExtendedRenderer.modidCaps + ":particles/white");
			event.map.setTextureEntry(res.toString(), new TextureAtlasSpriteImpl(res.toString()));
			
			res = new ResourceLocation(ExtendedRenderer.modidCaps + ":particles/smoke_00");
			event.map.setTextureEntry(res.toString(), new TextureAtlasSpriteImpl(res.toString()));
			
			res = new ResourceLocation(ExtendedRenderer.modidCaps + ":particles/cloud64");
			event.map.setTextureEntry(res.toString(), new TextureAtlasSpriteImpl(res.toString()));
			
			res = new ResourceLocation(ExtendedRenderer.modidCaps + ":particles/cloud256");
			event.map.setTextureEntry(res.toString(), new TextureAtlasSpriteImpl(res.toString()));
			
			res = new ResourceLocation(ExtendedRenderer.modidCaps + ":particles/cloud256dark");
			event.map.setTextureEntry(res.toString(), new TextureAtlasSpriteImpl(res.toString()));
			
			res = new ResourceLocation(ExtendedRenderer.modidCaps + ":particles/downfall");
			event.map.setTextureEntry(res.toString(), new TextureAtlasSpriteImpl(res.toString()));/*
			
			ResourceLocation squareGrey2 = new ResourceLocation(ExtendedRenderer.modidCaps + ":particles/white");
			event.map.setTextureEntry(squareGrey2.toString(), new TextureAtlasSpriteImpl(squareGrey2.toString()));
			
			squareGrey = event.map.registerIcon("ExtendedRenderer:particles/white");
			smoke = event.map.registerIcon("ExtendedRenderer:particles/smoke_00");
			cloud = event.map.registerIcon("ExtendedRenderer:particles/cloud64");
			cloud256 = event.map.registerIcon("ExtendedRenderer:particles/cloud256");
			cloud256dark = event.map.registerIcon("ExtendedRenderer:particles/cloud256dark");
			cloudDownfall = event.map.registerIcon("ExtendedRenderer:particles/downfall");*/
			
		//}
	}
	
	public static class TextureAtlasSpriteImpl extends TextureAtlasSprite {

		protected TextureAtlasSpriteImpl(String spriteName) {
			super(spriteName);
		}
		
	}
}
