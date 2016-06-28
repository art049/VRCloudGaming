package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandSetSpawnpoint extends CommandBase {
   public String getCommandName() {
      return "spawnpoint";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getCommandUsage(ICommandSender sender) {
      return "commands.spawnpoint.usage";
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if(args.length > 1 && args.length < 4) {
         throw new WrongUsageException("commands.spawnpoint.usage", new Object[0]);
      } else {
         EntityPlayerMP entityplayermp = args.length > 0?getPlayer(server, sender, args[0]):getCommandSenderAsPlayer(sender);
         BlockPos blockpos = args.length > 3?parseBlockPos(sender, args, 1, true):entityplayermp.getPosition();
         if(entityplayermp.worldObj != null) {
            entityplayermp.setSpawnPoint(blockpos, true);
            notifyCommandListener(sender, this, "commands.spawnpoint.success", new Object[]{entityplayermp.getName(), Integer.valueOf(blockpos.getX()), Integer.valueOf(blockpos.getY()), Integer.valueOf(blockpos.getZ())});
         }
      }
   }

   public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
      return args.length == 1?getListOfStringsMatchingLastWord(args, server.getAllUsernames()):(args.length > 1 && args.length <= 4?getTabCompletionCoordinate(args, 1, pos):Collections.<String>emptyList());
   }

   public boolean isUsernameIndex(String[] args, int index) {
      return index == 0;
   }
}
