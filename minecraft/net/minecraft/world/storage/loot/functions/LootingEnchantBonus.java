package net.minecraft.world.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;

public class LootingEnchantBonus extends LootFunction
{
    private final RandomValueRange count;

    public LootingEnchantBonus(LootCondition[] conditionsIn, RandomValueRange randomRange)
    {
        super(conditionsIn);
        this.count = randomRange;
    }

    public ItemStack apply(ItemStack stack, Random rand, LootContext context)
    {
        Entity entity = context.getKiller();

        if (entity instanceof EntityLivingBase)
        {
            int i = EnchantmentHelper.getLootingModifier((EntityLivingBase)entity);

            if (i == 0)
            {
                return stack;
            }

            float f = (float)i * this.count.generateFloat(rand);
            stack.stackSize += Math.round(f);
        }

        return stack;
    }

    public static class Serializer extends LootFunction.Serializer<LootingEnchantBonus>
    {
        protected Serializer()
        {
            super(new ResourceLocation("looting_enchant"), LootingEnchantBonus.class);
        }

        public void serialize(JsonObject object, LootingEnchantBonus functionClazz, JsonSerializationContext serializationContext)
        {
            object.add("count", serializationContext.serialize(functionClazz.count));
        }

        public LootingEnchantBonus deserialize(JsonObject object, JsonDeserializationContext deserializationContext, LootCondition[] conditionsIn)
        {
            return new LootingEnchantBonus(conditionsIn, (RandomValueRange)JsonUtils.deserializeClass(object, "count", deserializationContext, RandomValueRange.class));
        }
    }
}
