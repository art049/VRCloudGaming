package net.minecraft.entity.projectile;

import com.google.common.base.Optional;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityPotion extends EntityThrowable {
   private static final DataParameter<Optional<ItemStack>> ITEM = EntityDataManager.<Optional<ItemStack>>createKey(EntityItem.class, DataSerializers.OPTIONAL_ITEM_STACK);
   private static final Logger LOGGER = LogManager.getLogger();

   public EntityPotion(World worldIn) {
      super(worldIn);
   }

   public EntityPotion(World worldIn, EntityLivingBase throwerIn, ItemStack potionDamageIn) {
      super(worldIn, throwerIn);
      this.setItem(potionDamageIn);
   }

   public EntityPotion(World worldIn, double x, double y, double z, @Nullable ItemStack potionDamageIn) {
      super(worldIn, x, y, z);
      if(potionDamageIn != null) {
         this.setItem(potionDamageIn);
      }
   }

   protected void entityInit() {
      this.getDataManager().register(ITEM, Optional.<ItemStack>absent());
   }

   public ItemStack getPotion() {
      ItemStack itemstack = (ItemStack)((Optional)this.getDataManager().get(ITEM)).orNull();
      if(itemstack == null || itemstack.getItem() != Items.SPLASH_POTION && itemstack.getItem() != Items.LINGERING_POTION) {
         if(this.worldObj != null) {
            LOGGER.error("ThrownPotion entity " + this.getEntityId() + " has no item?!");
         }

         return new ItemStack(Items.SPLASH_POTION);
      } else {
         return itemstack;
      }
   }

   public void setItem(@Nullable ItemStack stack) {
      this.getDataManager().set(ITEM, Optional.fromNullable(stack));
      this.getDataManager().setDirty(ITEM);
   }

   protected float getGravityVelocity() {
      return 0.05F;
   }

   protected void onImpact(RayTraceResult result) {
      if(!this.worldObj.isRemote) {
         ItemStack itemstack = this.getPotion();
         PotionType potiontype = PotionUtils.getPotionFromItem(itemstack);
         List<PotionEffect> list = PotionUtils.getEffectsFromStack(itemstack);
         if(result.typeOfHit == RayTraceResult.Type.BLOCK && potiontype == PotionTypes.WATER && list.isEmpty()) {
            BlockPos blockpos = result.getBlockPos().offset(result.sideHit);
            this.extinguishFires(blockpos);

            for(EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
               this.extinguishFires(blockpos.offset(enumfacing));
            }

            this.worldObj.playEvent(2002, new BlockPos(this), PotionType.getID(potiontype));
            this.setDead();
         } else {
            if(!list.isEmpty()) {
               if(this.isLingering()) {
                  EntityAreaEffectCloud entityareaeffectcloud = new EntityAreaEffectCloud(this.worldObj, this.posX, this.posY, this.posZ);
                  entityareaeffectcloud.setOwner(this.getThrower());
                  entityareaeffectcloud.setRadius(3.0F);
                  entityareaeffectcloud.setRadiusOnUse(-0.5F);
                  entityareaeffectcloud.setWaitTime(10);
                  entityareaeffectcloud.setRadiusPerTick(-entityareaeffectcloud.getRadius() / (float)entityareaeffectcloud.getDuration());
                  entityareaeffectcloud.setPotion(potiontype);

                  for(PotionEffect potioneffect : PotionUtils.getFullEffectsFromItem(itemstack)) {
                     entityareaeffectcloud.addEffect(new PotionEffect(potioneffect.getPotion(), potioneffect.getDuration(), potioneffect.getAmplifier()));
                  }

                  this.worldObj.spawnEntityInWorld(entityareaeffectcloud);
               } else {
                  AxisAlignedBB axisalignedbb = this.getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D);
                  List<EntityLivingBase> list1 = this.worldObj.<EntityLivingBase>getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb);
                  if(!list1.isEmpty()) {
                     for(EntityLivingBase entitylivingbase : list1) {
                        if(entitylivingbase.canBeHitWithPotion()) {
                           double d0 = this.getDistanceSqToEntity(entitylivingbase);
                           if(d0 < 16.0D) {
                              double d1 = 1.0D - Math.sqrt(d0) / 4.0D;
                              if(entitylivingbase == result.entityHit) {
                                 d1 = 1.0D;
                              }

                              for(PotionEffect potioneffect1 : list) {
                                 Potion potion = potioneffect1.getPotion();
                                 if(potion.isInstant()) {
                                    potion.affectEntity(this, this.getThrower(), entitylivingbase, potioneffect1.getAmplifier(), d1);
                                 } else {
                                    int i = (int)(d1 * (double)potioneffect1.getDuration() + 0.5D);
                                    if(i > 20) {
                                       entitylivingbase.addPotionEffect(new PotionEffect(potion, i, potioneffect1.getAmplifier()));
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            this.worldObj.playEvent(2002, new BlockPos(this), PotionType.getID(potiontype));
            this.setDead();
         }
      }
   }

   private boolean isLingering() {
      return this.getPotion().getItem() == Items.LINGERING_POTION;
   }

   private void extinguishFires(BlockPos pos) {
      if(this.worldObj.getBlockState(pos).getBlock() == Blocks.FIRE) {
         this.worldObj.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
      }
   }

   public void readEntityFromNBT(NBTTagCompound compound) {
      super.readEntityFromNBT(compound);
      ItemStack itemstack = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("Potion"));
      if(itemstack == null) {
         this.setDead();
      } else {
         this.setItem(itemstack);
      }
   }

   public void writeEntityToNBT(NBTTagCompound compound) {
      super.writeEntityToNBT(compound);
      ItemStack itemstack = this.getPotion();
      if(itemstack != null) {
         compound.setTag("Potion", itemstack.writeToNBT(new NBTTagCompound()));
      }
   }
}
