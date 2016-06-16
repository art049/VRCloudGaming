package net.minecraft.tileentity;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;

public class TileEntityStructure extends TileEntity
{
    private String name = "";
    private String author = "";
    private String metadata = "";
    private BlockPos position = new BlockPos(1, 1, 1);
    private BlockPos size = BlockPos.ORIGIN;
    private Mirror mirror = Mirror.NONE;
    private Rotation rotation = Rotation.NONE;
    private TileEntityStructure.Mode mode = TileEntityStructure.Mode.DATA;
    private boolean ignoreEntities;

    public NBTTagCompound func_189515_b(NBTTagCompound p_189515_1_)
    {
        super.func_189515_b(p_189515_1_);
        p_189515_1_.setString("name", this.name);
        p_189515_1_.setString("author", this.author);
        p_189515_1_.setString("metadata", this.metadata);
        p_189515_1_.setInteger("posX", this.position.getX());
        p_189515_1_.setInteger("posY", this.position.getY());
        p_189515_1_.setInteger("posZ", this.position.getZ());
        p_189515_1_.setInteger("sizeX", this.size.getX());
        p_189515_1_.setInteger("sizeY", this.size.getY());
        p_189515_1_.setInteger("sizeZ", this.size.getZ());
        p_189515_1_.setString("rotation", this.rotation.toString());
        p_189515_1_.setString("mirror", this.mirror.toString());
        p_189515_1_.setString("mode", this.mode.toString());
        p_189515_1_.setBoolean("ignoreEntities", this.ignoreEntities);
        return p_189515_1_;
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.name = compound.getString("name");
        this.author = compound.getString("author");
        this.metadata = compound.getString("metadata");
        this.position = new BlockPos(compound.getInteger("posX"), compound.getInteger("posY"), compound.getInteger("posZ"));
        this.size = new BlockPos(compound.getInteger("sizeX"), compound.getInteger("sizeY"), compound.getInteger("sizeZ"));

        try
        {
            this.rotation = Rotation.valueOf(compound.getString("rotation"));
        }
        catch (IllegalArgumentException var5)
        {
            this.rotation = Rotation.NONE;
        }

        try
        {
            this.mirror = Mirror.valueOf(compound.getString("mirror"));
        }
        catch (IllegalArgumentException var4)
        {
            this.mirror = Mirror.NONE;
        }

        try
        {
            this.mode = TileEntityStructure.Mode.valueOf(compound.getString("mode"));
        }
        catch (IllegalArgumentException var3)
        {
            this.mode = TileEntityStructure.Mode.DATA;
        }

        this.ignoreEntities = compound.getBoolean("ignoreEntities");
    }

    @Nullable
    public SPacketUpdateTileEntity func_189518_D_()
    {
        return new SPacketUpdateTileEntity(this.pos, 7, this.func_189517_E_());
    }

    public NBTTagCompound func_189517_E_()
    {
        return this.func_189515_b(new NBTTagCompound());
    }

    public void setName(String nameIn)
    {
        this.name = nameIn;
    }

    public void setPosition(BlockPos posIn)
    {
        this.position = posIn;
    }

    public void setSize(BlockPos sizeIn)
    {
        this.size = sizeIn;
    }

    public void setMirror(Mirror mirrorIn)
    {
        this.mirror = mirrorIn;
    }

    public void setRotation(Rotation rotationIn)
    {
        this.rotation = rotationIn;
    }

    public void setMetadata(String metadataIn)
    {
        this.metadata = metadataIn;
    }

    public void setMode(TileEntityStructure.Mode modeIn)
    {
        this.mode = modeIn;
        IBlockState iblockstate = this.worldObj.getBlockState(this.getPos());

        if (iblockstate.getBlock() == Blocks.STRUCTURE_BLOCK)
        {
            this.worldObj.setBlockState(this.getPos(), iblockstate.withProperty(BlockStructure.MODE, modeIn), 2);
        }
    }

    public void setIgnoresEntities(boolean ignoreEntitiesIn)
    {
        this.ignoreEntities = ignoreEntitiesIn;
    }

