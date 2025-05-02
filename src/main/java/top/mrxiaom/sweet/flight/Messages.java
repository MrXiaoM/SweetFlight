package top.mrxiaom.sweet.flight;

import top.mrxiaom.pluginbase.func.language.IHolderAccessor;
import top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder;

import java.util.List;

import static top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder.wrap;

public enum Messages implements IHolderAccessor {
    player__not_online("&e玩家不在线 &7(或不存在)"),
    player__only("&e这个命令只能由玩家执行"),
    player__data_not_found("&e玩家数据异常"),
    player__data_invalid_start("&c数据异常，请联系服务器管理员"),
    no_permission("&c你没有执行此操作的权限"),
    command__set__not_time("&e请输入大于等于0秒的时间"),
    command__set__success("&a已设置玩家&e %player% &a的额外飞行时间为&e %time%"),
    command__add__not_time("&e请输入大于0秒的时间"),
    command__add__success("&a已为玩家&e %player% &a增加&e %added% &a的额外飞行时间，增加后为&e %time%"),
    command__reset__success("&a已重置玩家&e %player% &a的今日飞行时间为&e %time%"),
    command__reload__config("&a配置文件已重载"),
    command__reload__database("&a已重新连接数据库"),
    command__on__success("&f飞行已&a开启"),
    command__off__success("&f飞行已&e关闭"),
    time_not_enough__join("&e飞行时间已耗尽"),
    time_not_enough__start("&e飞行时间已耗尽"),
    time_not_enough__timer("&e飞行时间已耗尽"),
    time_not_enough__command("&e飞行时间已耗尽"),

    help__normal("&9&lSweetFlight &e&l帮助命令&r",
            "&f/sf on &7开启飞行",
            "&f/sf off &7关闭飞行"),
    help__admin("&9&lSweetFlight &e&l帮助命令&r",
            "&f/sf on [玩家] &7开启飞行",
            "&f/sf off [玩家] &7关闭飞行",
            "&f/sf reset <玩家> &7重置玩家今日的基础飞行时间",
            "&f/sf set <玩家> <时间> &7设置玩家的额外飞行时间",
            "&f/sf add <玩家> <时间> &7为玩家增加额外飞行时间"),

    ;
    Messages(String defaultValue) {
        holder = wrap(this, defaultValue);
    }
    Messages(String... defaultValue) {
        holder = wrap(this, defaultValue);
    }
    Messages(List<String> defaultValue) {
        holder = wrap(this, defaultValue);
    }
    private final LanguageEnumAutoHolder<Messages> holder;
    public LanguageEnumAutoHolder<Messages> holder() {
        return holder;
    }
}
