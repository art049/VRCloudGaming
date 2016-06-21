package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketEntityVelocity implements Packet<INetHandlerPlayClient> {
   private int entityID;
   private int motionX;
   private int motionY;
   private int motionZ;

   public SPacketEntityVelocity() {
   }

   public SPacketEntityVelocity(Entity entityIn) {
      this(entityIn.getEntityId(), entityIn.motionX, entityIn.motionY, entityIn.motionZ);
   }

   public SPacketEntityVelocity(int entityIdIn, double motionXIn, double motionYIn, double motionZIn) {
      this.entityID = entityIdIn;
      double d0 = 3.9D;
      if(motionXIn < -d0) {
         motionXIn = -d0;
      }

      if(motionYIn < -d0) {
         motionYIn = -d0;
      }

      if(motionZIn < -d0) {
         motionZIn = -d0;
      }

      if(motionXIn > d0) {
         motionXIn = d0;
      }

      if(motionYIn > d0) {
         motionYIn = d0;
      }

      if(motionZIn > d0) {
         motionZIn = d0;
      }

      this.motionX = (int)(motionXIn * 8000.0D);
      this.motionY = (int)(motionYIn * 8000.0D);
      this.motionZ = (int)(motionZIn * 8000.0D);
   }

   public void readPacketData(PacketBuffer buf) throws IOException {
      this.entityID = buf.readVarIntFromBuffer();
      this.motionX = buf.readShort();
      this.motionY = buf.readShort();
      this.motionZ = buf.readShort();
   }

   public void writePacketData(PacketBuffer buf) throws IOException {
      buf.writeVarIntToBuffer(this.entityID);
      buf.writeShort(this.motionX);
      buf.writeShort(this.motionY);
      buf.writeShort(this.motionZ);
   }

   public void processPacket(INetHandlerPlayClient handler) {
      handler.handleEntityVelocity(this);
   }

   public int getEntityID() {
      return this.entityID;
   }

   public int getMotionX() {
      return this.motionX;
   }

   public int getMotionY() {
      return this.motionY;
   }

   public int getMotionZ() {
      return this.motionZ;
   }
}
