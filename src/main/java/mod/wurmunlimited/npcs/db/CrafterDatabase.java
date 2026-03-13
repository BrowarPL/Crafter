package mod.wurmunlimited.npcs.db;

import com.wurmonline.server.Constants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.shared.exceptions.WurmServerException;
import mod.wurmunlimited.npcs.CrafterMod;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("SqlNoDataSourceInspection") // Suppress IDE warnings about missing DB schema
public class CrafterDatabase {
    private static final Logger logger = Logger.getLogger(CrafterDatabase.class.getName());
    private static String dbString = "";
    private static boolean created = false;

    // Removed unused fields: clock, tags, currencies

    public interface Execute {
        void run(Connection db) throws SQLException;
    }

    public static class FailedToSaveSkills extends WurmServerException {
        private FailedToSaveSkills() {
            super("An error occurred when attempting to save Crafter skills.");
        }
    }

    private static void init() throws SQLException {
        try (Connection conn = DriverManager.getConnection(dbString);
             Statement stmt1 = conn.createStatement();
             Statement stmt2 = conn.createStatement()) {

            stmt1.execute("CREATE TABLE IF NOT EXISTS saved_skills (" +
                    "contract_id INTEGER," +
                    "skill_id INTEGER," +
                    "skill_level REAL," +
                    "UNIQUE (contract_id, skill_id) ON CONFLICT REPLACE" +
                    ");");

            stmt2.execute("CREATE TABLE IF NOT EXISTS donated_tools (" +
                    "crafter_id INTEGER," +
                    "item_id INTEGER," +
                    "UNIQUE (crafter_id, item_id) ON CONFLICT REPLACE" +
                    ");");
        }

        created = true;
    }

    private static void execute(Execute execute) throws SQLException {
        int maxRetries = 15;
        long retryDelayMs = 300L;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            Connection db = null;
            try {
                if (dbString.isEmpty()) {
                    dbString = "jdbc:sqlite:" + Constants.dbHost + "/sqlite/" + CrafterMod.dbName;
                }
                if (!created) {
                    init();
                }

                db = DriverManager.getConnection(dbString);
                execute.run(db);

                return;
            } catch (SQLException e) {
                String msg = e.getMessage().toLowerCase();
                boolean isLocked = msg.contains("busy") || msg.contains("locked");

                if (!isLocked || attempt == maxRetries) {
                    if (attempt == maxRetries) {
                        logger.severe("CrafterDatabase critical fail after " + maxRetries + " attempts: " + e.getMessage());
                    }
                    throw e;
                }

                logger.warning("CrafterDatabase locked or busy (attempt " + attempt + "/" + maxRetries + "). Retrying in " + retryDelayMs + "ms... Reason: " + e.getMessage());
                try {
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Thread interrupted while waiting for database retry", ie);
                }
            } finally {
                try {
                    if (db != null && !db.isClosed()) {
                        db.close();
                    }
                } catch (SQLException e1) {
                    // Replaced printStackTrace with proper logger
                    logger.log(Level.WARNING, "Could not close connection to database.", e1);
                }
            }
        }
    }

    @SuppressWarnings("SqlResolve")
    public static Map<Integer, Double> loadSkillsFor(Item contract) throws SQLException {
        Map<Integer, Double> skills = new HashMap<>();

        execute(db -> {
            try (PreparedStatement ps = db.prepareStatement("SELECT skill_id, skill_level FROM saved_skills WHERE contract_id=?;")) {
                ps.setLong(1, contract.getWurmId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        skills.put(rs.getInt(1), rs.getDouble(2));
                    }
                }
            }
        });

        return skills;
    }

    @SuppressWarnings("SqlResolve")
    public static void saveSkillsFor(Creature crafter, Item writ) throws FailedToSaveSkills {
        try {
            execute(db -> {
                db.setAutoCommit(false);
                try (PreparedStatement ps = db.prepareStatement("INSERT INTO saved_skills VALUES (?, ?, ?);")) {
                    for (Map.Entry<Integer, Skill> entry : crafter.getSkills().getSkillTree().entrySet()) {
                        ps.setLong(1, writ.getWurmId());
                        ps.setInt(2, entry.getKey());
                        ps.setDouble(3, entry.getValue().getKnowledge());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                db.commit();
            });
        } catch (SQLException e) {
            // Replaced printStackTrace with proper logger
            logger.log(Level.WARNING, "Failed to save skills for " + crafter.getName() + ".", e);
            throw new FailedToSaveSkills();
        }
    }

    @SuppressWarnings("SqlResolve")
    public static Set<Long> getGivenToolsFor(Creature crafter) throws SQLException {
        Set<Long> tools = new HashSet<>();

        execute(db -> {
            try (PreparedStatement ps = db.prepareStatement("SELECT item_id FROM donated_tools WHERE crafter_id=?;")) {
                ps.setLong(1, crafter.getWurmId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        tools.add(rs.getLong(1));
                    }
                }
            }
        });

        return tools;
    }

    @SuppressWarnings("SqlResolve")
    public static void addGivenToolFor(Creature crafter, Item tool) throws SQLException {
        execute(db -> {
            try (PreparedStatement ps = db.prepareStatement("INSERT INTO donated_tools VALUES(?, ?);")) {
                ps.setLong(1, crafter.getWurmId());
                ps.setLong(2, tool.getWurmId());
                ps.executeUpdate();
            }
        });
    }

    @SuppressWarnings("SqlResolve")
    public static void removeGivenToolFor(Creature crafter, Item tool) throws SQLException {
        execute(db -> {
            try (PreparedStatement ps = db.prepareStatement("DELETE FROM donated_tools WHERE crafter_id=? AND item_id=?;")) {
                ps.setLong(1, crafter.getWurmId());
                ps.setLong(2, tool.getWurmId());
                ps.executeUpdate();
            }
        });
    }
}