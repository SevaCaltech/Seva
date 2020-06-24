package edu.caltech.seva.activities.Main.Fragments.Notifications;

import java.util.List;

import edu.caltech.seva.models.IncomingError;

/**
 * Interface for the Notifications Fragment which defines the necessary operations for both the
 * View and Presenter.
 */
public class NotificationsContract {

    /**
     * Contract for the Notification Fragment view. Defines visual operations used by the presenter.
     */
    interface View {
        /**
         * Display the assigned toilet names in the spinner. Used to sort the notifications by
         * toilet name.
         *
         * @param toiletNames A list of the toilet names assigned to the user
         */
        void addToiletNamesToSpinner(List<CharSequence> toiletNames);

        /**
         * Adds the error along with the corresponding toilet information and repair information to
         * the Notification recycler.
         *
         * @param error All of the relevant information needed to display the notification
         */
        void addErrorToAdapter(IncomingError error);
    }

    /**
     * Contract for the Notifications Fragment presenter. Defines query operations used by the view.
     */
    interface Presenter {

        /**
         * Loads toilet names from the SQLiteDB and adds them to the FilterSpinner.
         */
        void loadToiletNames();

        /**
         * Queries the DynamoDB for all of the errors associated with the user's toilets. Saves them
         * in the local SQLiteDB as a batch.
         */
        void loadErrors();

        /**
         * Queries the local SQLiteDB to populate the IncommingError objects for each error. Calls
         * the view to add each error to the notification recycler adapter.
         */
        void loadErrorInfo();

        /**
         * Deletes the error from the DynamoDB when the user has confirmed.
         *
         * @param timestamp the string of the timestamp of the error notification
         * @param toiletID the IP address of the corresponding toilet unit
         */
        void handleDeleteNotification(String timestamp, String toiletID);
    }
}
