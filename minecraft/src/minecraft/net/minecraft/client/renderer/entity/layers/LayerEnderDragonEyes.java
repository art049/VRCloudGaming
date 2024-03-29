package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderDragon;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.ResourceLocation;

public class LayerEnderDragonEyes implements LayerRenderer<EntityDragon> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/enderdragon/dragon_eyes.png");
   private final RenderDragon dragonRenderer;

   public LayerEnderDragonEyes(RenderDragon dragonRendererIn) {
      this.dragonRenderer = dragonRendererIn;
   }

   public void doRenderLayer(EntityDragon entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
      this.dragonRenderer.bindTexture(TEXTURE);
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
      GlStateManager.disableLighting();
      GlStateManager.depthFunc(514);
      int i = 61680;
      int j = i % 65536;
      int k = i / 65536;
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
      GlStateManager.enableLighting();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.dragonRenderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
      this.dragonRenderer.setLightmap(entitylivingbaseIn, partialTicks);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.depthFunc(515);
   }

   public boolean shouldCombineTextures() {
      return false;
   }
}
