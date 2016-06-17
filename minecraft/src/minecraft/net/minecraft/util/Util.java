package net.minecraft.util;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public class Util {
   public static Util.EnumOS getOSType() {
      String s = System.getProperty("os.name").toLowerCase();
      return s.contains("win")?Util.EnumOS.WINDOWS:(s.contains("mac")?Util.EnumOS.OSX:(s.contains("solaris")?Util.EnumOS.SOLARIS:(s.contains("sunos")?Util.EnumOS.SOLARIS:(s.contains("linux")?Util.EnumOS.LINUX:(s.contains("unix")?Util.EnumOS.LINUX:Util.EnumOS.UNKNOWN)))));
   }

   @Nullable
   public static <V> V runTask(FutureTask<V> task, Logger logger) {
      try {
         task.run();
         return task.get();
      } catch (ExecutionException executionexception) {
         logger.fatal((String)"Error executing task", (Throwable)executionexception);
      } catch (InterruptedException interruptedexception) {
         logger.fatal((String)"Error executing task", (Throwable)interruptedexception);
      }

      return (V)null;
   }

   public static <T> T getLastElement(List<T> list) {
      return (T)list.get(list.size() - 1);
   }

   public static enum EnumOS {
      LINUX,
      SOLARIS,
      WINDOWS,
      OSX,
      UNKNOWN;
   }
}
