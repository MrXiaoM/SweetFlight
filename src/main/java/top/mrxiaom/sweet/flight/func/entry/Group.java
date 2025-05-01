package top.mrxiaom.sweet.flight.func.entry;

public class Group {
    private final int priority;
    private final String name;
    private final int timeSecond;

    public Group(int priority, String name, int timeSecond) {
        this.priority = priority;
        this.name = name;
        this.timeSecond = timeSecond;
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
}
