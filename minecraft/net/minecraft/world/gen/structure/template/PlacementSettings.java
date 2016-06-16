package net.minecraft.world.gen.structure.template;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public class PlacementSettings
{
    private Mirror mirror;
    private Rotation rotation;
    private boolean ignoreEntities;

    /**
     * the type of block in the world that will get replaced by the structure
     */
    private Block replacedBlock;

    /** the chunk the structure is within */
    private ChunkPos chunk;

    /** the bounds the structure is contained within */
    private StructureBoundingBox boundingBox;
    private boolean ignoreStructureBlock;

    public PlacementSettings()
    {
        this(Mirror.NONE, Rotation.NONE, false, (Block)null, (StructureBoundingBox)null);
    }

    public PlacementSettings(Mirror mirrorIn, Rotation rotationIn, boolean ignoreEntitiesIn, @Nullable Block replacedBlockIn, @Nullable StructureBoundingBox boundingBoxIn)
    {
        this.rotation = rotationIn;
        this.mirror = mirrorIn;
        this.ignoreEntities = ignoreEntitiesIn;
        this.replacedBlock = replacedBlockIn;
        this.chunk = null;
        this.boundingBox = boundingBoxIn;
        this.ignoreStructureBlock = true;
    }

    public PlacementSettings copy()
    {
        return (new PlacementSettings(this.mirror, this.rotation, this.ignoreEntities, this.replacedBlock, this.boundingBox)).setChunk(this.chunk).setIgnoreStructureBlock(this.ignoreStructureBlock);
    }

    public PlacementSettings setMirror(Mirror mirrorIn)
    {
        this.mirror = mirrorIn;
        return this;
    }

    public PlacementSettings setRotation(Rotation rotationIn)
    {
        this.rotation = rotationIn;
        return this;
    }

    public PlacementSettings setIgnoreEntities(boolean ignoreEntitiesIn)
    {
        this.ignoreEntities = ignoreEntitiesIn;
        return this;
    }

    public PlacementSettings setReplacedBlock(Block replacedBlockIn)
    {
        this.replacedBlock = replacedBlockIn;
        return this;
    }

    public PlacementSettings setChunk(ChunkPos chunkPosIn)
    {
        this.chunk = chunkPosIn;
        return this;
    }

    public PlacementSettings setBoundingBox(StructureBoundingBox boundingBoxIn)
    {
        this.boundingBox = boundingBoxIn;
        return this;
    }

    public Mirror getMirror()
    {
        return this.mirror;
    }

    public PlacementSettings setIgnoreStructureBlock(boolean ignoreStructureBlockIn)
    {
        this.ignoreStructureBlock = ignoreStructureBlockIn;
        return this;
    }

    public Rotation getRotation()
    {
        return this.rotation;
    }

    public boolean getIgnoreEntities()
    {
        return this.ignoreEntities;
    }

    public Block getReplacedBlock()
    {
        return this.replacedBlock;
    }

    @Nullable
    public StructureBoundingBox getBoundingBox()
    {
        if (this.boundingBox == null && this.chunk != null)
        {
            this.setBoundingBoxFromChunk();
        }

        return this.boundingBox;
    }

    public boolean getIgnoreStructureBlock()
    {
        return this.ignoreStructureBlock;
    }

    void setBoundingBoxFromChunk()
    {
        this.boundingBox = this.getBoundingBoxFromChunk(this.chunk);
    }

    @Nullable
    private StructureBoundingBox getBoundingBoxFromChunk(@Nullable ChunkPos pos)
    {
        if (pos == null)
        {
            return null;
        }
        else
        {
            int i = pos.chunkXPos * 16;
            int j = pos.chunkZPos * 16;
            return new StructureBoundingBox(i, 0, j, i + 16 - 1, 255, j + 16 - 1);
        }
    }
}
