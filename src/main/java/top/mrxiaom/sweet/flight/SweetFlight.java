package top.mrxiaom.sweet.flight;
        
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.resolver.DefaultLibraryResolver;
import top.mrxiaom.pluginbase.utils.ClassLoaderWrapper;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.pluginbase.utils.scheduler.FoliaLibScheduler;
import top.mrxiaom.sweet.flight.database.FlightDatabase;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class SweetFlight extends BukkitPlugin {
    public static SweetFlight getInstance() {
        return (SweetFlight) BukkitPlugin.getInstance();
    }

    public SweetFlight() throws Exception {
        super(options()
                .bungee(false)
                .adventure(true)
                .database(true)
                .reconnectDatabaseWhenReloadConfig(false)
                .scanIgnore("top.mrxiaom.sweet.flight.libs")
        );
        this.scheduler = new FoliaLibScheduler(this);

        info("正在检查依赖库状态");
        File librariesDir = ClassLoaderWrapper.isSupportLibraryLoader
                ? new File("libraries")
                : new File(this.getDataFolder(), "libraries");
        DefaultLibraryResolver resolver = new DefaultLibraryResolver(getLogger(), librariesDir);

        resolver.addResolvedLibrary(BuildConstants.RESOLVED_LIBRARIES);

        List<URL> libraries = resolver.doResolve();
        info("正在添加 " + libraries.size() + " 个依赖库到类加载器");
        for (URL library : libraries) {
            this.classLoader.addURL(library);
        }
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
