package extendedrenderer.particle;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import extendedrenderer.ExtendedRenderer;

public class ParticleRegistry {

	public static TextureAtlasSprite squareGrey;
	public static TextureAtlasSprite smoke;
	public static TextureAtlasSprite cloud;
	public static TextureAtlasSprite cloud256;
	//public static TextureAtlasSprite test;
	public static TextureAtlasSprite cloud256dark;
	public static TextureAtlasSprite cloudDownfall;
	
	public static void init(TextureStitchEvent event) {
		//type is gone now afaik, since items texture sheet doesnt exist now
		//if (event.map.getTextureType() == 1) {
		
			ResourceLocation res;
		
			res = new ResourceLocation(ExtendedRenderer.modid + ":particles/white");
			event.getMap().setTextureEntry(res.toString(), squareGrey = new TextureAtlasSpriteImpl(res.toString()));
			
			res = new ResourceLocation(ExtendedRenderer.modid + ":particles/smoke_00");
			event.getMap().setTextureEntry(res.toString(), smoke = new TextureAtlasSpriteImpl(res.toString()));
			
			res = new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud64");
			event.getMap().setTextureEntry(res.toString(), cloud = new TextureAtlasSpriteImpl(res.toString()));
			
			res = new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256");
			event.getMap().setTextureEntry(res.toString(), cloud256 = new TextureAtlasSpriteImpl(res.toString()));
			
			/*res = new ResourceLocation(ExtendedRenderer.modid + ":particles/radarIconCyclone");
			event.map.setTextureEntry(res.toString(), test = new TextureAtlasSpriteImpl(res.toString()));*/
			
			res = new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256dark");
			event.getMap().setTextureEntry(res.toString(), cloud256dark = new TextureAtlasSpriteImpl(res.toString()));
			
			res = new ResourceLocation(ExtendedRenderer.modid + ":particles/downfall");
			event.getMap().setTextureEntry(res.toString(), cloudDownfall = new TextureAtlasSpriteImpl(res.toString()));/*
			
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

		public TextureAtlasSpriteImpl(String spriteName) {
			super(spriteName);
		}
		
	}
}
