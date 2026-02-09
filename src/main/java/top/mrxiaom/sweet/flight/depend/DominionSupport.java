package top.mrxiaom.sweet.flight.depend;

import cn.lunadeer.dominion.api.DominionAPI;
import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.MemberDTO;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.flight.SweetFlight;
import top.mrxiaom.sweet.flight.api.IFlyChecker;
import top.mrxiaom.sweet.flight.func.AbstractModule;
import top.mrxiaom.sweet.flight.func.FlightManager;

import java.util.stream.Collectors;
/**
 * Dominion 插件的飞行支持模块。
 * <p>
 * 该模块负责与 Dominion 领地插件集成，根据领地内的飞行权限设置
 * 来判断玩家是否可以飞行。
 */
@AutoRegister(requirePlugins = "Dominion", priority = 1001)
public class DominionSupport extends AbstractModule implements IFlyChecker {

    private final DominionAPI dominionAPI;

    public DominionSupport(SweetFlight plugin) {
        super(plugin);
        this.dominionAPI = DominionAPI.getInstance();
        FlightManager.inst().registerChecker(this);
    }

    /**
     * 检查玩家是否可以在指定位置飞行。
     * <p>
     * 逻辑：
     * 1. 如果玩家有 Dominion 的管理绕过权限，则允许飞行。
     * 2. 如果玩家不在任何领地内（在野外），则允许飞行。
     * 3. 如果玩家在领地内，则使用 API 检查该玩家是否拥有 "fly" 权限。
     *
     * @param player 要检查的玩家
     * @param loc    要检查的位置
     * @return 如果玩家可以飞行，则返回 true
     */
    @Override
    public boolean canPlayerFlyAt(@NotNull Player player, @NotNull Location loc) {
        if (player.hasPermission("dominion.admin.bypass")) {
            return true;
        }

        if (dominionAPI.getDominion(loc) == null) {
            return true;
        }

        return dominionAPI.checkPrivilegeFlagSilence(loc, Flags.FLY, player);
    }

    @Override
    public boolean canInfiniteFly(@NotNull Player player, @NotNull Location loc) {
        if (!FlightManager.inst().getDominionInfiniteFly()) return false;
        DominionDTO dominion = dominionAPI.getDominion(loc);
        if (dominion != null) {
            // 检查玩家是否是领地成员或者是领地主人，如果是，返回 true
            return dominion.getMembers().stream().map(MemberDTO::getPlayerUUID).collect(Collectors.toList()).contains(player.getUniqueId()) ||
                    dominion.getOwner().equals(player.getUniqueId());
        }
        return false;
    }
}