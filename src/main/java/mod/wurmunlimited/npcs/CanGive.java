package mod.wurmunlimited.npcs;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;

public interface CanGive {
    boolean canGive(Creature performer, Item item, Creature target);
    default boolean isWearable(Item item) { return false; }
}