    public boolean detectSize()
    {
        if (this.mode != TileEntityStructure.Mode.SAVE)
        {
            return false;
        }
        else
        {
            BlockPos blockpos = this.getPos();
            int i = 128;
            BlockPos blockpos1 = new BlockPos(blockpos.getX() - 128, 0, blockpos.getZ() - 128);
            BlockPos blockpos2 = new BlockPos(blockpos.getX() + 128, 255, blockpos.getZ() + 128);
            List<TileEntityStructure> list = this.getNearbyCornerBlocks(blockpos1, blockpos2);
            List<TileEntityStructure> list1 = this.filterRelatedCornerBlocks(list);

            if (list1.size() < 1)
            {
                return false;
            }
            else
            {
                StructureBoundingBox structureboundingbox = this.calculateEnclosingBoundingBox(blockpos, list1);

                if (structureboundingbox.maxX - structureboundingbox.minX > 1 && structureboundingbox.maxY - structureboundingbox.minY > 1 && structureboundingbox.maxZ - structureboundingbox.minZ > 1)
                {
                    this.position = new BlockPos(structureboundingbox.minX - blockpos.getX() + 1, structureboundingbox.minY - blockpos.getY() + 1, structureboundingbox.minZ - blockpos.getZ() + 1);
                    this.size = new BlockPos(structureboundingbox.maxX - structureboundingbox.minX - 1, structureboundingbox.maxY - structureboundingbox.minY - 1, structureboundingbox.maxZ - structureboundingbox.minZ - 1);
                    this.markDirty();
                    IBlockState iblockstate = this.worldObj.getBlockState(blockpos);
                    this.worldObj.notifyBlockUpdate(blockpos, iblockstate, iblockstate, 3);
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
    }

    private List<TileEntityStructure> filterRelatedCornerBlocks(List<TileEntityStructure> p_184415_1_)
    {
        Iterable<TileEntityStructure> iterable = Iterables.filter(p_184415_1_, new Predicate<TileEntityStructure>()
        {
            public boolean apply(@Nullable TileEntityStructure p_apply_1_)
            {
                return p_apply_1_.mode == TileEntityStructure.Mode.CORNER && TileEntityStructure.this.name.equals(p_apply_1_.name);
            }
        });
        return Lists.newArrayList(iterable);
    }

    private List<TileEntityStructure> getNearbyCornerBlocks(BlockPos p_184418_1_, BlockPos p_184418_2_)
    {
        List<TileEntityStructure> list = Lists.<TileEntityStructure>newArrayList();

        for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(p_184418_1_, p_184418_2_))
        {
            IBlockState iblockstate = this.worldObj.getBlockState(blockpos$mutableblockpos);

            if (iblockstate.getBlock() == Blocks.STRUCTURE_BLOCK)
            {
                TileEntity tileentity = this.worldObj.getTileEntity(blockpos$mutableblockpos);

                if (tileentity != null && tileentity instanceof TileEntityStructure)
                {
                    list.add((TileEntityStructure)tileentity);
                }
            }
        }

        return list;
    }

    private StructureBoundingBox calculateEnclosingBoundingBox(BlockPos p_184416_1_, List<TileEntityStructure> p_184416_2_)
    {
        StructureBoundingBox structureboundingbox;

        if (p_184416_2_.size() > 1)
        {
            BlockPos blockpos = ((TileEntityStructure)p_184416_2_.get(0)).getPos();
            structureboundingbox = new StructureBoundingBox(blockpos, blockpos);
        }
        else
        {
            structureboundingbox = new StructureBoundingBox(p_184416_1_, p_184416_1_);
        }

        for (TileEntityStructure tileentitystructure : p_184416_2_)
        {
            BlockPos blockpos1 = tileentitystructure.getPos();

            if (blockpos1.getX() < structureboundingbox.minX)
            {
                structureboundingbox.minX = blockpos1.getX();
            }
            else if (blockpos1.getX() > structureboundingbox.maxX)
            {
                structureboundingbox.maxX = blockpos1.getX();
            }

            if (blockpos1.getY() < structureboundingbox.minY)
            {
                structureboundingbox.minY = blockpos1.getY();
            }
            else if (blockpos1.getY() > structureboundingbox.maxY)
            {
                structureboundingbox.maxY = blockpos1.getY();
            }

            if (blockpos1.getZ() < structureboundingbox.minZ)
            {
                structureboundingbox.minZ = blockpos1.getZ();
            }
            else if (blockpos1.getZ() > structureboundingbox.maxZ)
            {
                structureboundingbox.maxZ = blockpos1.getZ();
            }
        }

        return structureboundingbox;
    }

    public boolean save()
    {
        if (this.mode == TileEntityStructure.Mode.SAVE && !this.worldObj.isRemote)
        {
            BlockPos blockpos = this.getPos().add(this.position);
            WorldServer worldserver = (WorldServer)this.worldObj;
            MinecraftServer minecraftserver = this.worldObj.getMinecraftServer();
            TemplateManager templatemanager = worldserver.getStructureTemplateManager();
            Template template = templatemanager.getTemplate(minecraftserver, new ResourceLocation(this.name));
            template.takeBlocksFromWorld(this.worldObj, blockpos, this.size, !this.ignoreEntities, Blocks.BARRIER);
            template.setAuthor(this.author);
            templatemanager.writeTemplate(minecraftserver, new ResourceLocation(this.name));
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean load()
    {
        if (this.mode == TileEntityStructure.Mode.LOAD && !this.worldObj.isRemote)
        {
            BlockPos blockpos = this.getPos().add(this.position);
            WorldServer worldserver = (WorldServer)this.worldObj;
            MinecraftServer minecraftserver = this.worldObj.getMinecraftServer();
            TemplateManager templatemanager = worldserver.getStructureTemplateManager();
            Template template = templatemanager.getTemplate(minecraftserver, new ResourceLocation(this.name));

            if (!StringUtils.isNullOrEmpty(template.getAuthor()))
            {
                this.author = template.getAuthor();
            }

            if (!this.size.equals(template.getSize()))
            {
                this.size = template.getSize();
                return false;
            }
            else
            {
                BlockPos blockpos1 = template.transformedSize(this.rotation);

                for (Entity entity : this.worldObj.getEntitiesWithinAABBExcludingEntity((Entity)null, new AxisAlignedBB(blockpos, blockpos1.add(blockpos).add(-1, -1, -1))))
                {
                    this.worldObj.removeEntityDangerously(entity);
                }

                PlacementSettings placementsettings = (new PlacementSettings()).setMirror(this.mirror).setRotation(this.rotation).setIgnoreEntities(this.ignoreEntities).setChunk((ChunkPos)null).setReplacedBlock((Block)null).setIgnoreStructureBlock(false);
                template.addBlocksToWorldChunk(this.worldObj, blockpos, placementsettings);
                return true;
            }
        }
        else
        {
            return false;
        }
    }

    public static enum Mode implements IStringSerializable
    {
        SAVE("save", 0),
        LOAD("load", 1),
        CORNER("corner", 2),
        DATA("data", 3);

        private static final TileEntityStructure.Mode[] MODES = new TileEntityStructure.Mode[values().length];
        private final String modeName;
        private final int modeId;

        private Mode(String modeNameIn, int modeIdIn)
        {
            this.modeName = modeNameIn;
            this.modeId = modeIdIn;
        }

        public String getName()
        {
            return this.modeName;
        }

        public int getModeId()
        {
            return this.modeId;
        }

        public static TileEntityStructure.Mode getById(int id)
        {
            if (id < 0 || id >= MODES.length)
            {
                id = 0;
            }

            return MODES[id];
        }

        static {
            for (TileEntityStructure.Mode tileentitystructure$mode : values())
            {
                MODES[tileentitystructure$mode.getModeId()] = tileentitystructure$mode;
            }
        }
    }
}
