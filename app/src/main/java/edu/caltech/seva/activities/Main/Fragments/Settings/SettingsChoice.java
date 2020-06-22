package edu.caltech.seva.activities.Main.Fragments.Settings;

public enum SettingsChoice {

    WRITTEN(0), AUDIO(1), SMS(2), PUSH(3);

    private final int choice;

    SettingsChoice(int choice) {
        this.choice = choice;
    }

    public int getValue() {
        return this.choice;
    }
}
