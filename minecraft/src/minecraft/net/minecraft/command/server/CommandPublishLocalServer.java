package net.minecraft.command.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldSettings;

public class CommandPublishLocalServer extends CommandBase {
   public String getCommandName() {
      return "publish";
   }

   public String getCommandUsage(ICommandSender sender) {
      return "commands.publish.usage";
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      String s = server.shareToLAN(WorldSettings.GameType.SURVIVAL, false);
      if(s != null) {
         notifyCommandListener(sender, this, "commands.publish.started", new Object[]{s});
      } else {
         notifyCommandListener(sender, this, "commands.publish.failed", new Object[0]);
      }
   }
}
