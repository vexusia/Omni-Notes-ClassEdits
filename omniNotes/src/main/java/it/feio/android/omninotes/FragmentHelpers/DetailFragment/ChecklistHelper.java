package it.feio.android.omninotes.FragmentHelpers.DetailFragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nineoldandroids.view.ViewPropertyAnimator;

import it.feio.android.checklistview.Settings;
import it.feio.android.checklistview.exceptions.ViewNotSupportedException;
import it.feio.android.checklistview.models.ChecklistManager;
import it.feio.android.omninotes.DetailFragment;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.utils.Constants;

public class ChecklistHelper {
    private final DetailFragment detailFragment;

    public ChecklistHelper(DetailFragment detailFragment) {
        this.detailFragment = detailFragment;
    }

    /**
     *
     */
    public void toggleChecklist() {

        // In case checklist is active a prompt will ask about many options
        // to decide hot to convert back to simple text
        if (!detailFragment.getNoteTmp().isChecklist()) {
            toggleChecklist2();
            return;
        }

        // If checklist is active but no items are checked the conversion in done automatically
        // without prompting user
        if (detailFragment.mChecklistManager.getCheckedCount() == 0) {
            toggleChecklist2(true, false);
            return;
        }

        // Inflate the popup_layout.xml
        LayoutInflater inflater = (LayoutInflater) detailFragment.getMainActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_remove_checklist_layout,
                (ViewGroup) detailFragment.getView().findViewById(R.id.layout_root));

        // Retrieves options checkboxes and initialize their values
        final CheckBox keepChecked = (CheckBox) layout.findViewById(R.id.checklist_keep_checked);
        final CheckBox keepCheckmarks = (CheckBox) layout.findViewById(R.id.checklist_keep_checkmarks);
        keepChecked.setChecked(detailFragment.getPrefs().getBoolean(Constants.PREF_KEEP_CHECKED, true));
        keepCheckmarks.setChecked(detailFragment.getPrefs().getBoolean(Constants.PREF_KEEP_CHECKMARKS, true));

        new MaterialDialog.Builder(detailFragment.getMainActivity())
                .customView(layout, false)
                .positiveText(R.string.ok)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog materialDialog) {
                        detailFragment.getPrefs().edit()
                                .putBoolean(Constants.PREF_KEEP_CHECKED, keepChecked.isChecked())
                                .putBoolean(Constants.PREF_KEEP_CHECKMARKS, keepCheckmarks.isChecked())
                                .apply();
                        toggleChecklist2();
                    }
                }).build().show();
    }

    /**
     * Toggles checklist view
     */
    public void toggleChecklist2() {
        boolean keepChecked = detailFragment.getPrefs().getBoolean(Constants.PREF_KEEP_CHECKED, true);
        boolean showChecks = detailFragment.getPrefs().getBoolean(Constants.PREF_KEEP_CHECKMARKS, true);
        toggleChecklist2(keepChecked, showChecks);
    }

    @SuppressLint("NewApi")
    public void toggleChecklist2(final boolean keepChecked, final boolean showChecks) {
        // Get instance and set options to convert EditText to CheckListView

        detailFragment.mChecklistManager = detailFragment.mChecklistManager == null ? new ChecklistManager(detailFragment.getMainActivity()) : detailFragment.mChecklistManager;
        int checkedItemsBehavior = Integer.valueOf(detailFragment.getPrefs().getString("settings_checked_items_behavior", String.valueOf
                (Settings.CHECKED_HOLD)));
        detailFragment.mChecklistManager
                .showCheckMarks(showChecks)
                .newEntryHint(detailFragment.getString(R.string.checklist_item_hint))
                .keepChecked(keepChecked)
                .undoBarContainerView(detailFragment.getScrollView())
                .moveCheckedOnBottom(checkedItemsBehavior);

        // Links parsing options
        detailFragment.mChecklistManager.setOnTextLinkClickListener(detailFragment.getTextLinkClickListener());
        detailFragment.mChecklistManager.addTextChangedListener(detailFragment.getmFragment());
        detailFragment.mChecklistManager.setCheckListChangedListener(detailFragment.getmFragment());

        // Switches the views
        View newView = null;
        try {
            newView = detailFragment.mChecklistManager.convert(detailFragment.getToggleChecklistView());
        } catch (ViewNotSupportedException e) {
            Log.e(Constants.TAG, "Error switching checklist view", e);
        }

        // Switches the views
        if (newView != null) {
            detailFragment.mChecklistManager.replaceViews(detailFragment.getToggleChecklistView(), newView);
            detailFragment.setToggleChecklistView(newView);
            ViewPropertyAnimator.animate(detailFragment.getToggleChecklistView()).alpha(1).scaleXBy(0).scaleX(1).scaleYBy(0).scaleY(1);
            detailFragment.getNoteTmp().setChecklist(!detailFragment.getNoteTmp().isChecklist());
        }
    }
}