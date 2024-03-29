package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.resources.I18n;

public class ServerListEntryLanDetected implements GuiListExtended.IGuiListEntry {
   private final GuiMultiplayer screen;
   protected final Minecraft mc;
   protected final LanServerDetector.LanServer serverData;
   private long lastClickTime = 0L;

   protected ServerListEntryLanDetected(GuiMultiplayer screenIn, LanServerDetector.LanServer serverDataIn) {
      this.screen = screenIn;
      this.serverData = serverDataIn;
      this.mc = Minecraft.getMinecraft();
   }

   public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
      this.mc.fontRendererObj.drawString(I18n.format("lanServer.title", new Object[0]), x + 32 + 3, y + 1, 16777215);
      this.mc.fontRendererObj.drawString(this.serverData.getServerMotd(), x + 32 + 3, y + 12, 8421504);
      if(this.mc.gameSettings.hideServerAddress) {
         this.mc.fontRendererObj.drawString(I18n.format("selectServer.hiddenAddress", new Object[0]), x + 32 + 3, y + 12 + 11, 3158064);
      } else {
         this.mc.fontRendererObj.drawString(this.serverData.getServerIpPort(), x + 32 + 3, y + 12 + 11, 3158064);
      }
   }

   public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
      this.screen.selectServer(slotIndex);
      if(Minecraft.getSystemTime() - this.lastClickTime < 250L) {
         this.screen.connectToSelected();
      }

      this.lastClickTime = Minecraft.getSystemTime();
      return false;
   }

   public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_) {
   }

   public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
   }

   public LanServerDetector.LanServer getLanServer() {
      return this.serverData;
   }
}
