package it.feio.android.omninotes.FragmentHelpers.DetailFragment;

import android.text.Editable;
import android.text.Selection;

import java.text.DateFormat;
import java.util.Date;

import it.feio.android.checklistview.models.CheckListView;
import it.feio.android.omninotes.DetailFragment;

public class TimestampHelper {
    private final DetailFragment detailFragment;

    public TimestampHelper(DetailFragment detailFragment) {
        this.detailFragment = detailFragment;
    }

    public void addTimestamp() {
        Editable editable = detailFragment.getContent().getText();
        int position = detailFragment.getContent().getSelectionStart();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        String dateStamp = dateFormat.format(new Date().getTime()) + " ";
        if (detailFragment.getNoteTmp().isChecklist()) {
            if (detailFragment.mChecklistManager.getFocusedItemView() != null) {
                editable = detailFragment.mChecklistManager.getFocusedItemView().getEditText().getEditableText();
                position = detailFragment.mChecklistManager.getFocusedItemView().getEditText().getSelectionStart();
            } else {
                ((CheckListView) detailFragment.getToggleChecklistView())
                        .addItem(dateStamp, false, detailFragment.mChecklistManager.getCount());
            }
        }
        String leadSpace = position == 0 ? "" : " ";
        dateStamp = leadSpace + dateStamp;
        editable.insert(position, dateStamp);
        Selection.setSelection(editable, position + dateStamp.length());
    }
}