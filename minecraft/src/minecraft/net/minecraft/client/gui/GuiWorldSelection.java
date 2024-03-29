package net.minecraft.client.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.resources.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiWorldSelection extends GuiScreen implements GuiYesNoCallback {
   private static final Logger LOGGER = LogManager.getLogger();
   protected GuiScreen prevScreen;
   protected String title = "Select world";
   private String worldVersTooltip;
   private GuiButton deleteButton;
   private GuiButton selectButton;
   private GuiButton renameButton;
   private GuiButton copyButton;
   private GuiListWorldSelection selectionList;

   public GuiWorldSelection(GuiScreen screenIn) {
      this.prevScreen = screenIn;
   }

   public void initGui() {
      this.title = I18n.format("selectWorld.title", new Object[0]);
      this.selectionList = new GuiListWorldSelection(this, this.mc, this.width, this.height, 32, this.height - 64, 36);
      this.postInit();
   }

   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      this.selectionList.handleMouseInput();
   }

   public void postInit() {
      this.buttonList.add(this.selectButton = new GuiButton(1, this.width / 2 - 154, this.height - 52, 150, 20, I18n.format("selectWorld.select", new Object[0])));
      this.buttonList.add(new GuiButton(3, this.width / 2 + 4, this.height - 52, 150, 20, I18n.format("selectWorld.create", new Object[0])));
      this.buttonList.add(this.renameButton = new GuiButton(4, this.width / 2 - 154, this.height - 28, 72, 20, I18n.format("selectWorld.edit", new Object[0])));
      this.buttonList.add(this.deleteButton = new GuiButton(2, this.width / 2 - 76, this.height - 28, 72, 20, I18n.format("selectWorld.delete", new Object[0])));
      this.buttonList.add(this.copyButton = new GuiButton(5, this.width / 2 + 4, this.height - 28, 72, 20, I18n.format("selectWorld.recreate", new Object[0])));
      this.buttonList.add(new GuiButton(0, this.width / 2 + 82, this.height - 28, 72, 20, I18n.format("gui.cancel", new Object[0])));
      this.selectButton.enabled = false;
      this.deleteButton.enabled = false;
      this.renameButton.enabled = false;
      this.copyButton.enabled = false;
   }

   protected void actionPerformed(GuiButton button) throws IOException {
      if(button.enabled) {
         GuiListWorldSelectionEntry guilistworldselectionentry = this.selectionList.getSelectedWorld();
         if(button.id == 2) {
            if(guilistworldselectionentry != null) {
               guilistworldselectionentry.deleteWorld();
            }
         } else if(button.id == 1) {
            if(guilistworldselectionentry != null) {
               guilistworldselectionentry.joinWorld();
            }
         } else if(button.id == 3) {
            this.mc.displayGuiScreen(new GuiCreateWorld(this));
         } else if(button.id == 4) {
            if(guilistworldselectionentry != null) {
               guilistworldselectionentry.editWorld();
            }
         } else if(button.id == 0) {
            this.mc.displayGuiScreen(this.prevScreen);
         } else if(button.id == 5 && guilistworldselectionentry != null) {
            guilistworldselectionentry.recreateWorld();
         }
      }
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.worldVersTooltip = null;
      this.selectionList.drawScreen(mouseX, mouseY, partialTicks);
      this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 20, 16777215);
      super.drawScreen(mouseX, mouseY, partialTicks);
      if(this.worldVersTooltip != null) {
         this.drawHoveringText(Lists.newArrayList(Splitter.on("\n").split(this.worldVersTooltip)), mouseX, mouseY);
      }
   }

   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      this.selectionList.mouseClicked(mouseX, mouseY, mouseButton);
   }

   protected void mouseReleased(int mouseX, int mouseY, int state) {
      super.mouseReleased(mouseX, mouseY, state);
      this.selectionList.mouseReleased(mouseX, mouseY, state);
   }

   public void setVersionTooltip(String p_184861_1_) {
      this.worldVersTooltip = p_184861_1_;
   }

   public void selectWorld(@Nullable GuiListWorldSelectionEntry entry) {
      boolean flag = entry != null;
      this.selectButton.enabled = flag;
      this.deleteButton.enabled = flag;
      this.renameButton.enabled = flag;
      this.copyButton.enabled = flag;
   }
}
