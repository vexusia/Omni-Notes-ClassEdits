package it.feio.android.omninotes.FragmentHelpers.DetailFragment;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;

import it.feio.android.omninotes.DetailFragment;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.NavigationDrawerFragment;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.utils.Constants;

public class ActivityCreateHelper {
    private DetailFragment detailFragment;

    public ActivityCreateHelper(DetailFragment detailFragment) {
        this.detailFragment = detailFragment;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        detailFragment.setMainActivity((MainActivity) detailFragment.getActivity());

        detailFragment.setPrefs(detailFragment.getMainActivity().prefs);

        detailFragment.getMainActivity().getSupportActionBar().setDisplayShowTitleEnabled(false);
        detailFragment.getMainActivity().getToolbar().setNavigationOnClickListener(v -> detailFragment.navigateUp());

        // Force the navigation drawer to stay opened if tablet mode is on, otherwise has to stay closed
        if (NavigationDrawerFragment.isDoublePanelActive()) {
            detailFragment.getMainActivity().getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
        } else {
            detailFragment.getMainActivity().getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        // Restored temp note after orientation change
        if (savedInstanceState != null) {
            detailFragment.setNoteTmp(savedInstanceState.getParcelable("noteTmp"));
            detailFragment.setNote(savedInstanceState.getParcelable("note"));
            detailFragment.setNoteOriginal(savedInstanceState.getParcelable("noteOriginal"));
            detailFragment.setAttachmentUri(savedInstanceState.getParcelable("attachmentUri"));
            detailFragment.setOrientationChanged(savedInstanceState.getBoolean("orientationChanged"));
        }

        // Added the sketched image if present returning from SketchFragment
        if (detailFragment.getMainActivity().sketchUri != null) {
            Attachment mAttachment = new Attachment(detailFragment.getMainActivity().sketchUri, Constants.MIME_TYPE_SKETCH);
            detailFragment.addAttachment(mAttachment);
            detailFragment.getMainActivity().sketchUri = null;
            // Removes previous version of edited image
            if (detailFragment.getSketchEdited() != null) {
                detailFragment.getNoteTmp().getAttachmentsList().remove(detailFragment.getSketchEdited());
                detailFragment.setSketchEdited(null);
            }
        }

        detailFragment.init();

        detailFragment.setHasOptionsMenu(true);
        detailFragment.setRetainInstance(false);
    }
}