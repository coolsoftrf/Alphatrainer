package ru.coolsoft.alphatrainer.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import ru.coolsoft.alphatrainer.R;

/**
 * Created by Bobby© on 26.04.2015.
 * Dialog fragment that's intended to display {@link android.app.AlertDialog}s with simple messages in the application
 */
public class AlertDialogFragment extends DialogFragment{
    public static final String ITEM_PREFERENCE_KEY = "item_preference_key";
    public static final String ITEM_PREFERENCE_TITLE = "item_preference_title";
    public static final String ITEM_IS_EULA = "item_is_eula";
    public static final String ITEM_IS_EULA_ACCEPTED = "item_is_eula_accepted";

    private boolean _isEula;

    private final DialogInterface.OnClickListener _buttonClickListener = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Activity activity = AlertDialogFragment.this.getActivity();
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    if (_isEula) {
                        SharedPreferences.Editor editor = PreferenceManager
                                .getDefaultSharedPreferences(activity)
                                .edit();
                        editor.putBoolean(getString(R.string.pref_key_eula), true);
                        editor.apply();
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    if (_isEula) {
                        //TrainerApplication.app().setEulaDeclined();
                        activity.finish();
                    }
                    break;
            }
            /*TrainerApplication.app().setEulaVisible(false);*/
        }
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String key = getArguments().getString(ITEM_PREFERENCE_KEY);
        String title = getArguments().getString(ITEM_PREFERENCE_TITLE);
        _isEula = getArguments().getBoolean(ITEM_IS_EULA);
        boolean isEulaAccepted = getArguments().getBoolean(ITEM_IS_EULA_ACCEPTED);

        AlertDialog.Builder bdr = new AlertDialog.Builder(getActivity());
        if (_isEula) {
            bdr.setTitle(isEulaAccepted
                    ? getString(R.string.pref_caption_eula_accepted)
                    : getString(R.string.pref_caption_eula_pending));
        } else {
            bdr.setTitle(title);
        }

        if(_isEula) {
            bdr.setMessage(R.string.pref_text_eula);
        } else if (key.equals(getString(R.string.pref_key_about))){
            bdr.setMessage(R.string.pref_text_about);
        }

        bdr.setPositiveButton(_isEula
                        ? android.R.string.yes
                        : android.R.string.ok
                , _buttonClickListener
        );

        if (_isEula && !isEulaAccepted) {
            bdr.setNegativeButton(
                    android.R.string.no
                    , _buttonClickListener);
        }

        return bdr.create();
    }
}
