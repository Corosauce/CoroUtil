package com.corosus.coroutil.lazydfu.mixin;

import com.corosus.coroutil.lazydfu.LazyDataFixerBuilder;
import com.mojang.datafixers.DataFixer;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.util.datafix.DataFixers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(DataFixers.class)
public class SchemasMixin {

    /**
     * @author Corosus
     * @reason stop mojangs rediculous and pointless cpu usage on load that no modpack user would ever benefit from
     */
    @Overwrite
    private static DataFixer createFixerUpper() {
        LazyDataFixerBuilder datafixerbuilder = new LazyDataFixerBuilder(SharedConstants.getCurrentVersion().getWorldVersion());
        DataFixers.addFixers(datafixerbuilder);
        return datafixerbuilder.build(Util.bootstrapExecutor());
    }
}