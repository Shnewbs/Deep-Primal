package com.shnewbs.deepprimal.themes.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.shnewbs.deepprimal.themes.ThemeContainerArt;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
abstract class ThemeGraphicsMixin {
    @Inject(method="blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIIIIII)V",at=@At("HEAD"),cancellable=true)
    private void deepprimalThemes$panel(RenderPipeline pipeline,Identifier id,int x,int y,float u,float v,int w,int h,int sw,int sh,int tw,int th,int color,CallbackInfo ci){
        if(ThemeContainerArt.background((GuiGraphicsExtractor)(Object)this,id,x,y,w,h,u,v))ci.cancel();
    }
    @Inject(method="blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V",at=@At("HEAD"),cancellable=true)
    private void deepprimalThemes$hotbar(RenderPipeline pipeline,Identifier id,int x,int y,int w,int h,int color,CallbackInfo ci){
        if(ThemeContainerArt.sprite((GuiGraphicsExtractor)(Object)this,id,x,y,w,h))ci.cancel();
    }
}
