package org.androidcru.crucentralcoast.data.services;

import org.androidcru.crucentralcoast.data.models.Campus;
import org.androidcru.crucentralcoast.data.models.CruEvent;
import org.androidcru.crucentralcoast.data.models.CruUser;
import org.androidcru.crucentralcoast.data.models.MinistrySubscription;
import org.androidcru.crucentralcoast.data.models.MinistryTeam;
import org.androidcru.crucentralcoast.data.models.Passenger;
import org.androidcru.crucentralcoast.data.models.Resource;
import org.androidcru.crucentralcoast.data.models.ResourceTag;
import org.androidcru.crucentralcoast.data.models.Ride;
import org.androidcru.crucentralcoast.data.models.SummerMission;

import java.util.ArrayList;

import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

public interface CruApiService
{
    /**
     * Method modeling the GET request for Events
     * @return Callback request
     */
    @GET("/api/event/list")
    public Observable<ArrayList<CruEvent>> getEvents();

    @GET("/api/ministry/list")
    public Observable<ArrayList<MinistrySubscription>> getMinistries();

    @GET("/api/campus/list")
    public Observable<ArrayList<Campus>> getCampuses();

    @GET("/api/ministryteam/list")
    public Observable<ArrayList<MinistryTeam>> getMinistryTeams();

    @FormUrlEncoded
    @POST("/api/user/find")
    public Observable<ArrayList<CruUser>> getMinistryTeamLeaders(@Field("ministryTeams") ArrayList<String> ministryId);

    @GET("/api/ride/list")
    Observable<ArrayList<Ride>> getRides();

    @GET("api/resource/list")
    public Observable<ArrayList<Resource>> getResources();

    @FormUrlEncoded
    @POST("/api/resourcetag/find")
    Observable<ArrayList<ResourceTag>> findSingleResourceTag(@Field("resources") String id);

    @FormUrlEncoded
    @POST("/api/ride/find")
    Observable<ArrayList<Ride>> findSingleRide(@Field("_id") String id);

    @FormUrlEncoded
    @POST("/api/passenger/find")
    Observable<ArrayList<Passenger>> findSinglePassenger(@Field("_id") String id);

    @FormUrlEncoded
    @POST("/api/event/find")
    Observable<ArrayList<CruEvent>> findSingleCruEvent(@Field("_id") String id);

    @POST("/api/ride/create")
    Observable<Ride> postRide(@Body Ride ride);

    @POST("/api/ride/update")
    Observable<Ride> updateRide(@Body Ride ride);

    @POST("/api/passenger/create")
    Observable<Passenger> createPassenger(@Body Passenger passenger);

    @FormUrlEncoded
    @POST("/api/ride/addPassenger")
    Observable<Void> addPassenger(@Field("ride_id") String rideId, @Field("passenger_id") String passengerId);

    @FormUrlEncoded
    @POST("/api/ride/dropPassenger")
    Observable<Void> dropPassenger(@Field("ride_id") String rideId, @Field("passenger_id") String passengerId);

    @FormUrlEncoded
    @POST("/api/ride/dropRide")
    Observable<Void> dropRide(@Field("ride_id") String rideId);

    @GET("/api/summermission/list")
    Observable<ArrayList<SummerMission>> getSummerMissions();

//    @GET("api/event/:id")
//    public CruEvent getEventByID(String id);

    //@POST("/api/passenger/create")
    //Observable<Passenger> postPassenger(@Body Passenger passenger);
}
