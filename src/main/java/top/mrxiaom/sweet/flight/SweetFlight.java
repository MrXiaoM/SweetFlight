package top.mrxiaom.sweet.flight;
        
import top.mrxiaom.pluginbase.BukkitPlugin;

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
        // this.scheduler = new FoliaLibScheduler(this);
    }

    @Override
    protected void beforeEnable() {
        options.registerDatabase(
                // 在这里添加数据库 (如果需要的话)
        );
    }

    @Override
    protected void afterEnable() {
        getLogger().info("SweetFlight 加载完毕");
    }
}
