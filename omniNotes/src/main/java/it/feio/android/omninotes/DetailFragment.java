/*
 * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.feio.android.omninotes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;

import com.afollestad.materialdialogs.MaterialDialog;
import com.neopixl.pixlui.components.edittext.EditText;
import com.neopixl.pixlui.components.textview.TextView;
import com.pushbullet.android.extension.MessagingExtension;
import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Style;
import it.feio.android.checklistview.interfaces.CheckListChangedListener;
import it.feio.android.checklistview.models.CheckListViewItem;
import it.feio.android.checklistview.models.ChecklistManager;
import it.feio.android.omninotes.FragmentHelpers.DetailFragment.ActivityCreateHelper;
import it.feio.android.omninotes.FragmentHelpers.DetailFragment.AttachmentHelper;
import it.feio.android.omninotes.FragmentHelpers.DetailFragment.ChecklistHelper;
import it.feio.android.omninotes.FragmentHelpers.DetailFragment.GeoHelper;
import it.feio.android.omninotes.FragmentHelpers.DetailFragment.GeoUtilResultListener;
import it.feio.android.omninotes.FragmentHelpers.DetailFragment.GoogleNowHelper;
import it.feio.android.omninotes.FragmentHelpers.DetailFragment.NoteDisposalHelper;
import it.feio.android.omninotes.FragmentHelpers.DetailFragment.NoteLockHelper;
import it.feio.android.omninotes.FragmentHelpers.DetailFragment.OptionsHelper;
import it.feio.android.omninotes.FragmentHelpers.DetailFragment.PhotoHelper;
import it.feio.android.omninotes.FragmentHelpers.DetailFragment.SaveHelper;
import it.feio.android.omninotes.FragmentHelpers.DetailFragment.SketchHelper;
import it.feio.android.omninotes.FragmentHelpers.DetailFragment.TagHelper;
import it.feio.android.omninotes.FragmentHelpers.DetailFragment.TimestampHelper;
import it.feio.android.omninotes.FragmentHelpers.DetailFragment.VideoHelper;
import it.feio.android.omninotes.FragmentHelpers.DetailFragment.ViewHelper;
import it.feio.android.omninotes.async.AttachmentTask;
import it.feio.android.omninotes.async.bus.PushbulletReplyEvent;
import it.feio.android.omninotes.async.bus.SwitchFragmentEvent;
import it.feio.android.omninotes.async.notes.NoteProcessorDelete;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.helpers.AttachmentsHelper;
import it.feio.android.omninotes.helpers.PermissionsHelper;
import it.feio.android.omninotes.helpers.date.DateHelper;
import it.feio.android.omninotes.models.*;
import it.feio.android.omninotes.models.adapters.AttachmentAdapter;
import it.feio.android.omninotes.models.adapters.NavDrawerCategoryAdapter;
import it.feio.android.omninotes.models.listeners.OnAttachingFileListener;
import it.feio.android.omninotes.models.listeners.OnGeoUtilResultListener;
import it.feio.android.omninotes.models.listeners.OnNoteSaved;
import it.feio.android.omninotes.models.listeners.OnReminderPickedListener;
import it.feio.android.omninotes.models.views.ExpandableHeightGridView;
import it.feio.android.omninotes.utils.*;
import it.feio.android.omninotes.utils.Display;
import it.feio.android.pixlui.links.TextLinkClickListener;

import java.io.File;
import java.util.*;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;


public class DetailFragment extends BaseFragment implements OnReminderPickedListener, OnTouchListener,
		OnAttachingFileListener, TextWatcher, CheckListChangedListener, OnNoteSaved,
		OnGeoUtilResultListener {

	public static final int TAKE_PHOTO = 1;
	public static final int TAKE_VIDEO = 2;
	public static final int SET_PASSWORD = 3;
	public static final int SKETCH = 4;
	public static final int CATEGORY = 5;
	public static final int DETAIL = 6;
	public static final int FILES = 7;
	public static final int RC_READ_EXTERNAL_STORAGE_PERMISSION = 1;
	public final ActivityCreateHelper activityCreateHelper = new ActivityCreateHelper(this);
	public final NoteLockHelper noteLockHelper = new NoteLockHelper(this);
	public final GoogleNowHelper googleNowHelper = new GoogleNowHelper(this);
	public final PhotoHelper photoHelper = new PhotoHelper(this);
	public final AttachmentHelper attachmentHelper = new AttachmentHelper(this);
	public final VideoHelper videoHelper = new VideoHelper(this);
	public final ViewHelper viewHelper = new ViewHelper(this);
	public final TagHelper tagHelper = new TagHelper(this);
	public final GeoHelper geoHelper = new GeoHelper(this);
	public final OptionsHelper optionsHelper = new OptionsHelper(this);
	public final ChecklistHelper checklistHelper = new ChecklistHelper(this);
	public final SketchHelper sketchHelper = new SketchHelper(this);
	public final TimestampHelper timestampHelper = new TimestampHelper(this);
	public final SaveHelper saveHelper = new SaveHelper(this);
	public final NoteDisposalHelper noteDisposalHelper = new NoteDisposalHelper(this);

	@BindView(R.id.detail_root)
	ViewGroup root;
	@BindView(R.id.detail_title)
	public EditText title;
	@BindView(R.id.detail_content)
	public EditText content;
	@BindView(R.id.detail_attachments_above)
	ViewStub attachmentsAbove;
	@BindView(R.id.detail_attachments_below)
	ViewStub attachmentsBelow;
	@Nullable
	@BindView(R.id.gridview)
	ExpandableHeightGridView mGridView;
	@BindView(R.id.location)
	TextView locationTextView;
	@BindView(R.id.detail_timestamps)
	View timestampsView;
	@BindView(R.id.reminder_layout)
	LinearLayout reminder_layout;
	@BindView(R.id.reminder_icon)
	ImageView reminderIcon;
	@BindView(R.id.datetime)
	TextView datetime;
	@BindView(R.id.detail_tile_card)
	View titleCardView;
	@BindView(R.id.content_wrapper)
	ScrollView scrollView;
	@BindView(R.id.creation)
	TextView creationTextView;
	@BindView(R.id.last_modification)
	TextView lastModificationTextView;
	@BindView(R.id.title_wrapper)
	View titleWrapperView;
	@BindView(R.id.tag_marker)
	View tagMarkerView;
	@BindView(R.id.detail_wrapper)
	ViewManager detailWrapperView;
	@BindView(R.id.snackbar_placeholder)
	View snackBarPlaceholder;

	public OnDateSetListener onDateSetListener;
	public OnTimeSetListener onTimeSetListener;
	public boolean goBack = false;
	View toggleChecklistView;
	private Uri attachmentUri;
	private AttachmentAdapter mAttachmentAdapter;
	public MaterialDialog attachmentDialog;
	public Note note;
	public Note noteTmp;
	public Note noteOriginal;
	// Audio recording
	private String recordName;
	private MediaRecorder mRecorder = null;
	private MediaPlayer mPlayer = null;
	public Bitmap recordingBitmap;
	public ChecklistManager mChecklistManager;
	// Values to print result
	private String exitMessage;
	private Style exitCroutonStyle = ONStyle.CONFIRM;
	// Flag to check if after editing it will return to ListActivity or not
	// and in the last case a Toast will be shown instead than Crouton
	private boolean afterSavedReturnsToList = true;
	private boolean showKeyboard = false;
	private boolean swiping;
	private int startSwipeX;
	public SharedPreferences prefs;
	private boolean orientationChanged;
	private DetailFragment mFragment;
	private Attachment sketchEdited;
	private int contentLineCounter = 1;
	public int contentCursorPosition;
	private ArrayList<String> mergedNotesIds;
	private MainActivity mainActivity;
	private boolean activityPausing;

	public void setGoBack(boolean value) {
		this.goBack = value;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFragment = this;
	}

	@Override
	public void onStart() {
		super.onStart();
		EventBus.getDefault().post(new SwitchFragmentEvent(SwitchFragmentEvent.Direction.CHILDREN));
		EventBus.getDefault().register(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EventBus.getDefault().unregister(this);
		GeocodeHelper.stop();
	}

	@Override
	public void onResume() {
		super.onResume();
		activityPausing = false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_detail, container, false);
		ButterKnife.bind(this, view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		// Force the navigation drawer to stay opened if tablet mode is on, otherwise has to stay closed

		// Restored temp note after orientation change

		// Added the sketched image if present returning from SketchFragment
		super.onActivityCreated(savedInstanceState);
		activityCreateHelper.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (noteTmp != null) {
			noteTmp.setTitle(getNoteTitle());
			noteTmp.setContent(getNoteContent());
			outState.putParcelable("noteTmp", noteTmp);
			outState.putParcelable("note", note);
			outState.putParcelable("noteOriginal", noteOriginal);
			outState.putParcelable("attachmentUri", attachmentUri);
			outState.putBoolean("orientationChanged", orientationChanged);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		super.onPause();

		activityPausing = true;

		// Checks "goBack" value to avoid performing a double saving
		if (!goBack) {
			saveHelper.saveNote(this);
		}

		if (toggleChecklistView != null) {
			KeyboardUtils.hideKeyboard(toggleChecklistView);
			content.clearFocus();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (getResources().getConfiguration().orientation != newConfig.orientation) {
			orientationChanged = true;
		}
	}

	public void init() {

		// Handling of Intent actions
		handleIntents();

		if (noteOriginal == null) {
			noteOriginal = getArguments().getParcelable(Constants.INTENT_NOTE);
		}

		if (note == null) {
			note = new Note(noteOriginal);
		}

		if (noteTmp == null) {
			noteTmp = new Note(note);
		}

		if (noteTmp.isLocked() && !noteTmp.isPasswordChecked()) {
			noteLockHelper.checkNoteLock(noteTmp);
			return;
		}

		initViews();
	}

	public void handleIntents() {
		Intent i = mainActivity.getIntent();
		HandleActionMerge(i);
		// Action called from home shortcut
		HandleActionNotification(i);
		// Check if is launched from a widget
		photoHelper.HandleTakePhoto(i);
		photoHelper.HandleFabTakePhoto(i);
		// Handles third party apps requests of sharing
		googleNowHelper.HandleGoogleNow(i);
		HandleActionWidget(i);
		i.setAction(null);
	}

	public void HandleActionWidget(Intent i) {
		if (IntentChecker.checkAction(i, Intent.ACTION_MAIN, Constants.ACTION_WIDGET_SHOW_LIST, Constants
				.ACTION_SHORTCUT_WIDGET, Constants.ACTION_WIDGET)) {
			showKeyboard = true;
		}
	}

	public void HandleActionNotification(Intent i) {
		if (IntentChecker.checkAction(i, Constants.ACTION_SHORTCUT, Constants.ACTION_NOTIFICATION_CLICK)) {
			afterSavedReturnsToList = false;
			noteOriginal = DbHelper.getInstance().getNote(i.getLongExtra(Constants.INTENT_KEY, 0));
			// Checks if the note pointed from the shortcut has been deleted
			try {
				note = new Note(noteOriginal);
				noteTmp = new Note(noteOriginal);
			} catch (NullPointerException e) {
				mainActivity.showToast(getText(R.string.shortcut_note_deleted), Toast.LENGTH_LONG);
				mainActivity.finish();
			}
		}
	}

	public void HandleActionMerge(Intent i) {
		if (IntentChecker.checkAction(i, Constants.ACTION_MERGE)) {
			noteOriginal = new Note();
			note = new Note(noteOriginal);
			noteTmp = getArguments().getParcelable(Constants.INTENT_NOTE);
			if (i.getStringArrayListExtra("merged_notes") != null) {
				mergedNotesIds = i.getStringArrayListExtra("merged_notes");
			}
		}
	}

	public void importAttachments(Intent i) {

		attachmentHelper.importAttachments(i);
	}

	@SuppressLint("NewApi")
	private void initViews() {

		// Sets onTouchListener to the whole activity to swipe notes
		root.setOnTouchListener(this);

		// Overrides font sizes with the one selected from user
		Fonts.overrideTextSize(mainActivity, prefs, root);

		// Color of tag marker if note is tagged a function is active in preferences
		tagHelper.setTagMarkerColor(noteTmp.getCategory());

		viewHelper.initViewTitle();

		viewHelper.initViewContent();

		viewHelper.initViewLocation();

		attachmentHelper.initViewAttachments();

		viewHelper.initViewReminder();

		viewHelper.initViewFooter();
	}

	private void initViewFooter() {
		// Footer dates of creation...

		// ... and last modification
		viewHelper.initViewFooter();
	}

	private void initViewReminder() {

		// Preparation for reminder icon

		// Reminder
		viewHelper.initViewReminder();
	}

	private void initViewLocation() {

		// Automatic location insertion

		viewHelper.initViewLocation();
	}

	public void getLocation(OnGeoUtilResultListener onGeoUtilResultListener) {
		PermissionsHelper.requestPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION, R.string
				.permission_coarse_location, snackBarPlaceholder, () -> GeocodeHelper.getLocation
				(onGeoUtilResultListener));
	}


	public void StartImageSketchVideoActivity(Attachment attachment) {
		this.videoHelper.StartImageSketchVideoActivity(attachment);
	}

	public void SetGridLongClickListener() {
		mGridView.setOnItemLongClickListener((parent, v, position, id) -> {
			// To avoid deleting audio attachment during playback
			if (mPlayer != null) return false;
			List<String> items = Arrays.asList(getResources().getStringArray(R.array.attachments_actions));
			if (!Constants.MIME_TYPE_SKETCH.equals(mAttachmentAdapter.getItem(position).getMime_type())) {
				items = items.subList(0, items.size() - 1);
			}
			Attachment attachment = mAttachmentAdapter.getItem(position);
			new MaterialDialog.Builder(mainActivity)
					.title(attachment.getName() + " (" + AttachmentsHelper.getSize(attachment) + ")")
					.items(items.toArray(new String[items.size()]))
					.itemsCallback((materialDialog, view, i, charSequence) ->
							attachmentHelper.performAttachmentAction(position, i))
					.build()
					.show();
			return true;
		});
	}

	private void initViewTitle() {
		// To avoid dropping here the  dragged checklist items
		//When editor action is pressed focus is moved to last character in content field
		viewHelper.initViewTitle();
	}

	private void initViewContent() {

		// Avoids focused line goes under the keyboard

		// Restore checklist
		viewHelper.initViewContent();
	}

	/**
	 * Force focus and shows soft keyboard. Only happens if it's a new note, without shared content.
	 * {@link showKeyboard} is used to check if the note is created from shared content.
	 */
	@SuppressWarnings("JavadocReference")
	public void requestFocus(final EditText view) {
		if (note.get_id() == null && !noteTmp.isChanged(note) && showKeyboard) {
			KeyboardUtils.showKeyboard(view);
		}
	}

	/**
	 * Colors tag marker in note's title and content elements
	 */
	private void setTagMarkerColor(Category tag) {

		// Checking preference
		tagHelper.setTagMarkerColor(tag);
	}

	private void displayLocationDialog() {
		getLocation(new GeoUtilResultListener(mainActivity, mFragment, noteTmp));
	}

	public SharedPreferences getPrefs() {
		return prefs;
	}

	public boolean isOrientationChanged() {
		return orientationChanged;
	}

	public Note getNoteOriginal() {
		return noteOriginal;
	}

	public Note getNote() {
		return note;
	}

	public Attachment getSketchEdited() {
		return sketchEdited;
	}

	public MainActivity getMainActivity() {
		return mainActivity;
	}

	public Note getNoteTmp() {
		return noteTmp;
	}

	public void setPrefs(SharedPreferences prefs) {
		this.prefs = prefs;
	}

	public void setOrientationChanged(boolean orientationChanged) {
		this.orientationChanged = orientationChanged;
	}

	public void setNoteOriginal(Note noteOriginal) {
		this.noteOriginal = noteOriginal;
	}

	public void setNote(Note note) {
		this.note = note;
	}

	public void setSketchEdited(Attachment sketchEdited) {
		this.sketchEdited = sketchEdited;
	}

	public void setMainActivity(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	public void setNoteTmp(Note noteTmp) {
		this.noteTmp = noteTmp;
	}

	public boolean isAfterSavedReturnsToList() {
		return afterSavedReturnsToList;
	}

	public void setAfterSavedReturnsToList(boolean afterSavedReturnsToList) {
		this.afterSavedReturnsToList = afterSavedReturnsToList;
	}

	public boolean isShowKeyboard() {
		return showKeyboard;
	}

	public void setShowKeyboard(boolean showKeyboard) {
		this.showKeyboard = showKeyboard;
	}

	public ViewGroup getRoot() {
		return root;
	}

	public ViewStub getAttachmentsBelow() {
		return attachmentsBelow;
	}

	public AttachmentAdapter getmAttachmentAdapter() {
		return mAttachmentAdapter;
	}

	@Nullable
	public ExpandableHeightGridView getmGridView() {
		return mGridView;
	}

	public ViewStub getAttachmentsAbove() {
		return attachmentsAbove;
	}

	public Uri getAttachmentUri() {
		return attachmentUri;
	}

	public void setmAttachmentAdapter(AttachmentAdapter mAttachmentAdapter) {
		this.mAttachmentAdapter = mAttachmentAdapter;
	}

	public void setmGridView(@Nullable ExpandableHeightGridView mGridView) {
		this.mGridView = mGridView;
	}

	public void setAttachmentUri(Uri attachmentUri) {
		this.attachmentUri = attachmentUri;
	}

	public View getSnackBarPlaceholder() {
		return snackBarPlaceholder;
	}

	public MediaPlayer getmPlayer() {
		return mPlayer;
	}

	public MediaRecorder getmRecorder() {
		return mRecorder;
	}

	public Bitmap getRecordingBitmap() {
		return recordingBitmap;
	}

	public String getRecordName() {
		return recordName;
	}

	public void setmPlayer(MediaPlayer mPlayer) {
		this.mPlayer = mPlayer;
	}

	public void setmRecorder(MediaRecorder mRecorder) {
		this.mRecorder = mRecorder;
	}

	public void setRecordingBitmap(Bitmap recordingBitmap) {
		this.recordingBitmap = recordingBitmap;
	}

	public void setRecordName(String recordName) {
		this.recordName = recordName;
	}

	@Override
	public void onLocationRetrieved(Location location) {
		geoHelper.onLocationRetrieved(location);
	}

	@Override
	public void onLocationUnavailable() {
		geoHelper.onLocationUnavailable();
	}

	@Override
	public void onAddressResolved(String address) {
		geoHelper.onAddressResolved(address);
	}

	@Override
	public void onCoordinatesResolved(Location location, String address) {
		geoHelper.onCoordinatesResolved(location, address);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_detail, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {

		// Closes search view if left open in List fragment
		MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
		if (searchMenuItem != null) {
			MenuItemCompat.collapseActionView(searchMenuItem);
		}

		boolean newNote = noteTmp.get_id() == null;

		menu.findItem(R.id.menu_checklist_on).setVisible(!noteTmp.isChecklist());
		menu.findItem(R.id.menu_checklist_off).setVisible(noteTmp.isChecklist());
		menu.findItem(R.id.menu_lock).setVisible(!noteTmp.isLocked());
		menu.findItem(R.id.menu_unlock).setVisible(noteTmp.isLocked());
		// If note is trashed only this options will be available from menu
		if (noteTmp.isTrashed()) {
			menu.findItem(R.id.menu_untrash).setVisible(true);
			menu.findItem(R.id.menu_delete).setVisible(true);
			// Otherwise all other actions will be available
		} else {
			menu.findItem(R.id.menu_add_shortcut).setVisible(!newNote);
			menu.findItem(R.id.menu_archive).setVisible(!newNote && !noteTmp.isArchived());
			menu.findItem(R.id.menu_unarchive).setVisible(!newNote && noteTmp.isArchived());
			menu.findItem(R.id.menu_trash).setVisible(!newNote);
		}
	}

	@SuppressLint("NewApi")
	public boolean goHome() {
		videoHelper.stopPlaying();

		// The activity has managed a shared intent from third party app and
		// performs a normal onBackPressed instead of returning back to ListActivity
		if (!afterSavedReturnsToList) {
			if (!TextUtils.isEmpty(exitMessage)) {
				mainActivity.showToast(exitMessage, Toast.LENGTH_SHORT);
			}
			mainActivity.finish();

		} else {

			if (!TextUtils.isEmpty(exitMessage) && exitCroutonStyle != null) {
				mainActivity.showMessage(exitMessage, exitCroutonStyle);
			}

			// Otherwise the result is passed to ListActivity
			if (mainActivity != null && mainActivity.getSupportFragmentManager() != null) {
				mainActivity.getSupportFragmentManager().popBackStack();
				if (mainActivity.getSupportFragmentManager().getBackStackEntryCount() == 1) {
					mainActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
					if (mainActivity.getDrawerToggle() != null) {
						mainActivity.getDrawerToggle().setDrawerIndicatorEnabled(true);
					}
					EventBus.getDefault().post(new SwitchFragmentEvent(SwitchFragmentEvent.Direction.PARENT));
				}
			}
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		optionsHelper.onOptionsItemSelected(item);
		return super.onOptionsItemSelected(item);
	}

	public void showNoteInfo() {
		noteTmp.setTitle(getNoteTitle());
		noteTmp.setContent(getNoteContent());
		Intent intent = new Intent(getContext(), NoteInfosActivity.class);
		intent.putExtra(Constants.INTENT_NOTE, (android.os.Parcelable) noteTmp);
		startActivity(intent);

	}

	public void navigateUp() {
		afterSavedReturnsToList = true;
		saveAndExit(this);
	}

	/**
	 * Toggles checklist view
	 */
	public void toggleChecklist2() {
		checklistHelper.toggleChecklist2();
	}

	@SuppressLint("NewApi")
	public void toggleChecklist2(final boolean keepChecked, final boolean showChecks) {
		// Get instance and set options to convert EditText to CheckListView

		// Links parsing options

		// Switches the views

		// Switches the views
		checklistHelper.toggleChecklist2(keepChecked, showChecks);
	}

	/**
	 * Categorize note choosing from a list of previously created categories
	 */
	public void categorizeNote() {
		// Retrieves all available categories
		final ArrayList<Category> categories = DbHelper.getInstance().getCategories();

		String currentCategory = noteTmp.getCategory() != null ? String.valueOf(noteTmp.getCategory().getId()) : null;

		final MaterialDialog dialog = new MaterialDialog.Builder(mainActivity)
				.title(R.string.categorize_as)
				.adapter(new NavDrawerCategoryAdapter(mainActivity, categories, currentCategory), null)
				.positiveText(R.string.add_category)
				.positiveColorRes(R.color.colorPrimary)
				.negativeText(R.string.remove_category)
				.negativeColorRes(R.color.colorAccent)
				.callback(new MaterialDialog.ButtonCallback() {
					@Override
					public void onPositive(MaterialDialog dialog) {
						Intent intent = new Intent(mainActivity, CategoryActivity.class);
						intent.putExtra("noHome", true);
						startActivityForResult(intent, CATEGORY);
					}

					@Override
					public void onNegative(MaterialDialog dialog) {
						noteTmp.setCategory(null);
						tagHelper.setTagMarkerColor(null);
					}
				})
				.build();

		dialog.getListView().setOnItemClickListener((parent, view, position, id) -> {
			noteTmp.setCategory(categories.get(position));
			tagHelper.setTagMarkerColor(categories.get(position));
			dialog.dismiss();
		});

		dialog.show();
	}

	public void showAttachmentsPopup() {
		LayoutInflater inflater = mainActivity.getLayoutInflater();
		final View layout = inflater.inflate(R.layout.attachment_dialog, null);

		attachmentDialog = new MaterialDialog.Builder(mainActivity)
				.autoDismiss(false)
				.customView(layout, false)
				.build();
		attachmentDialog.show();

		// Camera
		android.widget.TextView cameraSelection = (android.widget.TextView) layout.findViewById(R.id.camera);
		cameraSelection.setOnClickListener(new AttachmentOnClickListener());
		// Audio recording
		android.widget.TextView recordingSelection = (android.widget.TextView) layout.findViewById(R.id.recording);
		videoHelper.toggleAudioRecordingStop(recordingSelection);
		recordingSelection.setOnClickListener(new AttachmentOnClickListener());
		// Video recording
		android.widget.TextView videoSelection = (android.widget.TextView) layout.findViewById(R.id.video);
		videoSelection.setOnClickListener(new AttachmentOnClickListener());
		// Files
		android.widget.TextView filesSelection = (android.widget.TextView) layout.findViewById(R.id.files);
		filesSelection.setOnClickListener(new AttachmentOnClickListener());
		// Sketch
		android.widget.TextView sketchSelection = (android.widget.TextView) layout.findViewById(R.id.sketch);
		sketchSelection.setOnClickListener(new AttachmentOnClickListener());
		// Location
		android.widget.TextView locationSelection = (android.widget.TextView) layout.findViewById(R.id.location);
		locationSelection.setOnClickListener(new AttachmentOnClickListener());
		// Time
		android.widget.TextView timeStampSelection = (android.widget.TextView) layout.findViewById(R.id.timestamp);
		timeStampSelection.setOnClickListener(new AttachmentOnClickListener());
		// Desktop note with PushBullet
		android.widget.TextView pushbulletSelection = (android.widget.TextView) layout.findViewById(R.id.pushbullet);
		pushbulletSelection.setVisibility(View.VISIBLE);
		pushbulletSelection.setOnClickListener(new AttachmentOnClickListener());
	}



	public void takeSketch(Attachment attachment) {

		// Forces portrait orientation to this fragment only

		// Fragments replacing
		sketchHelper.takeSketch(attachment);
	}

	private void addTimestamp() {
		timestampHelper.addTimestamp();
	}

	@SuppressLint("NewApi")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// Fetch uri from activities, store into adapter and refresh adapter
		Attachment attachment;
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case TAKE_PHOTO:
					ProcessTakePhotoResult();
					break;
				case TAKE_VIDEO:
					videoHelper.ProcessTakeVideoResult(intent);
					break;
				case FILES:
					onActivityResultManageReceivedFiles(intent);
					break;
				case SET_PASSWORD:
					ProcessSetPasswordResult();
					break;
				case SKETCH:
					sketchHelper.ProcessSketchResult(attachmentUri, Constants.MIME_TYPE_SKETCH);
					break;
				case CATEGORY:
					ProcessCategoryResult(intent);
					break;
				case DETAIL:
					mainActivity.showMessage(R.string.note_updated, ONStyle.CONFIRM);
					break;
				default:
					Log.e(Constants.TAG, "Wrong element choosen: " + requestCode);
			}
		}
	}

	public void ProcessCategoryResult(Intent intent) {
		mainActivity.showMessage(R.string.category_saved, ONStyle.CONFIRM);
		Category category = intent.getParcelableExtra("category");
		noteTmp.setCategory(category);
		tagHelper.setTagMarkerColor(category);
	}

	public void ProcessSketchResult(Uri attachmentUri, String mimeTypeSketch) {
		sketchHelper.ProcessSketchResult(attachmentUri, mimeTypeSketch);
	}

	public void ProcessSetPasswordResult() {
		noteTmp.setPasswordChecked(true);
		lockUnlock();
	}


	public void ProcessTakePhotoResult() {
		Attachment attachment;
		attachment = new Attachment(attachmentUri, Constants.MIME_TYPE_IMAGE);
		addAttachment(attachment);
		mAttachmentAdapter.notifyDataSetChanged();
		mGridView.autoresize();
	}

	private void onActivityResultManageReceivedFiles(Intent intent) {
		List<Uri> uris = new ArrayList<>();
		if (Build.VERSION.SDK_INT > 16 && intent.getClipData() != null) {
			for (int i = 0; i < intent.getClipData().getItemCount(); i++) {
				uris.add(intent.getClipData().getItemAt(i).getUri());
			}
		} else {
			uris.add(intent.getData());
		}
		for (Uri uri : uris) {
			String name = FileHelper.getNameFromUri(mainActivity, uri);
			new AttachmentTask(this, uri, name, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	public void saveAndExit(OnNoteSaved mOnNoteSaved) {
		if (isAdded()) {
			exitMessage = getString(R.string.note_updated);
			exitCroutonStyle = ONStyle.CONFIRM;
			goBack = true;
			saveHelper.saveNote(mOnNoteSaved);
		}
	}

	/**
	 * Save new notes, modify them or archive
	 */
	void saveNote(OnNoteSaved mOnNoteSaved) {

		// Changed fields

		// Check if some text or attachments of any type have been inserted or is an empty note

		saveHelper.saveNote(mOnNoteSaved);
	}

	/**
	 * Checks if nothing is changed to avoid committing if possible (check)
	 */
	private boolean saveNotNeeded() {
		return saveHelper.saveNotNeeded();
	}

	/**
	 * Checks if only tag, archive or trash status have been changed
	 * and then force to not update last modification date*
	 */
	private boolean lastModificationUpdatedNeeded() {
		return saveHelper.lastModificationUpdatedNeeded();
	}

	@Override
	public void onNoteSaved(Note noteSaved) {
		saveHelper.onNoteSaved(noteSaved);
	}

	public void deleteMergedNotes(List<String> mergedNotesIds) {
		ArrayList<Note> notesToDelete = new ArrayList<Note>();
		if (mergedNotesIds != null) {
			for (String mergedNoteId : mergedNotesIds) {
				Note note = new Note();
				note.set_id(Long.valueOf(mergedNoteId));
				notesToDelete.add(note);
			}
			new NoteProcessorDelete(notesToDelete).process();
		}
	}

	public String getNoteTitle() {
		if (title != null && !TextUtils.isEmpty(title.getText())) {
			return title.getText().toString();
		} else {
			return "";
		}
	}

	public String getNoteContent() {
		String contentText = "";
		if (!noteTmp.isChecklist()) {
			// Due to checklist library introduction the returned EditText class is no more a
			// com.neopixl.pixlui.components.edittext.EditText but a standard android.widget.EditText
			View contentView = root.findViewById(R.id.detail_content);
			if (contentView instanceof EditText) {
				contentText = ((EditText) contentView).getText().toString();
			} else if (contentView instanceof android.widget.EditText) {
				contentText = ((android.widget.EditText) contentView).getText().toString();
			}
		} else {
			if (mChecklistManager != null) {
				mChecklistManager.keepChecked(true).showCheckMarks(true);
				contentText = mChecklistManager.getText();
			}
		}
		return contentText;
	}

	/**
	 * Updates share intent
	 */
	public void shareNote() {
		Note sharedNote = new Note(noteTmp);
		sharedNote.setTitle(getNoteTitle());
		sharedNote.setContent(getNoteContent());
		mainActivity.shareNote(sharedNote);
	}

	/**
	 * Notes locking with security password to avoid viewing, editing or deleting from unauthorized
	 */
	public void lockNote() {
		this.noteLockHelper.lockNote();
	}

	public void lockUnlock() {
		this.noteLockHelper.lockUnlock();
	}

	/**
	 * Used to set actual reminder state when initializing a note to be edited
	 */
	public String initReminder(Note note) {
		if (noteTmp.getAlarm() == null) {
			return "";
		}
		long reminder = parseLong(note.getAlarm());
		String rrule = note.getRecurrenceRule();
		if (!TextUtils.isEmpty(rrule)) {
			return DateHelper.getNoteRecurrentReminderText(reminder, rrule);
		} else {
			return DateHelper.getNoteReminderText(reminder);
		}
	}

	/**
	 * Audio recordings playback
	 */
	public void playback(View v, Uri uri) {
		// Some recording is playing right now
		videoHelper.playback(v, uri);
	}

	public void replacePlayingAudioBitmap(View v) {
		this.videoHelper.replacePlayingAudioBitmap(v);
	}

	public void fade(final View v, boolean fadeIn) {

		int anim = R.animator.fade_out_support;
		int visibilityTemp = View.GONE;

		if (fadeIn) {
			anim = R.animator.fade_in_support;
			visibilityTemp = View.VISIBLE;
		}

		final int visibility = visibilityTemp;

		// Checks if user has left the app
		if (mainActivity != null) {
			Animation mAnimation = AnimationUtils.loadAnimation(mainActivity, anim);
			mAnimation.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					// Nothing to do
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// Nothing to do
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					v.setVisibility(visibility);
				}
			});
			v.startAnimation(mAnimation);
		}
	}

	/**
	 * Adding shortcut on Home screen
	 */
	public void addShortcut() {
		ShortcutHelper.addShortcut(OmniNotes.getAppContext(), noteTmp);
		mainActivity.showMessage(R.string.shortcut_added, ONStyle.INFO);
	}

	TextLinkClickListener textLinkClickListener = new TextLinkClickListener() {
		@Override
		public void onTextLinkClick(View view, final String clickedString, final String url) {
			new MaterialDialog.Builder(mainActivity)
					.content(clickedString)
					.negativeColorRes(R.color.colorPrimary)
					.positiveText(R.string.open)
					.negativeText(R.string.copy)
					.callback(new MaterialDialog.ButtonCallback() {
						@Override
						public void onPositive(MaterialDialog dialog) {
							boolean error = false;
							Intent intent = null;
							try {
								intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
								intent.addCategory(Intent.CATEGORY_BROWSABLE);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							} catch (NullPointerException e) {
								error = true;
							}

							if (intent == null
									|| error
									|| !IntentChecker
									.isAvailable(
											mainActivity,
											intent,
											new String[]{PackageManager.FEATURE_CAMERA})) {
								mainActivity.showMessage(R.string.no_application_can_perform_this_action,
										ONStyle.ALERT);

							} else {
								startActivity(intent);
							}
						}

						@Override
						public void onNegative(MaterialDialog dialog) {
							android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
									mainActivity
											.getSystemService(Activity.CLIPBOARD_SERVICE);
							android.content.ClipData clip = android.content.ClipData.newPlainText("text label",
									clickedString);
							clipboard.setPrimaryClip(clip);
						}
					}).build().show();
			View clickedView = noteTmp.isChecklist() ? toggleChecklistView : content;
			clickedView.clearFocus();
			KeyboardUtils.hideKeyboard(clickedView);
			new Handler().post(() -> {
				View clickedView1 = noteTmp.isChecklist() ? toggleChecklistView : content;
				KeyboardUtils.hideKeyboard(clickedView1);
			});
		}
	};

	@SuppressLint("NewApi")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();

		switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN:
				Log.v(Constants.TAG, "MotionEvent.ACTION_DOWN");
				int w;

				Point displaySize = Display.getUsableSize(mainActivity);
				w = displaySize.x;

				if (x < Constants.SWIPE_MARGIN || x > w - Constants.SWIPE_MARGIN) {
					swiping = true;
					startSwipeX = x;
				}

				break;

			case MotionEvent.ACTION_UP:
				Log.v(Constants.TAG, "MotionEvent.ACTION_UP");
				if (swiping)
					swiping = false;
				break;

			case MotionEvent.ACTION_MOVE:
				if (swiping) {
					Log.v(Constants.TAG, "MotionEvent.ACTION_MOVE at position " + x + ", " + y);
					if (Math.abs(x - startSwipeX) > Constants.SWIPE_OFFSET) {
						swiping = false;
						FragmentTransaction transaction = mainActivity.getSupportFragmentManager().beginTransaction();
						mainActivity.animateTransition(transaction, mainActivity.TRANSITION_VERTICAL);
						DetailFragment mDetailFragment = new DetailFragment();
						Bundle b = new Bundle();
						b.putParcelable(Constants.INTENT_NOTE, new Note());
						mDetailFragment.setArguments(b);
						transaction.replace(R.id.fragment_container, mDetailFragment,
								mainActivity.FRAGMENT_DETAIL_TAG).addToBackStack(mainActivity
								.FRAGMENT_DETAIL_TAG).commit();
					}
				}
				break;

			default:
				Log.e(Constants.TAG, "Wrong element choosen: " + event.getAction());
		}

		return true;
	}

	@Override
	public void onAttachingFileErrorOccurred(Attachment mAttachment) {
		mainActivity.showMessage(R.string.error_saving_attachments, ONStyle.ALERT);
		if (noteTmp.getAttachmentsList().contains(mAttachment)) {
			removeAttachment(mAttachment);
			mAttachmentAdapter.notifyDataSetChanged();
			mGridView.autoresize();
		}
	}

	public void addAttachment(Attachment attachment) {
		noteTmp.addAttachment(attachment);
	}

	private void removeAttachment(Attachment mAttachment) {
		noteTmp.removeAttachment(mAttachment);
	}

	public void removeAttachment(int position) {
		noteTmp.removeAttachment(noteTmp.getAttachmentsList().get(position));
	}

	public void removeAllAttachments() {
		noteTmp.setAttachmentsList(new ArrayList<>());
		mAttachmentAdapter = new AttachmentAdapter(mainActivity, new ArrayList<>(), mGridView);
		mGridView.invalidateViews();
		mGridView.setAdapter(mAttachmentAdapter);
	}

	@Override
	public void onAttachingFileFinished(Attachment mAttachment) {
		addAttachment(mAttachment);
		mAttachmentAdapter.notifyDataSetChanged();
		mGridView.autoresize();
	}

	@Override
	public void onReminderPicked(long reminder) {
		noteTmp.setAlarm(reminder);
		if (mFragment.isAdded()) {
			reminderIcon.setImageResource(R.drawable.ic_alarm_black_18dp);
			datetime.setText(DateHelper.getNoteReminderText(reminder));
		}
	}

	@Override
	public void onRecurrenceReminderPicked(String recurrenceRule) {
		noteTmp.setRecurrenceRule(recurrenceRule);
		if (!TextUtils.isEmpty(recurrenceRule)) {
			Log.d(Constants.TAG, "Recurrent reminder set: " + recurrenceRule);
			datetime.setText(DateHelper.getNoteRecurrentReminderText(parseLong(noteTmp
					.getAlarm()), recurrenceRule));
		}
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		scrollContent();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// Nothing to do
	}

	@Override
	public void afterTextChanged(Editable s) {
		// Nothing to do
	}

	@Override
	public void onCheckListChanged() {
		scrollContent();
	}

	private void scrollContent() {
		if (noteTmp.isChecklist()) {
			if (mChecklistManager.getCount() > contentLineCounter) {
				scrollView.scrollBy(0, 60);
			}
			contentLineCounter = mChecklistManager.getCount();
		} else {
			if (content.getLineCount() > contentLineCounter) {
				scrollView.scrollBy(0, 60);
			}
			contentLineCounter = content.getLineCount();
		}
	}

	/**
	 * Add previously created tags to content
	 */
	private void addTags() {

		// Retrieves all available categories

		// If there is no tag a message will be shown

		// Dialog and events creation
		tagHelper.addTags();
	}

	public void tagNote(List<Tag> tags, Integer[] selectedTags, Note note) {
		this.tagHelper.tagNote(tags,selectedTags,note);
	}

	public int getCursorIndex() {
		if (!noteTmp.isChecklist()) {
			return content.getSelectionStart();
		} else {
			CheckListViewItem mCheckListViewItem = mChecklistManager.getFocusedItemView();
			if (mCheckListViewItem != null) {
				return mCheckListViewItem.getEditText().getSelectionStart();
			} else {
				return 0;
			}
		}
	}

	/**
	 * Used to check currently opened note from activity to avoid openind multiple times the same one
	 */
	public Note getCurrentNote() {
		return note;
	}

	public boolean isNoteLocationValid() {
		return noteTmp.getLatitude() != null
				&& noteTmp.getLatitude() != 0
				&& noteTmp.getLongitude() != null
				&& noteTmp.getLongitude() != 0;
	}

	public EditText getContent() {
		return content;
	}

	public EditText getTitle() {
		return title;
	}

	public TextLinkClickListener getTextLinkClickListener() {
		return textLinkClickListener;
	}

	public DetailFragment getmFragment() {
		return mFragment;
	}

	public LinearLayout getReminder_layout() {
		return reminder_layout;
	}

	public TextView getDatetime() {
		return datetime;
	}

	public View getToggleChecklistView() {
		return toggleChecklistView;
	}

	public TextView getLocationTextView() {
		return locationTextView;
	}

	public TextView getCreationTextView() {
		return creationTextView;
	}

	public TextView getLastModificationTextView() {
		return lastModificationTextView;
	}

	public ImageView getReminderIcon() {
		return reminderIcon;
	}

	public void setToggleChecklistView(View toggleChecklistView) {
		this.toggleChecklistView = toggleChecklistView;
	}

	public int getContentCursorPosition() {
		return contentCursorPosition;
	}

	public View getTitleWrapperView() {
		return titleWrapperView;
	}

	public ScrollView getScrollView() {
		return scrollView;
	}

	public View getTagMarkerView() {
		return tagMarkerView;
	}

	public void setContentCursorPosition(int contentCursorPosition) {
		this.contentCursorPosition = contentCursorPosition;
	}

	public TagHelper getTagHelper() {
		return tagHelper;
	}

	public boolean isActivityPausing() {
		return activityPausing;
	}

	public String getExitMessage() {
		return exitMessage;
	}

	public ArrayList<String> getMergedNotesIds() {
		return mergedNotesIds;
	}

	public Style getExitCroutonStyle() {
		return exitCroutonStyle;
	}

	public void setExitMessage(String exitMessage) {
		this.exitMessage = exitMessage;
	}

	public void setExitCroutonStyle(Style exitCroutonStyle) {
		this.exitCroutonStyle = exitCroutonStyle;
	}

	public SaveHelper getSaveHelper() {
		return saveHelper;
	}

	/**
	 * Manages clicks on attachment dialog
	 */
	@SuppressLint("InlinedApi")
	public class AttachmentOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {
				// Photo from camera
				case R.id.camera:
					photoHelper.takePhoto();
					break;
				case R.id.recording:
					RecordingAction(v);
					break;
				case R.id.video:
					videoHelper.takeVideo();
					break;
				case R.id.files:
					AttachFiles();
					break;
				case R.id.sketch:
					sketchHelper.takeSketch(null);
					break;
				case R.id.location:
					displayLocationDialog();
					break;
				case R.id.timestamp:
					timestampHelper.addTimestamp();
					break;
				case R.id.pushbullet:
					ExecutePushBullet();
					break;
				default:
					Log.e(Constants.TAG, "Wrong element choosen: " + v.getId());
			}
			if (!videoHelper.isRecording()) attachmentDialog.dismiss();
		}

		public void ExecutePushBullet() {
			MessagingExtension.mirrorMessage(mainActivity, getString(R.string.app_name),
                    getString(R.string.pushbullet),
                    getNoteContent(), BitmapFactory.decodeResource(getResources(),
                            R.drawable.ic_stat_literal_icon),
                    null, 0);
		}

		public void AttachFiles() {
			if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                startGetContentAction();
            } else {
                askReadExternalStoragePermission();
            }
		}

		public void RecordingAction(View v) {
			if (!videoHelper.isRecording()) {
				videoHelper.startRecording(v);
            } else {
				videoHelper.stopRecording();
                Attachment attachment = new Attachment(Uri.fromFile(new File(recordName)), Constants
                        .MIME_TYPE_AUDIO);
                attachment.setLength(videoHelper.getAudioRecordingTime());
                addAttachment(attachment);
                mAttachmentAdapter.notifyDataSetChanged();
                mGridView.autoresize();
            }
		}
	}

	public void startGetContentAction() {
		Intent filesIntent;
		filesIntent = new Intent(Intent.ACTION_GET_CONTENT);
		filesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		filesIntent.addCategory(Intent.CATEGORY_OPENABLE);
		filesIntent.setType("*/*");
		startActivityForResult(filesIntent, FILES);
	}

	private void askReadExternalStoragePermission() {
		PermissionsHelper.requestPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE,
				R.string.permission_external_storage_detail_attachment,
				snackBarPlaceholder, this::startGetContentAction);
	}

	public void onEventMainThread(PushbulletReplyEvent pushbulletReplyEvent) {
		String text = getNoteContent() + System.getProperty("line.separator") + pushbulletReplyEvent.message;
		content.setText(text);
	}
}



