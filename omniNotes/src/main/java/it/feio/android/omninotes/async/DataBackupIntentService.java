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

package it.feio.android.omninotes.async;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import exceptions.ImportException;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.listeners.OnAttachingFileListener;
import it.feio.android.omninotes.utils.*;
import it.feio.android.springpadimporter.Importer;
import it.feio.android.springpadimporter.models.SpringpadAttachment;
import it.feio.android.springpadimporter.models.SpringpadComment;
import it.feio.android.springpadimporter.models.SpringpadElement;
import it.feio.android.springpadimporter.models.SpringpadItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.io.InputStream;



public class DataBackupIntentService extends IntentService implements OnAttachingFileListener {

    public final static String INTENT_BACKUP_NAME = "backup_name";
    public final static String INTENT_BACKUP_INCLUDE_SETTINGS = "backup_include_settings";
    public final static String ACTION_DATA_EXPORT = "action_data_export";
    public final static String ACTION_DATA_IMPORT = "action_data_import";
    public final static String ACTION_DATA_IMPORT_SPRINGPAD = "action_data_import_springpad";
    public final static String ACTION_DATA_DELETE = "action_data_delete";
    public final static String EXTRA_SPRINGPAD_BACKUP = "extra_springpad_backup";

    private SharedPreferences prefs;
    private NotificationsHelper mNotificationsHelper;

    private int importedSpringpadNotes, importedSpringpadNotebooks;


    public DataBackupIntentService() {
        super("DataBackupIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);

        // Creates an indeterminate processing notification until the work is complete
        mNotificationsHelper = new NotificationsHelper(this)
                .createNotification(R.drawable.ic_content_save_white_24dp, getString(R.string.working), null)
                .setIndeterminate().setOngoing().show();

        // If an alarm has been fired a notification must be generated
        if (ACTION_DATA_EXPORT.equals(intent.getAction())) {
            exportData(intent);
        } else if (ACTION_DATA_IMPORT.equals(intent.getAction())) {
            importData(intent);
        } else if (ACTION_DATA_IMPORT_SPRINGPAD.equals(intent.getAction())) {
            importDataFromSpringpad(intent);
        } else if (ACTION_DATA_DELETE.equals(intent.getAction())) {
            deleteData(intent);
        }
    }


    synchronized private void exportData(Intent intent) {

        // Gets backup folder
        String backupName = intent.getStringExtra(INTENT_BACKUP_NAME);
        File backupDir = StorageHelper.getBackupDir(backupName);

        // Directory clean in case of previously used backup name
        StorageHelper.delete(this, backupDir.getAbsolutePath());

        // Directory is re-created in case of previously used backup name (removed above)
        backupDir = StorageHelper.getBackupDir(backupName);

        // Database backup
        exportDB(backupDir);

        Log.d("directory,", "name = "+backupDir.getName());
        Log.d("directory,", "path = "+backupDir.getAbsolutePath());


        // Attachments backup
        exportAttachments(backupDir);

        // Settings
        if (intent.getBooleanExtra(INTENT_BACKUP_INCLUDE_SETTINGS, true)) {
            exportSettings(backupDir);
        }
////////////////////////////////////////////////////////////////////////////////////////////////////
        //create zip file of backup settings
        zipFileAtPath(backupDir.getAbsolutePath(), backupDir.getName());

        //delete original backup folder
        StorageHelper.delete(this, backupDir.getAbsolutePath());
////////////////////////////////////////////////////////////////////////////////////////////////////
        // Notification of operation ended
        String title = getString(R.string.data_export_completed);
        String text = backupDir.getPath();
        createNotification(intent, this, title, text, backupDir);
    }



    private boolean extractFolder(File destination, File zipFile) {
        int BUFFER = 8192;
        File file = zipFile;
        //This can throw ZipException if file is not valid zip archive
        try{
            ZipFile zip = new ZipFile(file);
            String newPath = destination.getAbsolutePath() + File.separator + FilenameUtils.removeExtension(zipFile.getName());
            //Create destination directory
            new File(newPath).mkdir();
            Enumeration zipFileEntries = zip.entries();

            //Iterate overall zip file entries
            while (zipFileEntries.hasMoreElements())
            {
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();
                File destFile = new File(newPath, currentEntry);
                File destinationParent = destFile.getParentFile();
                //If entry is directory create sub directory on file system
                destinationParent.mkdirs();

                if (!entry.isDirectory())
                {
                    //Copy over data into destination file
                    BufferedInputStream is = new BufferedInputStream(zip
                            .getInputStream(entry));
                    int currentByte;
                    byte data[] = new byte[BUFFER];
                    //copying file data using streams
                    FileOutputStream fos = new FileOutputStream(destFile);
                    BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
                    while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, currentByte);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }
            }
        }catch(Exception e){
            Log.d("ExceptionEE", e.toString());
            return false;
        }
        return true;
    }


