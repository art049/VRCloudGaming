package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class CPacketResourcePackStatus implements Packet<INetHandlerPlayServer> {
   private String hash;
   private CPacketResourcePackStatus.Action action;

   public CPacketResourcePackStatus() {
   }

   public CPacketResourcePackStatus(String hashIn, CPacketResourcePackStatus.Action actionIn) {
      if(hashIn.length() > 40) {
         hashIn = hashIn.substring(0, 40);
      }

      this.hash = hashIn;
      this.action = actionIn;
   }

   public void readPacketData(PacketBuffer buf) throws IOException {
      this.hash = buf.readStringFromBuffer(40);
      this.action = (CPacketResourcePackStatus.Action)buf.readEnumValue(CPacketResourcePackStatus.Action.class);
   }

   public void writePacketData(PacketBuffer buf) throws IOException {
      buf.writeString(this.hash);
      buf.writeEnumValue(this.action);
   }

   public void processPacket(INetHandlerPlayServer handler) {
      handler.handleResourcePackStatus(this);
   }

   public static enum Action {
      SUCCESSFULLY_LOADED,
      DECLINED,
      FAILED_DOWNLOAD,
      ACCEPTED;
   }
}
