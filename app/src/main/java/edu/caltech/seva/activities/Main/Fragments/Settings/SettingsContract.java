package edu.caltech.seva.activities.Main.Fragments.Settings;

/**
 * Interface for the Settings Fragment which defines the necessary operations for both the View and
 * Presenter.
 */
public class SettingsContract {

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
        //currently no necessary functions
    }
}
