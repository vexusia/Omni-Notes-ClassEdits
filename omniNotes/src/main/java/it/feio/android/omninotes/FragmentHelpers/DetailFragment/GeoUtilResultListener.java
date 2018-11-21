package it.feio.android.omninotes.FragmentHelpers.DetailFragment;

import android.location.Location;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.ref.WeakReference;

import it.feio.android.omninotes.DetailFragment;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.models.adapters.PlacesAutoCompleteAdapter;
import it.feio.android.omninotes.models.listeners.OnGeoUtilResultListener;
import it.feio.android.omninotes.utils.ConnectionManager;
import it.feio.android.omninotes.utils.GeocodeHelper;

/**
 * Created by nates on 11/18/2018.
 */
public class GeoUtilResultListener implements OnGeoUtilResultListener {

    private final WeakReference<MainActivity> mainActivityWeakReference;
    private final WeakReference<DetailFragment> detailFragmentWeakReference;
    private final WeakReference<Note> noteTmpWeakReference;

    public GeoUtilResultListener(MainActivity activity, DetailFragment mFragment, Note noteTmp) {
        this.mainActivityWeakReference = new WeakReference<>(activity);
        this.detailFragmentWeakReference = new WeakReference<>(mFragment);
        this.noteTmpWeakReference = new WeakReference<>(noteTmp);
    }

    @Override
    public void onAddressResolved(String address) {
    }

    @Override
    public void onCoordinatesResolved(Location location, String address) {
    }

    @Override
    public void onLocationUnavailable() {
        mainActivityWeakReference.get().showMessage(R.string.location_not_found, ONStyle.ALERT);
    }

    @Override
    public void onLocationRetrieved(Location location) {

        if (!checkWeakReferences()) {
            return;
        }

        if (location == null) {
            return;
        }
        if (!ConnectionManager.internetAvailable(mainActivityWeakReference.get())) {
            noteTmpWeakReference.get().setLatitude(location.getLatitude());
            noteTmpWeakReference.get().setLongitude(location.getLongitude());
            onAddressResolved("");
            return;
        }
        LayoutInflater inflater = mainActivityWeakReference.get().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_location, null);
        final AutoCompleteTextView autoCompView = (AutoCompleteTextView) v.findViewById(R.id
                .auto_complete_location);
        autoCompView.setHint(mainActivityWeakReference.get().getString(R.string.search_location));
        autoCompView.setAdapter(new PlacesAutoCompleteAdapter(mainActivityWeakReference.get(), R.layout
                .simple_text_layout));
        final MaterialDialog dialog = new MaterialDialog.Builder(mainActivityWeakReference.get())
                .customView(autoCompView, false)
                .positiveText(R.string.use_current_location)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog materialDialog) {
                        if (TextUtils.isEmpty(autoCompView.getText().toString())) {
                            noteTmpWeakReference.get().setLatitude(location.getLatitude());
                            noteTmpWeakReference.get().setLongitude(location.getLongitude());
                            GeocodeHelper.getAddressFromCoordinates(location, detailFragmentWeakReference.get());
                        } else {
                            GeocodeHelper.getCoordinatesFromAddress(autoCompView.getText().toString(),
                                    detailFragmentWeakReference.get());
                        }
                    }
                })
                .build();
        autoCompView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    dialog.setActionButton(DialogAction.POSITIVE, mainActivityWeakReference.get().getString(R
                            .string.confirm));
                } else {
                    dialog.setActionButton(DialogAction.POSITIVE, mainActivityWeakReference.get().getString(R
                            .string
                            .use_current_location));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        dialog.show();
    }

    private boolean checkWeakReferences() {
        return mainActivityWeakReference.get() != null && !mainActivityWeakReference.get().isFinishing()
                && detailFragmentWeakReference.get() != null && noteTmpWeakReference.get() != null;
    }
}
