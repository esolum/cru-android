package org.androidcru.crucentralcoast.presentation.viewmodels.ridesharing;

import android.app.FragmentManager;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.google.android.gms.maps.model.LatLng;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Select;

import org.androidcru.crucentralcoast.CruApplication;
import org.androidcru.crucentralcoast.R;
import org.androidcru.crucentralcoast.data.models.CruEvent;
import org.androidcru.crucentralcoast.data.models.Ride;
import org.androidcru.crucentralcoast.data.models.queries.ConditionsBuilder;
import org.androidcru.crucentralcoast.data.models.queries.OptionsBuilder;
import org.androidcru.crucentralcoast.data.models.queries.Query;
import org.androidcru.crucentralcoast.presentation.util.ViewUtil;
import org.androidcru.crucentralcoast.presentation.views.ridesharing.passengersignup.RideInfoFragment;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import butterknife.Bind;
import butterknife.OnClick;
import timber.log.Timber;

public class RideFilterVM extends BaseRideVM
{
    private LatLng precisePlace;
    private CruEvent event;

    @Bind(R.id.round_trip) RadioButton roundTrip;
    @Bind(R.id.direction) RadioGroup directionGroup;
    @Bind(R.id.gender_field) @Select Spinner genderField;
    @Bind(R.id.time_field) @NotEmpty EditText rideTime;
    @Bind(R.id.date_field) @NotEmpty EditText rideDate;
    @Bind(com.google.android.gms.R.id.place_autocomplete_search_input) @NotEmpty EditText searchInput;

    public RideFilterVM(RideInfoFragment fragment, FragmentManager fm, CruEvent event)
    {
        super(fragment, fm);
        this.event = event;
        bindUI();
    }

    @Override
    protected void placeSelected(LatLng precisePlace, String placeAddress)
    {
        this.precisePlace = precisePlace;
    }

    public Query getQuery()
    {
        int selectedRadioIndex = directionGroup.indexOfChild(rootView.findViewById(directionGroup.getCheckedRadioButtonId()));
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
        String gender = (String) genderField.getSelectedItem();
        if(gender.equals("Any"))
            gender = null;

        ZonedDateTime dateTime = ZonedDateTime.of(date, time, ZoneId.systemDefault());

        ZonedDateTime threeHoursAfter = dateTime.plusHours(3l);
        ZonedDateTime threeHoursBefore = dateTime.minusHours(3l);

        Query query = new Query.Builder()
                .setCondition(new ConditionsBuilder()
                        .setCombineOperator(ConditionsBuilder.OPERATOR.AND)
                        .addRestriction(new ConditionsBuilder()
                                .setField(Ride.serializedGender)
                                .addRestriction(ConditionsBuilder.OPERATOR.REGEX, gender))
                        .addRestriction(new ConditionsBuilder()
                                .setField(Ride.serializedTime)
                                .addRestriction(ConditionsBuilder.OPERATOR.LTE, CruApplication.gson.toJsonTree(threeHoursAfter))
                                .addRestriction(ConditionsBuilder.OPERATOR.GTE, CruApplication.gson.toJsonTree(threeHoursBefore)))
                        .addRestriction(new ConditionsBuilder()
                                .setField(Ride.serializedDirection)
                                .addRestriction(ConditionsBuilder.OPERATOR.REGEX, direction.getValue()))
                        .build())
                .setOptions(new OptionsBuilder()
                        .addOption(OptionsBuilder.OPTIONS.LIMIT, 5)
                        .build())
                .build();

        Timber.d(CruApplication.gson.toJson(query));

        return query;
    }

    private void bindUI()
    {
        roundTrip.setChecked(true);
        ViewUtil.setSpinner(genderField, gendersForSpinner(R.array.genders_filter), null, 0);
    }

    @OnClick(R.id.time_field)
    public void onTimeClicked(View v)
    {
        onEventTimeClicked(v, org.threeten.bp.DateTimeUtils.toGregorianCalendar(event.startDate));
    }

    @OnClick(R.id.date_field)
    public void onDateClicked(View v)
    {
        onEventDateClicked(v, org.threeten.bp.DateTimeUtils.toGregorianCalendar(event.startDate));
    }


}
