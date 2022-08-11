package ca.fxco.experimentalperformance.mixin.memoryDensity;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Main.class)
public class Main_clientLoadedMixin {


    @Inject(
            method = "main([Ljava/lang/String;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;shouldRenderAsync()Z",
                    shift = At.Shift.BEFORE
            )
    )
    private static void loadAllMixinsThenShouldRenderAsync(String[] args, boolean optimizeDataFixer, CallbackInfo ci) {
        ExperimentalPerformance.forceLoadAllMixins();
    }
}
