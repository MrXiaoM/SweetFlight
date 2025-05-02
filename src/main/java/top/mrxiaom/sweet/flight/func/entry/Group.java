package top.mrxiaom.sweet.flight.func.entry;

public class Group {
    public enum Mode {
        SET, ADD
    }
    private final int priority;
    private final String name;
    private final int timeSecond;
    private final Mode mode;

    public Group(int priority, String name, int timeSecond, Mode mode) {
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

    public Mode getMode() {
        return mode;
    }
}
