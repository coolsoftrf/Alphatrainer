package ru.coolsoft.alphatrainer.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import androidx.fragment.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;

import ru.coolsoft.alphatrainer.R;
import ru.coolsoft.alphatrainer.ui.controls.DynamicListView;
import ru.coolsoft.alphatrainer.ui.controls.StableArrayAdapter;

/**
 * Created by Bobby© on 26.04.2015.
 * Displays {@link DynamicListView} in a dialog providing item sorting feature
 */
public class SortableListDialogFragment extends DialogFragment{
    public interface SortableListDialogListener{
        void onPositiveButtonClicked(LinkedList<Long> precedence);
        //void onNegativeButtonClicked();
    }

    public static final String ITEM_NAMES = "item_names";
    public static final String ITEM_KEYS = "item_keys";

    private SortableListDialogListener mListener;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context c = getActivity();
        AlertDialog.Builder bdr = new AlertDialog.Builder(c);
        final DynamicListView listView = (DynamicListView) LayoutInflater.from(c/*bdr.getContext()*/)
                .inflate(R.layout.language_list, new LinearLayout(c), false);
        bdr.setView(listView)
                .setTitle(R.string.pref_caption_script);
        AlertDialog dlgPriority = bdr.create();

        //Fill list contents
        ArrayList<String> langs = getArguments().getStringArrayList(ITEM_NAMES);
        ArrayList<String> langIDs = getArguments().getStringArrayList(ITEM_KEYS);
        final StableArrayAdapter adapter = new StableArrayAdapter(
                getActivity() , android.R.layout.simple_list_item_activated_1, langs, langIDs
        );
        listView.setCheeseList(langs);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        //Configure buttons
        final DialogInterface.OnClickListener clickListener = (dialog, which) -> {
            switch (which){
                case AlertDialog.BUTTON_POSITIVE:
                    //Save new precedence
                    LinkedList<Long> precedence = new LinkedList<>();
                    for (int i = 0; i < adapter.getCount(); i++) {
                        precedence.add(adapter.getItemId(i));
                    }

                    mListener.onPositiveButtonClicked(precedence);
                    break;
/*
                case DialogInterface.BUTTON_NEGATIVE:
                    mListener.onNegativeButtonClicked();
*/
            }
        };
        dlgPriority.setButton(AlertDialog.BUTTON_POSITIVE, getActivity().getString(android.R.string.ok), clickListener);
        dlgPriority.setButton(AlertDialog.BUTTON_NEGATIVE, getActivity().getString(android.R.string.cancel), clickListener);

        return dlgPriority;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (SortableListDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.getClass().getSimpleName()
                    + " must implement SortableListDialogListener");
        }
    }
}
