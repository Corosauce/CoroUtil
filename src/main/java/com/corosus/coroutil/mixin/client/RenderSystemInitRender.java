package com.corosus.coroutil.mixin.client;

import com.corosus.coroutil.common.core.modconfig.ConfigMod;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class RenderSystemInitRender {

    @Inject(method = "initRenderer", at = @At("TAIL"), remap = false)
    private static void initRenderer(int i, boolean bl, CallbackInfo info) {
        ConfigMod.instance().registerForgeConfigs();
    }
}