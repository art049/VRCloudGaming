package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class CommandExecuteAt extends CommandBase {
   public String getCommandName() {
      return "execute";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getCommandUsage(ICommandSender sender) {
      return "commands.execute.usage";
   }

   public void execute(final MinecraftServer server, final ICommandSender sender, String[] args) throws CommandException {
      if(args.length < 5) {
         throw new WrongUsageException("commands.execute.usage", new Object[0]);
      } else {
         final Entity entity = getEntity(server, sender, args[0], Entity.class);
         final double d0 = parseDouble(entity.posX, args[1], false);
         final double d1 = parseDouble(entity.posY, args[2], false);
         final double d2 = parseDouble(entity.posZ, args[3], false);
         final BlockPos blockpos = new BlockPos(d0, d1, d2);
         int i = 4;
         if("detect".equals(args[4]) && args.length > 10) {
            World world = entity.getEntityWorld();
            double d3 = parseDouble(d0, args[5], false);
            double d4 = parseDouble(d1, args[6], false);
            double d5 = parseDouble(d2, args[7], false);
            Block block = getBlockByText(sender, args[8]);
            int k = parseInt(args[9], -1, 15);
            BlockPos blockpos1 = new BlockPos(d3, d4, d5);
            IBlockState iblockstate = world.getBlockState(blockpos1);
            if(iblockstate.getBlock() != block || k >= 0 && iblockstate.getBlock().getMetaFromState(iblockstate) != k) {
               throw new CommandException("commands.execute.failed", new Object[]{"detect", entity.getName()});
            }

            i = 10;
         }

         String s = buildString(args, i);
         ICommandSender icommandsender = new ICommandSender() {
            public String getName() {
               return entity.getName();
            }

            public ITextComponent getDisplayName() {
               return entity.getDisplayName();
            }

            public void addChatMessage(ITextComponent component) {
               sender.addChatMessage(component);
            }

            public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
               return sender.canCommandSenderUseCommand(permLevel, commandName);
            }

            public BlockPos getPosition() {
               return blockpos;
            }

            public Vec3d getPositionVector() {
               return new Vec3d(d0, d1, d2);
            }

            public World getEntityWorld() {
               return entity.worldObj;
            }

            public Entity getCommandSenderEntity() {
               return entity;
            }

            public boolean sendCommandFeedback() {
               return server == null || server.worldServers[0].getGameRules().getBoolean("commandBlockOutput");
            }

            public void setCommandStat(CommandResultStats.Type type, int amount) {
               entity.setCommandStat(type, amount);
            }

            public MinecraftServer getServer() {
               return entity.getServer();
            }
         };
         ICommandManager icommandmanager = server.getCommandManager();

         try {
            int j = icommandmanager.executeCommand(icommandsender, s);
            if(j < 1) {
               throw new CommandException("commands.execute.allInvocationsFailed", new Object[]{s});
            }
         } catch (Throwable var24) {
            throw new CommandException("commands.execute.failed", new Object[]{s, entity.getName()});
         }
      }
   }

   public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
      return args.length == 1?getListOfStringsMatchingLastWord(args, server.getAllUsernames()):(args.length > 1 && args.length <= 4?getTabCompletionCoordinate(args, 1, pos):(args.length > 5 && args.length <= 8 && "detect".equals(args[4])?getTabCompletionCoordinate(args, 5, pos):(args.length == 9 && "detect".equals(args[4])?getListOfStringsMatchingLastWord(args, Block.REGISTRY.getKeys()):Collections.<String>emptyList())));
   }

   public boolean isUsernameIndex(String[] args, int index) {
      return index == 0;
   }
}
