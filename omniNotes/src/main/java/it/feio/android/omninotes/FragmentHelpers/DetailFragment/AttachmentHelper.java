package it.feio.android.omninotes.FragmentHelpers.DetailFragment;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import it.feio.android.omninotes.DetailFragment;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.async.AttachmentTask;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.models.adapters.AttachmentAdapter;
import it.feio.android.omninotes.models.views.ExpandableHeightGridView;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.FileHelper;
import it.feio.android.omninotes.utils.IntentChecker;
import it.feio.android.omninotes.utils.StorageHelper;

public class AttachmentHelper {
    private final DetailFragment detailFragment;

    public AttachmentHelper(DetailFragment detailFragment) {
        this.detailFragment = detailFragment;
    }

    public void importAttachments(Intent i) {

        if (!i.hasExtra(Intent.EXTRA_STREAM)) return;

        if (i.getExtras().get(Intent.EXTRA_STREAM) instanceof Uri) {
            Uri uri = i.getParcelableExtra(Intent.EXTRA_STREAM);
            // Google Now passes Intent as text but with audio recording attached the case must be handled like this
            if (!Constants.INTENT_GOOGLE_NOW.equals(i.getAction())) {
                String name = FileHelper.getNameFromUri(detailFragment.getMainActivity(), uri);
                new AttachmentTask(detailFragment, uri, name, detailFragment).execute();
            }
        } else {
            ArrayList<Uri> uris = i.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            for (Uri uriSingle : uris) {
                String name = FileHelper.getNameFromUri(detailFragment.getMainActivity(), uriSingle);
                new AttachmentTask(detailFragment, uriSingle, name, detailFragment).execute();
            }
        }
    }

    public void initViewAttachments() {

        // Attachments position based on preferences
        if (detailFragment.getPrefs().getBoolean(Constants.PREF_ATTANCHEMENTS_ON_BOTTOM, false)) {
            detailFragment.getAttachmentsBelow().inflate();
        } else {
            detailFragment.getAttachmentsAbove().inflate();
        }
        detailFragment.setmGridView((ExpandableHeightGridView) detailFragment.getRoot().findViewById(R.id.gridview));

        // Some fields can be filled by third party application and are always shown
        detailFragment.setmAttachmentAdapter(new AttachmentAdapter(detailFragment.getMainActivity(), detailFragment.getNoteTmp().getAttachmentsList(), detailFragment.getmGridView()));

        // Initialzation of gridview for images
        detailFragment.getmGridView().setAdapter(detailFragment.getmAttachmentAdapter());
        detailFragment.getmGridView().autoresize();

        // Click events for images in gridview (zooms image)
        detailFragment.getmGridView().setOnItemClickListener((parent, v, position, id) -> {
            Attachment attachment = (Attachment) parent.getAdapter().getItem(position);
            Uri uri = attachment.getUri();
            Intent attachmentIntent;
            if (Constants.MIME_TYPE_FILES.equals(attachment.getMime_type())) {

                StartAttachmentActivity(attachment, uri);

                // Media files will be opened in internal gallery
            } else if (Constants.MIME_TYPE_IMAGE.equals(attachment.getMime_type())
                    || Constants.MIME_TYPE_SKETCH.equals(attachment.getMime_type())
                    || Constants.MIME_TYPE_VIDEO.equals(attachment.getMime_type())) {
                detailFragment.StartImageSketchVideoActivity(attachment);


            } else if (Constants.MIME_TYPE_AUDIO.equals(attachment.getMime_type())) {
                detailFragment.playback(v, attachment.getUri());
            }

        });

        detailFragment.SetGridLongClickListener();
    }

    public void StartAttachmentActivity(Attachment attachment, Uri uri) {
        Intent attachmentIntent;
        attachmentIntent = new Intent(Intent.ACTION_VIEW);
        attachmentIntent.setDataAndType(uri, StorageHelper.getMimeType(detailFragment.getMainActivity(),
                attachment.getUri()));
        attachmentIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent
                .FLAG_GRANT_WRITE_URI_PERMISSION);
        if (IntentChecker.isAvailable(detailFragment.getMainActivity().getApplicationContext(), attachmentIntent, null)) {
            detailFragment.startActivity(attachmentIntent);
        } else {
            detailFragment.getMainActivity().showMessage(R.string.feature_not_available_on_this_device, ONStyle.WARN);
        }
    }

    /**
     * Performs an action when long-click option is selected
     *
     * @param attachmentPosition
     * @param i                  item index
     */
    public void performAttachmentAction(int attachmentPosition, int i) {
        switch (detailFragment.getResources().getStringArray(R.array.attachments_actions_values)[i]) {
            case "share":
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                Attachment attachment = detailFragment.getmAttachmentAdapter().getItem(attachmentPosition);
                shareIntent.setType(StorageHelper.getMimeType(OmniNotes.getAppContext(), attachment.getUri()));
                shareIntent.putExtra(Intent.EXTRA_STREAM, attachment.getUri());
                if (IntentChecker.isAvailable(OmniNotes.getAppContext(), shareIntent, null)) {
                    detailFragment.startActivity(shareIntent);
                } else {
                    detailFragment.getMainActivity().showMessage(R.string.feature_not_available_on_this_device, ONStyle.WARN);
                }
                break;
            case "delete":
                detailFragment.removeAttachment(attachmentPosition);
                detailFragment.getmAttachmentAdapter().notifyDataSetChanged();
                detailFragment.getmGridView().autoresize();
                break;
            case "delete all":
                new MaterialDialog.Builder(detailFragment.getMainActivity())
                        .title(R.string.delete_all_attachments)
                        .positiveText(R.string.confirm)
                        .onPositive((materialDialog, dialogAction) -> detailFragment.removeAllAttachments())
                        .build()
                        .show();
                break;
            case "edit":
                detailFragment.takeSketch(detailFragment.getmAttachmentAdapter().getItem(attachmentPosition));
                break;
            default:
                Log.w(Constants.TAG, "No action available");
        }
    }

    public Uri getAttachmentUri() {
        return detailFragment.getAttachmentUri();
    }

    public void setAttachmentUri(Uri attachmentUri) {
        detailFragment.setAttachmentUri(attachmentUri);
    }


}