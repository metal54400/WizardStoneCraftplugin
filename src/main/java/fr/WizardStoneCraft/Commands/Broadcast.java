package fr.WizardStoneCraft.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Broadcast implements CommandExecutor {
    @Override
    public boolean onCommand( CommandSender sender,  Command cmd,  String s,  String[] agrs) {
        sender.sendMessage("§7[§e?§7] test");
        return false;
    }
}