package it.feio.android.omninotes.FragmentHelpers.DetailFragment;

import android.content.Intent;

import it.feio.android.omninotes.DetailFragment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.IntentChecker;

public class GoogleNowHelper {
    private final DetailFragment detailFragment;

    public GoogleNowHelper(DetailFragment detailFragment) {
        this.detailFragment = detailFragment;
    }

    public void HandleGoogleNow(Intent i) {
        if (IntentChecker.checkAction(i, Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE, Constants.INTENT_GOOGLE_NOW)
                && i.getType() != null) {

            detailFragment.setAfterSavedReturnsToList(false);

            if (detailFragment.getNoteTmp() == null) detailFragment.setNoteTmp(new Note());

            // Text title
            String title = i.getStringExtra(Intent.EXTRA_SUBJECT);
            if (title != null) {
                detailFragment.getNoteTmp().setTitle(title);
            }

            // Text content
            String content = i.getStringExtra(Intent.EXTRA_TEXT);
            if (content != null) {
                detailFragment.getNoteTmp().setContent(content);
            }

            detailFragment.importAttachments(i);

        }
    }
}