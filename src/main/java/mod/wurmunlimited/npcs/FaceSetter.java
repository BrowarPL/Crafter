package mod.wurmunlimited.npcs;

import com.wurmonline.server.creatures.Creature;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import java.util.function.Predicate;

public class FaceSetter {
    public FaceSetter(Predicate<Creature> predicate, String dbName) {}
    public static void init(HookManager manager) {}
    public long getFaceFor(Creature creature) { return 0L; }
}