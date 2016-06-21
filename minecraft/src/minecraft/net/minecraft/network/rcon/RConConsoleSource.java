package net.minecraft.network.rcon;

import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class RConConsoleSource implements ICommandSender {
   private final StringBuffer buffer = new StringBuffer();
   private final MinecraftServer server;

   public RConConsoleSource(MinecraftServer serverIn) {
      this.server = serverIn;
   }

   public String getName() {
      return "Rcon";
   }

   public ITextComponent getDisplayName() {
      return new TextComponentString(this.getName());
   }

   public void addChatMessage(ITextComponent component) {
      this.buffer.append(component.getUnformattedText());
   }

   public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
      return true;
   }

   public BlockPos getPosition() {
      return BlockPos.ORIGIN;
   }

   public Vec3d getPositionVector() {
      return Vec3d.ZERO;
   }

   public World getEntityWorld() {
      return this.server.getEntityWorld();
   }

   public Entity getCommandSenderEntity() {
      return null;
   }

   public boolean sendCommandFeedback() {
      return true;
   }

   public void setCommandStat(CommandResultStats.Type type, int amount) {
   }

   public MinecraftServer getServer() {
      return this.server;
   }
}
