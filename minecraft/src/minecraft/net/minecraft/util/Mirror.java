package net.minecraft.util;

public enum Mirror {
   NONE("no_mirror"),
   LEFT_RIGHT("mirror_left_right"),
   FRONT_BACK("mirror_front_back");

   private final String name;
   private static String[] mirrorNames = new String[values().length];

   private Mirror(String nameIn) {
      this.name = nameIn;
   }

   public int mirrorRotation(int rotationIn, int rotationCount) {
      int i = rotationCount / 2;
      int j = rotationIn > i?rotationIn - rotationCount:rotationIn;
      switch(this) {
      case LEFT_RIGHT:
         return (rotationCount - j) % rotationCount;
      case FRONT_BACK:
         return (i - j + rotationCount) % rotationCount;
      default:
         return rotationIn;
      }
   }

   public Rotation toRotation(EnumFacing facing) {
      EnumFacing.Axis enumfacing$axis = facing.getAxis();
      return (this != LEFT_RIGHT || enumfacing$axis != EnumFacing.Axis.Z) && (this != FRONT_BACK || enumfacing$axis != EnumFacing.Axis.X)?Rotation.NONE:Rotation.CLOCKWISE_180;
   }

   public EnumFacing mirror(EnumFacing facing) {
      switch(this) {
      case LEFT_RIGHT:
         if(facing == EnumFacing.WEST) {
            return EnumFacing.EAST;
         } else {
            if(facing == EnumFacing.EAST) {
               return EnumFacing.WEST;
            }

            return facing;
         }
      case FRONT_BACK:
         if(facing == EnumFacing.NORTH) {
            return EnumFacing.SOUTH;
         } else {
            if(facing == EnumFacing.SOUTH) {
               return EnumFacing.NORTH;
            }

            return facing;
         }
      default:
         return facing;
      }
   }

   static {
      int i = 0;

      for(Mirror mirror : values()) {
         mirrorNames[i++] = mirror.name;
      }
   }
}