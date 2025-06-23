package top.mrxiaom.sweet.flight.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface IFlyChecker {
    /**
     * 优先级，默认为 1000，数字越小越先检查
     */
    default int priority() {
        return 1000;
    }

    /**
     * 玩家是否可以在指定的位置飞行
     * @param player 玩家
     * @param loc 指定的位置
     */
    boolean canPlayerFlyAt(@NotNull Player player, @NotNull Location loc);

    /**
     * 玩家是否可以在指定的位置无限飞行
     * @param player 玩家
     * @param loc 指定的位置
     */
    default boolean canInfiniteFly(@NotNull Player player, @NotNull Location loc) {
        return false;
    }
}
