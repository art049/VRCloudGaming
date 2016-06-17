package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.realms.RealmsScrolledSelectionList;

public class GuiSlotRealmsProxy extends GuiSlot {
   private final RealmsScrolledSelectionList selectionList;

   public GuiSlotRealmsProxy(RealmsScrolledSelectionList selectionListIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
      super(Minecraft.getMinecraft(), widthIn, heightIn, topIn, bottomIn, slotHeightIn);
      this.selectionList = selectionListIn;
   }

   protected int getSize() {
      return this.selectionList.getItemCount();
   }

   protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
      this.selectionList.selectItem(slotIndex, isDoubleClick, mouseX, mouseY);
   }

   protected boolean isSelected(int slotIndex) {
      return this.selectionList.isSelectedItem(slotIndex);
   }

   protected void drawBackground() {
      this.selectionList.renderBackground();
   }

   protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn) {
      this.selectionList.renderItem(entryID, insideLeft, yPos, insideSlotHeight, mouseXIn, mouseYIn);
   }

   public int getWidth() {
      return super.width;
   }

   public int getMouseY() {
      return super.mouseY;
   }

   public int getMouseX() {
      return super.mouseX;
   }

   protected int getContentHeight() {
      return this.selectionList.getMaxPosition();
   }

   protected int getScrollBarX() {
      return this.selectionList.getScrollbarPosition();
   }

   public void handleMouseInput() {
      super.handleMouseInput();
   }
}
