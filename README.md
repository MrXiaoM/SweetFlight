# SweetFlight

Minecraft 限时飞行插件

## 简介

这个插件可以很好地限制玩家的每日飞行时间。
+ 你可以设置玩家的每日基础飞行时间，每天指定时间自动重置基础飞行时间。
+ 你也可以通过命令给予玩家额外飞行时间，让玩家可兑换更多的飞行时间。

处于不同飞行组 (`groups.yml`) 的玩家每日基础飞行时间不同，可设置叠加飞行时间，以默认配置为例
+ `default` 飞行组有 `5m` 飞行时间
+ `vip` 飞行组有 `+1h` 飞行时间

如果玩家同时有 `default` 和 `vip` 飞行组的权限，那么他的基础飞行时间为 `1小时5分钟`。 

在玩家飞行时，如果玩家有剩余的额外飞行时间，会优先消耗额外飞行时间，当额外飞行时间耗尽时，才会去消耗每日基础飞行时间。  
使用这个消耗机制避免玩家过度囤积额外飞行时间，当然，如果你需要，也可以到配置文件修改飞行时间的消耗优先级。

## 兼容性

目前本插件与以下插件兼容
+ [Residence](https://www.spigotmc.org/resources/11480/) -- 兼容领地权限，在有 `nofly` 权限的领地关闭飞行，有 `fly` 权限的领地开启无限飞行。

想要添加更多插件兼容吗？参考 [ResidenceSupport.java](https://github.com/MrXiaoM/SweetFlight/blob/main/src/main/java/top/mrxiaom/sweet/flight/depend/ResidenceSupport.java) 向本插件提交 Pull Request 吧！

## 命令

根命令为 `/sweetflight`，别名 `/sweetfly`、`/sflight`、`/sfly`、`/sf`  
以 `<>` 包裹的为必选参数，以 `[]` 包裹的为可选参数。

| 命令                    | 描述                   | 权限                          |
|-----------------------|----------------------|-----------------------------|
| 玩家命令                  |                      |                             |
| `/sf on`              | 开启飞行                 | 无                           |
| `/sf off`             | 关闭飞行                 | 无                           |
| `/sf check`           | 查看当前飞行组，以及可用飞行时间     | `sweet.flight.check`        |
| 管理员命令                 |                      |                             |
| `/sf on [玩家]`         | 开启自己或某人的飞行           | `sweet.flight.toggle.other` |
| `/sf off [玩家]`        | 关闭自己或某人的飞行           | `sweet.flight.toggle.other` |
| `/sf check [玩家]`      | 查看自己或某人的飞行组，以及可用飞行时间 | `sweet.flight.check.other`  |
| `/sf reset <玩家>`      | 重置玩家今日基础飞行时间         | OP/控制台                      |
| `/sf set <玩家> <时间>`   | 设置玩家的额外飞行时间          | OP/控制台                      |
| `/sf add <玩家> <时间>`   | 为玩家增加额外飞行时间          | OP/控制台                      |
| `/sf reload database` | 重新连接数据库              | OP/控制台                      |
| `/sf reload`          | 重载插件配置文件             | OP/控制台                      |

命令中提到的时间格式为 `数值+单位`，可用单位有 `h` (小时)、`m` (分钟)、`s` (秒)。示例如下
+ `1m` 一分钟
+ `2h3m` 两小时三分钟
+ `4h5m6s` 四小时五分钟六秒

## 变量

本插件向 PlaceholderAPI 注册了如下变量

```
%sweetflight_standard_time%            玩家每日的基础飞行时间 (已格式化)
%sweetflight_standard_time_seconds%    玩家每日的基础飞行时间 (秒)
%sweetflight_status_time%              玩家当前剩余的基础飞行时间 (已格式化)
%sweetflight_status_time_seconds%      玩家当前剩余的基础飞行时间 (秒)
%sweetflight_extra_time%               玩家当前剩余的额外飞行时间 (已格式化)
%sweetflight_extra_time_seconds%       玩家当前剩余的额外飞行时间 (秒)
%sweetflight_time%                     玩家当前剩余的总飞行时间 (已格式化)
%sweetflight_time_seconds%             玩家当前剩余的总飞行时间 (秒)
```

## 开发者

[![](https://jitpack.io/v/MrXiaoM/SweetFlight.svg)](https://jitpack.io/#MrXiaoM/SweetFlight)
```kotlin
repositories {
    maven("https://jitpack.io/")
}
dependencies {
    compileOnly("com.github.MrXiaoM:SweetFlight:$VERSION")
}
```
