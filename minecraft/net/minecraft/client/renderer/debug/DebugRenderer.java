package net.minecraft.client.renderer.debug;

import net.minecraft.client.Minecraft;

public class DebugRenderer
{
    public final DebugRendererPathfinding debugRendererPathfinding;
    public final DebugRendererWater debugRendererWater;

    public DebugRenderer(Minecraft clientIn)
    {
        this.debugRendererPathfinding = new DebugRendererPathfinding(clientIn);
        this.debugRendererWater = new DebugRendererWater(clientIn);
    }
}
