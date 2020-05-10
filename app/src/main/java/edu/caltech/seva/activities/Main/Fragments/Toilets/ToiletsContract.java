package edu.caltech.seva.activities.Main.Fragments.Toilets;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

import edu.caltech.seva.models.Toilet;

/**
 * Interface for the Toilets Fragment which defines the necessary operations for both the View and
 * Presenter.
 */
public class ToiletsContract {

    /**
     * Contract for the Toilets Fragment view. Defines visual operations used by the presenter.
     */
    interface View {

        /**
         * Displays the toilet markers on the mapView.
         *
         * @param toiletList List of Toilet objects containing all relevant information
         */
        void displayToilets(ArrayList<Toilet> toiletList);

        /**
         * Displays the toiletInfoCard as a popup with more information for the user.
         *
         * @param marker GoogleMap marker object that the user has selected
         */
        void displayInfoCard(Marker marker);

        /**
         * Displays the empty toiletInfoCard if there is not information for the user.
         *
         * @param marker GoogleMap marker object that the user has selected
         */
        void displayEmptyInfoCard(Marker marker);

    }

    /**
     * Contract for the Toilets Fragment presenter. Defines query operations used by the view.
     */
    interface Presenter {
        /**
         * Query the local database for information on the user's assigned toilets.
         */
        void getToiletInfo();

        /**
         * Query AWS DynamoDB to see if the toilet is Enabled or in an error state.
         */
        void getToiletStatus();

        /**
         * Checks if the async task for checking toilet status is done.
         *
         * @return true the task has completed in the background
         */
        boolean isStatusCheckDone();
    }
}
