package blindgps.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * dialog box displayed if the Internet network is inaccessible
 */
@SuppressLint("ValidFragment")
public class InformationDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.no_network_message).setPositiveButton(R.string.close_dialog_box, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Activity currentActivity = getActivity();
                if(currentActivity.getClass() != HomeActivity.class) // if not into the start activity
                    currentActivity.finish(); // go back into the start activity (remove activity from stack top)
            }
        });
        return builder.create(); // creation of the dialog
    }
}