package it.feio.android.omninotes.FragmentHelpers.DetailFragment;

import android.util.Log;
import android.view.MenuItem;

import it.feio.android.omninotes.DetailFragment;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.utils.Constants;

public class OptionsHelper {
    private final DetailFragment detailFragment;

    public OptionsHelper(DetailFragment detailFragment) {
        this.detailFragment = detailFragment;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_attachment:
                detailFragment.showAttachmentsPopup();
                break;
            case R.id.menu_tag:
                detailFragment.getTagHelper().addTags();
                break;
            case R.id.menu_category:
                detailFragment.categorizeNote();
                break;
            case R.id.menu_share:
                detailFragment.shareNote();
                break;
            case R.id.menu_checklist_on:
                detailFragment.checklistHelper.toggleChecklist();
                break;
            case R.id.menu_checklist_off:
                detailFragment.checklistHelper.toggleChecklist();
                break;
            case R.id.menu_lock:
                detailFragment.lockNote();
                break;
            case R.id.menu_unlock:
                detailFragment.lockNote();
                break;
            case R.id.menu_add_shortcut:
                detailFragment.addShortcut();
                break;
            case R.id.menu_archive:
                detailFragment.noteDisposalHelper.archiveNote(true);
                break;
            case R.id.menu_unarchive:
                detailFragment.noteDisposalHelper.archiveNote(false);
                break;
            case R.id.menu_trash:
                detailFragment.noteDisposalHelper.trashNote(true);
                break;
            case R.id.menu_untrash:
                detailFragment.noteDisposalHelper.trashNote(false);
                break;
            case R.id.menu_discard_changes:
                detailFragment.noteDisposalHelper.discard();
                break;
            case R.id.menu_delete:
                detailFragment.noteDisposalHelper.deleteNote();
                break;
            case R.id.menu_note_info:
                detailFragment.showNoteInfo();
                break;
            default:
                Log.w(Constants.TAG, "Invalid menu option selected");
        }

        ((OmniNotes) detailFragment.getActivity().getApplication()).getAnalyticsHelper().trackActionFromResourceId(detailFragment.getActivity(),
                item.getItemId());

        return true;
    }
}