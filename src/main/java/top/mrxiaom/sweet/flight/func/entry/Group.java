package top.mrxiaom.sweet.flight.func.entry;

public class Group {
    @Deprecated
    public enum Mode {
        SET, ADD
    }
    private final int priority;
    private final String name;
    private final int timeSecond;
    private final EnumMode mode;

    public Group(int priority, String name, int timeSecond, EnumMode mode) {
        this.priority = priority;
        this.name = name;
        this.timeSecond = timeSecond;
        this.mode = mode;
    }

    public int priority() {
        return priority;
    }

    public String getName() {
        return name;
    }

    public int getTimeSecond() {
        return timeSecond;
    }

    @Deprecated
    public Mode getMode() {
        switch (mode) {
            case ADD:
                return Mode.ADD;
            case SET:
            default:
                return Mode.SET;
        }
    }

    public EnumMode getTimeMode() {
        return mode;
    }
}
