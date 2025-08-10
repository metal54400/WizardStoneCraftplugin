package fr.WizardStoneCraft.API.Logger;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class LoggerUtil {
    private static final File logFile = new File(WizardStoneCraft.getInstance().getDataFolder(), "passive_attacks.log");

    public static void log(String message) {
        try (FileWriter writer = new FileWriter(logFile, true)) {
            String time = LocalDateTime.now().toString().replace("T", " ").split("\\.")[0];
            writer.write("[" + time + "] " + message + "\n");
        } catch (IOException e) {
            Bukkit.getLogger().warning("Erreur lors de l’écriture du log passif : " + e.getMessage());
        }
    }
}