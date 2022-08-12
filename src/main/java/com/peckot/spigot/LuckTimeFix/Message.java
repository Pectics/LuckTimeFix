package com.peckot.spigot.LuckTimeFix;

import org.bukkit.configuration.file.FileConfiguration;

public final class Message {

    public String HELP = "§cUsage: /lucktime <player> <group|perm> <groupName|permissionNode> <add|remove> <time>";

    public String PLAYER_NOT_FOUND = "§cPlayer not found";
    public String GROUP_NOT_FOUND = "§cGroup not found";

    public String PERMANENT = "§cPlayer already has it permanently";
    public String INVALID_OPERATION = "§cInvalid operation";
    public String INVALID_TIME = "§cInvalid time";

    public String ADDED = "§aSuccess";
    public String REMOVED = "§aSuccess";

    public String ARGUMENT_ERROR = "§cArgument error";
    public String RELOADED = "§aSuccess";
    public String NO_PERMISSION = "§cYou don't have permission";

    public Message(FileConfiguration config) {
        HELP = config.getString("messages.help", HELP).replaceAll("&([a-z])", "\u00A7$1");
        PLAYER_NOT_FOUND = config.getString("messages.playerNotFound", PLAYER_NOT_FOUND).replaceAll("&([a-z])", "\u00A7$1");
        GROUP_NOT_FOUND = config.getString("messages.groupNotFound", GROUP_NOT_FOUND).replaceAll("&([a-z])", "\u00A7$1");
        PERMANENT = config.getString("messages.permanent", PERMANENT).replaceAll("&([a-z])", "\u00A7$1");
        INVALID_OPERATION = config.getString("messages.invalidOperation", INVALID_OPERATION).replaceAll("&([a-z])", "\u00A7$1");
        INVALID_TIME = config.getString("messages.invalidTime", INVALID_TIME).replaceAll("&([a-z])", "\u00A7$1");
        ADDED = config.getString("messages.added", ADDED).replaceAll("&([a-z])", "\u00A7$1");
        REMOVED = config.getString("messages.removed", REMOVED).replaceAll("&([a-z])", "\u00A7$1");
        ARGUMENT_ERROR = config.getString("messages.argumentError", ARGUMENT_ERROR).replaceAll("&([a-z])", "\u00A7$1");
        RELOADED = config.getString("messages.reloaded", RELOADED).replaceAll("&([a-z])", "\u00A7$1");
        NO_PERMISSION = config.getString("messages.noPermission", NO_PERMISSION).replaceAll("&([a-z])", "\u00A7$1");
    }

}
