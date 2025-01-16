package fr.WizardStoneCraft.PlaceHolderApi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;

public class PlaceHolderApi extends PlaceholderExpansion {
    @Override
    public  String getIdentifier() {
        return "WizardStoneCraft";
    }

    @Override
    public @NotNull String getAuthor() {
        return "metal54400";
    }

    @Override
    public @NotNull String getVersion() {
        return "0.0.5";
    }
}
