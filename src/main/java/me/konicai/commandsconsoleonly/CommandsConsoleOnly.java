package me.konicai.commandsconsoleonly;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public class CommandsConsoleOnly extends JavaPlugin implements Listener, CommandExecutor {

    private static Logger logger;

    @Override
    public void onEnable() {
        logger = getLogger();
        initializeConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("cconly").setExecutor(this);
        if (!getConfig().getBoolean("enable-restrictions")) {
            logger.warning("Restrictions are disabled. Run \"/cconly enable\" to enable them");
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

    @EventHandler
    public void onPlayerCommandEvent(PlayerCommandPreprocessEvent event) {
        if (getConfig().getBoolean("enable-restrictions") && getConfig().isSet("console-only-commands")) {
            // Get the full command in lowercase, remove the first character, then split it in half at the first occurrence of a space. Save the first half.
            String sentCommand = event.getMessage().toLowerCase().substring(1).split(" ", 2)[0];
            List<String> disabledCommands = getConfig().getStringList("console-only-commands");

            if (disabledCommands.contains(sentCommand)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.YELLOW + "That command is restricted!");
            }
        }
    }

    @EventHandler
    public void onServerCommandEvent(ServerCommandEvent event) {
        if (getConfig().getBoolean("enable-restrictions") && !(event.getSender() instanceof ConsoleCommandSender) && getConfig().isSet("console-only-commands")) {
            // Get the full command in lowercase, then split it in half at the first occurrence of a space. Save the first half.
            String sentCommand = event.getCommand().toLowerCase().split(" ", 2)[0];
            List<String> disabledCommands = getConfig().getStringList("console-only-commands");

            if (disabledCommands.contains(sentCommand)) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            if (args.length == 1) {
                switch (args[0]) {
                    case "reload":
                        initializeConfig();
                        logger.info("The config has been reloaded.");
                        break;
                    case "enable":
                        if (getConfig().getBoolean("enable-restrictions")) {
                            logger.info("Restrictions are already enabled!");
                        } else {
                            getConfig().set("enable-restrictions", true);
                            saveConfig();
                            logger.info("Restrictions have been enabled!");
                        }
                        break;
                    case "disable":
                        if (!getConfig().getBoolean("enable-restrictions")) {
                            logger.info("Restrictions are already disabled!");
                        } else {
                            getConfig().set("enable-restrictions", false);
                            saveConfig();
                            logger.info("Restrictions have been disabled!");
                        }
                        break;
                    default:
                        return false;
                }
                return true;
            } else {
                return false;
            }
        } else {
            sender.sendMessage(ChatColor.YELLOW + "That command is restricted!");
            return true;
        }
    }
}
