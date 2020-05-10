package edu.caltech.seva.activities.Repair.fragments;

import edu.caltech.seva.models.RepairStep;

/**
 * Interface for the Repair Tab Fragment which defines the necessary operations for both the
 * View and Presenter.
 */
public class RepairContract {

    /**
     * Contract for the Repair Tab Fragment view. Defines visual operations used by the presenter.
     */
    interface View {

        /**
         * Populates the repair step Tab fragment with all of the necessary information for the
         * given step.
         *
         * @param repairStep Data object that contains all info for a specific repair step
         */
        void showRepairStep(RepairStep repairStep);
    }

    /**
     * Contract for the Repair Tab Fragment presenter. Defines query operations used by the view.
     */
    interface Presenter {
        /**
         * Queries the local SQLiteDB for a specific repair step corresponding to the given repair
         * code.
         *
         * @param repairCode The code that describes the repair being done
         * @param stepNum    The step number corresponding to the tab
         */
        void loadRepairStep(String repairCode, int stepNum);
    }
}
