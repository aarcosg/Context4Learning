package es.us.context4learning.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.us.context4learning.Constants;
import es.us.context4learning.MoodleContextApplication;
import es.us.context4learning.R;
import es.us.context4learning.backend.locationRestrictionApi.LocationRestrictionApi;
import es.us.context4learning.backend.locationRestrictionApi.model.GeoPt;
import es.us.context4learning.backend.locationRestrictionApi.model.LocationRestriction;
import es.us.context4learning.backend.locationRestrictionApi.model.User;
import es.us.context4learning.data.api.google.awareness.AwarenessApiHelper;
import es.us.context4learning.di.HasComponent;
import es.us.context4learning.di.components.DaggerMapComponent;
import es.us.context4learning.di.components.MapComponent;
import es.us.context4learning.exception.InternetNotAvailableException;
import es.us.context4learning.exception.LocationRestrictionNotInsertedGAEException;
import es.us.context4learning.observable.FetchAddressObservable;
import es.us.context4learning.observable.appengine.InsertLocationRestrictionGAEObservable;
import es.us.context4learning.observable.google.awareness.LocationObservable;
import es.us.context4learning.utils.RxNetwork;
import es.us.context4learning.utils.Utils;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class MapActivity extends BaseActivity implements GoogleMap.OnMarkerDragListener, OnMapReadyCallback, HasComponent<MapComponent> {

    private static final String TAG = MapActivity.class.getCanonicalName();

    @Inject
    Context mContext;
    @Inject
    RxNetwork mRxNetwork;
    @Inject
    SharedPreferences mPrefs;
    @Inject
    LocationRestrictionApi mGAELocationRestrictionApi;

    @Bind(R.id.progress_bar)
    ProgressBar mProgressBar;

    private MapComponent mMapComponent;
    private GoogleMap mMap;
    private LatLng mLocation;
    private LatLng mNewLocation;
    private Circle mRestrictionRadius;
    private Subscription mSubscription = Subscriptions.empty();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initializeInjector();
        setContentView(R.layout.activity_map);
        buildActionBarToolbar(getString(R.string.title_map_activity),true);
        matchStatusBarHeight();
        ButterKnife.bind(this);
        buildGoogleApiClient();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        if(mSubscription.isUnsubscribed()){
            mSubscription.unsubscribe();
        }
    }

    @Override
    public MapComponent getComponent() {
        if(this.mMapComponent == null){
            this.initializeInjector();
        }
        return this.mMapComponent;
    }

    private void initializeInjector() {
        mMapComponent = DaggerMapComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build();
        mMapComponent.inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.action_done:
                item.setVisible(false);
                fetchAddress();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        super.onConnected(dataBundle);
        if (mMap == null) {
            if (checkPlayServices()) {
                ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getResources().getString(R.string.error_getting_maps));
                builder.setCancelable(true);
                builder.setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setOnMarkerDragListener(this);

        mSubscription = MoodleContextApplication.get(this)
                .getApplicationComponent()
                .getObservableGoogleApiClientAwareness()
                .subscribeOn(Schedulers.immediate())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(googleApiClient ->
                        Observable.create(new LocationObservable(this, googleApiClient))
                                .subscribeOn(Schedulers.immediate())
                                .observeOn(AndroidSchedulers.mainThread())
                ).subscribe(
                        this::onLocationFetched
                        , throwable -> {
                            Log.d(TAG, "Location could not be fetched with Awareness API. Use default location instead");
                            Location location = new Location("es.us.context4learning");
                            location.setLatitude(Constants.DEFAULT_LATITUDE);
                            location.setLongitude(Constants.DEFAULT_LONGITUDE);
                            onLocationFetched(location);
                        });
    }

    @Override
    public void onMarkerDrag(Marker marker) {}

    @Override
    public void onMarkerDragStart(Marker marker) {}

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mNewLocation = marker.getPosition();
        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        mRestrictionRadius.setCenter(marker.getPosition());
    }

    private void onLocationFetched(Location location){
        Utils.saveLastLocationToPreferences(mContext, location);
        mLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, 11));
        mRestrictionRadius = mMap.addCircle(new CircleOptions()
                .center(mLocation)
                .radius(Constants.DEFAULT_RADIUS_RESTRICTION)
                .strokeWidth(2)
                .strokeColor(ContextCompat.getColor(this, R.color.accent))
                .fillColor(ContextCompat.getColor(this, R.color.radius_fill_color))
        );
        mMap.addMarker(new MarkerOptions()
                .position(mLocation)
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    }

    private void fetchAddress(){
        mProgressBar.setVisibility(View.VISIBLE);
        mNewLocation = mNewLocation == null ? mLocation : mNewLocation;
        mSubscription = mRxNetwork.checkInternetConnection()
                .andThen(Observable.create(new FetchAddressObservable(mContext, mNewLocation))
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread()))
                .subscribe(
                        this::saveLocationRestriction
                        ,throwable -> {
                            saveLocationRestriction(null);
                        }
                );
    }

    private void saveLocationRestriction(String address){
        Long userId = mPrefs.getLong(Constants.PROPERTY_USER_ID,0L);
        if(userId > 0L){
            LocationRestriction locationRestriction = new LocationRestriction();
            GeoPt geoPoint = new GeoPt();
            geoPoint.setLatitude((float) mNewLocation.latitude);
            geoPoint.setLongitude((float) mNewLocation.longitude);
            locationRestriction.setLocation(geoPoint);
            locationRestriction.setAddress(address);
            User user = new User();
            user.setId(userId);
            locationRestriction.setUser(user);

            mSubscription = mRxNetwork.checkInternetConnection()
                    .andThen(Observable.create(new InsertLocationRestrictionGAEObservable(mGAELocationRestrictionApi, locationRestriction))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread()))
                    .subscribe(
                            this::onLocationRestrictionInserted
                            ,this::showErrorMessage
                    );
        }

    }

    private void onLocationRestrictionInserted(LocationRestriction locationRestriction) {
        mProgressBar.setVisibility(View.GONE);
        if(locationRestriction != null) {
            Log.d(TAG, "New location restriction added: " + locationRestriction.getLocation().getLatitude()
                    + " - " + locationRestriction.getLocation().getLongitude());
            // Update awareness fences
            AwarenessApiHelper.updateMainAwarenessFence(mContext, null);
            Intent returnIntent = new Intent();
            returnIntent.putExtra(Constants.EXTRA_LATITUDE,locationRestriction.getLocation().getLatitude());
            returnIntent.putExtra(Constants.EXTRA_LONGITUDE,locationRestriction.getLocation().getLongitude());
            returnIntent.putExtra(Constants.EXTRA_ID,locationRestriction.getId());
            returnIntent.putExtra(Constants.EXTRA_ADDRESS,
                    locationRestriction.getAddress() != null ?
                            locationRestriction.getAddress() :
                            locationRestriction.getLocation().getLatitude() + ", "
                                    + locationRestriction.getLocation().getLongitude());
            setResult(RESULT_OK,returnIntent);
            finish();
        }
    }

    private void showErrorMessage(Throwable throwable) {
        mProgressBar.setVisibility(View.GONE);
        invalidateOptionsMenu();
        String message = "";
        if(throwable instanceof InternetNotAvailableException){
            message = getString(R.string.exception_message_no_connection);
        }else if(throwable instanceof LocationRestrictionNotInsertedGAEException){
            message = getString(R.string.error_restriction_not_added);
        }
        Snackbar.make(mProgressBar,
                message,
                Snackbar.LENGTH_LONG)
                .show();
    }

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, MapActivity.class);
        ActivityCompat.startActivity(activity, intent, null);
    }
}