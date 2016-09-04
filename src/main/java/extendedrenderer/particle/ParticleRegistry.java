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
	public static TextureAtlasSprite leaf;
	public static TextureAtlasSprite rain;
	public static TextureAtlasSprite snow;
	//public static TextureAtlasSprite test;
	public static TextureAtlasSprite cloud256dark;
	public static TextureAtlasSprite cloudDownfall;
	
	public static void init(TextureStitchEvent.Pre event) {
		//type is gone now afaik, since items texture sheet doesnt exist now
		//if (event.map.getTextureType() == 1) {
		
			//ResourceLocation res;
			
			squareGrey = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/white"));
			smoke = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/smoke_00"));
			cloud = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud64"));
			cloud256 = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256"));
			leaf = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/leaf"));
			rain = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/rain"));
			snow = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/snow"));
			cloud256dark = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256dark"));
			cloudDownfall = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/downfall"));
		
			/*res = new ResourceLocation(ExtendedRenderer.modid + ":particles/white");
			event.getMap().setTextureEntry(squareGrey = new TextureAtlasSpriteImpl(res.toString()));
			
			res = new ResourceLocation(ExtendedRenderer.modid + ":particles/smoke_00");
			event.getMap().setTextureEntry(smoke = new TextureAtlasSpriteImpl(res.toString()));
			
			res = new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud64");
			event.getMap().setTextureEntry(cloud = new TextureAtlasSpriteImpl(res.toString()));
			
			res = new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256");
			event.getMap().setTextureEntry(cloud256 = new TextureAtlasSpriteImpl(res.toString()));
			
			res = new ResourceLocation(ExtendedRenderer.modid + ":particles/leaf");
			event.getMap().setTextureEntry(leaf = new TextureAtlasSpriteImpl(res.toString()));
			
			res = new ResourceLocation(ExtendedRenderer.modid + ":particles/rain");
			event.getMap().setTextureEntry(rain = new TextureAtlasSpriteImpl(res.toString()));
			
			res = new ResourceLocation(ExtendedRenderer.modid + ":particles/snow");
			event.getMap().setTextureEntry(snow = new TextureAtlasSpriteImpl(res.toString()));
			
			res = new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256dark");
			event.getMap().setTextureEntry(cloud256dark = new TextureAtlasSpriteImpl(res.toString()));
			
			res = new ResourceLocation(ExtendedRenderer.modid + ":particles/downfall");
			event.getMap().setTextureEntry(cloudDownfall = new TextureAtlasSpriteImpl(res.toString()));*/
				
			
			/*
			
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
