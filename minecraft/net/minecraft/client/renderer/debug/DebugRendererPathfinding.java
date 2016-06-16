package net.minecraft.client.renderer.debug;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.pathfinding.Path;

public class DebugRendererPathfinding
{
    private final Minecraft minecraft;
    private Map<Integer, Path> pathMap = new HashMap();
    private Map<Integer, Float> pathMaxDistance = new HashMap();
    private Map<Integer, Long> creationMap = new HashMap();
    private static float MAX_RENDER_DISTANCE = 40.0F;

    public DebugRendererPathfinding(Minecraft minecraftIn)
    {
        this.minecraft = minecraftIn;
    }

    public void addPath(int p_188289_1_, Path p_188289_2_, float p_188289_3_)
    {
        this.pathMap.put(Integer.valueOf(p_188289_1_), p_188289_2_);
        this.creationMap.put(Integer.valueOf(p_188289_1_), Long.valueOf(System.currentTimeMillis()));
        this.pathMaxDistance.put(Integer.valueOf(p_188289_1_), Float.valueOf(p_188289_3_));
    }
}
