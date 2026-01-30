package top.mrxiaom.sweet.flight.commands;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FakeToggleFlightEvent extends PlayerToggleFlightEvent {
    private final Logger logger;
    public FakeToggleFlightEvent(@NonNull Player player, boolean isFlying, Logger logger) {
        super(player, isFlying);
        this.logger = logger;
    }

    @Override
    public void setCancelled(boolean cancel) {
        super.setCancelled(cancel);
        logger.log(Level.WARNING, "发现调用 setCancelled(" + cancel + ")", new RuntimeException("调用堆栈如下"));
    }
}
