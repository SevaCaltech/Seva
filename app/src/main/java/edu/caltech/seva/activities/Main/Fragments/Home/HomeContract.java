package edu.caltech.seva.activities.Main.Fragments.Home;

import edu.caltech.seva.models.User;

/**
 * Interface for the Home Fragment which defines the necessary operations for both the View and
 * Presenter.
 */
public class HomeContract {

    /**
     * Contract for the Home Fragment view. Defines visual operations used by the presenter.
     */
    interface View {

        /**
         * Displays the number of notifications stored in the local database.
         *
         * @param num The given number of notifications
         */
        void showNumNotifications(int num);

        /**
         * Displays the user information in the home fragment.
         *
         * @param user Object containing relevant user information to show.
         */
        void showUserInfo(User user);

        /**
         * Display the progress bar indicating the DB is being queried.
         */
        void showProgressBar();

        /**
         * Hide the progress bar indicating no DB operations are being performed.
         */
        void hideProgressBar();

    }

    /**
     * Contract for the Home Fragment presenter. Defines query operations used by the view.
     */
    interface Presenter {

        /**
         * Query the database for the number of notifications.
         */
        void loadNotifications();

        /**
         * Query the database for the user information.
         */
        void loadUserInfo();
    }
}
