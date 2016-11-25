package extendedrenderer.particle;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import extendedrenderer.ExtendedRenderer;

public class ParticleRegistry {

	public static TextureAtlasSprite squareGrey;
	public static TextureAtlasSprite smoke;
	public static TextureAtlasSprite smokeTest;
	public static TextureAtlasSprite cloud;
	public static TextureAtlasSprite cloud256;
	public static TextureAtlasSprite cloud256_2;
	public static TextureAtlasSprite cloud256_6;
	public static TextureAtlasSprite cloud256_7;
	public static TextureAtlasSprite chicken;
	public static TextureAtlasSprite potato;
	public static TextureAtlasSprite leaf;
	public static TextureAtlasSprite rain;
	public static TextureAtlasSprite snow;
	//public static TextureAtlasSprite test;
	public static TextureAtlasSprite cloud256dark;
	public static TextureAtlasSprite cloudDownfall;
	
	public static void init(TextureStitchEvent.Pre event) {
		
		//optifine breaks (removes) forge added method setTextureEntry, dont use it
		
		squareGrey = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/white"));
		smoke = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/smoke_00"));
		smokeTest = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/smoke_2"));
		cloud = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud64"));
		cloud256 = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256"));
		cloud256_2 = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_5"));
		cloud256_6 = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_6"));
		cloud256_7 = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_7"));
		chicken = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/chicken"));
		potato = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/potato"));
		leaf = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/leaf"));
		rain = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/rain"));
		snow = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/snow"));
		cloud256dark = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256dark"));
		cloudDownfall = event.getMap().registerSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/downfall"));
	}
	
	public static class TextureAtlasSpriteImpl extends TextureAtlasSprite {

		public TextureAtlasSpriteImpl(String spriteName) {
			super(spriteName);
		}
		
	}
}
