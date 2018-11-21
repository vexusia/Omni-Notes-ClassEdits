package it.feio.android.omninotes.FragmentHelpers.DetailFragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;

import it.feio.android.omninotes.DetailFragment;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.IntentChecker;
import it.feio.android.omninotes.utils.StorageHelper;
import it.feio.android.omninotes.utils.TextHelper;

public class PhotoHelper {
    private final DetailFragment detailFragment;

    public PhotoHelper(DetailFragment detailFragment) {
        this.detailFragment = detailFragment;
    }

    public void HandleFabTakePhoto(Intent i) {
        if (IntentChecker.checkAction(i, Constants.ACTION_FAB_TAKE_PHOTO)) {
            takePhoto();
        }
    }

    public void HandleTakePhoto(Intent i) {
        if (IntentChecker.checkAction(i, Constants.ACTION_WIDGET, Constants.ACTION_WIDGET_TAKE_PHOTO)) {

            detailFragment.setAfterSavedReturnsToList(false);
            detailFragment.setShowKeyboard(true);

            //  with tags to set tag
            if (i.hasExtra(Constants.INTENT_WIDGET)) {
                String widgetId = i.getExtras().get(Constants.INTENT_WIDGET).toString();
                if (widgetId != null) {
                    String sqlCondition = detailFragment.getPrefs().getString(Constants.PREF_WIDGET_PREFIX + widgetId, "");
                    String categoryId = TextHelper.checkIntentCategory(sqlCondition);
                    if (categoryId != null) {
                        Category category;
                        try {
                            category = DbHelper.getInstance().getCategory(Long.parseLong(categoryId));
                            detailFragment.setNoteTmp(new Note());
                            detailFragment.getNoteTmp().setCategory(category);
                        } catch (NumberFormatException e) {
                            Log.e(Constants.TAG, "Category with not-numeric value!", e);
                        }
                    }
                }
            }

            // Sub-action is to take a photo
            if (IntentChecker.checkAction(i, Constants.ACTION_WIDGET_TAKE_PHOTO)) {
                takePhoto();
            }
        }
    }

    public void takePhoto() {
        // Checks for camera app available
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (!IntentChecker.isAvailable(detailFragment.getMainActivity(), intent, new String[]{PackageManager.FEATURE_CAMERA})) {
            detailFragment.getMainActivity().showMessage(R.string.feature_not_available_on_this_device, ONStyle.ALERT);

            return;
        }
        // Checks for created file validity
        File f = StorageHelper.createNewAttachmentFile(detailFragment.getMainActivity(), Constants.MIME_TYPE_IMAGE_EXT);
        if (f == null) {
            detailFragment.getMainActivity().showMessage(R.string.error, ONStyle.ALERT);
            return;
        }
        // Launches intent
        detailFragment.setAttachmentUri(Uri.fromFile(f));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, detailFragment.getAttachmentUri());
        detailFragment.startActivityForResult(intent, DetailFragment.TAKE_PHOTO);
    }
}