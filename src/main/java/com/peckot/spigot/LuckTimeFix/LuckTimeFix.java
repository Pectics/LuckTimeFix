package com.peckot.spigot.LuckTimeFix;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LuckTimeFix extends JavaPlugin implements CommandExecutor {

    private static LuckPerms api;
    private static boolean allowNegativeTime;
    private static Message message;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) api = provider.getProvider();
        getCommand("lucktime").setExecutor(this);
        message = new Message(this.getConfig());
        allowNegativeTime = getConfig().getBoolean("allowNegativeTime");
        getLogger().info("Plugin LuckTimeFix Enabled!");
        getLogger().info("Made by Pectics. https://peckot.com");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin LuckTimeFix Disabled! Goodbye!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("lucktime")) return true;
        if (!sender.hasPermission("lucktimefix.admin")) {
            sender.sendMessage(message.NO_PERMISSION);
            return true;
        }
        if (args.length <= 3) {
            if (args.length == 0) {
                sender.sendMessage(message.HELP);
                return true;
            }
            if (args.length == 1) {
                switch (args[0]) {
                    case "help":
                        sender.sendMessage(message.HELP);
                        return true;
                    case "reload":
                        reloadConfig();
                        sender.sendMessage(message.RELOADED);
                        return true;
                    default:
                        sender.sendMessage(message.ARGUMENT_ERROR);
                        return true;
                }
            } else {
                sender.sendMessage(message.ARGUMENT_ERROR);
                return true;
            }
        } else {
            OfflinePlayer player = Arrays.stream(Bukkit.getOfflinePlayers()).filter(p -> p.getName().equals(args[0])).findFirst().orElse(null);
            if (player == null) {
                sender.sendMessage(message.PLAYER_NOT_FOUND.replaceAll("\\{player}", args[0]));
                return true;
            }
            User user = api.getUserManager().loadUser(player.getUniqueId()).join();
            boolean isAdd;
            switch (args[3]) {
                case "add":
                    isAdd = true;
                    break;
                case "remove":
                    isAdd = false;
                    break;
                default:
                    sender.sendMessage(message.INVALID_OPERATION);
                    return true;
            }
            switch (args[1]) {
                case "perm":
                    long permissionTime;
                    PermissionNode pastPermissionNode = user.getNodes(NodeType.PERMISSION).stream()
                            .filter(g -> g.getKey().equalsIgnoreCase(args[2]))
                            .findFirst().orElse(null);
                    PermissionNode newPermissionNode;
                    if (null == pastPermissionNode || pastPermissionNode.hasExpired()) {
                        permissionTime = System.currentTimeMillis() / 1000 + parseTimePeriod(args[4]);
                        newPermissionNode = PermissionNode.builder(args[2]).expiry(permissionTime).build();
                    } else {
                        if (!pastPermissionNode.hasExpiry() || null == pastPermissionNode.getExpiry()) {
                            sender.sendMessage(message.PERMANENT.replaceAll("\\{player}", player.getName()).replaceAll("\\{arg}", pastPermissionNode.getKey()));
                            return true;
                        }
                        if (isAdd) {
                            permissionTime = pastPermissionNode.getExpiry().getEpochSecond() + parseTimePeriod(args[4]);
                        } else {
                            if (allowNegativeTime) if (parseTimePeriod(args[4]) > pastPermissionNode.getExpiry().getEpochSecond() - System.currentTimeMillis() / 1000) {
                                sender.sendMessage(message.INVALID_TIME);
                                return true;
                            }
                            permissionTime = pastPermissionNode.getExpiry().getEpochSecond() - parseTimePeriod(args[4]);
                        }
                        newPermissionNode = pastPermissionNode.toBuilder().expiry(permissionTime).build();
                        user.data().remove(pastPermissionNode);
                    }
                    user.data().add(newPermissionNode);
                    api.getUserManager().saveUser(user);
                    if (isAdd) sender.sendMessage(message.ADDED
                            .replaceAll("\\{player}", player.getName())
                            .replaceAll("\\{arg}", newPermissionNode.getPermission())
                            .replaceAll("\\{time}", args[4])
                    );
                    else sender.sendMessage(message.REMOVED
                            .replaceAll("\\{player}", player.getName())
                            .replaceAll("\\{arg}", newPermissionNode.getPermission())
                            .replaceAll("\\{time}", args[4])
                    );
                    break;
                case "group":
                    Group group = api.getGroupManager().getGroup(args[2]);
                    if (group == null) {
                        sender.sendMessage(message.GROUP_NOT_FOUND.replaceAll("\\{group}", args[2]));
                        return true;
                    }
                    long groupTime;
                    InheritanceNode pastGroupNode = user.getNodes(NodeType.INHERITANCE).stream()
                            .map(NodeType.INHERITANCE::cast)
                            .filter(g -> g.getGroupName().equalsIgnoreCase(args[2]))
                            .findFirst().orElse(null);
                    InheritanceNode newGroupNode;
                    if (null == pastGroupNode || pastGroupNode.hasExpired()) {
                        groupTime = System.currentTimeMillis() / 1000 + parseTimePeriod(args[4]);
                        newGroupNode = InheritanceNode.builder(group.getName()).expiry(groupTime).value(true).build();
                    } else {
                        if (!pastGroupNode.hasExpiry() || null == pastGroupNode.getExpiry()) {
                            sender.sendMessage(message.PERMANENT.replaceAll("\\{player}", player.getName()).replaceAll("\\{arg}", pastGroupNode.getKey()));
                            return true;
                        }
                        if (isAdd) {
                            groupTime = pastGroupNode.getExpiry().getEpochSecond() + parseTimePeriod(args[4]);
                        } else {
                            if (allowNegativeTime) if (parseTimePeriod(args[4]) > pastGroupNode.getExpiry().getEpochSecond() - System.currentTimeMillis() / 1000) {
                                sender.sendMessage(message.INVALID_TIME);
                                return true;
                            }
                            groupTime = pastGroupNode.getExpiry().getEpochSecond() - parseTimePeriod(args[4]);
                        }
                        newGroupNode = pastGroupNode.toBuilder().expiry(groupTime).value(true).build();
                        user.data().remove(pastGroupNode);
                    }
                    user.data().add(newGroupNode);
                    api.getUserManager().saveUser(user);
                    if (isAdd) sender.sendMessage(message.ADDED
                            .replaceAll("\\{player}", player.getName())
                            .replaceAll("\\{arg}", newGroupNode.getKey().replaceFirst("group.", ""))
                            .replaceAll("\\{time}", args[4])
                    );
                    else sender.sendMessage(message.REMOVED
                            .replaceAll("\\{player}", player.getName())
                            .replaceAll("\\{arg}", newGroupNode.getKey().replaceFirst("group.", ""))
                            .replaceAll("\\{time}", args[4])
                    );
                    break;
            }
        }
        return true;
    }

    /**
     * parse 1y2mo3w4d5h6m7s to seconds
     * @param timeString string to parse
     * @return long seconds
     */
    public static long parseTimePeriod(String timeString) {
        Pattern relativeDatePattern = Pattern
                .compile("(\\d+(?:[.,]\\d+)?)((mo)|[smhdwy])");
        Matcher relativeDateMatcher = relativeDatePattern.matcher(timeString);
        relativeDateMatcher.reset();
        long periodSeconds = 0;
        while (relativeDateMatcher.find()) {
            double time = Double.parseDouble(relativeDateMatcher.group(1));
            String unitStr = relativeDateMatcher.group(2).toLowerCase();
            int unitMultiplier = 0;
            if (unitStr.startsWith("mo")) {
                unitMultiplier = 30 * 24 * 60 * 60;
            } else if (unitStr.startsWith("s")) {
                unitMultiplier = 1;
            } else if (unitStr.startsWith("m")) {
                unitMultiplier = 60;
            } else if (unitStr.startsWith("h")) {
                unitMultiplier = 60 * 60;
            } else if (unitStr.startsWith("d")) {
                unitMultiplier = 24 * 60 * 60;
            } else if (unitStr.startsWith("w")) {
                unitMultiplier = 7 * 24 * 60 * 60;
            } else if (unitStr.startsWith("y")) {
                unitMultiplier = 365 * 24 * 60 * 60;
            }
            periodSeconds += (long) (time * unitMultiplier);
        }
        return periodSeconds;
    }

}
