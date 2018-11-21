package it.feio.android.omninotes.FragmentHelpers.DetailFragment;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;

import java.io.File;

import it.feio.android.omninotes.DetailFragment;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.SketchFragment;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageHelper;

public class SketchHelper {
    private final DetailFragment detailFragment;

    public SketchHelper(DetailFragment detailFragment) {
        this.detailFragment = detailFragment;
    }

    public void takeSketch(Attachment attachment) {

        File f = StorageHelper.createNewAttachmentFile(detailFragment.getMainActivity(), Constants.MIME_TYPE_SKETCH_EXT);
        if (f == null) {
            detailFragment.getMainActivity().showMessage(R.string.error, ONStyle.ALERT);
            return;
        }
        detailFragment.setAttachmentUri(Uri.fromFile(f));

        // Forces portrait orientation to this fragment only
        detailFragment.getMainActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Fragments replacing
        FragmentTransaction transaction = detailFragment.getMainActivity().getSupportFragmentManager().beginTransaction();
        detailFragment.getMainActivity().animateTransition(transaction, detailFragment.getMainActivity().TRANSITION_HORIZONTAL);
        SketchFragment mSketchFragment = new SketchFragment();
        Bundle b = new Bundle();
        b.putParcelable(MediaStore.EXTRA_OUTPUT, detailFragment.getAttachmentUri());
        if (attachment != null) {
            b.putParcelable("base", attachment.getUri());
        }
        mSketchFragment.setArguments(b);
        transaction.replace(R.id.fragment_container, mSketchFragment, detailFragment.getMainActivity().FRAGMENT_SKETCH_TAG)
                .addToBackStack(detailFragment.getMainActivity().FRAGMENT_DETAIL_TAG).commit();
    }

    public void ProcessSketchResult(Uri attachmentUri, String mimeTypeSketch) {
        Attachment attachment;
        attachment = new Attachment(attachmentUri, mimeTypeSketch);
        detailFragment.addAttachment(attachment);
        detailFragment.getmAttachmentAdapter().notifyDataSetChanged();
        detailFragment.getmGridView().autoresize();
    }
}