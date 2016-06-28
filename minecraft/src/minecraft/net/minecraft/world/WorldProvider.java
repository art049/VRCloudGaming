package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.gen.ChunkProviderDebug;
import net.minecraft.world.gen.ChunkProviderFlat;
import net.minecraft.world.gen.ChunkProviderOverworld;
import net.minecraft.world.gen.FlatGeneratorInfo;

public abstract class WorldProvider {
   public static final float[] MOON_PHASE_FACTORS = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
   protected World worldObj;
   private WorldType terrainType;
   private String generatorSettings;
   protected BiomeProvider biomeProvider;
   protected boolean isHellWorld;
   protected boolean hasNoSky;
   protected final float[] lightBrightnessTable = new float[16];
   private final float[] colorsSunriseSunset = new float[4];

   public final void registerWorld(World worldIn) {
      this.worldObj = worldIn;
      this.terrainType = worldIn.getWorldInfo().getTerrainType();
      this.generatorSettings = worldIn.getWorldInfo().getGeneratorOptions();
      this.createBiomeProvider();
      this.generateLightBrightnessTable();
   }

   protected void generateLightBrightnessTable() {
      float f = 0.0F;

      for(int i = 0; i <= 15; ++i) {
         float f1 = 1.0F - (float)i / 15.0F;
         this.lightBrightnessTable[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * (1.0F - f) + f;
      }
   }

   protected void createBiomeProvider() {
      WorldType worldtype = this.worldObj.getWorldInfo().getTerrainType();
      if(worldtype == WorldType.FLAT) {
         FlatGeneratorInfo flatgeneratorinfo = FlatGeneratorInfo.createFlatGeneratorFromString(this.worldObj.getWorldInfo().getGeneratorOptions());
         this.biomeProvider = new BiomeProviderSingle(Biome.getBiome(flatgeneratorinfo.getBiome(), Biomes.DEFAULT));
      } else if(worldtype == WorldType.DEBUG_WORLD) {
         this.biomeProvider = new BiomeProviderSingle(Biomes.PLAINS);
      } else {
         this.biomeProvider = new BiomeProvider(this.worldObj.getWorldInfo());
      }
   }

   public IChunkGenerator createChunkGenerator() {
      return (IChunkGenerator)(this.terrainType == WorldType.FLAT?new ChunkProviderFlat(this.worldObj, this.worldObj.getSeed(), this.worldObj.getWorldInfo().isMapFeaturesEnabled(), this.generatorSettings):(this.terrainType == WorldType.DEBUG_WORLD?new ChunkProviderDebug(this.worldObj):(this.terrainType == WorldType.CUSTOMIZED?new ChunkProviderOverworld(this.worldObj, this.worldObj.getSeed(), this.worldObj.getWorldInfo().isMapFeaturesEnabled(), this.generatorSettings):new ChunkProviderOverworld(this.worldObj, this.worldObj.getSeed(), this.worldObj.getWorldInfo().isMapFeaturesEnabled(), this.generatorSettings))));
   }

   public boolean canCoordinateBeSpawn(int x, int z) {
      BlockPos blockpos = new BlockPos(x, 0, z);
      return this.worldObj.getBiomeGenForCoords(blockpos).ignorePlayerSpawnSuitability()?true:this.worldObj.getGroundAboveSeaLevel(blockpos).getBlock() == Blocks.GRASS;
   }

   public float calculateCelestialAngle(long worldTime, float partialTicks) {
      int i = (int)(worldTime % 24000L);
      float f = ((float)i + partialTicks) / 24000.0F - 0.25F;
      if(f < 0.0F) {
         ++f;
      }

      if(f > 1.0F) {
         --f;
      }

      float f1 = 1.0F - (float)((Math.cos((double)f * Math.PI) + 1.0D) / 2.0D);
      f = f + (f1 - f) / 3.0F;
      return f;
   }

   public int getMoonPhase(long worldTime) {
      return (int)(worldTime / 24000L % 8L + 8L) % 8;
   }

   public boolean isSurfaceWorld() {
      return true;
   }

   @Nullable
   public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
      float f = 0.4F;
      float f1 = MathHelper.cos(celestialAngle * ((float)Math.PI * 2F)) - 0.0F;
      float f2 = -0.0F;
      if(f1 >= f2 - f && f1 <= f2 + f) {
         float f3 = (f1 - f2) / f * 0.5F + 0.5F;
         float f4 = 1.0F - (1.0F - MathHelper.sin(f3 * (float)Math.PI)) * 0.99F;
         f4 = f4 * f4;
         this.colorsSunriseSunset[0] = f3 * 0.3F + 0.7F;
         this.colorsSunriseSunset[1] = f3 * f3 * 0.7F + 0.2F;
         this.colorsSunriseSunset[2] = f3 * f3 * 0.0F + 0.2F;
         this.colorsSunriseSunset[3] = f4;
         return this.colorsSunriseSunset;
      } else {
         return null;
      }
   }

   public Vec3d getFogColor(float p_76562_1_, float p_76562_2_) {
      float f = MathHelper.cos(p_76562_1_ * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
      f = MathHelper.clamp_float(f, 0.0F, 1.0F);
      float f1 = 0.7529412F;
      float f2 = 0.84705883F;
      float f3 = 1.0F;
      f1 = f1 * (f * 0.94F + 0.06F);
      f2 = f2 * (f * 0.94F + 0.06F);
      f3 = f3 * (f * 0.91F + 0.09F);
      return new Vec3d((double)f1, (double)f2, (double)f3);
   }

   public boolean canRespawnHere() {
      return true;
   }

   public float getCloudHeight() {
      return 128.0F;
   }

   public boolean isSkyColored() {
      return true;
   }

   public BlockPos getSpawnCoordinate() {
      return null;
   }

   public int getAverageGroundLevel() {
      return this.terrainType == WorldType.FLAT?4:this.worldObj.getSeaLevel() + 1;
   }

   public double getVoidFogYFactor() {
      return this.terrainType == WorldType.FLAT?1.0D:0.03125D;
   }

   public boolean doesXZShowFog(int x, int z) {
      return false;
   }

   public BiomeProvider getBiomeProvider() {
      return this.biomeProvider;
   }

   public boolean doesWaterVaporize() {
      return this.isHellWorld;
   }

   public boolean getHasNoSky() {
      return this.hasNoSky;
   }

   public float[] getLightBrightnessTable() {
      return this.lightBrightnessTable;
   }

   public WorldBorder createWorldBorder() {
      return new WorldBorder();
   }

   public void onPlayerAdded(EntityPlayerMP player) {
   }

   public void onPlayerRemoved(EntityPlayerMP player) {
   }

   public abstract DimensionType getDimensionType();

   public void onWorldSave() {
   }

   public void onWorldUpdateEntities() {
   }

   public boolean canDropChunk(int x, int z) {
      return true;
   }
}
