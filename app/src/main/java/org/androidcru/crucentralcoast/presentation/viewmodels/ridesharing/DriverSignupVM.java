package org.androidcru.crucentralcoast.presentation.viewmodels.ridesharing;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Pattern;
import com.mobsandgeeks.saripaar.annotation.Select;
import com.orhanobut.logger.Logger;

import org.androidcru.crucentralcoast.AppConstants;
import org.androidcru.crucentralcoast.CruApplication;
import org.androidcru.crucentralcoast.R;
import org.androidcru.crucentralcoast.data.models.Location;
import org.androidcru.crucentralcoast.data.models.Ride;
import org.androidcru.crucentralcoast.data.providers.EventProvider;
import org.androidcru.crucentralcoast.presentation.util.ViewUtil;
import org.androidcru.crucentralcoast.util.DisplayMetricsUtil;
import org.androidcru.crucentralcoast.util.MathUtil;
import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.GregorianCalendar;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.android.schedulers.AndroidSchedulers;

public class DriverSignupVM extends BaseRideVM {
    SharedPreferences sharedPreferences = CruApplication.getSharedPreferences();

    private Ride ride;

    public Double radius;
    private GoogleMap map;
    private Marker marker;
    private Circle circle;
    private LatLng center;

    public boolean editing;
    private GregorianCalendar eventStartDateTime;
    private int minCapacity;

    @Bind(R.id.name_field) @NotEmpty public EditText nameField;
    @Bind(R.id.phone_field) @Pattern(regex = AppConstants.PHONE_REGEX) public EditText phoneField;

    @Bind(R.id.round_trip) RadioButton roundTrip;
    @Bind(R.id.to_event) RadioButton toEvent;
    @Bind(R.id.direction) RadioGroup directionGroup;

    @Bind(R.id.gender_field) @Select Spinner genderField;
    @Bind(R.id.gender_view) TextView genderView;

    @Bind(R.id.time_field) @NotEmpty EditText rideTime;
    @Bind(R.id.date_field) @NotEmpty EditText rideDate;

    @Bind(R.id.car_capacity_field) @NotEmpty EditText carCapacity;

    @Bind(R.id.radius_field) TextView radiusField;
    @Bind(com.google.android.gms.R.id.place_autocomplete_search_input) @NotEmpty EditText searchInput;

    public DriverSignupVM(Activity activity, FragmentManager fm, String eventID) {
        super(activity, fm);
        this.editing = false;
        this.ride = new Ride();

        setEventTime(eventID);
        bindUI();
    }

    public DriverSignupVM(Activity activity, FragmentManager fm, Ride ride) {
        super(activity, fm);
        this.editing = ride != null && !ride.isEmpty();
        this.ride = ride != null ? ride : new Ride();

        if (editing) {
            date = this.ride.time.toLocalDate();
            time = this.ride.time.toLocalTime();
            setEventTime(this.ride.eventId);
        }

        bindUI();
    }

    private void bindUI() {
        phoneField.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        if (editing) {
            genderField.setVisibility(View.GONE);
            genderView.setVisibility(View.VISIBLE);
            genderView.setText(ride.gender);

            carCapacity.setText(Integer.toString(ride.carCapacity));
            minCapacity = ride.passengerIds.size();

            nameField.setText(ride.driverName);
            phoneField.setText(ride.driverNumber);

            rideTime.setText(ride.time.toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME));
            rideDate.setText(ride.time.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));

            radiusField.setText(Double.toString(ride.radius));

