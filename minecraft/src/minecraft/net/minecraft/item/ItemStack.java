package net.minecraft.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentDurability;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public final class ItemStack {
   public static final DecimalFormat DECIMALFORMAT = new DecimalFormat("#.##");
   public int stackSize;
   public int animationsToGo;
   private Item item;
   private NBTTagCompound stackTagCompound;
   private int itemDamage;
   private EntityItemFrame itemFrame;
   private Block canDestroyCacheBlock;
   private boolean canDestroyCacheResult;
   private Block canPlaceOnCacheBlock;
   private boolean canPlaceOnCacheResult;

   public ItemStack(Block blockIn) {
      this((Block)blockIn, 1);
   }

   public ItemStack(Block blockIn, int amount) {
      this((Block)blockIn, amount, 0);
   }

   public ItemStack(Block blockIn, int amount, int meta) {
      this(Item.getItemFromBlock(blockIn), amount, meta);
   }

   public ItemStack(Item itemIn) {
      this((Item)itemIn, 1);
   }

   public ItemStack(Item itemIn, int amount) {
      this((Item)itemIn, amount, 0);
   }

   public ItemStack(Item itemIn, int amount, int meta) {
      this.canDestroyCacheBlock = null;
      this.canDestroyCacheResult = false;
      this.canPlaceOnCacheBlock = null;
      this.canPlaceOnCacheResult = false;
      this.item = itemIn;
      this.stackSize = amount;
      this.itemDamage = meta;
      if(this.itemDamage < 0) {
         this.itemDamage = 0;
      }
   }

   public static ItemStack loadItemStackFromNBT(NBTTagCompound nbt) {
      ItemStack itemstack = new ItemStack();
      itemstack.readFromNBT(nbt);
      return itemstack.getItem() != null?itemstack:null;
   }

   private ItemStack() {
      this.canDestroyCacheBlock = null;
      this.canDestroyCacheResult = false;
      this.canPlaceOnCacheBlock = null;
      this.canPlaceOnCacheResult = false;
   }

   public ItemStack splitStack(int amount) {
      amount = Math.min(amount, this.stackSize);
      ItemStack itemstack = new ItemStack(this.item, amount, this.itemDamage);
      if(this.stackTagCompound != null) {
         itemstack.stackTagCompound = (NBTTagCompound)this.stackTagCompound.copy();
      }

      this.stackSize -= amount;
      return itemstack;
   }

   public Item getItem() {
      return this.item;
   }

   public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
      EnumActionResult enumactionresult = this.getItem().onItemUse(this, playerIn, worldIn, pos, hand, side, hitX, hitY, hitZ);
      if(enumactionresult == EnumActionResult.SUCCESS) {
         playerIn.addStat(StatList.getObjectUseStats(this.item));
      }

      return enumactionresult;
   }

   public float getStrVsBlock(IBlockState blockIn) {
      return this.getItem().getStrVsBlock(this, blockIn);
   }

   public ActionResult<ItemStack> useItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
      return this.getItem().onItemRightClick(this, worldIn, playerIn, hand);
   }

   @Nullable
   public ItemStack onItemUseFinish(World worldIn, EntityLivingBase entityLiving) {
      return this.getItem().onItemUseFinish(this, worldIn, entityLiving);
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      ResourceLocation resourcelocation = (ResourceLocation)Item.REGISTRY.getNameForObject(this.item);
      nbt.setString("id", resourcelocation == null?"minecraft:air":resourcelocation.toString());
      nbt.setByte("Count", (byte)this.stackSize);
      nbt.setShort("Damage", (short)this.itemDamage);
      if(this.stackTagCompound != null) {
         nbt.setTag("tag", this.stackTagCompound);
      }

      return nbt;
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.item = Item.getByNameOrId(nbt.getString("id"));
      this.stackSize = nbt.getByte("Count");
      this.itemDamage = nbt.getShort("Damage");
      if(this.itemDamage < 0) {
         this.itemDamage = 0;
      }

      if(nbt.hasKey("tag", 10)) {
         this.stackTagCompound = nbt.getCompoundTag("tag");
         if(this.item != null) {
            this.item.updateItemStackNBT(this.stackTagCompound);
         }
      }
   }

   public int getMaxStackSize() {
      return this.getItem().getItemStackLimit();
   }

   public boolean isStackable() {
      return this.getMaxStackSize() > 1 && (!this.isItemStackDamageable() || !this.isItemDamaged());
   }

   public boolean isItemStackDamageable() {
      return this.item == null?false:(this.item.getMaxDamage() <= 0?false:!this.hasTagCompound() || !this.getTagCompound().getBoolean("Unbreakable"));
   }

   public boolean getHasSubtypes() {
      return this.item.getHasSubtypes();
   }

   public boolean isItemDamaged() {
      return this.isItemStackDamageable() && this.itemDamage > 0;
   }

   public int getItemDamage() {
      return this.itemDamage;
   }

   public int getMetadata() {
      return this.itemDamage;
   }

   public void setItemDamage(int meta) {
      this.itemDamage = meta;
      if(this.itemDamage < 0) {
         this.itemDamage = 0;
      }
   }

   public int getMaxDamage() {
      return this.item == null?0:this.item.getMaxDamage();
   }

   public boolean attemptDamageItem(int amount, Random rand) {
      if(!this.isItemStackDamageable()) {
         return false;
      } else {
         if(amount > 0) {
            int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, this);
            int j = 0;

            for(int k = 0; i > 0 && k < amount; ++k) {
               if(EnchantmentDurability.negateDamage(this, i, rand)) {
                  ++j;
               }
            }

            amount -= j;
            if(amount <= 0) {
               return false;
            }
         }

         this.itemDamage += amount;
         return this.itemDamage > this.getMaxDamage();
      }
   }

   public void damageItem(int amount, EntityLivingBase entityIn) {
      if(!(entityIn instanceof EntityPlayer) || !((EntityPlayer)entityIn).capabilities.isCreativeMode) {
         if(this.isItemStackDamageable()) {
            if(this.attemptDamageItem(amount, entityIn.getRNG())) {
               entityIn.renderBrokenItemStack(this);
               --this.stackSize;
               if(entityIn instanceof EntityPlayer) {
                  EntityPlayer entityplayer = (EntityPlayer)entityIn;
                  entityplayer.addStat(StatList.getObjectBreakStats(this.item));
               }

               if(this.stackSize < 0) {
                  this.stackSize = 0;
               }

               this.itemDamage = 0;
            }
         }
      }
   }

   public void hitEntity(EntityLivingBase entityIn, EntityPlayer playerIn) {
      boolean flag = this.item.hitEntity(this, entityIn, playerIn);
      if(flag) {
         playerIn.addStat(StatList.getObjectUseStats(this.item));
      }
   }

   public void onBlockDestroyed(World worldIn, IBlockState blockIn, BlockPos pos, EntityPlayer playerIn) {
      boolean flag = this.item.onBlockDestroyed(this, worldIn, blockIn, pos, playerIn);
      if(flag) {
         playerIn.addStat(StatList.getObjectUseStats(this.item));
      }
   }

   public boolean canHarvestBlock(IBlockState blockIn) {
      return this.item.canHarvestBlock(blockIn);
   }

   public boolean interactWithEntity(EntityPlayer playerIn, EntityLivingBase entityIn, EnumHand hand) {
      return this.item.itemInteractionForEntity(this, playerIn, entityIn, hand);
   }

   public ItemStack copy() {
      ItemStack itemstack = new ItemStack(this.item, this.stackSize, this.itemDamage);
      if(this.stackTagCompound != null) {
         itemstack.stackTagCompound = (NBTTagCompound)this.stackTagCompound.copy();
      }

      return itemstack;
   }

   public static boolean areItemStackTagsEqual(@Nullable ItemStack stackA, @Nullable ItemStack stackB) {
      return stackA == null && stackB == null?true:(stackA != null && stackB != null?(stackA.stackTagCompound == null && stackB.stackTagCompound != null?false:stackA.stackTagCompound == null || stackA.stackTagCompound.equals(stackB.stackTagCompound)):false);
   }

   public static boolean areItemStacksEqual(@Nullable ItemStack stackA, @Nullable ItemStack stackB) {
      return stackA == null && stackB == null?true:(stackA != null && stackB != null?stackA.isItemStackEqual(stackB):false);
   }

   private boolean isItemStackEqual(ItemStack other) {
      return this.stackSize != other.stackSize?false:(this.item != other.item?false:(this.itemDamage != other.itemDamage?false:(this.stackTagCompound == null && other.stackTagCompound != null?false:this.stackTagCompound == null || this.stackTagCompound.equals(other.stackTagCompound))));
   }

   public static boolean areItemsEqual(@Nullable ItemStack stackA, @Nullable ItemStack stackB) {
      return stackA == stackB?true:(stackA != null && stackB != null?stackA.isItemEqual(stackB):false);
   }

   public static boolean areItemsEqualIgnoreDurability(@Nullable ItemStack stackA, @Nullable ItemStack stackB) {
      return stackA == stackB?true:(stackA != null && stackB != null?stackA.isItemEqualIgnoreDurability(stackB):false);
   }

   public boolean isItemEqual(@Nullable ItemStack other) {
      return other != null && this.item == other.item && this.itemDamage == other.itemDamage;
   }

   public boolean isItemEqualIgnoreDurability(@Nullable ItemStack stack) {
      return !this.isItemStackDamageable()?this.isItemEqual(stack):stack != null && this.item == stack.item;
   }

   public String getUnlocalizedName() {
      return this.item.getUnlocalizedName(this);
   }

   public static ItemStack copyItemStack(ItemStack stack) {
      return stack == null?null:stack.copy();
   }

   public String toString() {
      return this.stackSize + "x" + this.item.getUnlocalizedName() + "@" + this.itemDamage;
   }

   public void updateAnimation(World worldIn, Entity entityIn, int inventorySlot, boolean isCurrentItem) {
      if(this.animationsToGo > 0) {
         --this.animationsToGo;
      }

      if(this.item != null) {
         this.item.onUpdate(this, worldIn, entityIn, inventorySlot, isCurrentItem);
      }
   }

   public void onCrafting(World worldIn, EntityPlayer playerIn, int amount) {
      playerIn.addStat(StatList.getCraftStats(this.item), amount);
      this.item.onCreated(this, worldIn, playerIn);
   }

   public int getMaxItemUseDuration() {
      return this.getItem().getMaxItemUseDuration(this);
   }

   public EnumAction getItemUseAction() {
      return this.getItem().getItemUseAction(this);
   }

   public void onPlayerStoppedUsing(World worldIn, EntityLivingBase entityLiving, int timeLeft) {
      this.getItem().onPlayerStoppedUsing(this, worldIn, entityLiving, timeLeft);
   }

   public boolean hasTagCompound() {
      return this.stackTagCompound != null;
   }

   @Nullable
   public NBTTagCompound getTagCompound() {
      return this.stackTagCompound;
   }

   public NBTTagCompound getSubCompound(String key, boolean create) {
      if(this.stackTagCompound != null && this.stackTagCompound.hasKey(key, 10)) {
         return this.stackTagCompound.getCompoundTag(key);
      } else if(create) {
         NBTTagCompound nbttagcompound = new NBTTagCompound();
         this.setTagInfo(key, nbttagcompound);
         return nbttagcompound;
      } else {
         return null;
      }
   }

   public NBTTagList getEnchantmentTagList() {
      return this.stackTagCompound == null?null:this.stackTagCompound.getTagList("ench", 10);
   }

   public void setTagCompound(NBTTagCompound nbt) {
      this.stackTagCompound = nbt;
   }

   public String getDisplayName() {
      String s = this.getItem().getItemStackDisplayName(this);
      if(this.stackTagCompound != null && this.stackTagCompound.hasKey("display", 10)) {
         NBTTagCompound nbttagcompound = this.stackTagCompound.getCompoundTag("display");
         if(nbttagcompound.hasKey("Name", 8)) {
            s = nbttagcompound.getString("Name");
         }
      }

      return s;
   }

   public ItemStack setStackDisplayName(String displayName) {
      if(this.stackTagCompound == null) {
         this.stackTagCompound = new NBTTagCompound();
      }

      if(!this.stackTagCompound.hasKey("display", 10)) {
         this.stackTagCompound.setTag("display", new NBTTagCompound());
      }

      this.stackTagCompound.getCompoundTag("display").setString("Name", displayName);
      return this;
   }

   public void clearCustomName() {
      if(this.stackTagCompound != null) {
         if(this.stackTagCompound.hasKey("display", 10)) {
            NBTTagCompound nbttagcompound = this.stackTagCompound.getCompoundTag("display");
            nbttagcompound.removeTag("Name");
            if(nbttagcompound.hasNoTags()) {
               this.stackTagCompound.removeTag("display");
               if(this.stackTagCompound.hasNoTags()) {
                  this.setTagCompound((NBTTagCompound)null);
               }
            }
         }
      }
   }

   public boolean hasDisplayName() {
      return this.stackTagCompound == null?false:(!this.stackTagCompound.hasKey("display", 10)?false:this.stackTagCompound.getCompoundTag("display").hasKey("Name", 8));
   }

   public List<String> getTooltip(EntityPlayer playerIn, boolean advanced) {
      List<String> list = Lists.<String>newArrayList();
      String s = this.getDisplayName();
      if(this.hasDisplayName()) {
         s = TextFormatting.ITALIC + s;
      }

      s = s + TextFormatting.RESET;
      if(advanced) {
         String s1 = "";
         if(!s.isEmpty()) {
            s = s + " (";
            s1 = ")";
         }

         int i = Item.getIdFromItem(this.item);
         if(this.getHasSubtypes()) {
            s = s + String.format("#%04d/%d%s", new Object[]{Integer.valueOf(i), Integer.valueOf(this.itemDamage), s1});
         } else {
            s = s + String.format("#%04d%s", new Object[]{Integer.valueOf(i), s1});
         }
      } else if(!this.hasDisplayName() && this.item == Items.FILLED_MAP) {
         s = s + " #" + this.itemDamage;
      }

      list.add(s);
      int i1 = 0;
      if(this.hasTagCompound() && this.stackTagCompound.hasKey("HideFlags", 99)) {
         i1 = this.stackTagCompound.getInteger("HideFlags");
      }

      if((i1 & 32) == 0) {
         this.item.addInformation(this, playerIn, list, advanced);
      }

      if(this.hasTagCompound()) {
         if((i1 & 1) == 0) {
            NBTTagList nbttaglist = this.getEnchantmentTagList();
            if(nbttaglist != null) {
               for(int j = 0; j < nbttaglist.tagCount(); ++j) {
                  int k = nbttaglist.getCompoundTagAt(j).getShort("id");
                  int l = nbttaglist.getCompoundTagAt(j).getShort("lvl");
                  if(Enchantment.getEnchantmentByID(k) != null) {
                     list.add(Enchantment.getEnchantmentByID(k).getTranslatedName(l));
                  }
               }
            }
         }

         if(this.stackTagCompound.hasKey("display", 10)) {
            NBTTagCompound nbttagcompound = this.stackTagCompound.getCompoundTag("display");
            if(nbttagcompound.hasKey("color", 3)) {
               if(advanced) {
                  list.add("Color: #" + String.format("%06X", new Object[]{Integer.valueOf(nbttagcompound.getInteger("color"))}));
               } else {
                  list.add(TextFormatting.ITALIC + I18n.translateToLocal("item.dyed"));
               }
            }

            if(nbttagcompound.getTagId("Lore") == 9) {
               NBTTagList nbttaglist3 = nbttagcompound.getTagList("Lore", 8);
               if(!nbttaglist3.hasNoTags()) {
                  for(int l1 = 0; l1 < nbttaglist3.tagCount(); ++l1) {
                     list.add(TextFormatting.DARK_PURPLE + "" + TextFormatting.ITALIC + nbttaglist3.getStringTagAt(l1));
                  }
               }
            }
         }
      }

      for(EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values()) {
         Multimap<String, AttributeModifier> multimap = this.getAttributeModifiers(entityequipmentslot);
         if(!multimap.isEmpty() && (i1 & 2) == 0) {
            list.add("");
            list.add(I18n.translateToLocal("item.modifiers." + entityequipmentslot.getName()));

            for(Entry<String, AttributeModifier> entry : multimap.entries()) {
               AttributeModifier attributemodifier = (AttributeModifier)entry.getValue();
               double d0 = attributemodifier.getAmount();
               boolean flag = false;
               if(attributemodifier.getID() == Item.ATTACK_DAMAGE_MODIFIER) {
                  d0 = d0 + playerIn.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                  d0 = d0 + (double)EnchantmentHelper.getModifierForCreature(this, EnumCreatureAttribute.UNDEFINED);
                  flag = true;
               } else if(attributemodifier.getID() == Item.ATTACK_SPEED_MODIFIER) {
                  d0 += playerIn.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
                  flag = true;
               }

               double d1;
               if(attributemodifier.getOperation() != 1 && attributemodifier.getOperation() != 2) {
                  d1 = d0;
               } else {
                  d1 = d0 * 100.0D;
               }

               if(flag) {
                  list.add(" " + I18n.translateToLocalFormatted("attribute.modifier.equals." + attributemodifier.getOperation(), new Object[]{DECIMALFORMAT.format(d1), I18n.translateToLocal("attribute.name." + (String)entry.getKey())}));
               } else if(d0 > 0.0D) {
                  list.add(TextFormatting.BLUE + " " + I18n.translateToLocalFormatted("attribute.modifier.plus." + attributemodifier.getOperation(), new Object[]{DECIMALFORMAT.format(d1), I18n.translateToLocal("attribute.name." + (String)entry.getKey())}));
               } else if(d0 < 0.0D) {
                  d1 = d1 * -1.0D;
                  list.add(TextFormatting.RED + " " + I18n.translateToLocalFormatted("attribute.modifier.take." + attributemodifier.getOperation(), new Object[]{DECIMALFORMAT.format(d1), I18n.translateToLocal("attribute.name." + (String)entry.getKey())}));
               }
            }
         }
      }

      if(this.hasTagCompound() && this.getTagCompound().getBoolean("Unbreakable") && (i1 & 4) == 0) {
         list.add(TextFormatting.BLUE + I18n.translateToLocal("item.unbreakable"));
      }

      if(this.hasTagCompound() && this.stackTagCompound.hasKey("CanDestroy", 9) && (i1 & 8) == 0) {
         NBTTagList nbttaglist1 = this.stackTagCompound.getTagList("CanDestroy", 8);
         if(!nbttaglist1.hasNoTags()) {
            list.add("");
            list.add(TextFormatting.GRAY + I18n.translateToLocal("item.canBreak"));

            for(int j1 = 0; j1 < nbttaglist1.tagCount(); ++j1) {
               Block block = Block.getBlockFromName(nbttaglist1.getStringTagAt(j1));
               if(block != null) {
                  list.add(TextFormatting.DARK_GRAY + block.getLocalizedName());
               } else {
                  list.add(TextFormatting.DARK_GRAY + "missingno");
               }
            }
         }
      }

      if(this.hasTagCompound() && this.stackTagCompound.hasKey("CanPlaceOn", 9) && (i1 & 16) == 0) {
         NBTTagList nbttaglist2 = this.stackTagCompound.getTagList("CanPlaceOn", 8);
         if(!nbttaglist2.hasNoTags()) {
            list.add("");
            list.add(TextFormatting.GRAY + I18n.translateToLocal("item.canPlace"));

            for(int k1 = 0; k1 < nbttaglist2.tagCount(); ++k1) {
               Block block1 = Block.getBlockFromName(nbttaglist2.getStringTagAt(k1));
               if(block1 != null) {
                  list.add(TextFormatting.DARK_GRAY + block1.getLocalizedName());
               } else {
                  list.add(TextFormatting.DARK_GRAY + "missingno");
               }
            }
         }
      }

      if(advanced) {
         if(this.isItemDamaged()) {
            list.add("Durability: " + (this.getMaxDamage() - this.getItemDamage()) + " / " + this.getMaxDamage());
         }

         list.add(TextFormatting.DARK_GRAY + ((ResourceLocation)Item.REGISTRY.getNameForObject(this.item)).toString());
         if(this.hasTagCompound()) {
            list.add(TextFormatting.DARK_GRAY + "NBT: " + this.getTagCompound().getKeySet().size() + " tag(s)");
         }
      }

      return list;
   }

   public boolean hasEffect() {
      return this.getItem().hasEffect(this);
   }

   public EnumRarity getRarity() {
      return this.getItem().getRarity(this);
   }

   public boolean isItemEnchantable() {
      return !this.getItem().isItemTool(this)?false:!this.isItemEnchanted();
   }

   public void addEnchantment(Enchantment ench, int level) {
      if(this.stackTagCompound == null) {
         this.setTagCompound(new NBTTagCompound());
      }

      if(!this.stackTagCompound.hasKey("ench", 9)) {
         this.stackTagCompound.setTag("ench", new NBTTagList());
      }

      NBTTagList nbttaglist = this.stackTagCompound.getTagList("ench", 10);
      NBTTagCompound nbttagcompound = new NBTTagCompound();
      nbttagcompound.setShort("id", (short)Enchantment.getEnchantmentID(ench));
      nbttagcompound.setShort("lvl", (short)((byte)level));
      nbttaglist.appendTag(nbttagcompound);
   }

   public boolean isItemEnchanted() {
      return this.stackTagCompound != null && this.stackTagCompound.hasKey("ench", 9);
   }

   public void setTagInfo(String key, NBTBase value) {
      if(this.stackTagCompound == null) {
         this.setTagCompound(new NBTTagCompound());
      }

      this.stackTagCompound.setTag(key, value);
   }

   public boolean canEditBlocks() {
      return this.getItem().canItemEditBlocks();
   }

   public boolean isOnItemFrame() {
      return this.itemFrame != null;
   }

   public void setItemFrame(EntityItemFrame frame) {
      this.itemFrame = frame;
   }

   @Nullable
   public EntityItemFrame getItemFrame() {
      return this.itemFrame;
   }

   public int getRepairCost() {
      return this.hasTagCompound() && this.stackTagCompound.hasKey("RepairCost", 3)?this.stackTagCompound.getInteger("RepairCost"):0;
   }

   public void setRepairCost(int cost) {
      if(!this.hasTagCompound()) {
         this.stackTagCompound = new NBTTagCompound();
      }

      this.stackTagCompound.setInteger("RepairCost", cost);
   }

   public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
      Multimap<String, AttributeModifier> multimap;
      if(this.hasTagCompound() && this.stackTagCompound.hasKey("AttributeModifiers", 9)) {
         multimap = HashMultimap.<String, AttributeModifier>create();
         NBTTagList nbttaglist = this.stackTagCompound.getTagList("AttributeModifiers", 10);

         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            AttributeModifier attributemodifier = SharedMonsterAttributes.readAttributeModifierFromNBT(nbttagcompound);
            if(attributemodifier != null && (!nbttagcompound.hasKey("Slot", 8) || nbttagcompound.getString("Slot").equals(equipmentSlot.getName())) && attributemodifier.getID().getLeastSignificantBits() != 0L && attributemodifier.getID().getMostSignificantBits() != 0L) {
               multimap.put(nbttagcompound.getString("AttributeName"), attributemodifier);
            }
         }
      } else {
         multimap = this.getItem().getItemAttributeModifiers(equipmentSlot);
      }

      return multimap;
   }

   public void addAttributeModifier(String attributeName, AttributeModifier modifier, EntityEquipmentSlot equipmentSlot) {
      if(this.stackTagCompound == null) {
         this.stackTagCompound = new NBTTagCompound();
      }

      if(!this.stackTagCompound.hasKey("AttributeModifiers", 9)) {
         this.stackTagCompound.setTag("AttributeModifiers", new NBTTagList());
      }

      NBTTagList nbttaglist = this.stackTagCompound.getTagList("AttributeModifiers", 10);
      NBTTagCompound nbttagcompound = SharedMonsterAttributes.writeAttributeModifierToNBT(modifier);
      nbttagcompound.setString("AttributeName", attributeName);
      if(equipmentSlot != null) {
         nbttagcompound.setString("Slot", equipmentSlot.getName());
      }

      nbttaglist.appendTag(nbttagcompound);
   }

   @Deprecated
   public void setItem(Item newItem) {
      this.item = newItem;
   }

   public ITextComponent getTextComponent() {
      TextComponentString textcomponentstring = new TextComponentString(this.getDisplayName());
      if(this.hasDisplayName()) {
         textcomponentstring.getStyle().setItalic(Boolean.valueOf(true));
      }

      ITextComponent itextcomponent = (new TextComponentString("[")).appendSibling(textcomponentstring).appendText("]");
      if(this.item != null) {
         NBTTagCompound nbttagcompound = this.writeToNBT(new NBTTagCompound());
         itextcomponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new TextComponentString(nbttagcompound.toString())));
         itextcomponent.getStyle().setColor(this.getRarity().rarityColor);
      }

      return itextcomponent;
   }

   public boolean canDestroy(Block blockIn) {
      if(blockIn == this.canDestroyCacheBlock) {
         return this.canDestroyCacheResult;
      } else {
         this.canDestroyCacheBlock = blockIn;
         if(this.hasTagCompound() && this.stackTagCompound.hasKey("CanDestroy", 9)) {
            NBTTagList nbttaglist = this.stackTagCompound.getTagList("CanDestroy", 8);

            for(int i = 0; i < nbttaglist.tagCount(); ++i) {
               Block block = Block.getBlockFromName(nbttaglist.getStringTagAt(i));
               if(block == blockIn) {
                  this.canDestroyCacheResult = true;
                  return true;
               }
            }
         }

         this.canDestroyCacheResult = false;
         return false;
      }
   }

   public boolean canPlaceOn(Block blockIn) {
      if(blockIn == this.canPlaceOnCacheBlock) {
         return this.canPlaceOnCacheResult;
      } else {
         this.canPlaceOnCacheBlock = blockIn;
         if(this.hasTagCompound() && this.stackTagCompound.hasKey("CanPlaceOn", 9)) {
            NBTTagList nbttaglist = this.stackTagCompound.getTagList("CanPlaceOn", 8);

            for(int i = 0; i < nbttaglist.tagCount(); ++i) {
               Block block = Block.getBlockFromName(nbttaglist.getStringTagAt(i));
               if(block == blockIn) {
                  this.canPlaceOnCacheResult = true;
                  return true;
               }
            }
         }

         this.canPlaceOnCacheResult = false;
         return false;
      }
   }
}