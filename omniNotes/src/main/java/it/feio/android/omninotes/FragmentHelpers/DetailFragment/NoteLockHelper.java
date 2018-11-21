package it.feio.android.omninotes.FragmentHelpers.DetailFragment;

import android.content.Intent;
import android.util.Log;

import it.feio.android.omninotes.DetailFragment;
import it.feio.android.omninotes.PasswordActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.PasswordHelper;

public class NoteLockHelper {
    private final DetailFragment detailFragment;

    public NoteLockHelper(DetailFragment detailFragment) {
        this.detailFragment = detailFragment;
    }

    /**
     * Checks note lock and password before showing note content
     */
    public void checkNoteLock(Note note) {
        // If note is locked security password will be requested
        if (note.isLocked()
                && detailFragment.getPrefs().getString(Constants.PREF_PASSWORD, null) != null
                && !detailFragment.getPrefs().getBoolean("settings_password_access", false)) {
            PasswordHelper.requestPassword(detailFragment.getMainActivity(), passwordConfirmed -> {
                if (passwordConfirmed) {
                    detailFragment.getNoteTmp().setPasswordChecked(true);
                    detailFragment.init();
                } else {
                    detailFragment.setGoBack(true);
                    detailFragment.goHome();
                }
            });
        } else {
            detailFragment.getNoteTmp().setPasswordChecked(true);
            detailFragment.init();
        }
    }

    /**
     * Notes locking with security password to avoid viewing, editing or deleting from unauthorized
     */
    public void lockNote() {
        Log.d(Constants.TAG, "Locking or unlocking note " + this.detailFragment.note.get_id());

        // If security password is not set yes will be set right now
        if (this.detailFragment.prefs.getString(Constants.PREF_PASSWORD, null) == null) {
            Intent passwordIntent = new Intent(this.detailFragment.getMainActivity(), PasswordActivity.class);
            this.detailFragment.startActivityForResult(passwordIntent, this.detailFragment.SET_PASSWORD);
            return;
        }

        // If password has already been inserted will not be asked again
        if (this.detailFragment.noteTmp.isPasswordChecked() || this.detailFragment.prefs.getBoolean("settings_password_access", false)) {
            this.lockUnlock();
            return;
        }

        // Password will be requested here
        PasswordHelper.requestPassword(this.detailFragment.getMainActivity(), passwordConfirmed -> {
            if (passwordConfirmed) {
                this.lockUnlock();
            }
        });
    }

    public void lockUnlock() {
        // Empty password has been set
        if (this.detailFragment.prefs.getString(Constants.PREF_PASSWORD, null) == null) {
            this.detailFragment.getMainActivity().showMessage(R.string.password_not_set, ONStyle.WARN);
            return;
        }
        this.detailFragment.getMainActivity().showMessage(R.string.save_note_to_lock_it, ONStyle.INFO);
        this.detailFragment.getMainActivity().supportInvalidateOptionsMenu();
        this.detailFragment.noteTmp.setLocked(!this.detailFragment.noteTmp.isLocked());
        this.detailFragment.noteTmp.setPasswordChecked(true);
    }
}