package edu.caltech.seva.activities.Main.Fragments.Settings;

/**
 * Interface for the Settings Fragment which defines the necessary operations for both the View and
 * Presenter.
 */
class SettingsContract {

    /**
     * Contract for the Home Fragment view. Defines visual operations used by the presenter.
     */
    interface View {

        /**
         * Displays the proper text for changing written language settings.
         */
        void showWrittenSettings();

        /**
         * Displays the proper text for changing audio language settings
         */
        void showAudioSettings();
    }

    /**
     * Contract for the Settings Fragment presenter. Defines query operations used by the view.
     */
    interface Presenter {

        /**
         * Handles the management of user notification settings. Manages userPrefs as well as
         * DynamoDB updates.
         * @param choice Indicates which setting the user has chosen to toggle
         * @param isChecked The boolean value for turning the notifications on or off.
         */
        void handleNotificationSettings(SettingsChoice choice, boolean isChecked);
    }
}
