package net.minecraft.world;

public class ColorizerGrass {
   private static int[] grassBuffer = new int[65536];

   public static void setGrassBiomeColorizer(int[] grassBufferIn) {
      grassBuffer = grassBufferIn;
   }

   public static int getGrassColor(double temperature, double humidity) {
      humidity = humidity * temperature;
      int i = (int)((1.0D - temperature) * 255.0D);
      int j = (int)((1.0D - humidity) * 255.0D);
      int k = j << 8 | i;
      return k > grassBuffer.length?-65281:grassBuffer[k];
   }
}
