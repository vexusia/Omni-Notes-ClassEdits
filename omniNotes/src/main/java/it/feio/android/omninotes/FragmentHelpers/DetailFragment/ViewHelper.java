package it.feio.android.omninotes.FragmentHelpers.DetailFragment;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.commons.lang.StringUtils;

import it.feio.android.omninotes.DetailFragment;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.helpers.date.DateHelper;
import it.feio.android.omninotes.utils.AlphaManager;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.GeocodeHelper;
import it.feio.android.omninotes.utils.IntentChecker;
import it.feio.android.omninotes.utils.ReminderHelper;
import it.feio.android.omninotes.utils.date.DateUtils;
import it.feio.android.omninotes.utils.date.ReminderPickers;

public class ViewHelper {
    private final DetailFragment detailFragment;

    public ViewHelper(DetailFragment detailFragment) {
        this.detailFragment = detailFragment;
    }

    public void initViewFooter() {
        // Footer dates of creation...
        String creation = DateHelper.getFormattedDate(detailFragment.getNoteTmp().getCreation(), detailFragment.getPrefs().getBoolean(Constants
                .PREF_PRETTIFIED_DATES, true));
        detailFragment.getCreationTextView().append(creation.length() > 0 ? detailFragment.getString(R.string.creation) + " " + creation : "");
        if (detailFragment.getCreationTextView().getText().length() == 0)
            detailFragment.getCreationTextView().setVisibility(View.GONE);

        // ... and last modification
        String lastModification = DateHelper.getFormattedDate(detailFragment.getNoteTmp().getLastModification(), detailFragment.getPrefs().getBoolean(Constants
                .PREF_PRETTIFIED_DATES, true));
        detailFragment.getLastModificationTextView().append(lastModification.length() > 0 ? detailFragment.getString(R.string.last_update) + " " +
                lastModification : "");
        if (detailFragment.getLastModificationTextView().getText().length() == 0)
            detailFragment.getLastModificationTextView().setVisibility(View.GONE);
    }

    public void initViewReminder() {

        // Preparation for reminder icon
        detailFragment.getReminder_layout().setOnClickListener(v -> {
            int pickerType = detailFragment.getPrefs().getBoolean("settings_simple_calendar", false) ? ReminderPickers.TYPE_AOSP :
                    ReminderPickers.TYPE_GOOGLE;
            ReminderPickers reminderPicker = new ReminderPickers(detailFragment.getMainActivity(), detailFragment.getmFragment(), pickerType);
            reminderPicker.pick(DateUtils.getPresetReminder(detailFragment.getNoteTmp().getAlarm()), detailFragment.getNoteTmp()
                    .getRecurrenceRule());
            detailFragment.onDateSetListener=reminderPicker;
            detailFragment.onTimeSetListener=reminderPicker;
        });

        detailFragment.getReminder_layout().setOnLongClickListener(v -> {
            MaterialDialog dialog = new MaterialDialog.Builder(detailFragment.getMainActivity())
                    .content(R.string.remove_reminder)
                    .positiveText(R.string.ok)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog materialDialog) {
                            ReminderHelper.removeReminder(OmniNotes.getAppContext(), detailFragment.getNoteTmp());
                            detailFragment.getNoteTmp().setAlarm(null);
                            detailFragment.getReminderIcon().setImageResource(R.drawable.ic_alarm_black_18dp);
                            detailFragment.getDatetime().setText("");
                        }
                    }).build();
            dialog.show();
            return true;
        });

        // Reminder
        String reminderString = detailFragment.initReminder(detailFragment.getNoteTmp());
        if (!StringUtils.isEmpty(reminderString)) {
            detailFragment.getReminderIcon().setImageResource(R.drawable.ic_alarm_add_black_18dp);
            detailFragment.getDatetime().setText(reminderString);
        }
    }

    public void initViewLocation() {
        if (detailFragment.isNoteLocationValid()) {
            if (TextUtils.isEmpty(detailFragment.getNoteTmp().getAddress())) {
                //FIXME: What's this "sasd"?
                GeocodeHelper.getAddressFromCoordinates(new Location("sasd"), detailFragment);
            } else {
                detailFragment.getLocationTextView().setText(detailFragment.getNoteTmp().getAddress());
                detailFragment.getLocationTextView().setVisibility(View.VISIBLE);
            }
        }

        // Automatic location insertion
        if (detailFragment.getPrefs().getBoolean(Constants.PREF_AUTO_LOCATION, false) && detailFragment.getNoteTmp().get_id() == null) {
            detailFragment.getLocation(detailFragment);
        }

        detailFragment.getLocationTextView().setOnClickListener(v -> {
            String uriString = "geo:" + detailFragment.getNoteTmp().getLatitude() + ',' + detailFragment.getNoteTmp().getLongitude()
                    + "?q=" + detailFragment.getNoteTmp().getLatitude() + ',' + detailFragment.getNoteTmp().getLongitude();
            Intent locationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
            if (!IntentChecker.isAvailable(detailFragment.getMainActivity(), locationIntent, null)) {
                uriString = "http://maps.google.com/maps?q=" + detailFragment.getNoteTmp().getLatitude() + ',' + detailFragment.getNoteTmp()
                        .getLongitude();
                locationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
            }
            detailFragment.startActivity(locationIntent);
        });
        detailFragment.getLocationTextView().setOnLongClickListener(v -> {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(detailFragment.getMainActivity());
            builder.content(R.string.remove_location);
            builder.positiveText(R.string.ok);
            builder.callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog materialDialog) {
                    detailFragment.getNoteTmp().setLatitude("");
                    detailFragment.getNoteTmp().setLongitude("");
                    detailFragment.fade(detailFragment.getLocationTextView(), false);
                }
            });
            MaterialDialog dialog = builder.build();
            dialog.show();
            return true;
        });
    }

    public void initViewTitle() {
        detailFragment.getTitle().setText(detailFragment.getNoteTmp().getTitle());
        detailFragment.getTitle().gatherLinksForText();
        detailFragment.getTitle().setOnTextLinkClickListener(detailFragment.getTextLinkClickListener());
        // To avoid dropping here the  dragged checklist items
        detailFragment.getTitle().setOnDragListener((v, event) -> {
//					((View)event.getLocalState()).setVisibility(View.VISIBLE);
            return true;
        });
        //When editor action is pressed focus is moved to last character in content field
        detailFragment.getTitle().setOnEditorActionListener((v, actionId, event) -> {
            detailFragment.getContent().requestFocus();
            detailFragment.getContent().setSelection(detailFragment.getContent().getText().length());
            return false;
        });
        detailFragment.requestFocus(detailFragment.getTitle());
    }

    public void initViewContent() {

        detailFragment.getContent().setText(detailFragment.getNoteTmp().getContent());
        detailFragment.getContent().gatherLinksForText();
        detailFragment.getContent().setOnTextLinkClickListener(detailFragment.getTextLinkClickListener());
        // Avoids focused line goes under the keyboard
        detailFragment.getContent().addTextChangedListener(detailFragment);

        // Restore checklist
        detailFragment.setToggleChecklistView(detailFragment.getContent());
        if (detailFragment.getNoteTmp().isChecklist()) {
            detailFragment.getNoteTmp().setChecklist(false);
            AlphaManager.setAlpha(detailFragment.getToggleChecklistView(), 0);
            detailFragment.toggleChecklist2();
        }
    }
}