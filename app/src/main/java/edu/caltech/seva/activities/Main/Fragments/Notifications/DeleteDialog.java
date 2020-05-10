package edu.caltech.seva.activities.Main.Fragments.Notifications;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import edu.caltech.seva.R;

//Delete Dialog fragment class that pops up on top of Notification Fragment
public class DeleteDialog extends DialogFragment {

    LayoutInflater inflater;
    View v;
    DialogData data;

    //sets the layout and buttons of the dialog fragment
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        inflater = getActivity().getLayoutInflater();
        v = inflater.inflate(R.layout.dialog_delete, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Notification?");
        builder.setView(v).setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                data.deleteConfirmed();
                dialogInterface.cancel();
            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.cancel();
            }
        });

        return builder.create();
    }

    //Connects to the target fragment which is the notification fragment allowing data to be sent there
    @Override
    public void onAttach(Context context) {
        data = (DialogData)getTargetFragment();
        super.onAttach(context);
    }

    //interface that allows the Parent Fragment to access the state of the delete button
    public interface DialogData{
        public void deleteConfirmed();
    }
}

