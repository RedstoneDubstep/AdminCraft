package fr.liveinground.admin_craft.logging;

import fr.liveinground.admin_craft.AdminCraft;
import fr.liveinground.admin_craft.logging.objects.InteractionType;
import fr.liveinground.admin_craft.logging.objects.LogEntry;
import fr.liveinground.admin_craft.storage.PlayerDataManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoggingDatabase {
    private static final String URL = "jdbc:sqlite:" + PlayerDataManager.ROOT + "world_logger.db";
    private static final ExecutorService DATABASE_EXECUTOR =
            Executors.newSingleThreadExecutor();

    public static void start() {
        try (Connection connection = DriverManager.getConnection(URL)) {
            if (connection != null) {
                initDatabase();
                AdminCraft.LOGGER.info("Connected to logger's database");
            }
        } catch (SQLException e) {
            AdminCraft.LOGGER.warn("An issue occurred while trying to start the world logger: ", e);
        }
    }

    private static void initDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS world_changes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "type STRING," +
                "world STRING" +
                "x INTEGER," +
                "y INTEGER" +
                "z INTEGER," +
                "material STRING," +
                "timestamp INTEGER," +
                "rolledback BOOLEAN)";

        asyncPostCommandToDB(sql);
    }

    private static Future<?> queryDB(@NotNull String command) {
        return getDatabaseExecutor().submit(() -> {
            try (Connection conn = DriverManager.getConnection(URL);
                 PreparedStatement stmt = conn.prepareStatement(command)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    ArrayList<LogEntry> output = new ArrayList<>();
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        InteractionType type = InteractionType.valueOf(rs.getString("type"));
                        String world = rs.getString("world");
                        int x = rs.getInt("x");
                        int y = rs.getInt("y");
                        int z = rs.getInt("z");
                        Block material = ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(rs.getString("material")));

                    }
                } catch (SQLException e) {
                    AdminCraft.LOGGER.error("An issue occurred while getting database response: ", e);
                }
            } catch (SQLException e) {
                AdminCraft.LOGGER.error("An issue occurred while connecting to the database: ", e);
            }
        });
    }

    private static Future<?> queryDB(@NotNull String command, String[] args) {
        return getDatabaseExecutor().submit(() -> {
            try (Connection conn = DriverManager.getConnection(URL);
                 PreparedStatement stmt = conn.prepareStatement(command)) {
                if (args != null) {
                    for (int i = 0; i < args.length; i++) {
                        stmt.setString(i + 1, args[i]);
                    }
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                    }
                } catch (SQLException e) {
                    AdminCraft.LOGGER.error("An issue occurred while getting database response: ", e);
                }
            } catch (SQLException e) {
                AdminCraft.LOGGER.error("An issue occurred while connecting to the database: ", e);
            }
        });
    }

    private static Future<Integer> asyncPostCommandToDB(@NotNull String command, @Nullable String[] args) {
        return getDatabaseExecutor().submit(() -> {
            try (Connection conn = DriverManager.getConnection(URL);
                 PreparedStatement stmt = conn.prepareStatement(command)) {

                if (args != null) {
                    for (int i = 0; i < args.length; i++) {
                        stmt.setString(i + 1, args[i]);
                    }
                }

                return stmt.executeUpdate();

            } catch (SQLException e) {
                AdminCraft.LOGGER.error("Failed to run database command: ", e);
                return -1;
            }
        });
    }

    private static Future<Integer> asyncPostCommandToDB(@NotNull String command) {
        return getDatabaseExecutor().submit(() -> {
            try (Connection conn = DriverManager.getConnection(URL);
                 PreparedStatement stmt = conn.prepareStatement(command)) {

                return stmt.executeUpdate();
            } catch (SQLException e) {
                AdminCraft.LOGGER.error("Failed to run database command: ", e);
                return -1;
            }
        });
    }

    public int rollbackWorld(BlockPos rollbackOrigin, Date from, int radius, @Nullable String playerUUID, @Nullable InteractionType action, @Nullable Block material) {
        //todo


        return 0;
    }

    public int restaureWorld(BlockPos rollbackOrigin, Date from, int radius, @Nullable String playerUUID, @Nullable InteractionType action, @Nullable Block material) {
        //todo


        return 0;
    }

    public boolean logAction(BlockPos location, Block material, String playerUUID, InteractionType action) {
        //todo
        return false;
    }

    private static ExecutorService getDatabaseExecutor() {
        return DATABASE_EXECUTOR;
    }

    public static void shutdown() {
        DATABASE_EXECUTOR.shutdown();
    }
}