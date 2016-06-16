package net.minecraft.util.datafix.walkers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.IDataFixer;

public class ItemStackDataLists extends Filtered
{
    private final String[] matchingTags;

    public ItemStackDataLists(String id, String... tags)
    {
        super("id", id);
        this.matchingTags = tags;
    }

    NBTTagCompound filteredProcess(IDataFixer fixer, NBTTagCompound compound, int versionIn)
    {
        int i = 0;

        for (int j = this.matchingTags.length; i < j; ++i)
        {
            compound = DataFixesManager.processInventory(fixer, compound, versionIn, this.matchingTags[i]);
        }

        return compound;
    }
}
