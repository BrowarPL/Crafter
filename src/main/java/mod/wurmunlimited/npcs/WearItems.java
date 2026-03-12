package mod.wurmunlimited.npcs;

import com.wurmonline.server.creatures.Creature;

public interface WearItems {
    void beforeWearing(Creature creature);
    void afterWearing(Creature creature);
    boolean isApplicableCreature(Creature creature);
}