package net.minecraft.item;

import com.google.common.collect.Multimap;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemTool extends Item {
   private Set<Block> effectiveBlocks;
   protected float efficiencyOnProperMaterial;
   protected float damageVsEntity;
   protected float attackSpeed;
   protected Item.ToolMaterial toolMaterial;

   protected ItemTool(float attackDamageIn, float attackSpeedIn, Item.ToolMaterial materialIn, Set<Block> effectiveBlocksIn) {
      this.efficiencyOnProperMaterial = 4.0F;
      this.toolMaterial = materialIn;
      this.effectiveBlocks = effectiveBlocksIn;
      this.maxStackSize = 1;
      this.setMaxDamage(materialIn.getMaxUses());
      this.efficiencyOnProperMaterial = materialIn.getEfficiencyOnProperMaterial();
      this.damageVsEntity = attackDamageIn + materialIn.getDamageVsEntity();
      this.attackSpeed = attackSpeedIn;
      this.setCreativeTab(CreativeTabs.TOOLS);
   }

   protected ItemTool(Item.ToolMaterial materialIn, Set<Block> effectiveBlocksIn) {
      this(0.0F, 0.0F, materialIn, effectiveBlocksIn);
   }

   public float getStrVsBlock(ItemStack stack, IBlockState state) {
      return this.effectiveBlocks.contains(state.getBlock())?this.efficiencyOnProperMaterial:1.0F;
   }

   public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
      stack.damageItem(2, attacker);
      return true;
   }

   public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
      if((double)state.getBlockHardness(worldIn, pos) != 0.0D) {
         stack.damageItem(1, entityLiving);
      }

      return true;
   }

   public boolean isFull3D() {
      return true;
   }

   public Item.ToolMaterial getToolMaterial() {
      return this.toolMaterial;
   }

   public int getItemEnchantability() {
      return this.toolMaterial.getEnchantability();
   }

   public String getToolMaterialName() {
      return this.toolMaterial.toString();
   }

   public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
      return this.toolMaterial.getRepairItem() == repair.getItem()?true:super.getIsRepairable(toRepair, repair);
   }

   public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
      Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);
      if(equipmentSlot == EntityEquipmentSlot.MAINHAND) {
         multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", (double)this.damageVsEntity, 0));
         multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", (double)this.attackSpeed, 0));
      }

      return multimap;
   }
}
