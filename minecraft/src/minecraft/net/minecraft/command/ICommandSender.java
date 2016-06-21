package net.minecraft.command;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public interface ICommandSender {
   String getName();

   ITextComponent getDisplayName();

   void addChatMessage(ITextComponent component);

   boolean canCommandSenderUseCommand(int permLevel, String commandName);

   BlockPos getPosition();

   Vec3d getPositionVector();

   World getEntityWorld();

   @Nullable
   Entity getCommandSenderEntity();

   boolean sendCommandFeedback();

   void setCommandStat(CommandResultStats.Type type, int amount);

   @Nullable
   MinecraftServer getServer();
}
