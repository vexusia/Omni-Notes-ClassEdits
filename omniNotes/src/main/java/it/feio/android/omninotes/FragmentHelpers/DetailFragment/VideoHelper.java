package it.feio.android.omninotes.FragmentHelpers.DetailFragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import it.feio.android.omninotes.DetailFragment;
import it.feio.android.omninotes.GalleryActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.helpers.PermissionsHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.IntentChecker;
import it.feio.android.omninotes.utils.StorageHelper;
import it.feio.android.omninotes.utils.TextHelper;

public class VideoHelper {
    private final DetailFragment detailFragment;
    public boolean isRecording = false;

    public boolean isRecording() {
        return isRecording;
    }

    public View isPlayingView = null;
    public long audioRecordingTimeStart;
    public long audioRecordingTime;

    public long getAudioRecordingTime() {
        return audioRecordingTime;
    }

    public VideoHelper(DetailFragment detailFragment) {
        this.detailFragment = detailFragment;
    }

    public void takeVideo() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (!IntentChecker.isAvailable(detailFragment.getMainActivity(), takeVideoIntent, new String[]{PackageManager.FEATURE_CAMERA})) {
            detailFragment.getMainActivity().showMessage(R.string.feature_not_available_on_this_device, ONStyle.ALERT);

            return;
        }
        // File is stored in custom ON folder to speedup the attachment
        File f = StorageHelper.createNewAttachmentFile(detailFragment.getMainActivity(), Constants.MIME_TYPE_VIDEO_EXT);
        if (f == null) {
            detailFragment.getMainActivity().showMessage(R.string.error, ONStyle.ALERT);
            return;
        }
        detailFragment.setAttachmentUri(Uri.fromFile(f));
        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, detailFragment.getAttachmentUri());
        String maxVideoSizeStr = "".equals(detailFragment.getPrefs().getString("settings_max_video_size",
                "")) ? "0" : detailFragment.getPrefs().getString("settings_max_video_size", "");
        long maxVideoSize = Long.parseLong(maxVideoSizeStr) * 1024L * 1024L;
        takeVideoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxVideoSize);
        detailFragment.startActivityForResult(takeVideoIntent, DetailFragment.TAKE_VIDEO);
    }

    public void ProcessTakeVideoResult(Intent intent) {
        Attachment attachment;// Gingerbread doesn't allow custom folder so data are retrieved from intent
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            attachment = new Attachment(detailFragment.getAttachmentUri(), Constants.MIME_TYPE_VIDEO);
        } else {
            attachment = new Attachment(intent.getData(), Constants.MIME_TYPE_VIDEO);
        }
        detailFragment.addAttachment(attachment);
        detailFragment.getmAttachmentAdapter().notifyDataSetChanged();
        detailFragment.getmGridView().autoresize();
    }

    /**
     * Audio recordings playback
     */
    public void playback(View v, Uri uri) {
        // Some recording is playing right now
        if (detailFragment.getmPlayer() != null && detailFragment.getmPlayer().isPlaying()) {
            if (isPlayingView != v) {
                // If the audio actually played is NOT the one from the click view the last one is played
                stopPlaying();
                isPlayingView = v;
                startPlaying(uri);
                detailFragment.replacePlayingAudioBitmap(v);
            } else {
                // Otherwise just stops playing
                stopPlaying();
            }
        } else {
            // If nothing is playing audio just plays
            isPlayingView = v;
            startPlaying(uri);
            detailFragment.replacePlayingAudioBitmap(v);
        }
    }

    public void startPlaying(Uri uri) {
        if (detailFragment.getmPlayer() == null) {
            detailFragment.setmPlayer(new MediaPlayer());
        }
        try {
            detailFragment.getmPlayer().setDataSource(detailFragment.getMainActivity(), uri);
            detailFragment.getmPlayer().prepare();
            detailFragment.getmPlayer().start();
            detailFragment.getmPlayer().setOnCompletionListener(mp -> {
                detailFragment.setmPlayer(null);
                if (isPlayingView != null) {
                    ((ImageView) isPlayingView.findViewById(R.id.gridview_item_picture)).setImageBitmap
                            (detailFragment.getRecordingBitmap());
                    detailFragment.setRecordingBitmap(null);
                    isPlayingView = null;
                }
            });
        } catch (IOException e) {
            Log.e(Constants.TAG, "prepare() failed", e);
            detailFragment.getMainActivity().showMessage(R.string.error, ONStyle.ALERT);
        }
    }

    public void stopPlaying() {
        if (detailFragment.getmPlayer() != null) {
            if (isPlayingView != null) {
                ((ImageView) isPlayingView.findViewById(R.id.gridview_item_picture)).setImageBitmap(detailFragment.getRecordingBitmap());
            }
            isPlayingView = null;
            detailFragment.setRecordingBitmap(null);
            detailFragment.getmPlayer().release();
            detailFragment.setmPlayer(null);
        }
    }

    public void startRecording(View v) {
        PermissionsHelper.requestPermission(detailFragment.getActivity(), Manifest.permission.RECORD_AUDIO,
                R.string.permission_audio_recording, detailFragment.getSnackBarPlaceholder(), () -> {

                    isRecording = true;
                    toggleAudioRecordingStop(v);

                    File f = StorageHelper.createNewAttachmentFile(detailFragment.getMainActivity(), Constants.MIME_TYPE_AUDIO_EXT);
                    if (f == null) {
                        detailFragment.getMainActivity().showMessage(R.string.error, ONStyle.ALERT);
                        return;
                    }
                    if (detailFragment.getmRecorder() == null) {
                        detailFragment.setmRecorder(new MediaRecorder());
                        detailFragment.getmRecorder().setAudioSource(MediaRecorder.AudioSource.MIC);
                        detailFragment.getmRecorder().setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                        detailFragment.getmRecorder().setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                        detailFragment.getmRecorder().setAudioEncodingBitRate(96000);
                        detailFragment.getmRecorder().setAudioSamplingRate(44100);
                    }
                    detailFragment.setRecordName(f.getAbsolutePath());
                    detailFragment.getmRecorder().setOutputFile(detailFragment.getRecordName());

                    try {
                        audioRecordingTimeStart = Calendar.getInstance().getTimeInMillis();
                        detailFragment.getmRecorder().prepare();
                        detailFragment.getmRecorder().start();
                    } catch (IOException | IllegalStateException e) {
                        Log.e(Constants.TAG, "prepare() failed", e);
                        detailFragment.getMainActivity().showMessage(R.string.error, ONStyle.ALERT);
                    }
                });
    }

    public void toggleAudioRecordingStop(View v) {
        if (isRecording) {
            ((TextView) v).setText(detailFragment.getString(R.string.stop));
            ((TextView) v).setTextColor(Color.parseColor("#ff0000"));
        }
    }

    public void stopRecording() {
        isRecording = false;
        if (detailFragment.getmRecorder() != null) {
            detailFragment.getmRecorder().stop();
            audioRecordingTime = Calendar.getInstance().getTimeInMillis() - audioRecordingTimeStart;
            detailFragment.getmRecorder().release();
            detailFragment.setmRecorder(null);
        }
    }
    public void StartImageSketchVideoActivity(Attachment attachment) {
        Intent attachmentIntent;// Title
        detailFragment.noteTmp.setTitle(detailFragment.getNoteTitle());
        detailFragment.noteTmp.setContent(detailFragment.getNoteContent());
        String title1 = TextHelper.parseTitleAndContent(detailFragment.getMainActivity(),
                detailFragment.noteTmp)[0].toString();
        // Images
        int clickedImage = 0;
        ArrayList<Attachment> images = new ArrayList<>();
        for (Attachment mAttachment : detailFragment.noteTmp.getAttachmentsList()) {
            if (Constants.MIME_TYPE_IMAGE.equals(mAttachment.getMime_type())
                    || Constants.MIME_TYPE_SKETCH.equals(mAttachment.getMime_type())
                    || Constants.MIME_TYPE_VIDEO.equals(mAttachment.getMime_type())) {
                images.add(mAttachment);
                if (mAttachment.equals(attachment)) {
                    clickedImage = images.size() - 1;
                }
            }
        }
        // Intent
        attachmentIntent = new Intent(detailFragment.getMainActivity(), GalleryActivity.class);
        attachmentIntent.putExtra(Constants.GALLERY_TITLE, title1);
        attachmentIntent.putParcelableArrayListExtra(Constants.GALLERY_IMAGES, images);
        attachmentIntent.putExtra(Constants.GALLERY_CLICKED_IMAGE, clickedImage);
        detailFragment.startActivity(attachmentIntent);
    }

    public void replacePlayingAudioBitmap(View v) {
        Drawable d = ((ImageView) v.findViewById(R.id.gridview_item_picture)).getDrawable();
        if (BitmapDrawable.class.isAssignableFrom(d.getClass())) {
            this.detailFragment.recordingBitmap = ((BitmapDrawable) d).getBitmap();
        } else {
            this.detailFragment.recordingBitmap = ((GlideBitmapDrawable) d.getCurrent()).getBitmap();
        }
        ((ImageView) v.findViewById(R.id.gridview_item_picture)).setImageBitmap(ThumbnailUtils
                .extractThumbnail(BitmapFactory.decodeResource(this.detailFragment.getMainActivity().getResources(),
                        R.drawable.stop), Constants.THUMBNAIL_SIZE, Constants.THUMBNAIL_SIZE));
    }
}