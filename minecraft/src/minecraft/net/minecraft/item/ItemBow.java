package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class ItemBow extends Item {
   public ItemBow() {
      this.maxStackSize = 1;
      this.setMaxDamage(384);
      this.setCreativeTab(CreativeTabs.COMBAT);
      this.addPropertyOverride(new ResourceLocation("pull"), new IItemPropertyGetter() {
         public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
            if(entityIn == null) {
               return 0.0F;
            } else {
               ItemStack itemstack = entityIn.getActiveItemStack();
               return itemstack != null && itemstack.getItem() == Items.BOW?(float)(stack.getMaxItemUseDuration() - entityIn.getItemInUseCount()) / 20.0F:0.0F;
            }
         }
      });
      this.addPropertyOverride(new ResourceLocation("pulling"), new IItemPropertyGetter() {
         public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
            return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack?1.0F:0.0F;
         }
      });
   }

   private ItemStack findAmmo(EntityPlayer player) {
      if(this.isArrow(player.getHeldItem(EnumHand.OFF_HAND))) {
         return player.getHeldItem(EnumHand.OFF_HAND);
      } else if(this.isArrow(player.getHeldItem(EnumHand.MAIN_HAND))) {
         return player.getHeldItem(EnumHand.MAIN_HAND);
      } else {
         for(int i = 0; i < player.inventory.getSizeInventory(); ++i) {
            ItemStack itemstack = player.inventory.getStackInSlot(i);
            if(this.isArrow(itemstack)) {
               return itemstack;
            }
         }

         return null;
      }
   }

   protected boolean isArrow(@Nullable ItemStack stack) {
      return stack != null && stack.getItem() instanceof ItemArrow;
   }

   public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
      if(entityLiving instanceof EntityPlayer) {
         EntityPlayer entityplayer = (EntityPlayer)entityLiving;
         boolean flag = entityplayer.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0;
         ItemStack itemstack = this.findAmmo(entityplayer);
         if(itemstack != null || flag) {
            if(itemstack == null) {
               itemstack = new ItemStack(Items.ARROW);
            }

            int i = this.getMaxItemUseDuration(stack) - timeLeft;
            float f = getArrowVelocity(i);
            if((double)f >= 0.1D) {
               boolean flag1 = flag && itemstack.getItem() == Items.ARROW;
               if(!worldIn.isRemote) {
                  ItemArrow itemarrow = (ItemArrow)((ItemArrow)(itemstack.getItem() instanceof ItemArrow?itemstack.getItem():Items.ARROW));
                  EntityArrow entityarrow = itemarrow.createArrow(worldIn, itemstack, entityplayer);
                  entityarrow.setAim(entityplayer, entityplayer.rotationPitch, entityplayer.rotationYaw, 0.0F, f * 3.0F, 1.0F);
                  if(f == 1.0F) {
                     entityarrow.setIsCritical(true);
                  }

                  int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);
                  if(j > 0) {
                     entityarrow.setDamage(entityarrow.getDamage() + (double)j * 0.5D + 0.5D);
                  }

                  int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);
                  if(k > 0) {
                     entityarrow.setKnockbackStrength(k);
                  }

                  if(EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack) > 0) {
                     entityarrow.setFire(100);
                  }

                  stack.damageItem(1, entityplayer);
                  if(flag1) {
                     entityarrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
                  }

                  worldIn.spawnEntityInWorld(entityarrow);
               }

               worldIn.playSound((EntityPlayer)null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
               if(!flag1) {
                  --itemstack.stackSize;
                  if(itemstack.stackSize == 0) {
                     entityplayer.inventory.deleteStack(itemstack);
                  }
               }

               entityplayer.addStat(StatList.getObjectUseStats(this));
            }
         }
      }
   }

   public static float getArrowVelocity(int charge) {
      float f = (float)charge / 20.0F;
      f = (f * f + f * 2.0F) / 3.0F;
      if(f > 1.0F) {
         f = 1.0F;
      }

      return f;
   }

   public int getMaxItemUseDuration(ItemStack stack) {
      return 72000;
   }

   public EnumAction getItemUseAction(ItemStack stack) {
      return EnumAction.BOW;
   }

   public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
      boolean flag = this.findAmmo(playerIn) != null;
      if(!playerIn.capabilities.isCreativeMode && !flag) {
         return !flag?new ActionResult(EnumActionResult.FAIL, itemStackIn):new ActionResult(EnumActionResult.PASS, itemStackIn);
      } else {
         playerIn.setActiveHand(hand);
         return new ActionResult(EnumActionResult.SUCCESS, itemStackIn);
      }
   }

   public int getItemEnchantability() {
      return 1;
   }
}
