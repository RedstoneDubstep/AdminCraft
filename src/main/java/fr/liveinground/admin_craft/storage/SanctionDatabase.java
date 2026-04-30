package fr.liveinground.admin_craft.storage;

import fr.liveinground.admin_craft.AdminCraft;
import fr.liveinground.admin_craft.storage.types.sanction.SanctionData;
import net.neoforged.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.security.SecureRandom;
import java.sql.*;

public class SanctionDatabase {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String URL = "jdbc:sqlite:" + FMLPaths.GAMEDIR.get()
            .resolve("AdminCraft_Storage")
            .resolve("sanctions.db").toAbsolutePath();

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    private static String generateID() {
        StringBuilder builder = new StringBuilder(8);
        builder.append("#");
        for (int i = 0; i < 8; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            builder.append(CHARACTERS.charAt(index));
        }
        String id = builder.toString();
        final String finalId = id;
        if (query("SELECT ign FROM sanctions WHERE id = ?", stmt -> stmt.setString(1, finalId), rs -> rs.next() ? rs.getString("ign") : null) != null) {
            id = generateID();
        }
        return id;
    }

    @FunctionalInterface
    public interface StatementPreparer {
        void prepare(PreparedStatement stmt) throws SQLException;
    }

    @FunctionalInterface
    public interface ResultExtractor<T> {
        T extract(ResultSet rs) throws SQLException;
    }

    @Nullable
    public static <T> T query(String sql, StatementPreparer preparer, ResultExtractor<T> extractor) {
        try (
                Connection conn = connect();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            preparer.prepare(stmt);

            try (ResultSet rs = stmt.executeQuery()) {
                return extractor.extract(rs);
            }
        } catch (SQLException e) {
            return null;
        }
    }

    public static boolean update(String sql, StatementPreparer preparer) {

        try (
                Connection conn = connect();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            preparer.prepare(stmt);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            AdminCraft.LOGGER.error("An issue occurred while trying to update the database: ", e);
            return false;
        }
    }

    @Nullable
    private static Exception post(String sql) {
        try (
                Connection connection = SanctionDatabase.connect();
                Statement statement = connection.createStatement();
        ) {
            statement.execute(sql);
            return null;
        } catch (Exception e) {
            return e;
        }
    }

    public static void start() {
        String init = """
                CREATE NEW TABLE IF NOT EXISTS sanctions (
                    id TEXT PRIMARY KEY,
                    type INTEGER,
                    uuid TEXT,
                    ign TEXT,
                    reason TEXT,
                    date TEXT,
                    expires TEXT,
                    appeal INTEGER,
                    appealDate INTEGER
                    );""";
        Exception ans = post(init);
        if (ans == null) {
            AdminCraft.LOGGER.info("Successfully connected to the sanction database");
        } else {
            AdminCraft.LOGGER.error("Error while initialising sanction database: ", ans);
        }
    }

    @Nullable
    public static String registerSanction(String uuid, String ign, SanctionData data) {
        final String ID = generateID();
        boolean result = update("INSERT INTO sanctions(id, type, uuid, ign, reason, date, expires, appeal, appealDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", stmt -> {
            stmt.setString(1, ID);
            stmt.setInt(2, data.sanctionType.ordinal());
            stmt.setString(3, uuid);
            stmt.setString(4, ign);
            stmt.setString(5, data.reason);
            stmt.setString(6, data.date.toString());
            if (data.expiresOn != null) {
                stmt.setString(7, data.expiresOn.toString());
            } else {
                stmt.setString(7, null);
            }
            //todo: appeal

        });
        if (result) {
            return ID;
        } else {
            return null;
        }
    }

    public static boolean isPlayerCurrentlySanctioned(String id, String ign) {
        return query("SELECT * FROM sanctions WHERE id = ?, ign = ?", stmt -> {
            stmt.setString(1, id);
            stmt.setString(2, ign);
        }, rs -> rs.next() ? rs.getString("ign") : null) != null;
    }
}
