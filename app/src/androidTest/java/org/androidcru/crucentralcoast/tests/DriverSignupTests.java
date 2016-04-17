package org.androidcru.crucentralcoast.tests;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.view.View;

import org.androidcru.crucentralcoast.R;
import org.androidcru.crucentralcoast.presentation.views.MainActivity;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by main on 3/10/2016.
 */
public class DriverSignupTests {

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<MainActivity>(MainActivity.class);

    private void switchToEvents()
    {
        onView(ViewMatchers.withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withText(R.string.nav_events)).perform(click());
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.close());
    }

    private void switchToMyRides()
    {
        onView(ViewMatchers.withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withText(R.string.nav_my_rides)).perform(click());
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.close());
    }

    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v != null) {
                    v.performClick();
                }
            }
        };
    }

    @Test
    public void testCarCapacityInDriverSignup() {
        String input = "5";

        switchToEvents();

        //click on first item
        onView(withId(R.id.recyclerview))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.rideSharingButton)));

        //click on the Driver part of the alert dialog
        onView(withText("DRIVER")).perform(click());

        //change the car capacity field
        onView(withId(R.id.car_capacity_field))
                .perform(typeText(input), closeSoftKeyboard());

        //check that the data was saved
        onView(withId(R.id.car_capacity_field))
                .check(matches(withText(input)));
    }

    @Test
    public void testingDisplayRideData() {
        switchToMyRides();

        onView(allOf(withId(R.id.recyclerview), isDisplayed()))
            .perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.editOffering)));

        //check the initial values
        onView(withId(R.id.name_field))
                .check(matches(withText("Daniel Toy")));
        onView(withId(R.id.phone_field))
                .check(matches(withText("(408) 207-3818")));
        onView(withId(R.id.gender_view))
                .check(matches(withText("Male")));
        onView(withId(R.id.car_capacity_field))
                .check(matches(withText("6")));
        onView(withId(R.id.date_field))
                .check(matches(withText("2016-10-16")));
        onView(withId(R.id.time_field))
                .check(matches(withText("14:00:00")));
        onView(withId(R.id.radius_field))
                .check(matches(withText("1.0")));
    }
}
