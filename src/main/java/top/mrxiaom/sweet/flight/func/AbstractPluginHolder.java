package top.mrxiaom.sweet.flight.func;
        
import top.mrxiaom.sweet.flight.SweetFlight;

@SuppressWarnings({"unused"})
public abstract class AbstractPluginHolder extends top.mrxiaom.pluginbase.func.AbstractPluginHolder<SweetFlight> {
    public AbstractPluginHolder(SweetFlight plugin) {
        super(plugin);
    }

    public AbstractPluginHolder(SweetFlight plugin, boolean register) {
        super(plugin, register);
    }
}
