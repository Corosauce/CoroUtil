package com.corosus.coroutil.mixin;

import com.corosus.coroutil.common.core.modconfig.ConfigMod;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DedicatedServerSettings.class)
public class Bootstrap {

    @Inject(method = "forceSave", at = @At("TAIL"))
    private void initRenderer(CallbackInfo info) {
        ConfigMod.instance().registerForgeConfigs();
    }
}