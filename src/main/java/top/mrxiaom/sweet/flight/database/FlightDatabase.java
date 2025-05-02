package top.mrxiaom.sweet.flight.database;

import com.zaxxer.hikari.pool.HikariProxyCallableStatement;
import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.database.IDatabase;
import top.mrxiaom.sweet.flight.SweetFlight;
import top.mrxiaom.sweet.flight.func.AbstractPluginHolder;
import top.mrxiaom.sweet.flight.func.entry.PlayerData;

import java.sql.*;
import java.time.LocalDateTime;

public class FlightDatabase extends AbstractPluginHolder implements IDatabase {
    private String STATUS_TABLE_NAME;
    private String EXTRA_TABLE_NAME;
    public FlightDatabase(SweetFlight plugin) {
        super(plugin);
    }
    @Override
    public void reload(Connection conn, String prefix) throws SQLException {
        STATUS_TABLE_NAME = (prefix + "status").toUpperCase();
        EXTRA_TABLE_NAME = (prefix + "extra").toUpperCase();
        try (PreparedStatement ps = conn.prepareStatement(
                "CREATE TABLE if NOT EXISTS `" + STATUS_TABLE_NAME + "`(" +
                        "`player` varchar(48) PRIMARY KEY," +
                        "`seconds` int," +
                        "`outdate` timestamp" +
                        ");"
        )) {
            ps.execute();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "CREATE TABLE if NOT EXISTS `" + EXTRA_TABLE_NAME + "`(" +
                        "`player` varchar(48) PRIMARY KEY," +
                        "`seconds` int" +
                        ");"
        )) {
            ps.execute();
        }
    }

    public Integer getPlayerStatus(Player player) {
        try (Connection conn = plugin.getConnection()) {
            return getPlayerStatus(conn, plugin.key(player));
        } catch (SQLException e) {
            warn(e);
        }
        return null;
    }
    public Integer getPlayerStatus(Connection conn, String id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM `" + STATUS_TABLE_NAME + "` WHERE `player`=?;"
        )) {
            ps.setString(1, id);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                    Timestamp outdate = resultSet.getTimestamp("outdate");
                    if (now.after(outdate)) {
                        return null;
                    }
                    return resultSet.getInt("seconds");
                }
            }
        }
        return null;
    }
    public void setPlayerStatus(Player player, int value, LocalDateTime nextOutdate) {
        try (Connection conn = plugin.getConnection()) {
            setPlayerStatus(conn, plugin.key(player), value, nextOutdate);
        } catch (SQLException e) {
            warn(e);
        }
    }
    private void setPlayerStatus(Connection conn, String id, int value, LocalDateTime nextOutdate) throws SQLException {
        String sentence;
        boolean mysql = plugin.options.database().isMySQL();
        if (mysql) {
            sentence = "INSERT INTO `" + STATUS_TABLE_NAME + "`(`player`,`seconds`,`outdate`) VALUES(?, ?, ?) on duplicate key update `seconds`=?, `outdate`=?;";
        } else {
            sentence = "INSERT OR REPLACE INTO `" + STATUS_TABLE_NAME + "`(`player`,`seconds`,`outdate`) VALUES(?, ?, ?);";
        }
        try (PreparedStatement ps = conn.prepareStatement(sentence)) {
            Timestamp outdate = Timestamp.valueOf(nextOutdate);
            ps.setString(1, id);
            ps.setInt(2, value);
            ps.setTimestamp(3, outdate);
            if (mysql) {
                ps.setInt(4, value);
                ps.setTimestamp(5, outdate);
            }
            ps.execute();
        }
    }

    public int getPlayerExtra(Player player) {
        try (Connection conn = plugin.getConnection()) {
            return getPlayerExtra(conn, plugin.key(player));
        } catch (SQLException e) {
            warn(e);
        }
        return 0;
    }
    public int getPlayerExtra(Connection conn, String id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM `" + EXTRA_TABLE_NAME + "` WHERE `player`=?;"
        )) {
            ps.setString(1, id);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("seconds");
                }
            }
        }
        return 0;
    }
    public void setPlayerExtra(Player player, int value) {
        try (Connection conn = plugin.getConnection()) {
            setPlayerExtra(conn, plugin.key(player), value);
        } catch (SQLException e) {
            warn(e);
        }
    }
    private void setPlayerExtra(Connection conn, String id, int value) throws SQLException {
        String sentence;
        boolean mysql = plugin.options.database().isMySQL();
        if (mysql) {
            sentence = "INSERT INTO `" + EXTRA_TABLE_NAME + "`(`player`,`seconds`) VALUES(?, ?) on duplicate key update `seconds`=?;";
        } else {
            sentence = "INSERT OR REPLACE INTO `" + EXTRA_TABLE_NAME + "`(`player`,`seconds`) VALUES(?, ?);";
        }
        try (PreparedStatement ps = conn.prepareStatement(sentence)) {
            ps.setString(1, id);
            ps.setInt(2, value);
            if (mysql) {
                ps.setInt(3, value);
            }
            ps.execute();
        }
    }
    public void setPlayer(Player player, int status, int extra, LocalDateTime nextOutdate) {
        try (Connection conn = plugin.getConnection()) {
            String id = plugin.key(player);
            setPlayerStatus(conn, id, status, nextOutdate);
            setPlayerExtra(conn, id, extra);
        } catch (SQLException e) {
            warn(e);
        }
    }
    public void setPlayer(PlayerData data) {
        setPlayer(data.player, data.status, data.extra, data.outdate);
    }
}
