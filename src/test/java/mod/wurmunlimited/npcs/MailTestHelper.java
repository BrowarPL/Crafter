package mod.wurmunlimited.npcs;

import com.wurmonline.server.items.WurmMail;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import java.util.List;

public class MailTestHelper {
    private static List<?> getAllMail() {
        try {
            return ReflectionUtil.getPrivateField(null, ReflectionUtil.getField(WurmMail.class, "allMail"));
        } catch (Exception e) {
            throw new RuntimeException("Nie udało się pobrać atrapy poczty", e);
        }
    }

    public static void clearMail() {
        getAllMail().clear();
    }

    public static int getSize() {
        return getAllMail().size();
    }

    public static boolean hasMail(long itemId) {
        try {
            for (Object m : getAllMail()) {
                long mItemId = ReflectionUtil.getPrivateField(m, ReflectionUtil.getField(WurmMail.class, "itemId"));
                if (mItemId == itemId) return true;
            }
            return false;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public static boolean hasMail(long itemId, long ownerId) {
        try {
            for (Object m : getAllMail()) {
                long mItemId = ReflectionUtil.getPrivateField(m, ReflectionUtil.getField(WurmMail.class, "itemId"));
                long mOwnerId = ReflectionUtil.getPrivateField(m, ReflectionUtil.getField(WurmMail.class, "ownerId"));
                if (mItemId == itemId && mOwnerId == ownerId) return true;
            }
            return false;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public static long getOwnerId(int index) {
        try {
            Object m = getAllMail().get(index);
            return ReflectionUtil.getPrivateField(m, ReflectionUtil.getField(WurmMail.class, "ownerId"));
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    public static long getItemId(int index) {
        try {
            Object m = getAllMail().get(index);
            return org.gotti.wurmunlimited.modloader.ReflectionUtil.getPrivateField(m, org.gotti.wurmunlimited.modloader.ReflectionUtil.getField(com.wurmonline.server.items.WurmMail.class, "itemId"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}