            switch (ride.direction)
            {
                case TO:
                    directionGroup.check(toEvent.getId());
                    break;
                default:
                    directionGroup.check(roundTrip.getId());
            }

        } else {
            ViewUtil.setSpinner(genderField, gendersForSpinner(R.array.genders), null, getGenderIndex(ride.gender));
            directionGroup.check(roundTrip.getId());
            rideTime.setOnKeyListener(null);
            rideDate.setOnKeyListener(null);
            minCapacity = 0;

            nameField.setText(sharedPreferences.getString(AppConstants.USER_NAME, null));
            phoneField.setText(sharedPreferences.getString(AppConstants.USER_PHONE_NUMBER, null));
        }

        carCapacity.addTextChangedListener(createCarCapacityWatcher());

    }

    private int retrieveCarCapacity() {
        return Integer.parseInt(carCapacity.getText().toString());
    }


    private Ride.Direction retrieveDirection(RadioGroup radioGroup)
    {
        int selectedRadioIndex = radioGroup.indexOfChild(rootView.findViewById(radioGroup.getCheckedRadioButtonId()));
        Ride.Direction direction = Ride.Direction.ROUNDTRIP;
        switch (selectedRadioIndex)
        {
            case 0:
                direction = Ride.Direction.TO;
                break;
            case 1:
                direction = Ride.Direction.ROUNDTRIP;
                break;
        }
        return direction;
    }

    public Ride getRide() {
        ride.driverName = nameField.getText().toString();
        ride.driverNumber = phoneField.getText().toString();
        ride.gender = (String) genderField.getSelectedItem();
        ride.carCapacity = retrieveCarCapacity();
        ride.direction = retrieveDirection(directionGroup);
        ride.time = ZonedDateTime.of(date, time, ZoneId.systemDefault());
        return ride;
    }

    private GregorianCalendar getDefaultEventTime() {
        return eventStartDateTime;
    }

    @OnClick(R.id.time_field)
    public void onTimeClicked(View v) {
        GregorianCalendar gc;
        if (editing)
            gc = DateTimeUtils.toGregorianCalendar(ride.time);
        else {
            gc = getDefaultEventTime();
        }
        onEventTimeClicked(v, gc);
    }

    @OnClick(R.id.date_field)
    public void onDateClicked(View v) {
        GregorianCalendar gc;
        if (editing)
            gc = DateTimeUtils.toGregorianCalendar(ride.time);
        else {
            gc = getDefaultEventTime();
        }
        onEventDateClicked(v, gc);
    }

    @Override
    protected void placeSelected(LatLng precisePlace, String placeAddress) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(precisePlace, 14.0f));
        center = precisePlace;
        setMarker(center);

        if (radius != null)
            setCircle(center, radius);

        if (placeAddress != null) {
            String[] splitAddress = placeAddress.split("\\s*,\\s*");
            String[] splitStateZip = splitAddress[2].split(" ");
            ride.location = new Location(splitStateZip[1], splitStateZip[0],
                    splitAddress[1], splitAddress[0], splitAddress[3]);
            ride.location.preciseLocation = precisePlace;
        }
    }

    @OnTextChanged(R.id.radius_field)
    public void onRadiusChanged(CharSequence s) {
        if (!s.toString().isEmpty()) {
            try {
                this.radius = Double.valueOf(s.toString());
                setCircle(center, this.radius);
                ride.radius = radius;
            } catch (NumberFormatException e) {
            }
        }
    }

    public OnMapReadyCallback onMapReady() {
        return googleMap -> {
            if (map == null) {
                map = googleMap;
                if (ride.address != null)
                    placeSelected(new LatLng(ride.address.getLatitude(), ride.address.getLongitude()), null);
                else
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(AppConstants.CALPOLY_LAT, AppConstants.CALPOLY_LNG), 14.0f));
            } else {
                Logger.d("Unable to display map....");
            }
        };
    }

    private void setMarker(LatLng latLng) {
        if (marker != null) {
            marker.remove();
        }

        marker = map.addMarker(new MarkerOptions()
                .position(latLng));
    }

    private void setCircle(LatLng center, double radius) {
        if (circle != null)
            circle.remove();

        if (center != null) {
            double radiusMeters = MathUtil.convertMilesToMeters(radius);
            circle = map.addCircle(new CircleOptions()
                    .center(center)
                    .radius(radiusMeters)
                    .strokeWidth(AppConstants.RADIUS_STROKE_WID)
                    .strokeColor(R.color.cruGray));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(center);
            builder.include(new LatLng(MathUtil.addMetersToLatitude(center.latitude, radiusMeters), center.longitude));
            builder.include(new LatLng(MathUtil.addMetersToLatitude(center.latitude, -radiusMeters), center.longitude));
            LatLngBounds bounds = builder.build();

            int padding = DisplayMetricsUtil.dpToPx(CruApplication.getContext(), 8);
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            map.animateCamera(cu);
        }
    }

    //TODO @daniel somehow, avoid this network call
    private void setEventTime(String eventID) {
        EventProvider.requestCruEventByID(eventID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    eventStartDateTime = DateTimeUtils.toGregorianCalendar(event.startDate);
                });
    }


    private TextWatcher createCarCapacityWatcher()
    {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @OnTextChanged(R.id.car_capacity_field)
            @Override
            public void afterTextChanged(Editable s) {
                try
                {
                    if (s == null || s.toString().equals("")) {
                        return;
                    }
                    //make sure is within bounds
                    int setTo = Integer.parseInt(s.toString());
                    if (setTo < minCapacity)
                    {
                        carCapacity.setText(Integer.toString(minCapacity));
                    }
                    else if (setTo > AppConstants.MAX_CAR_CAPACITY)
                    {
                        carCapacity.setText(Integer.toString(AppConstants.MAX_CAR_CAPACITY));
                    }
                }
                catch (NumberFormatException nfe)
                {

                }
            }
        };
    }
}