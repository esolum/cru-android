package org.androidcru.crucentralcoast.presentation.views.ridesharing.driversignup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.MapFragment;
import timber.log.Timber;

import org.androidcru.crucentralcoast.AppConstants;
import org.androidcru.crucentralcoast.CruApplication;
import org.androidcru.crucentralcoast.R;
import org.androidcru.crucentralcoast.data.models.Ride;
import org.androidcru.crucentralcoast.data.providers.RideProvider;
import org.androidcru.crucentralcoast.presentation.util.DrawableUtil;
import org.androidcru.crucentralcoast.presentation.validator.BaseValidator;
import org.androidcru.crucentralcoast.presentation.viewmodels.ridesharing.DriverSignupVM;
import org.androidcru.crucentralcoast.presentation.views.base.BaseAppCompatActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.observers.Observers;

public class DriverSignupActivity extends BaseAppCompatActivity
{
    SharedPreferences sharedPreferences = CruApplication.getSharedPreferences();

    private DriverSignupVM driverSignupVM;
    private BaseValidator validator;

    @Bind(R.id.fab) FloatingActionButton fab;
    SupportPlaceAutocompleteFragment autocompleteFragment;
    MapFragment mapFragment;

    private String eventID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_form);

        Bundle bundle = getIntent().getExtras();
        if(bundle == null || bundle.getString(AppConstants.EVENT_ID, "").isEmpty())
        {
            Timber.e("DriverSignupActivity requires that you pass an event");
            Timber.e("Finishing activity...");
            finish();
            return;
        }
        else
        {
            eventID = bundle.getString(AppConstants.EVENT_ID, "");
        }
        ButterKnife.bind(this);

        setupFab();

        autocompleteFragment = (SupportPlaceAutocompleteFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);

        String rideId = getIntent().getExtras().getString(AppConstants.RIDE_KEY);

        if (rideId != null)
            requestRides(rideId);
        else
            bindNewRideVM(null);
    }

    private Ride completeRide(Ride r)
    {
        r.gcmID = CruApplication.getGCMID();
        r.eventId = eventID;
        return r;
    }

    private void createDriver()
    {
        RideProvider.createRide(this, Observers.empty(), completeRide(driverSignupVM.getRide()));
    }

    private void updateDriver()
    {
        RideProvider.updateRide(this, Observers.empty(), completeRide(driverSignupVM.getRide()));
    }

    private void setupPlacesAutocomplete()
    {
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setHint("Pickup Location");
        autocompleteFragment.setOnPlaceSelectedListener(driverSignupVM.onPlaceSelected());
    }

    private void requestRides(String rideId)
    {
        //TODO GeocodeProvider was used to populate the address of the Ride at this time
        RideProvider.requestRideByID(this, Observers.empty(), rideId);
    }

    private void bindNewRideVM(Ride r) {
        if (r == null)
            driverSignupVM = new DriverSignupVM(this, getFragmentManager(), eventID);
        else
            driverSignupVM = new DriverSignupVM(this, getFragmentManager(), r);
        mapFragment.getMapAsync(driverSignupVM.onMapReady());
        setupPlacesAutocomplete();
        validator = new BaseValidator(driverSignupVM);
    }

    private void setupFab()
    {
        fab.setImageDrawable(DrawableUtil.getTintedDrawable(this, R.drawable.ic_check_grey600_48dp, android.R.color.white));
        fab.setOnClickListener(v -> {
            if(validator.validate())
            {
                sharedPreferences.edit().putString(AppConstants.USER_NAME, driverSignupVM.nameField.getText().toString()).apply();
                sharedPreferences.edit().putString(AppConstants.USER_PHONE_NUMBER, driverSignupVM.phoneField.getText().toString()).apply();

                if(driverSignupVM.editing)
                {
                    updateDriver();
                }
                else
                    createDriver();

                setResult(RESULT_OK);
                finish();
            }

        });
    }
}
