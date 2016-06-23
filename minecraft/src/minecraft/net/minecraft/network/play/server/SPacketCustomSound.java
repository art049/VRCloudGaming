package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.Validate;

public class SPacketCustomSound implements Packet<INetHandlerPlayClient> {
   private String soundName;
   private SoundCategory category;
   private int x;
   private int y = Integer.MAX_VALUE;
   private int z;
   private float volume;
   private int pitch;

   public SPacketCustomSound() {
   }

   public SPacketCustomSound(String soundNameIn, SoundCategory categoryIn, double xIn, double yIn, double zIn, float volumeIn, float pitchIn) {
      Validate.notNull(soundNameIn, "name", new Object[0]);
      this.soundName = soundNameIn;
      this.category = categoryIn;
      this.x = (int)(xIn * 8.0D);
      this.y = (int)(yIn * 8.0D);
      this.z = (int)(zIn * 8.0D);
      this.volume = volumeIn;
      this.pitch = (int)(pitchIn * 63.0F);
      pitchIn = MathHelper.clamp_float(pitchIn, 0.0F, 255.0F);
   }

   public void readPacketData(PacketBuffer buf) throws IOException {
      this.soundName = buf.readStringFromBuffer(256);
      this.category = (SoundCategory)buf.readEnumValue(SoundCategory.class);
      this.x = buf.readInt();
      this.y = buf.readInt();
      this.z = buf.readInt();
      this.volume = buf.readFloat();
      this.pitch = buf.readUnsignedByte();
   }

   public void writePacketData(PacketBuffer buf) throws IOException {
      buf.writeString(this.soundName);
      buf.writeEnumValue(this.category);
      buf.writeInt(this.x);
      buf.writeInt(this.y);
      buf.writeInt(this.z);
      buf.writeFloat(this.volume);
      buf.writeByte(this.pitch);
   }

   public String getSoundName() {
      return this.soundName;
   }

   public SoundCategory getCategory() {
      return this.category;
   }

   public double getX() {
      return (double)((float)this.x / 8.0F);
   }

   public double getY() {
      return (double)((float)this.y / 8.0F);
   }

   public double getZ() {
      return (double)((float)this.z / 8.0F);
   }

   public float getVolume() {
      return this.volume;
   }

   public float getPitch() {
      return (float)this.pitch / 63.0F;
   }

   public void processPacket(INetHandlerPlayClient handler) {
      handler.handleCustomSound(this);
   }
}