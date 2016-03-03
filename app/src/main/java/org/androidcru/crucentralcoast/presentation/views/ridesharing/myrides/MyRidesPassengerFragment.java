package org.androidcru.crucentralcoast.presentation.views.ridesharing.myrides;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.orhanobut.logger.Logger;

import org.androidcru.crucentralcoast.CruApplication;
import org.androidcru.crucentralcoast.R;
import org.androidcru.crucentralcoast.data.models.Passenger;
import org.androidcru.crucentralcoast.data.models.Ride;
import org.androidcru.crucentralcoast.data.providers.RideProvider;
import org.androidcru.crucentralcoast.presentation.viewmodels.ridesharing.MyRidesPassengerVM;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MyRidesPassengerFragment extends Fragment {

    //Injected Views
    @Bind(R.id.event_list) RecyclerView eventList;
    @Bind(R.id.event_swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.progress) ProgressBar progressBar;
    @Bind(R.id.empty_events_view) RelativeLayout emptyEventsView;

    private ArrayList<MyRidesPassengerVM> rideVMs;
    private LinearLayoutManager layoutManager;

    private Observer<List<Ride>> rideSubscriber;

    public MyRidesPassengerFragment()
    {
        rideVMs = new ArrayList<>();
        rideSubscriber = new Observer<List<Ride>>()
        {
            @Override
            public void onCompleted()
            {
                eventList.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(Throwable e)
            {
                Logger.e(e, "Rides failed to retrieve.");
            }

            @Override
            public void onNext(List<Ride> rides)
            {
                setRides(rides);
            }
        };
    }

    /**
     * Invoked early on from the Android framework during rendering.
     * @param inflater Object used to inflate new views, provided by Android
     * @param container Parent view to inflate in, provided by Android
     * @param savedInstanceState State of the application if it is being refreshed, given to Android by dev
     * @return inflated View
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    /**
     * Invoked after onCreateView() and deals with binding view references after the
     * view has already been inflated.
     * @param view Inflated View created by onCreateView()
     * @param savedInstanceState State of the application if it is being refreshed, given to Android by dev
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //Let ButterKnife find all injected views and bind them to member variables
        ButterKnife.bind(this, view);

        //Show progress screen while waiting to load
        emptyEventsView.setVisibility(View.GONE);
        eventList.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        //Enables actions in the Activity Toolbar (top-right buttons)
        setHasOptionsMenu(true);

        //LayoutManager for RecyclerView
        layoutManager = new LinearLayoutManager(getActivity());
        eventList.setLayoutManager(layoutManager);

        //Adapter for RecyclerView
        MyRidesDriverAdapter rideSharingAdapter = new MyRidesDriverAdapter(new ArrayList<>(), getContext());
        eventList.setAdapter(rideSharingAdapter);
        eventList.setHasFixedSize(true);

        //Set up SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeColors(R.color.cruDarkBlue, R.color.cruGold, R.color.cruOrange);
        swipeRefreshLayout.setOnRefreshListener(this::forceUpdate);

        forceUpdate();
    }



    private void forceUpdate()
    {
        RideProvider.requestRides()
                .flatMap(rides -> Observable.from(rides))
                .filter(ride -> {
                    for (Passenger p : ride.passengers)
                    {
                        if (p.gcm_id != null && p.gcm_id.equals(CruApplication.getGCMID()))
                        {
                            return true;
                        }
                    }
                    return false;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .toList()
                .subscribe(rideSubscriber);
    }

    /**
     * Updates the UI to reflect the Events in events
     * @param rides List of new Events the UI should adhere to
     */
    public void setRides(List<Ride> rides)
    {
        rideVMs.clear();
        rx.Observable.from(rides)
                .map(ride -> new MyRidesPassengerVM(ride, false, getActivity()))
                .subscribeOn(Schedulers.immediate())
                .subscribe(rideVMs::add);
        eventList.setAdapter(new MyRidesPassengerAdapter(rideVMs));
        swipeRefreshLayout.setRefreshing(false);
    }
}
