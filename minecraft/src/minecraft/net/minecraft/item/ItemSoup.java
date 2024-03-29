package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.world.World;

public class ItemSoup extends ItemFood {
   public ItemSoup(int healAmount) {
      super(healAmount, false);
      this.setMaxStackSize(1);
   }

   @Nullable
   public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
      super.onItemUseFinish(stack, worldIn, entityLiving);
      return new ItemStack(Items.BOWL);
   }
}
