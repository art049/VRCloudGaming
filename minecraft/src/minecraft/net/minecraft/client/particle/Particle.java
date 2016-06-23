package net.minecraft.client.particle;

import java.util.List;
import java.util.Random;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class Particle {
   private static final AxisAlignedBB EMPTY_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
   protected World worldObj;
   protected double prevPosX;
   protected double prevPosY;
   protected double prevPosZ;
   protected double posX;
   protected double posY;
   protected double posZ;
   protected double motionX;
   protected double motionY;
   protected double motionZ;
   private AxisAlignedBB boundingBox;
   protected boolean isCollided;
   protected boolean isExpired;
   protected float width;
   protected float height;
   protected Random rand;
   protected int particleTextureIndexX;
   protected int particleTextureIndexY;
   protected float particleTextureJitterX;
   protected float particleTextureJitterY;
   protected int particleAge;
   protected int particleMaxAge;
   protected float particleScale;
   protected float particleGravity;
   protected float particleRed;
   protected float particleGreen;
   protected float particleBlue;
   protected float particleAlpha;
   protected TextureAtlasSprite particleTexture;
   public static double interpPosX;
   public static double interpPosY;
   public static double interpPosZ;

   protected Particle(World worldIn, double posXIn, double posYIn, double posZIn) {
      this.boundingBox = EMPTY_AABB;
      this.width = 0.6F;
      this.height = 1.8F;
      this.rand = new Random();
      this.particleAlpha = 1.0F;
      this.worldObj = worldIn;
      this.setSize(0.2F, 0.2F);
      this.setPosition(posXIn, posYIn, posZIn);
      this.prevPosX = posXIn;
      this.prevPosY = posYIn;
      this.prevPosZ = posZIn;
      this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
      this.particleTextureJitterX = this.rand.nextFloat() * 3.0F;
      this.particleTextureJitterY = this.rand.nextFloat() * 3.0F;
      this.particleScale = (this.rand.nextFloat() * 0.5F + 0.5F) * 2.0F;
      this.particleMaxAge = (int)(4.0F / (this.rand.nextFloat() * 0.9F + 0.1F));
      this.particleAge = 0;
   }

   public Particle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
      this(worldIn, xCoordIn, yCoordIn, zCoordIn);
      this.motionX = xSpeedIn + (Math.random() * 2.0D - 1.0D) * 0.4000000059604645D;
      this.motionY = ySpeedIn + (Math.random() * 2.0D - 1.0D) * 0.4000000059604645D;
      this.motionZ = zSpeedIn + (Math.random() * 2.0D - 1.0D) * 0.4000000059604645D;
      float f = (float)(Math.random() + Math.random() + 1.0D) * 0.15F;
      float f1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
      this.motionX = this.motionX / (double)f1 * (double)f * 0.4000000059604645D;
      this.motionY = this.motionY / (double)f1 * (double)f * 0.4000000059604645D + 0.10000000149011612D;
      this.motionZ = this.motionZ / (double)f1 * (double)f * 0.4000000059604645D;
   }

   public Particle multiplyVelocity(float multiplier) {
      this.motionX *= (double)multiplier;
      this.motionY = (this.motionY - 0.10000000149011612D) * (double)multiplier + 0.10000000149011612D;
      this.motionZ *= (double)multiplier;
      return this;
   }

   public Particle multipleParticleScaleBy(float scale) {
      this.setSize(0.2F * scale, 0.2F * scale);
      this.particleScale *= scale;
      return this;
   }

   public void setRBGColorF(float particleRedIn, float particleGreenIn, float particleBlueIn) {
      this.particleRed = particleRedIn;
      this.particleGreen = particleGreenIn;
      this.particleBlue = particleBlueIn;
   }

   public void setAlphaF(float alpha) {
      this.particleAlpha = alpha;
   }

   public boolean isTransparent() {
      return false;
   }

   public float getRedColorF() {
      return this.particleRed;
   }

   public float getGreenColorF() {
      return this.particleGreen;
   }

   public float getBlueColorF() {
      return this.particleBlue;
   }

   public void setMaxAge(int p_187114_1_) {
      this.particleMaxAge = p_187114_1_;
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if(this.particleAge++ >= this.particleMaxAge) {
         this.setExpired();
      }

      this.motionY -= 0.04D * (double)this.particleGravity;
      this.moveEntity(this.motionX, this.motionY, this.motionZ);
      this.motionX *= 0.9800000190734863D;
      this.motionY *= 0.9800000190734863D;
      this.motionZ *= 0.9800000190734863D;
      if(this.isCollided) {
         this.motionX *= 0.699999988079071D;
         this.motionZ *= 0.699999988079071D;
      }
   }

   public void renderParticle(VertexBuffer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
      float f = (float)this.particleTextureIndexX / 16.0F;
      float f1 = f + 0.0624375F;
      float f2 = (float)this.particleTextureIndexY / 16.0F;
      float f3 = f2 + 0.0624375F;
      float f4 = 0.1F * this.particleScale;
      if(this.particleTexture != null) {
         f = this.particleTexture.getMinU();
         f1 = this.particleTexture.getMaxU();
         f2 = this.particleTexture.getMinV();
         f3 = this.particleTexture.getMaxV();
      }

      float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
      float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
      float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
      int i = this.getBrightnessForRender(partialTicks);
      int j = i >> 16 & 65535;
      int k = i & 65535;
      worldRendererIn.pos((double)(f5 - rotationX * f4 - rotationXY * f4), (double)(f6 - rotationZ * f4), (double)(f7 - rotationYZ * f4 - rotationXZ * f4)).tex((double)f1, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
      worldRendererIn.pos((double)(f5 - rotationX * f4 + rotationXY * f4), (double)(f6 + rotationZ * f4), (double)(f7 - rotationYZ * f4 + rotationXZ * f4)).tex((double)f1, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
      worldRendererIn.pos((double)(f5 + rotationX * f4 + rotationXY * f4), (double)(f6 + rotationZ * f4), (double)(f7 + rotationYZ * f4 + rotationXZ * f4)).tex((double)f, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
      worldRendererIn.pos((double)(f5 + rotationX * f4 - rotationXY * f4), (double)(f6 - rotationZ * f4), (double)(f7 + rotationYZ * f4 - rotationXZ * f4)).tex((double)f, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
   }

   public int getFXLayer() {
      return 0;
   }

   public void setParticleTexture(TextureAtlasSprite texture) {
      int i = this.getFXLayer();
      if(i == 1) {
         this.particleTexture = texture;
      } else {
         throw new RuntimeException("Invalid call to Particle.setTex, use coordinate methods");
      }
   }

   public void setParticleTextureIndex(int particleTextureIndex) {
      if(this.getFXLayer() != 0) {
         throw new RuntimeException("Invalid call to Particle.setMiscTex");
      } else {
         this.particleTextureIndexX = particleTextureIndex % 16;
         this.particleTextureIndexY = particleTextureIndex / 16;
      }
   }

   public void nextTextureIndexX() {
      ++this.particleTextureIndexX;
   }

   public String toString() {
      return this.getClass().getSimpleName() + ", Pos (" + this.posX + "," + this.posY + "," + this.posZ + "), RGBA (" + this.particleRed + "," + this.particleGreen + "," + this.particleBlue + "," + this.particleAlpha + "), Age " + this.particleAge;
   }

   public void setExpired() {
      this.isExpired = true;
   }

   protected void setSize(float p_187115_1_, float p_187115_2_) {
      if(p_187115_1_ != this.width || p_187115_2_ != this.height) {
         this.width = p_187115_1_;
         this.height = p_187115_2_;
         AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
         this.setEntityBoundingBox(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + (double)this.width, axisalignedbb.minY + (double)this.height, axisalignedbb.minZ + (double)this.width));
      }
   }

   public void setPosition(double p_187109_1_, double p_187109_3_, double p_187109_5_) {
      this.posX = p_187109_1_;
      this.posY = p_187109_3_;
      this.posZ = p_187109_5_;
      float f = this.width / 2.0F;
      float f1 = this.height;
      this.setEntityBoundingBox(new AxisAlignedBB(p_187109_1_ - (double)f, p_187109_3_, p_187109_5_ - (double)f, p_187109_1_ + (double)f, p_187109_3_ + (double)f1, p_187109_5_ + (double)f));
   }

   public void moveEntity(double x, double y, double z) {
      double d0 = x;
      double d1 = y;
      double d2 = z;
      List<AxisAlignedBB> list = this.worldObj.getCollisionBoxes((Entity)null, this.getEntityBoundingBox().addCoord(x, y, z));

      for(AxisAlignedBB axisalignedbb : list) {
         y = axisalignedbb.calculateYOffset(this.getEntityBoundingBox(), y);
      }

      this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));

      for(AxisAlignedBB axisalignedbb1 : list) {
         x = axisalignedbb1.calculateXOffset(this.getEntityBoundingBox(), x);
      }

      this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, 0.0D, 0.0D));

      for(AxisAlignedBB axisalignedbb2 : list) {
         z = axisalignedbb2.calculateZOffset(this.getEntityBoundingBox(), z);
      }

      this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, z));
      this.resetPositionToBB();
      this.isCollided = d1 != y && d1 < 0.0D;
      if(d0 != x) {
         this.motionX = 0.0D;
      }

      if(d2 != z) {
         this.motionZ = 0.0D;
      }
   }

   protected void resetPositionToBB() {
      AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
      this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
      this.posY = axisalignedbb.minY;
      this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
   }

   public int getBrightnessForRender(float p_189214_1_) {
      BlockPos blockpos = new BlockPos(this.posX, this.posY, this.posZ);
      return this.worldObj.isBlockLoaded(blockpos)?this.worldObj.getCombinedLight(blockpos, 0):0;
   }

   public boolean isAlive() {
      return !this.isExpired;
   }

   public AxisAlignedBB getEntityBoundingBox() {
      return this.boundingBox;
   }

   public void setEntityBoundingBox(AxisAlignedBB p_187108_1_) {
      this.boundingBox = p_187108_1_;
   }
}