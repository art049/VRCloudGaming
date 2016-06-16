package net.minecraft.world.gen.structure.template;

import com.google.common.collect.Maps;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

public class TemplateManager
{
    private final Map<String, Template> templates;

    /**
     * the folder in the assets folder where the structure templates are found.
     */
    private final String baseFolder;

    public TemplateManager()
    {
        this("structures");
    }

    public TemplateManager(String basefolderIn)
    {
        this.templates = Maps.<String, Template>newHashMap();
        this.baseFolder = basefolderIn;
    }

    public Template getTemplate(@Nullable MinecraftServer server, ResourceLocation id)
    {
        String s = id.getResourcePath();

        if (this.templates.containsKey(s))
        {
            return (Template)this.templates.get(s);
        }
        else
        {
            if (server != null)
            {
                this.readTemplate(server, id);
            }
            else
            {
                this.readTemplateFromJar(id);
            }

            if (this.templates.containsKey(s))
            {
                return (Template)this.templates.get(s);
            }
            else
            {
                Template template = new Template();
                this.templates.put(s, template);
                return template;
            }
        }
    }

    /**
     * This reads a structure template from the given location and stores it.
     * This first attempts get the template from an external folder.
     * If it isn't there then it attempts to take it from the minecraft jar.
     */
    public boolean readTemplate(MinecraftServer server, ResourceLocation id)
    {
        String s = id.getResourcePath();
        File file1 = server.getFile(this.baseFolder);
        File file2 = new File(file1, s + ".nbt");

        if (!file2.exists())
        {
            return this.readTemplateFromJar(id);
        }
        else
        {
            InputStream inputstream = null;
            boolean flag;

            try
            {
                inputstream = new FileInputStream(file2);
                this.readTemplateFromStream(s, inputstream);
                return true;
            }
            catch (Throwable var12)
            {
                flag = false;
            }
            finally
            {
                IOUtils.closeQuietly(inputstream);
            }

            return flag;
        }
    }

    /**
     * reads a template from the minecraft jar
     */
    private boolean readTemplateFromJar(ResourceLocation id)
    {
        String s = id.getResourceDomain();
        String s1 = id.getResourcePath();
        InputStream inputstream = null;
        boolean flag;

        try
        {
            inputstream = MinecraftServer.class.getResourceAsStream("/assets/" + s + "/structures/" + s1 + ".nbt");
            this.readTemplateFromStream(s1, inputstream);
            return true;
        }
        catch (Throwable var10)
        {
            flag = false;
        }
        finally
        {
            IOUtils.closeQuietly(inputstream);
        }

        return flag;
    }

    /**
     * reads a template from an inputstream
     */
    private void readTemplateFromStream(String id, InputStream stream) throws IOException
    {
        NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(stream);
        Template template = new Template();
        template.read(nbttagcompound);
        this.templates.put(id, template);
    }

    /**
     * writes the template to an external folder
     */
    public boolean writeTemplate(MinecraftServer server, ResourceLocation id)
    {
        String s = id.getResourcePath();

        if (!this.templates.containsKey(s))
        {
            return false;
        }
        else
        {
            File file1 = server.getFile(this.baseFolder);

            if (!file1.exists())
            {
                if (!file1.mkdirs())
                {
                    return false;
                }
            }
            else if (!file1.isDirectory())
            {
                return false;
            }

            File file2 = new File(file1, s + ".nbt");
            Template template = (Template)this.templates.get(s);
            OutputStream outputstream = null;
            boolean flag;

            try
            {
                NBTTagCompound nbttagcompound = template.func_189552_a(new NBTTagCompound());
                outputstream = new FileOutputStream(file2);
                CompressedStreamTools.writeCompressed(nbttagcompound, outputstream);
                return true;
            }
            catch (Throwable var13)
            {
                flag = false;
            }
            finally
            {
                IOUtils.closeQuietly(outputstream);
            }

            return flag;
        }
    }
}
