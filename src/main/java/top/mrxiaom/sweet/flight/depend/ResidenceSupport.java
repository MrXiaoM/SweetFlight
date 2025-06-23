package top.mrxiaom.sweet.flight.depend;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.flight.SweetFlight;
import top.mrxiaom.sweet.flight.func.AbstractModule;
import top.mrxiaom.sweet.flight.func.FlightManager;

@AutoRegister(requirePlugins = "Residence", priority = 1001)
public class ResidenceSupport extends AbstractModule {
    public ResidenceSupport(SweetFlight plugin) {
        super(plugin);
        FlightManager manager = FlightManager.inst();
        manager.registerChecker((player, loc) -> {
            ClaimedResidence res = ResidenceApi.getResidenceManager().getByLoc(loc);
            if (res != null) {
                ResidencePermissions perm = res.getPermissions();
                boolean globalNoFly = perm.has(Flags.nofly, false);
                return !perm.playerHas(player, Flags.nofly, globalNoFly);
            }
            return true;
        });
    }
}
