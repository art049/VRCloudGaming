package net.minecraft.util.registry;

import java.util.Set;
import javax.annotation.Nullable;

public interface IRegistry<K, V> extends Iterable<V> {
   @Nullable
   V getObject(K name);

   void putObject(K key, V value);

   Set<K> getKeys();
}
