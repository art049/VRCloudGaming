package net.minecraft.command;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldSettings;

public class CommandDefaultGameMode extends CommandGameMode {
   public String getCommandName() {
      return "defaultgamemode";
   }

   public String getCommandUsage(ICommandSender sender) {
      return "commands.defaultgamemode.usage";
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if(args.length <= 0) {
         throw new WrongUsageException("commands.defaultgamemode.usage", new Object[0]);
      } else {
         WorldSettings.GameType worldsettings$gametype = this.getGameModeFromCommand(sender, args[0]);
         this.setDefaultGameType(worldsettings$gametype, server);
         notifyCommandListener(sender, this, "commands.defaultgamemode.success", new Object[]{new TextComponentTranslation("gameMode." + worldsettings$gametype.getName(), new Object[0])});
      }
   }

   protected void setDefaultGameType(WorldSettings.GameType gameType, MinecraftServer server) {
      server.setGameType(gameType);
      if(server.getForceGamemode()) {
         for(EntityPlayerMP entityplayermp : server.getPlayerList().getPlayerList()) {
            entityplayermp.setGameType(gameType);
         }
      }
   }
}
