package it.feio.android.omninotes.FragmentHelpers.DetailFragment;

import android.location.Location;
import android.text.TextUtils;
import android.view.View;

import it.feio.android.omninotes.DetailFragment;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.models.listeners.OnGeoUtilResultListener;
import it.feio.android.omninotes.utils.GeocodeHelper;

public class GeoHelper implements OnGeoUtilResultListener {
    private final DetailFragment detailFragment;

    public GeoHelper(DetailFragment detailFragment) {
        this.detailFragment = detailFragment;
    }

    @Override
    public void onLocationRetrieved(Location location) {
        if (location == null) {
            detailFragment.getMainActivity().showMessage(R.string.location_not_found, ONStyle.ALERT);
        }
        if (location != null) {
            detailFragment.getNoteTmp().setLatitude(location.getLatitude());
            detailFragment.getNoteTmp().setLongitude(location.getLongitude());
            if (!TextUtils.isEmpty(detailFragment.getNoteTmp().getAddress())) {
                detailFragment.getLocationTextView().setVisibility(View.VISIBLE);
                detailFragment.getLocationTextView().setText(detailFragment.getNoteTmp().getAddress());
            } else {
                GeocodeHelper.getAddressFromCoordinates(location, detailFragment.getmFragment());
            }
        }
    }

    @Override
    public void onLocationUnavailable() {
        detailFragment.getMainActivity().showMessage(R.string.location_not_found, ONStyle.ALERT);
    }

    @Override
    public void onAddressResolved(String address) {
        if (TextUtils.isEmpty(address)) {
            if (!detailFragment.isNoteLocationValid()) {
                detailFragment.getMainActivity().showMessage(R.string.location_not_found, ONStyle.ALERT);
                return;
            }
            address = detailFragment.getNoteTmp().getLatitude() + ", " + detailFragment.getNoteTmp().getLongitude();
        }
        if (!GeocodeHelper.areCoordinates(address)) {
            detailFragment.getNoteTmp().setAddress(address);
        }
        detailFragment.getLocationTextView().setVisibility(View.VISIBLE);
        detailFragment.getLocationTextView().setText(address);
        detailFragment.fade(detailFragment.getLocationTextView(), true);
    }

    @Override
    public void onCoordinatesResolved(Location location, String address) {
        if (location != null) {
            detailFragment.getNoteTmp().setLatitude(location.getLatitude());
            detailFragment.getNoteTmp().setLongitude(location.getLongitude());
            detailFragment.getNoteTmp().setAddress(address);
            detailFragment.getLocationTextView().setVisibility(View.VISIBLE);
            detailFragment.getLocationTextView().setText(address);
            detailFragment.fade(detailFragment.getLocationTextView(), true);
        } else {
            detailFragment.getMainActivity().showMessage(R.string.location_not_found, ONStyle.ALERT);
        }
    }
}