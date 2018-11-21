package it.feio.android.omninotes.FragmentHelpers.DetailFragment;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;

import it.feio.android.omninotes.DetailFragment;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.async.notes.SaveNoteTask;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.ReminderHelper;
import it.feio.android.omninotes.utils.ShortcutHelper;
import it.feio.android.omninotes.utils.StorageHelper;

public class NoteDisposalHelper {
    private final DetailFragment detailFragment;

    public NoteDisposalHelper(DetailFragment detailFragment) {
        this.detailFragment = detailFragment;
    }

    /**
     * Discards changes done to the note and eventually delete new attachments
     */
    public void discard() {
        // Checks if some new files have been attached and must be removed
        if (!detailFragment.getNoteTmp().getAttachmentsList().equals(detailFragment.getNote().getAttachmentsList())) {
            for (Attachment newAttachment : detailFragment.getNoteTmp().getAttachmentsList()) {
                if (!detailFragment.getNote().getAttachmentsList().contains(newAttachment)) {
                    StorageHelper.delete(detailFragment.getMainActivity(), newAttachment.getUri().getPath());
                }
            }
        }

        detailFragment.setGoBack(true);

        if (!detailFragment.getNoteTmp().equals(detailFragment.getNoteOriginal())) {
            // Restore original status of the note
            if (detailFragment.getNoteOriginal().get_id() == null) {
                detailFragment.getMainActivity().deleteNote(detailFragment.getNoteTmp());
                detailFragment.goHome();
            } else {
                new SaveNoteTask(detailFragment, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, detailFragment.getNoteOriginal());
            }
            MainActivity.notifyAppWidgets(detailFragment.getMainActivity());
        } else {
            detailFragment.goHome();
        }
    }

    @SuppressLint("NewApi")
    public void archiveNote(boolean archive) {
        // Simply go back if is a new note
        if (detailFragment.getNoteTmp().get_id() == null) {
            detailFragment.goHome();
            return;
        }

        detailFragment.getNoteTmp().setArchived(archive);
        detailFragment.setGoBack(true);
        detailFragment.setExitMessage(archive ? detailFragment.getString(R.string.note_archived) : detailFragment.getString(R.string.note_unarchived));
        detailFragment.setExitCroutonStyle(archive ? ONStyle.WARN : ONStyle.INFO);
        detailFragment.getSaveHelper().saveNote(detailFragment);
    }

    @SuppressLint("NewApi")
    public void trashNote(boolean trash) {
        // Simply go back if is a new note
        if (detailFragment.getNoteTmp().get_id() == null) {
            detailFragment.goHome();
            return;
        }

        detailFragment.getNoteTmp().setTrashed(trash);
        detailFragment.setGoBack(true);
        detailFragment.setExitMessage(trash ? detailFragment.getString(R.string.note_trashed) : detailFragment.getString(R.string.note_untrashed));
        detailFragment.setExitCroutonStyle(trash ? ONStyle.WARN : ONStyle.INFO);
        if (trash) {
            ShortcutHelper.removeshortCut(OmniNotes.getAppContext(), detailFragment.getNoteTmp());
            ReminderHelper.removeReminder(OmniNotes.getAppContext(), detailFragment.getNoteTmp());
        } else {
            ReminderHelper.addReminder(OmniNotes.getAppContext(), detailFragment.getNote());
        }
        detailFragment.getSaveHelper().saveNote(detailFragment);
    }

    public void deleteNote() {
        new MaterialDialog.Builder(detailFragment.getMainActivity())
                .content(R.string.delete_note_confirmation)
                .positiveText(R.string.ok)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog materialDialog) {
                        detailFragment.getMainActivity().deleteNote(detailFragment.getNoteTmp());
                        Log.d(Constants.TAG, "Deleted note with id '" + detailFragment.getNoteTmp().get_id() + "'");
                        detailFragment.getMainActivity().showMessage(R.string.note_deleted, ONStyle.ALERT);
                        detailFragment.goHome();
                    }
                }).build().show();
    }
}