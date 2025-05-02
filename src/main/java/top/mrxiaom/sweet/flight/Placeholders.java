package top.mrxiaom.sweet.flight;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.flight.func.FlightManager;
import top.mrxiaom.sweet.flight.func.GroupManager;
import top.mrxiaom.sweet.flight.func.entry.PlayerData;

public class Placeholders extends PlaceholderExpansion {
    public final SweetFlight plugin;

    public Placeholders(SweetFlight plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean register() {
        try {
            unregister();
        } catch (Throwable ignored) {}
        return super.register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return plugin.getDescription().getName().toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equalsIgnoreCase("standard_time")) {
            int standard = GroupManager.inst().getFlightSeconds(player);
            return FlightManager.inst().formatTime(standard);
        }
        if (params.equalsIgnoreCase("standard_time_seconds")) {
            return String.valueOf(GroupManager.inst().getFlightSeconds(player));
        }
        if (params.equalsIgnoreCase("extra_time")) {
            FlightManager manager = FlightManager.inst();
            PlayerData data = manager.get(player);
            if (data == null) return "0s";
            return manager.formatTime(data.extra);
        }
        if (params.equalsIgnoreCase("status_time")) {
            FlightManager manager = FlightManager.inst();
            PlayerData data = manager.get(player);
            if (data == null) return "0s";
            return manager.formatTime(data.status);
        }
        if (params.equalsIgnoreCase("time")) {
            FlightManager manager = FlightManager.inst();
            PlayerData data = manager.get(player);
            if (data == null) return "0s";
            return manager.formatTime(data.status + data.extra);
        }
        if (params.equalsIgnoreCase("extra_time_seconds")) {
            FlightManager manager = FlightManager.inst();
            PlayerData data = manager.get(player);
            if (data == null) return "0";
            return String.valueOf(data.extra);
        }
        if (params.equalsIgnoreCase("status_time_seconds")) {
            FlightManager manager = FlightManager.inst();
            PlayerData data = manager.get(player);
            if (data == null) return "0";
            return String.valueOf(data.status);
        }
        if (params.equalsIgnoreCase("time_seconds")) {
            FlightManager manager = FlightManager.inst();
            PlayerData data = manager.get(player);
            if (data == null) return "0s";
            return String.valueOf(data.status + data.extra);
        }
        return super.onPlaceholderRequest(player, params);
    }
}
