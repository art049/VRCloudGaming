package net.minecraft.command;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public interface ICommand extends Comparable<ICommand> {
   String getCommandName();

   String getCommandUsage(ICommandSender sender);

   List<String> getCommandAliases();

   void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException;

   boolean checkPermission(MinecraftServer server, ICommandSender sender);

   List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos);

   boolean isUsernameIndex(String[] args, int index);
}
