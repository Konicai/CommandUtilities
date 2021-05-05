package me.konicai.commandutilities;

import me.konicai.commandutilities.listeners.CommandListeners;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;

public class CommandUtilities extends JavaPlugin implements Listener, CommandExecutor {

    private static Plugin plugin;
    private Logger logger;

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();
        initializeConfig();
        getServer().getPluginManager().registerEvents(new CommandListeners(), this);
        Objects.requireNonNull(getCommand("commandutil")).setExecutor(this);
        if (!getConfig().getBoolean("console-only-commands.enable")) {
            logger.warning("Restrictions are disabled. Run \"/commandutil set enable-restrictions true\" to enable them");
        }
    }

    @Override
    public void onDisable() {
    }

    public void initializeConfig() {
        File externalConfig = new File(getDataFolder(), "config.yml");
        if (!externalConfig.exists()) {
            saveDefaultConfig();
            logger.info("A new config.yml has been created.");
            // Replace the config in memory in case it exists
            reloadConfig();
        } else {
            reloadConfig();
            if (!getConfig().isSet("enable-restrictions") || !getConfig().isBoolean("enable-restrictions")) {
                getConfig().set("enable-restrictions", true);
                logger.warning("\"enable-restrictions\" was not true or false in the config, and was forcefully set true.");
            }
            saveConfig();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    initializeConfig();
                    logger.info("The config has been reloaded.");
                } else {
                    return false;
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("get")) {
                    Object value = getConfig().get(args[1], null);
                    if (value != null) {
                        logger.info("The value of " + args[1] + " is " + value);
                    } else {
                        logger.info("That option does not exist or is not set");
                    }
                } else {
                    return false;
                }
            } else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("set")) {
                    if (getConfig().contains(args[1])) {
                        getConfig().set(args[1], args[2]);
                    } else {
                        logger.info("That option does not exist!");
                    }
                }
            }
            return true;
        } else {
            sender.sendMessage(ChatColor.YELLOW + "That command is restricted!");
            return true;
        }
    }

    public static Plugin getInstance() {
        return plugin;
    }
}
