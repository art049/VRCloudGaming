package net.minecraft.client.renderer.entity;

import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.util.ResourceLocation;

public class RenderAreaEffectCloud extends Render<EntityAreaEffectCloud> {
   public RenderAreaEffectCloud(RenderManager manager) {
      super(manager);
   }

   public void doRender(EntityAreaEffectCloud entity, double x, double y, double z, float entityYaw, float partialTicks) {
      super.doRender(entity, x, y, z, entityYaw, partialTicks);
   }

   protected ResourceLocation getEntityTexture(EntityAreaEffectCloud entity) {
      return null;
   }
}
