package net.minecraft.entity.effect;

import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityLightningBolt extends EntityWeatherEffect {
   private int lightningState;
   public long boltVertex;
   private int boltLivingTime;
   private final boolean effectOnly;

   public EntityLightningBolt(World worldIn, double x, double y, double z, boolean effectOnlyIn) {
      super(worldIn);
      this.setLocationAndAngles(x, y, z, 0.0F, 0.0F);
      this.lightningState = 2;
      this.boltVertex = this.rand.nextLong();
      this.boltLivingTime = this.rand.nextInt(3) + 1;
      this.effectOnly = effectOnlyIn;
      BlockPos blockpos = new BlockPos(this);
      if(!effectOnlyIn && !worldIn.isRemote && worldIn.getGameRules().getBoolean("doFireTick") && (worldIn.getDifficulty() == EnumDifficulty.NORMAL || worldIn.getDifficulty() == EnumDifficulty.HARD) && worldIn.isAreaLoaded(blockpos, 10)) {
         if(worldIn.getBlockState(blockpos).getMaterial() == Material.AIR && Blocks.FIRE.canPlaceBlockAt(worldIn, blockpos)) {
            worldIn.setBlockState(blockpos, Blocks.FIRE.getDefaultState());
         }

         for(int i = 0; i < 4; ++i) {
            BlockPos blockpos1 = blockpos.add(this.rand.nextInt(3) - 1, this.rand.nextInt(3) - 1, this.rand.nextInt(3) - 1);
            if(worldIn.getBlockState(blockpos1).getMaterial() == Material.AIR && Blocks.FIRE.canPlaceBlockAt(worldIn, blockpos1)) {
               worldIn.setBlockState(blockpos1, Blocks.FIRE.getDefaultState());
            }
         }
      }
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.WEATHER;
   }

   public void onUpdate() {
      super.onUpdate();
      if(this.lightningState == 2) {
         this.worldObj.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.WEATHER, 10000.0F, 0.8F + this.rand.nextFloat() * 0.2F);
         this.worldObj.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_IMPACT, SoundCategory.WEATHER, 2.0F, 0.5F + this.rand.nextFloat() * 0.2F);
      }

      --this.lightningState;
      if(this.lightningState < 0) {
         if(this.boltLivingTime == 0) {
            this.setDead();
         } else if(this.lightningState < -this.rand.nextInt(10)) {
            --this.boltLivingTime;
            this.lightningState = 1;
            if(!this.effectOnly && !this.worldObj.isRemote) {
               this.boltVertex = this.rand.nextLong();
               BlockPos blockpos = new BlockPos(this);
               if(this.worldObj.getGameRules().getBoolean("doFireTick") && this.worldObj.isAreaLoaded(blockpos, 10) && this.worldObj.getBlockState(blockpos).getMaterial() == Material.AIR && Blocks.FIRE.canPlaceBlockAt(this.worldObj, blockpos)) {
                  this.worldObj.setBlockState(blockpos, Blocks.FIRE.getDefaultState());
               }
            }
         }
      }

      if(this.lightningState >= 0) {
         if(this.worldObj.isRemote) {
            this.worldObj.setLastLightningBolt(2);
         } else if(!this.effectOnly) {
            double d0 = 3.0D;
            List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(this.posX - d0, this.posY - d0, this.posZ - d0, this.posX + d0, this.posY + 6.0D + d0, this.posZ + d0));

            for(int i = 0; i < list.size(); ++i) {
               Entity entity = (Entity)list.get(i);
               entity.onStruckByLightning(this);
            }
         }
      }
   }

   protected void entityInit() {
   }

   protected void readEntityFromNBT(NBTTagCompound compound) {
   }

   protected void writeEntityToNBT(NBTTagCompound compound) {
   }
}
