package fr.liveinground.admin_craft.storage;

import fr.liveinground.admin_craft.AdminCraft;
import fr.liveinground.admin_craft.storage.types.sanction.AppealStatus;
import fr.liveinground.admin_craft.storage.types.sanction.Sanction;
import fr.liveinground.admin_craft.storage.types.sanction.SanctionData;
import net.neoforged.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        StringBuilder builder = new StringBuilder(9);
        builder.append("#");
        for (int i = 0; i < 8; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            builder.append(CHARACTERS.charAt(index));
        }
        String id = builder.toString();
        final String finalId = id;
        if (query("SELECT ign FROM sanctions WHERE id = ?;", stmt -> stmt.setString(1, finalId), rs -> rs.next() ? rs.getString("ign") : null) != null) {
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
                CREATE TABLE IF NOT EXISTS sanctions (
                    id TEXT PRIMARY KEY,
                    type TEXT NOT NULL,
                    uuid TEXT NOT NULL,
                    ign TEXT,
                    reason TEXT NOT NULL,
                    date BIGINT NOT NULL,
                    expires TEXT,
                    appealStatus TEXT NOT NULL,
                    appealDate BIGINT
                    );""";
        Exception ans = post(init);
        if (ans == null) {
            AdminCraft.LOGGER.info("Successfully connected to the sanction database");
        } else {
            AdminCraft.LOGGER.error("Error while initialising sanction database: ", ans);
        }
    }

    @Nullable
    public static String registerSanction(String uuid, String ign, SanctionData data, boolean appealable, @Nullable Date appealDelay) {
        final String ID = generateID();
        boolean result = update("INSERT INTO sanctions(id, type, uuid, ign, reason, date, expires, appealStatus, appealDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);", stmt -> {
            stmt.setString(1, ID);
            stmt.setString(2, data.sanctionType.toString());
            stmt.setString(3, uuid);
            stmt.setString(4, ign);
            stmt.setString(5, data.reason);
            stmt.setLong(6, data.date.getTime());
            if (data.expiresOn != null) {
                stmt.setString(7, data.expiresOn.toString());
            } else {
                stmt.setString(7, null);
            }
            if (appealable) {
                if (appealDelay != null) {
                    stmt.setString(8, AppealStatus.DELAYED.toString());
                    stmt.setString(9, appealDelay.toString());
                } else {
                    stmt.setString(8, AppealStatus.NOT_REQUESTED.toString());
                    stmt.setString(9, null);
                }
            } else {
                stmt.setString(8, AppealStatus.NOT_ALLOWED.toString());
                stmt.setString(9, null);
            }

        });
        if (result) {
            return ID;
        } else {
            return null;
        }
    }

    public static boolean isPlayerCurrentlySanctioned(String id, String ign) {
        return query(
                "SELECT ign FROM sanctions WHERE id = ? AND ign = ?;<",
                stmt -> {
                    stmt.setString(1, id);
                    stmt.setString(2, ign);
                },
                rs -> rs.next() ? rs.getString("ign") : null
        ) != null;
    }

    public static @Nullable AppealStatus getAppealStatus(String id) {
        String status = query(
                "SELECT appealStatus FROM sanctions WHERE id = ?;",
                stmt -> stmt.setString(1, id),
                rs -> rs.next() ? rs.getString("appealStatus") : null
        );

        if (status == null) {
            return null;
        }

        try {
            return AppealStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    public static Date getAppealDelay(String id) {
        if (!getAppealStatus(id).equals(AppealStatus.DELAYED)) return null;
        Long datelong = query("SELECT appealDelay FROM sanctions WHERE id = ?;",
                stmt -> {
            stmt.setString(1, id);
                },
                rs -> {if (!rs.next()) {
                    return null;
                }
                return rs.getLong("appealDelay");
        });
        if (datelong == null) return null;
        return new Date(datelong);
    }

    public static boolean changeAppealStatus(String id, AppealStatus status) {
        return update("UPDATE sanctions SET status = ? WHERE id = ?;",
                stmt -> {
            stmt.setString(1, status.toString());
            stmt.setString(2, id);
                });
    }

    @Nullable
    public static Map<UUID, SanctionData> getSanctionData(String id, String ign) {
        if (!isPlayerCurrentlySanctioned(id, ign)) {
            return null;
        }
        Map<String, Object> map = query("SELECT * FROM sanctions WHERE id = ? AND ign = ?;",
                stmt -> {
            stmt.setString(1, id);
            stmt.setString(2, ign);
                },
                rs -> {
            if (!rs.next()) {
                return null;
            }
            Map<String, Object> data = new HashMap<>();
            data.put("type", rs.getString("type"));
            data.put("reason", rs.getString("reason"));
            data.put("date", rs.getLong("date"));
            data.put("expires", Sanction.valueOf(rs.getString("expires")));
            data.put("uuid", rs.getString("uuid"));
            return data;
        });
        if (map == null) return null;
        Map<UUID, SanctionData> dataMap = new HashMap<>();
        dataMap.put(UUID.fromString((String) map.get("uuid")), new SanctionData((Sanction) map.get("type"), (String) map.get("reason"), new Date((long) map.get("date")), (Date) map.get("expires")));
        return dataMap;
    }

    @Nullable
    public static Map<UUID, SanctionData> getSanctionData(String id) {
        Map<String, Object> map = query("SELECT * FROM sanctions WHERE id = ?;",
                stmt -> {
                    stmt.setString(1, id);
                },
                rs -> {
                    if (!rs.next()) {
                        return null;
                    }
                    Map<String, Object> data = new HashMap<>();
                    data.put("type", rs.getString("type"));
                    data.put("reason", rs.getString("reason"));
                    data.put("date", rs.getLong("date"));
                    data.put("expires", Sanction.valueOf(rs.getString("expires")));
                    data.put("uuid", rs.getString("uuid"));
                    return data;
                });
        if (map == null) return null;
        Map<UUID, SanctionData> dataMap = new HashMap<>();
        dataMap.put(UUID.fromString((String) map.get("uuid")), new SanctionData((Sanction) map.get("type"), (String) map.get("reason"), new Date((long) map.get("date")), (Date) map.get("expires")));
        return dataMap;
    }
}
