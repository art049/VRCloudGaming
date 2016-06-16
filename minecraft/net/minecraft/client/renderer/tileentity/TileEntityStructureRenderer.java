package net.minecraft.client.renderer.tileentity;

import net.minecraft.tileentity.TileEntityStructure;

public class TileEntityStructureRenderer extends TileEntitySpecialRenderer<TileEntityStructure>
{
    public void renderTileEntityAt(TileEntityStructure te, double x, double y, double z, float partialTicks, int destroyStage)
    {
    }

    public boolean isGlobalRenderer(TileEntityStructure te)
    {
        return true;
    }
}