    /*
     *
     * Zips a file at a location and places the resulting zip file at the toLocation
     * Example: zipFileAtPath("downloads/myfolder", "downloads/myFolder.zip");
     */

    public boolean zipFileAtPath(String sourcePath, String toLocation) {
        final int BUFFER = 2048;

        sourcePath = sourcePath+"/";
        toLocation = "/storage/emulated/0/Omni Notes Foss/"+toLocation+".zip";
       // toLocation = toLocation+"/";
        Log.d("ExceptionEE", "new source path= "+sourcePath);
        File sourceFile = new File(sourcePath);
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(toLocation);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            if (sourceFile.isDirectory()) {
                zipSubFolder(out, sourceFile, sourceFile.getParent().length());
            } else {
                byte data[] = new byte[BUFFER];
                FileInputStream fi = new FileInputStream(sourcePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath));
                entry.setTime(sourceFile.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
            }
            out.close();
        } catch (Exception e) {
            Log.d("ExceptionEE", e.toString());
            return false;
        }
        return true;
    }


    private void zipSubFolder(ZipOutputStream out, File folder,
                              int basePathLength) throws IOException {

        final int BUFFER = 2048;

        File[] fileList = folder.listFiles();
        BufferedInputStream origin = null;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath
                        .substring(basePathLength);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                entry.setTime(file.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }


    /* gets the last path component
     *
     * Example: getLastPathComponent("downloads/example/fileToZip");
     * Result: "fileToZip"
     */
    public String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        if (segments.length == 0)
            return "";
        String lastPathComponent = segments[segments.length - 1];
        return lastPathComponent;
    }


    synchronized private void importData(Intent intent) {

        // Gets backup folder
        String backupName = FilenameUtils.removeExtension(intent.getStringExtra(INTENT_BACKUP_NAME));

        // Extract zip backup folder
        String backupHomeDir = "/storage/emulated/0/Omni Notes Foss/";
        String zip = backupName+".zip";
        File destinationfile = new File(backupHomeDir);
        File zipfile = new File(backupHomeDir+zip);
        extractFolder(destinationfile, zipfile);

        // Zip process nests the directory in a second directory hence +"/"+
        File backupDir = StorageHelper.getBackupDir(backupName+"/"+backupName);

        // Database backup
        importDB(backupDir);

        // Attachments backup
        importAttachments(backupDir);

		// Settings restore
		importSettings(backupDir);

		// Reminders restore
		resetReminders();
///////////////////////////////////////////////////////////////////////////////////////////////////
        //delete extra unzipped backup folder
        StorageHelper.delete(this, backupHomeDir+backupName);
        Log.d("chk", "deleton name = "+backupHomeDir+backupName);
///////////////////////////////////////////////////////////////////////////////////////////////////
        String title = getString(R.string.data_import_completed);
        String text = getString(R.string.click_to_refresh_application);
        createNotification(intent, this, title, text, backupDir);
        System.exit(0);
    }


	/**
     * Imports notes and notebooks from Springpad exported archive
     *
     * @param intent
     */
    synchronized private void importDataFromSpringpad(Intent intent) {
        String backupPath = intent.getStringExtra(EXTRA_SPRINGPAD_BACKUP);
        Importer importer = new Importer();
        try {
            importer.setZipProgressesListener(percentage -> mNotificationsHelper.setMessage(getString(R.string.extracted) + " " + percentage + "%").show());
            importer.doImport(backupPath);
            // Updating notification
            updateImportNotification(importer);
        } catch (ImportException e) {
            new NotificationsHelper(this)
                    .createNotification(R.drawable.ic_emoticon_sad_white_24dp,
                            getString(R.string.import_fail) + ": " + e.getMessage(), null).setLedActive().show();
            return;
        }
        List<SpringpadElement> elements = importer.getSpringpadNotes();

        // If nothing is retrieved it will exit
        if (elements == null || elements.size() == 0) {
            return;
        }

        // These maps are used to associate with post processing notes to categories (notebooks)
        HashMap<String, Category> categoriesWithUuid = new HashMap<>();

        // Adds all the notebooks (categories)
        for (SpringpadElement springpadElement : importer.getNotebooks()) {
            Category cat = new Category();
            cat.setName(springpadElement.getName());
            cat.setColor(String.valueOf(Color.parseColor("#F9EA1B")));
            DbHelper.getInstance().updateCategory(cat);
            categoriesWithUuid.put(springpadElement.getUuid(), cat);

            // Updating notification
            importedSpringpadNotebooks++;
            updateImportNotification(importer);
        }
        // And creates a default one for notes without notebook 
        Category defaulCategory = new Category();
        defaulCategory.setName("Springpad");
        defaulCategory.setColor(String.valueOf(Color.parseColor("#F9EA1B")));
        DbHelper.getInstance().updateCategory(defaulCategory);

        // And then notes are created
        Note note;
        Attachment mAttachment = null;
        Uri uri;
        for (SpringpadElement springpadElement : importer.getNotes()) {
            note = new Note();

            // Title
            note.setTitle(springpadElement.getName());

            // Content dependent from type of Springpad note
            StringBuilder content = new StringBuilder();
            content.append(TextUtils.isEmpty(springpadElement.getText()) ? "" : Html.fromHtml(springpadElement
                    .getText()));
            content.append(TextUtils.isEmpty(springpadElement.getDescription()) ? "" : springpadElement
                    .getDescription());

            // Some notes could have been exported wrongly
            if (springpadElement.getType() == null) {
                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                continue;
            }

            if (springpadElement.getType().equals(SpringpadElement.TYPE_VIDEO)) {
                try {
                    content.append(System.getProperty("line.separator")).append(springpadElement.getVideos().get(0));
                } catch (IndexOutOfBoundsException e) {
                    content.append(System.getProperty("line.separator")).append(springpadElement.getUrl());
                }
            }
            if (springpadElement.getType().equals(SpringpadElement.TYPE_TVSHOW)) {
                content.append(System.getProperty("line.separator")).append(
                        TextUtils.join(", ", springpadElement.getCast()));
            }
            if (springpadElement.getType().equals(SpringpadElement.TYPE_BOOK)) {
                content.append(System.getProperty("line.separator")).append("Author: ")
                        .append(springpadElement.getAuthor()).append(System.getProperty("line.separator"))
                        .append("Publication date: ").append(springpadElement.getPublicationDate());
            }
            if (springpadElement.getType().equals(SpringpadElement.TYPE_RECIPE)) {
                content.append(System.getProperty("line.separator")).append("Ingredients: ")
                        .append(springpadElement.getIngredients()).append(System.getProperty("line.separator"))
                        .append("Directions: ").append(springpadElement.getDirections());
            }
            if (springpadElement.getType().equals(SpringpadElement.TYPE_BOOKMARK)) {
                content.append(System.getProperty("line.separator")).append(springpadElement.getUrl());
            }
            if (springpadElement.getType().equals(SpringpadElement.TYPE_BUSINESS)
                    && springpadElement.getPhoneNumbers() != null) {
                content.append(System.getProperty("line.separator")).append("Phone number: ")
                        .append(springpadElement.getPhoneNumbers().getPhone());
            }
            if (springpadElement.getType().equals(SpringpadElement.TYPE_PRODUCT)) {
                content.append(System.getProperty("line.separator")).append("Category: ")
                        .append(springpadElement.getCategory()).append(System.getProperty("line.separator"))
                        .append("Manufacturer: ").append(springpadElement.getManufacturer())
                        .append(System.getProperty("line.separator")).append("Price: ")
                        .append(springpadElement.getPrice());
            }
            if (springpadElement.getType().equals(SpringpadElement.TYPE_WINE)) {
                content.append(System.getProperty("line.separator")).append("Wine type: ")
                        .append(springpadElement.getWine_type()).append(System.getProperty("line.separator"))
                        .append("Varietal: ").append(springpadElement.getVarietal())
                        .append(System.getProperty("line.separator")).append("Price: ")
                        .append(springpadElement.getPrice());
            }
            if (springpadElement.getType().equals(SpringpadElement.TYPE_ALBUM)) {
                content.append(System.getProperty("line.separator")).append("Artist: ")
                        .append(springpadElement.getArtist());
            }
            for (SpringpadComment springpadComment : springpadElement.getComments()) {
                content.append(System.getProperty("line.separator")).append(springpadComment.getCommenter())
                        .append(" commented at 0").append(springpadComment.getDate()).append(": ")
                        .append(springpadElement.getArtist());
            }

            note.setContent(content.toString());

            // Checklists
            if (springpadElement.getType().equals(SpringpadElement.TYPE_CHECKLIST)) {
                StringBuilder sb = new StringBuilder();
                String checkmark;
                for (SpringpadItem mSpringpadItem : springpadElement.getItems()) {
                    checkmark = mSpringpadItem.getComplete() ? it.feio.android.checklistview.interfaces.Constants
                            .CHECKED_SYM
                            : it.feio.android.checklistview.interfaces.Constants.UNCHECKED_SYM;
                    sb.append(checkmark).append(mSpringpadItem.getName()).append(System.getProperty("line.separator"));
                }
                note.setContent(sb.toString());
                note.setChecklist(true);
            }

            // Tags
            String tags = springpadElement.getTags().size() > 0 ? "#"
                    + TextUtils.join(" #", springpadElement.getTags()) : "";
            if (note.isChecklist()) {
                note.setTitle(note.getTitle() + tags);
            } else {
                note.setContent(note.getContent() + System.getProperty("line.separator") + tags);
            }

            // Address
            String address = springpadElement.getAddresses() != null ? springpadElement.getAddresses().getAddress()
                    : "";
            if (!TextUtils.isEmpty(address)) {
                try {
                    double[] coords = GeocodeHelper.getCoordinatesFromAddress(this, address);
                    note.setLatitude(coords[0]);
                    note.setLongitude(coords[1]);
                } catch (IOException e) {
                    Log.e(Constants.TAG, "An error occurred trying to resolve address to coords during Springpad import");
                }
                note.setAddress(address);
            }

            // Reminder
            if (springpadElement.getDate() != null) {
                note.setAlarm(springpadElement.getDate().getTime());
            }

            // Creation, modification, category
            note.setCreation(springpadElement.getCreated().getTime());
            note.setLastModification(springpadElement.getModified().getTime());

            // Image
            String image = springpadElement.getImage();
            if (!TextUtils.isEmpty(image)) {
                try {
                    File file = StorageHelper.createNewAttachmentFileFromHttp(this, image);
                    uri = Uri.fromFile(file);
                    String mimeType = StorageHelper.getMimeType(uri.getPath());
                    mAttachment = new Attachment(uri, mimeType);
                } catch (MalformedURLException e) {
                    uri = Uri.parse(importer.getWorkingPath() + image);
                    mAttachment = StorageHelper.createAttachmentFromUri(this, uri, true);
                } catch (IOException e) {
                    Log.e(Constants.TAG, "Error retrieving Springpad online image");
                }
                if (mAttachment != null) {
                    note.addAttachment(mAttachment);
                }
                mAttachment = null;
            }

            // Other attachments
            for (SpringpadAttachment springpadAttachment : springpadElement.getAttachments()) {
                // The attachment could be the image itself so it's jumped
                if (image != null && image.equals(springpadAttachment.getUrl())) continue;

                if (TextUtils.isEmpty(springpadAttachment.getUrl())) {
                    continue;
                }

                // Tries first with online images
                try {
                    File file = StorageHelper.createNewAttachmentFileFromHttp(this, springpadAttachment.getUrl());
                    uri = Uri.fromFile(file);
                    String mimeType = StorageHelper.getMimeType(uri.getPath());
                    mAttachment = new Attachment(uri, mimeType);
                } catch (MalformedURLException e) {
                    uri = Uri.parse(importer.getWorkingPath() + springpadAttachment.getUrl());
                    mAttachment = StorageHelper.createAttachmentFromUri(this, uri, true);
                } catch (IOException e) {
                    Log.e(Constants.TAG, "Error retrieving Springpad online image");
                }
                if (mAttachment != null) {
                    note.addAttachment(mAttachment);
                }
                mAttachment = null;
            }

            // If the note has a category is added to the map to be post-processed
            if (springpadElement.getNotebooks().size() > 0) {
                note.setCategory(categoriesWithUuid.get(springpadElement.getNotebooks().get(0)));
            } else {
                note.setCategory(defaulCategory);
            }

            // The note is saved
            DbHelper.getInstance().updateNote(note, false);
			ReminderHelper.addReminder(OmniNotes.getAppContext(), note);

            // Updating notification
            importedSpringpadNotes++;
            updateImportNotification(importer);
        }

        // Delete temp data
        try {
            importer.clean();
        } catch (IOException e) {
            Log.w(Constants.TAG, "Springpad import temp files not deleted");
        }

        String title = getString(R.string.data_import_completed);
        String text = getString(R.string.click_to_refresh_application);
        createNotification(intent, this, title, text, null);
    }


    private void updateImportNotification(Importer importer) {
        mNotificationsHelper.setMessage(
                importer.getNotebooksCount() + " " + getString(R.string.categories) + " ("
                        + importedSpringpadNotebooks + " " + getString(R.string.imported) + "), "
                        + +importer.getNotesCount() + " " + getString(R.string.notes) + " ("
                        + importedSpringpadNotes + " " + getString(R.string.imported) + ")").show();
    }


    synchronized private void deleteData(Intent intent) {

        // Gets backup folder
        String backupName = intent.getStringExtra(INTENT_BACKUP_NAME);
        File backupDir = StorageHelper.getBackupDir(backupName);

        // Backup directory removal
        StorageHelper.delete(this, backupDir.getAbsolutePath());

        String title = getString(R.string.data_deletion_completed);
        String text = backupName + " " + getString(R.string.deleted);
        createNotification(intent, this, title, text, backupDir);
    }


    /**
     * Creation of notification on operations completed
     */
    private void createNotification(Intent intent, Context mContext, String title, String message, File backupDir) {

        // The behavior differs depending on intent action
        Intent intentLaunch;
        if (DataBackupIntentService.ACTION_DATA_IMPORT.equals(intent.getAction())
                || DataBackupIntentService.ACTION_DATA_IMPORT_SPRINGPAD.equals(intent.getAction())) {
			intentLaunch = new Intent(mContext, MainActivity.class);
			intentLaunch.setAction(Constants.ACTION_RESTART_APP);
        } else {
            intentLaunch = new Intent();
        }
        // Add this bundle to the intent
        intentLaunch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Creates the PendingIntent
        PendingIntent notifyIntent = PendingIntent.getActivity(mContext, 0, intentLaunch,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationsHelper mNotificationsHelper = new NotificationsHelper(mContext);
        mNotificationsHelper.createNotification(R.drawable.ic_content_save_white_24dp, title, notifyIntent)
                .setMessage(message).setRingtone(prefs.getString("settings_notification_ringtone", null))
                .setLedActive();
        if (prefs.getBoolean("settings_notification_vibration", true)) mNotificationsHelper.setVibration();
        mNotificationsHelper.show();
    }


    /**
     * Export database to backup folder
     *
     * @return True if success, false otherwise
     */
    private boolean exportDB(File backupDir) {
        File database = getDatabasePath(Constants.DATABASE_NAME);
        return (StorageHelper.copyFile(database, new File(backupDir, Constants.DATABASE_NAME)));
    }

    private void exportNotes(File backupDir) {
		for (Note note : DbHelper.getInstance().getAllNotes(false)) {
			File noteFile = new File(backupDir, String.valueOf(note.get_id()));

			try {
				FileUtils.write(noteFile, note.toJSON());
			} catch (IOException e) {
				Log.e(Constants.TAG, "Error backupping note: " + note.get_id());
			}
		}
	}


    /**
     * Export attachments to backup folder
     *
     * @return True if success, false otherwise
     */
    private boolean exportAttachments(File backupDir) {
        File attachmentsDir = StorageHelper.getAttachmentDir(this);
        File destinationattachmentsDir = new File(backupDir, attachmentsDir.getName());

        DbHelper db = DbHelper.getInstance();
        ArrayList<Attachment> list = db.getAllAttachments();

        int exported = 0;
        for (Attachment attachment : list) {
            StorageHelper.copyToBackupDir(destinationattachmentsDir, new File(attachment.getUri().getPath()));
            mNotificationsHelper.setMessage(TextHelper.capitalize(getString(R.string.attachment)) + " " + exported++ + "/" + list.size())
                    .show();
        }
        return true;
    }


    /**
     * Exports settings if required
     */
    private boolean exportSettings(File backupDir) {
        File preferences = StorageHelper.getSharedPreferencesFile(this);
        return (StorageHelper.copyFile(preferences, new File(backupDir, preferences.getName())));
    }


    /**
     * Imports settings
     */
    private boolean importSettings(File backupDir) {
        File preferences = StorageHelper.getSharedPreferencesFile(this);
        File preferenceBackup = new File(backupDir, preferences.getName());
        return (StorageHelper.copyFile(preferenceBackup, preferences));
    }


	/**
	 * Schedules reminders
	 */
	private void resetReminders() {
		Log.d(Constants.TAG, "Resettings reminders");
		for (Note note : DbHelper.getInstance().getNotesWithReminderNotFired()) {
			ReminderHelper.addReminder(OmniNotes.getAppContext(), note);
		}
	}


    /**
     * Import database from backup folder
     */
    private boolean importDB(File backupDir) {
        File database = getDatabasePath(Constants.DATABASE_NAME);
        if (database.exists()) {
            database.delete();
        }
        return (StorageHelper.copyFile(new File(backupDir, Constants.DATABASE_NAME), database));
    }


    private void importNotes(File backupDir) {
		for (File file : FileUtils.listFiles(backupDir, new RegexFileFilter("\\d{13}"), TrueFileFilter.INSTANCE)) {
			try {
				Note note = new Note();
				note.buildFromJson(FileUtils.readFileToString(file));
				if (note.getCategory() != null) {
					DbHelper.getInstance().updateCategory(note.getCategory());
				}
				for (Attachment attachment : note.getAttachmentsList()) {
					DbHelper.getInstance().updateAttachment(attachment);
				}
				DbHelper.getInstance().updateNote(note, false);
			} catch (IOException e) {
				Log.e(Constants.TAG, "Error parsing note json");
			}
		}
    }


    /**
     * Import attachments from backup folder
     */
    private void importAttachments(File backupDir) {
        File attachmentsDir = StorageHelper.getAttachmentDir(this);
        File backupAttachmentsDir = new File(backupDir, attachmentsDir.getName());
        if (!backupAttachmentsDir.exists()) {
			return;
		}
        Collection list = FileUtils.listFiles(backupAttachmentsDir, FileFilterUtils.trueFileFilter(),
                TrueFileFilter.INSTANCE);
        Iterator i = list.iterator();
        int imported = 0;
        File file = null;
        while (i.hasNext()) {
            try {
                file = (File) i.next();
                FileUtils.copyFileToDirectory(file, attachmentsDir, true);
                mNotificationsHelper.setMessage(TextHelper.capitalize(getString(R.string.attachment)) + " " + imported++ + "/" + list.size())
                        .show();
            } catch (IOException e) {
                Log.e(Constants.TAG, "Error importing the attachment " + file.getName());
            }
        }
    }


    @Override
    public void onAttachingFileErrorOccurred(Attachment mAttachment) {
        // TODO Auto-generated method stub
    }


    @Override
    public void onAttachingFileFinished(Attachment mAttachment) {
        // TODO Auto-generated method stub
    }

}
