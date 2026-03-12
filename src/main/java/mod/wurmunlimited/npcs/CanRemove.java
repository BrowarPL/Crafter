package mod.wurmunlimited.npcs;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;

public interface CanRemove {
    boolean canRemoveFrom(Creature performer, Creature target);
    boolean canRemove(Creature performer, Item item, Creature target);
    default boolean isWearingItems(Creature target) { return false; }
}