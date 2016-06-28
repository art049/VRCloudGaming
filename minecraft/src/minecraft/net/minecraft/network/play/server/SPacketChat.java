package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;

public class SPacketChat implements Packet<INetHandlerPlayClient> {
   private ITextComponent chatComponent;
   private byte type;

   public SPacketChat() {
   }

   public SPacketChat(ITextComponent componentIn) {
      this(componentIn, (byte)1);
   }

   public SPacketChat(ITextComponent componentIn, byte typeIn) {
      this.chatComponent = componentIn;
      this.type = typeIn;
   }

   public void readPacketData(PacketBuffer buf) throws IOException {
      this.chatComponent = buf.readTextComponent();
      this.type = buf.readByte();
   }

   public void writePacketData(PacketBuffer buf) throws IOException {
      buf.writeTextComponent(this.chatComponent);
      buf.writeByte(this.type);
   }

   public void processPacket(INetHandlerPlayClient handler) {
      handler.handleChat(this);
   }

   public ITextComponent getChatComponent() {
      return this.chatComponent;
   }

   public boolean isSystem() {
      return this.type == 1 || this.type == 2;
   }

   public byte getType() {
      return this.type;
   }
}
