package net.minecraft.util;

<<<<<<< HEAD
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
=======
>>>>>>> Decodage
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

public class MouseHelper {
   public int deltaX;
   public int deltaY;

   public void grabMouseCursor() {
<<<<<<< HEAD
	  
      Mouse.setGrabbed(true);
      
=======
      Mouse.setGrabbed(true);
>>>>>>> Decodage
      this.deltaX = 0;
      this.deltaY = 0;
   }

   public void ungrabMouseCursor() {
      Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
      Mouse.setGrabbed(false);
   }

   public void mouseXYChange() {
      this.deltaX = Mouse.getDX();
      this.deltaY = Mouse.getDY();
<<<<<<< HEAD
     
   }
  
=======
   }
>>>>>>> Decodage
}
