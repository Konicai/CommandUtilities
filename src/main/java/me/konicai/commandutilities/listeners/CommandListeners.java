package me.konicai.commandutilities.listeners;

import me.konicai.commandutilities.CommandUtilities;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;

public class CommandListeners implements Listener {

    private final Plugin plugin = CommandUtilities.getInstance();

    /**
     * A map that holds blocks that have run commands and their respective tally values
     */
    private final HashMap<Location, Integer> trackedBlocks = new HashMap<>();

    /**
     * The amount of commands that have been successfully run by a block in the past memory length
     */
    private int totalTally = 0;

    /**
     * The last time any command was run from a block
     */
    private Long lastCommandTime;


    // Restrict player commands
    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerCommandEvent(PlayerCommandPreprocessEvent event) {
        if (plugin.getConfig().getBoolean("console-only-commands.enable") && plugin.getConfig().isSet("console-only-commands.restricted-commands")) {
            // Get the full command in lowercase, remove the first character, then split it in half at the first occurrence of a space. Save the first half.
            String sentCommand = event.getMessage().toLowerCase().substring(1).split(" ", 2)[0];
            List<String> disabledCommands = plugin.getConfig().getStringList("console-only-commands.restricted-commands");

            if (disabledCommands.contains(sentCommand)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.YELLOW + "That command is restricted!");
                if (plugin.getConfig().getBoolean("console-only-commands.log-fails")) {
                    plugin.getLogger().warning(event.getPlayer().getName() + " tried to run command " + event.getMessage());
                }
            }
        }
    }

    // Restrict anything that isn't the console
    @EventHandler (priority = EventPriority.LOWEST)
    public void onServerCommandEvent(ServerCommandEvent event) {
        if (!(event.getSender() instanceof ConsoleCommandSender) && plugin.getConfig().getBoolean("console-only-commands.enable") && plugin.getConfig().isSet("console-only-commands.restricted-commands")) {
            // Get the full command in lowercase, then split it in half at the first occurrence of a space. Save the first half.
            String sentCommand = event.getCommand().toLowerCase().split(" ", 2)[0];
            List<String> disabledCommands = plugin.getConfig().getStringList("console-only-commands.restricted-commands");

            if (disabledCommands.contains(sentCommand)) {
                event.setCancelled(true);
                if (plugin.getConfig().getBoolean("console-only-commands.log-fails")) {
                    plugin.getLogger().warning(event.getSender().getName() + " tried to run command " + event.getCommand());
                }
            }
        }
    }

    // for now we will rate all command blocks as a whole because it'll be easier
    @EventHandler (priority = EventPriority.LOW)
    public void onBlockCommandEvent(ServerCommandEvent event) {
        if (plugin.getConfig().getBoolean("rate-limit") && event.getSender() instanceof BlockCommandSender) {

            if (lastCommandTime == null) {
                lastCommandTime = System.currentTimeMillis();
                return;
            }
            BlockCommandSender blockSender = (BlockCommandSender) event.getSender();
            Block block = blockSender.getBlock();
            Location blockSite = block.getLocation();

            if (getDowntime() > plugin.getConfig().getInt("command-blocks.memory-length")) {
                // Forget everything if we are past the memory length.
                event.setCancelled(false);
                trackedBlocks.clear();
                trackedBlocks.put(blockSite, 1);
                totalTally = 1;
                lastCommandTime = System.currentTimeMillis();
            } else if (totalTally >= plugin.getConfig().getInt("command-blocks.command-threshold")) {
                // If we are in the memory length and we are over the threshold then we cancel the command
                // Don't track or increment the tally because we cancelled it
                event.setCancelled(true);
                if (plugin.getConfig().getBoolean("command-blocks.log-fails")) {
                    String coordinates = blockSite.getBlockX() + "," + blockSite.getBlockY() + "," + blockSite.getBlockZ();
                    plugin.getLogger().warning("Command from " + block.getType().toString() + " at " + coordinates + " in " + block.getWorld() + " has been cancelled because the rate limit threshold was reached!");
                }
            } else {
                // We haven't hit the threshold yet so we increment the tally
                if (trackedBlocks.containsKey(blockSite)) {
                    int oldTally = trackedBlocks.get(blockSite);
                    trackedBlocks.replace(blockSite, ++oldTally);
                } else {
                    trackedBlocks.put(blockSite, 1);
                    totalTally = 1;
                }
                lastCommandTime = System.currentTimeMillis();
            }
        }
    }

    /**
     * @return The amount of milliseconds since a command was last sent successfully by a block
     */
    private long getDowntime() {
        return System.currentTimeMillis() - lastCommandTime;
    }
}
