package it.feio.android.omninotes.FragmentHelpers.DetailFragment;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.DetailFragment;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.async.bus.NotesUpdatedEvent;
import it.feio.android.omninotes.async.notes.SaveNoteTask;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.models.listeners.OnNoteSaved;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.ReminderHelper;

public class SaveHelper implements OnNoteSaved {
    private final DetailFragment detailFragment;

    public SaveHelper(DetailFragment detailFragment) {
        this.detailFragment = detailFragment;
    }

    /**
     * Save new notes, modify them or archive
     */
    public void saveNote(OnNoteSaved mOnNoteSaved) {

        // Changed fields
        detailFragment.getNoteTmp().setTitle(detailFragment.getNoteTitle());
        detailFragment.getNoteTmp().setContent(detailFragment.getNoteContent());

        // Check if some text or attachments of any type have been inserted or is an empty note
        if (detailFragment.goBack && TextUtils.isEmpty(detailFragment.getNoteTmp().getTitle()) && TextUtils.isEmpty(detailFragment.getNoteTmp().getContent())
                && detailFragment.getNoteTmp().getAttachmentsList().size() == 0) {
            Log.d(Constants.TAG, "Empty note not saved");
            detailFragment.setExitMessage(detailFragment.getString(R.string.empty_note_not_saved));
            detailFragment.setExitCroutonStyle(ONStyle.INFO);
            detailFragment.goHome();
            return;
        }

        if (saveNotNeeded()) {
            detailFragment.setExitMessage("");
            if (detailFragment.goBack) {
                detailFragment.goHome();
            }
            return;
        }

        detailFragment.getNoteTmp().setAttachmentsListOld(detailFragment.getNote().getAttachmentsList());

        new SaveNoteTask(mOnNoteSaved, lastModificationUpdatedNeeded()).executeOnExecutor(AsyncTask
                .THREAD_POOL_EXECUTOR, detailFragment.getNoteTmp());
    }

    /**
     * Checks if nothing is changed to avoid committing if possible (check)
     */
    public boolean saveNotNeeded() {
        if (detailFragment.getNoteTmp().get_id() == null && detailFragment.getPrefs().getBoolean(Constants.PREF_AUTO_LOCATION, false)) {
            detailFragment.getNote().setLatitude(detailFragment.getNoteTmp().getLatitude());
            detailFragment.getNote().setLongitude(detailFragment.getNoteTmp().getLongitude());
        }
        return !detailFragment.getNoteTmp().isChanged(detailFragment.getNote()) || (detailFragment.getNoteTmp().isLocked() && !detailFragment.getNoteTmp().isPasswordChecked());
    }

    /**
     * Checks if only tag, archive or trash status have been changed
     * and then force to not update last modification date*
     */
    public boolean lastModificationUpdatedNeeded() {
        detailFragment.getNote().setCategory(detailFragment.getNoteTmp().getCategory());
        detailFragment.getNote().setArchived(detailFragment.getNoteTmp().isArchived());
        detailFragment.getNote().setTrashed(detailFragment.getNoteTmp().isTrashed());
        detailFragment.getNote().setLocked(detailFragment.getNoteTmp().isLocked());
        return detailFragment.getNoteTmp().isChanged(detailFragment.getNote());
    }

    @Override
    public void onNoteSaved(Note noteSaved) {
        MainActivity.notifyAppWidgets(OmniNotes.getAppContext());
        if (!detailFragment.isActivityPausing()) {
            EventBus.getDefault().post(new NotesUpdatedEvent());
            detailFragment.deleteMergedNotes(detailFragment.getMergedNotesIds());
            if (detailFragment.getNoteTmp().getAlarm() != null && !detailFragment.getNoteTmp().getAlarm().equals(detailFragment.getNote().getAlarm())) {
                ReminderHelper.showReminderMessage(detailFragment.getNoteTmp().getAlarm());
            }
        }
        detailFragment.setNote(new Note(noteSaved));
        if (detailFragment.goBack) {
            detailFragment.goHome();
        }
    }
}