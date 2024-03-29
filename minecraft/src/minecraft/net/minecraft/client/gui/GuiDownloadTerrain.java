package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.play.client.CPacketKeepAlive;

public class GuiDownloadTerrain extends GuiScreen {
   private NetHandlerPlayClient connection;
   private int progress;

   public GuiDownloadTerrain(NetHandlerPlayClient netHandler) {
      this.connection = netHandler;
   }

   protected void keyTyped(char typedChar, int keyCode) throws IOException {
   }

   public void initGui() {
      this.buttonList.clear();
   }

   public void updateScreen() {
      ++this.progress;
      if(this.progress % 20 == 0) {
         this.connection.sendPacket(new CPacketKeepAlive());
      }
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawBackground(0);
      this.drawCenteredString(this.fontRendererObj, I18n.format("multiplayer.downloadingTerrain", new Object[0]), this.width / 2, this.height / 2 - 50, 16777215);
      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   public boolean doesGuiPauseGame() {
      return false;
   }
}
