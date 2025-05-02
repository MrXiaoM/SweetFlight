package top.mrxiaom.sweet.flight;
        
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.pluginbase.utils.scheduler.FoliaLibScheduler;
import top.mrxiaom.sweet.flight.database.FlightDatabase;

public class SweetFlight extends BukkitPlugin {
    public static SweetFlight getInstance() {
        return (SweetFlight) BukkitPlugin.getInstance();
    }

    public SweetFlight() {
        super(options()
                .bungee(false)
                .adventure(true)
                .database(true)
                .reconnectDatabaseWhenReloadConfig(false)
                .scanIgnore("top.mrxiaom.sweet.flight.libs")
        );
        this.scheduler = new FoliaLibScheduler(this);
    }
    private boolean onlineMode;
    private FlightDatabase flightDatabase;
    public FlightDatabase getFlightDatabase() {
        return flightDatabase;
    }

    public boolean isOnlineMode() {
        return onlineMode;
    }

    @Override
    protected void beforeEnable() {
        options.registerDatabase(
                flightDatabase = new FlightDatabase(this)
        );
        LanguageManager.inst()
                .setLangFile("messages.yml")
                .register(Messages.class, Messages::holder);
    }

    @Override
    protected void beforeReloadConfig(FileConfiguration config) {
        String online = config.getString("online-mode", "auto").toLowerCase();
        switch (online) {
            case "true":
                onlineMode = true;
                break;
            case "false":
                onlineMode = false;
                break;
            case "auto":
            default:
                onlineMode = Bukkit.getOnlineMode();
                break;
        }
    }

    @Override
    protected void afterEnable() {
        if (PAPI.isEnabled()) {
            new Placeholders(this).register();
        }
        getLogger().info("SweetFlight 加载完毕");
    }

    public String key(Player player) {
        if (isOnlineMode()) {
            return player.getUniqueId().toString();
        } else {
            return player.getName();
        }
    }
}
