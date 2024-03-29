package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiWinGame extends GuiScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ResourceLocation MINECRAFT_LOGO = new ResourceLocation("textures/gui/title/minecraft.png");
   private static final ResourceLocation VIGNETTE_TEXTURE = new ResourceLocation("textures/misc/vignette.png");
   private int time;
   private List<String> lines;
   private int totalScrollLength;
   private float scrollSpeed = 0.5F;

   public void updateScreen() {
      MusicTicker musicticker = this.mc.getMusicTicker();
      SoundHandler soundhandler = this.mc.getSoundHandler();
      if(this.time == 0) {
         musicticker.stopMusic();
         musicticker.playMusic(MusicTicker.MusicType.CREDITS);
         soundhandler.resumeSounds();
      }

      soundhandler.update();
      ++this.time;
      float f = (float)(this.totalScrollLength + this.height + this.height + 24) / this.scrollSpeed;
      if((float)this.time > f) {
         this.sendRespawnPacket();
      }
   }

   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      if(keyCode == 1) {
         this.sendRespawnPacket();
      }
   }

   private void sendRespawnPacket() {
      this.mc.thePlayer.connection.sendPacket(new CPacketClientStatus(CPacketClientStatus.State.PERFORM_RESPAWN));
      this.mc.displayGuiScreen((GuiScreen)null);
   }

   public boolean doesGuiPauseGame() {
      return true;
   }

   public void initGui() {
      if(this.lines == null) {
         this.lines = Lists.<String>newArrayList();
         IResource iresource = null;

         try {
            String s = "";
            String s1 = "" + TextFormatting.WHITE + TextFormatting.OBFUSCATED + TextFormatting.GREEN + TextFormatting.AQUA;
            int i = 274;
            iresource = this.mc.getResourceManager().getResource(new ResourceLocation("texts/end.txt"));
            InputStream inputstream = iresource.getInputStream();
            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputstream, Charsets.UTF_8));
            Random random = new Random(8124371L);

            while((s = bufferedreader.readLine()) != null) {
               String s2;
               String s3;
               for(s = s.replaceAll("PLAYERNAME", this.mc.getSession().getUsername()); s.contains(s1); s = s2 + TextFormatting.WHITE + TextFormatting.OBFUSCATED + "XXXXXXXX".substring(0, random.nextInt(4) + 3) + s3) {
                  int j = s.indexOf(s1);
                  s2 = s.substring(0, j);
                  s3 = s.substring(j + s1.length());
               }

               this.lines.addAll(this.mc.fontRendererObj.listFormattedStringToWidth(s, i));
               this.lines.add("");
            }

            inputstream.close();

            for(int k = 0; k < 8; ++k) {
               this.lines.add("");
            }

            inputstream = this.mc.getResourceManager().getResource(new ResourceLocation("texts/credits.txt")).getInputStream();
            bufferedreader = new BufferedReader(new InputStreamReader(inputstream, Charsets.UTF_8));

            while((s = bufferedreader.readLine()) != null) {
               s = s.replaceAll("PLAYERNAME", this.mc.getSession().getUsername());
               s = s.replaceAll("\t", "    ");
               this.lines.addAll(this.mc.fontRendererObj.listFormattedStringToWidth(s, i));
               this.lines.add("");
            }

            inputstream.close();
            this.totalScrollLength = this.lines.size() * 12;
         } catch (Exception exception) {
            LOGGER.error((String)"Couldn\'t load credits", (Throwable)exception);
         } finally {
            IOUtils.closeQuietly((Closeable)iresource);
         }
      }
   }

   private void drawWinGameScreen(int p_146575_1_, int p_146575_2_, float p_146575_3_) {
      Tessellator tessellator = Tessellator.getInstance();
      VertexBuffer vertexbuffer = tessellator.getBuffer();
      this.mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
      vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      int i = this.width;
      float f = 0.0F - ((float)this.time + p_146575_3_) * 0.5F * this.scrollSpeed;
      float f1 = (float)this.height - ((float)this.time + p_146575_3_) * 0.5F * this.scrollSpeed;
      float f2 = 0.015625F;
      float f3 = ((float)this.time + p_146575_3_ - 0.0F) * 0.02F;
      float f4 = (float)(this.totalScrollLength + this.height + this.height + 24) / this.scrollSpeed;
      float f5 = (f4 - 20.0F - ((float)this.time + p_146575_3_)) * 0.005F;
      if(f5 < f3) {
         f3 = f5;
      }

      if(f3 > 1.0F) {
         f3 = 1.0F;
      }

      f3 = f3 * f3;
      f3 = f3 * 96.0F / 255.0F;
      vertexbuffer.pos(0.0D, (double)this.height, (double)this.zLevel).tex(0.0D, (double)(f * f2)).color(f3, f3, f3, 1.0F).endVertex();
      vertexbuffer.pos((double)i, (double)this.height, (double)this.zLevel).tex((double)((float)i * f2), (double)(f * f2)).color(f3, f3, f3, 1.0F).endVertex();
      vertexbuffer.pos((double)i, 0.0D, (double)this.zLevel).tex((double)((float)i * f2), (double)(f1 * f2)).color(f3, f3, f3, 1.0F).endVertex();
      vertexbuffer.pos(0.0D, 0.0D, (double)this.zLevel).tex(0.0D, (double)(f1 * f2)).color(f3, f3, f3, 1.0F).endVertex();
      tessellator.draw();
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawWinGameScreen(mouseX, mouseY, partialTicks);
      Tessellator tessellator = Tessellator.getInstance();
      VertexBuffer vertexbuffer = tessellator.getBuffer();
      int i = 274;
      int j = this.width / 2 - i / 2;
      int k = this.height + 50;
      float f = -((float)this.time + partialTicks) * this.scrollSpeed;
      GlStateManager.pushMatrix();
      GlStateManager.translate(0.0F, f, 0.0F);
      this.mc.getTextureManager().bindTexture(MINECRAFT_LOGO);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.drawTexturedModalRect(j, k, 0, 0, 155, 44);
      this.drawTexturedModalRect(j + 155, k, 0, 45, 155, 44);
      int l = k + 200;

      for(int i1 = 0; i1 < this.lines.size(); ++i1) {
         if(i1 == this.lines.size() - 1) {
            float f1 = (float)l + f - (float)(this.height / 2 - 6);
            if(f1 < 0.0F) {
               GlStateManager.translate(0.0F, -f1, 0.0F);
            }
         }

         if((float)l + f + 12.0F + 8.0F > 0.0F && (float)l + f < (float)this.height) {
            String s = (String)this.lines.get(i1);
            if(s.startsWith("[C]")) {
               this.fontRendererObj.drawStringWithShadow(s.substring(3), (float)(j + (i - this.fontRendererObj.getStringWidth(s.substring(3))) / 2), (float)l, 16777215);
            } else {
               this.fontRendererObj.fontRandom.setSeed((long)i1 * 4238972211L + (long)(this.time / 4));
               this.fontRendererObj.drawStringWithShadow(s, (float)j, (float)l, 16777215);
            }
         }

         l += 12;
      }

      GlStateManager.popMatrix();
      this.mc.getTextureManager().bindTexture(VIGNETTE_TEXTURE);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
      int j1 = this.width;
      int k1 = this.height;
      vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      vertexbuffer.pos(0.0D, (double)k1, (double)this.zLevel).tex(0.0D, 1.0D).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
      vertexbuffer.pos((double)j1, (double)k1, (double)this.zLevel).tex(1.0D, 1.0D).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
      vertexbuffer.pos((double)j1, 0.0D, (double)this.zLevel).tex(1.0D, 0.0D).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
      vertexbuffer.pos(0.0D, 0.0D, (double)this.zLevel).tex(0.0D, 0.0D).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
      tessellator.draw();
      GlStateManager.disableBlend();
      super.drawScreen(mouseX, mouseY, partialTicks);
   }
}
