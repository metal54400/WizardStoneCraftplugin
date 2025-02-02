package fr.WizardStoneCraft.PlaceHolderApi;

import fr.WizardStoneCraft.WizardStoneCraft;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;


public class PlaceHolderApi extends PlaceholderExpansion {
    @Override
    public  String getIdentifier() {
        return "WizardStoneCraft";
    }

    @Override
    public String getAuthor() {
        return "metal54400";
    }

    @Override
    public String getVersion() {
        return "0.0.11";
    }

    @Override
    public boolean register() {
        Object reputationCommand = WizardStoneCraft.ReputationCommand;
        return super.register();
    }

    @Override
    public boolean canRegister() {
        return super.canRegister();
    }
}
