package top.mrxiaom.sweet.flight.depend;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.flight.SweetFlight;
import top.mrxiaom.sweet.flight.api.IFlyChecker;
import top.mrxiaom.sweet.flight.func.AbstractModule;
import top.mrxiaom.sweet.flight.func.FlightManager;

@AutoRegister(requirePlugins = "Residence", priority = 1001)
public class ResidenceSupport extends AbstractModule implements IFlyChecker {
    public ResidenceSupport(SweetFlight plugin) {
        super(plugin);
        FlightManager.inst().registerChecker(this);
    }

    @Override
    public boolean canPlayerFlyAt(@NotNull Player player, @NotNull Location loc) {
        if (player.hasPermission("residence.bypass.nofly")) return true;
        ClaimedResidence res = ResidenceApi.getResidenceManager().getByLoc(loc);
        if (res != null) {
            ResidencePermissions perm = res.getPermissions();
            boolean globalNoFly = perm.has(Flags.nofly, false);
            return !perm.playerHas(player, Flags.nofly, globalNoFly);
        }
        return true;
    }

    @Override
    public boolean canInfiniteFly(@NotNull Player player, @NotNull Location loc) {
        ClaimedResidence res = ResidenceApi.getResidenceManager().getByLoc(loc);
        if (res != null) {
            ResidencePermissions perm = res.getPermissions();
            boolean globalNoFly = perm.has(Flags.fly, false);
            return perm.playerHas(player, Flags.fly, globalNoFly);
        }
        return false;
    }
}
