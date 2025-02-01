package fr.WizardStoneCraft.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ProfileMenu implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Cette commande est réservée aux joueurs.");
            return true;
        }

        Player player = (Player) sender;

        Inventory profileMenu = Bukkit.createInventory(null, 27, ChatColor.GREEN + "Menu des Profils");

        for (Player target : Bukkit.getOnlinePlayers()) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            assert meta != null;
            meta.setOwningPlayer(target);
            meta.setDisplayName(ChatColor.YELLOW + target.getName());

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Clique pour voir les stats");
            meta.setLore(lore);

            head.setItemMeta(meta);
            profileMenu.addItem(head);
        }

        player.openInventory(profileMenu);
        return true;
    }
}


