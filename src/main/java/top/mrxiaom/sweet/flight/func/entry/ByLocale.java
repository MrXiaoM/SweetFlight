package top.mrxiaom.sweet.flight.func.entry;

public class ByLocale {
    private final String name;
    private final int timeSecond;
    private final EnumMode mode;

    public ByLocale(String name, int timeSecond, EnumMode mode) {
        this.name = name;
        this.timeSecond = timeSecond;
        this.mode = mode;
    }

    public String getName() {
        return name;
    }

    public int getTimeSecond() {
        return timeSecond;
    }

    public EnumMode getTimeMode() {
        return mode;
    }
}
