package net.minecraft.server.network;

import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.INetHandlerHandshakeServer;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.server.SPacketDisconnect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class NetHandlerHandshakeTCP implements INetHandlerHandshakeServer {
   private final MinecraftServer server;
   private final NetworkManager networkManager;

   public NetHandlerHandshakeTCP(MinecraftServer serverIn, NetworkManager netManager) {
      this.server = serverIn;
      this.networkManager = netManager;
   }

   public void processHandshake(C00Handshake packetIn) {
      switch(packetIn.getRequestedState()) {
      case LOGIN:
         this.networkManager.setConnectionState(EnumConnectionState.LOGIN);
         if(packetIn.getProtocolVersion() > 110) {
            TextComponentString textcomponentstring = new TextComponentString("Outdated server! I\'m still on 1.9.4");
            this.networkManager.sendPacket(new SPacketDisconnect(textcomponentstring));
            this.networkManager.closeChannel(textcomponentstring);
         } else if(packetIn.getProtocolVersion() < 110) {
            TextComponentString textcomponentstring1 = new TextComponentString("Outdated client! Please use 1.9.4");
            this.networkManager.sendPacket(new SPacketDisconnect(textcomponentstring1));
            this.networkManager.closeChannel(textcomponentstring1);
         } else {
            this.networkManager.setNetHandler(new NetHandlerLoginServer(this.server, this.networkManager));
         }
         break;
      case STATUS:
         this.networkManager.setConnectionState(EnumConnectionState.STATUS);
         this.networkManager.setNetHandler(new NetHandlerStatusServer(this.server, this.networkManager));
         break;
      default:
         throw new UnsupportedOperationException("Invalid intention " + packetIn.getRequestedState());
      }
   }

   public void onDisconnect(ITextComponent reason) {
   }
}
