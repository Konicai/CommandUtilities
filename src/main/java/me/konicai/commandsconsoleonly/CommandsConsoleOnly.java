package me.konicai.commandsconsoleonly;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CommandsConsoleOnly extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }
    @Override
    public void onDisable() {
    }
    @EventHandler
    public void onPlayerCommandEvent(PlayerCommandPreprocessEvent event) {
        Configuration config = getConfig();
        if (config.isSet("console-only-commands")) {
            // Get the full command in lowercase, remove the first character, then split it in half at the first occurrence of a space. Save the first half.
            String sentCommand = event.getMessage().toLowerCase().substring(1).split(" ", 2)[0];
            List<String> disabledCommands = config.getStringList("console-only-commands");

            if (disabledCommands.contains(sentCommand)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.YELLOW + "That command is restricted!");
            }
        }
    }
}
