package com.aone.menurandomchoice.views.customermain;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.aone.menurandomchoice.R;
import com.aone.menurandomchoice.databinding.ActivityCustomerMainBinding;
import com.aone.menurandomchoice.repository.model.MenuLocation;
import com.aone.menurandomchoice.repository.model.MenuLocationCamera;
import com.aone.menurandomchoice.repository.model.MenuSearchRequest;
import com.aone.menurandomchoice.views.base.BaseActivity;
import com.aone.menurandomchoice.views.locationsearch.LocationSearchActivity;
import com.aone.menurandomchoice.views.menuregister.adapter.MenuCategoryAdapter;
import com.aone.menurandomchoice.views.menuregister.adapter.MenuCategoryAdapterContract;
import com.aone.menurandomchoice.views.menuregister.adapter.viewholder.OnMenuCategoryClickListener;
import com.aone.menurandomchoice.views.menuselect.MenuSelectActivity;

import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

public class CustomerMainActivity extends BaseActivity<ActivityCustomerMainBinding, CustomerMainContract.View, CustomerMainContract.Presenter>
        implements CustomerMainContract.View, MapView.MapViewEventListener {

    private static final String LOG_TAG ="CustomerMainActivity";
    private final int LOCATION_DATA = 3000;

    private LocationManager locationManager;
    private List<View> radiusButton = new ArrayList<>();
    private MenuCategoryAdapterContract.View menuCategoryAdapterView;
    private MapView mMapView;
    private MapPOIItem mCustomMarker;
    private MapCircle circle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        setUpActivityToDataBinding();
        setUpPresenterToDataBinding();
        setUpSearchToolBar();
        setRadiusButtonList();
        setUpCategoryRecyclerView();
        getDataBinding().setMenuLocationCamera(new MenuLocationCamera(37.4980854357918
                                                                        ,127.028000275071
                                                                        ,2));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mMapView = new MapView(this);
        mMapView.setMapViewEventListener(this);
        mMapView.setMapType(MapView.MapType.Standard);

        getDataBinding().activityCustomerMainMvDaum.addView(mMapView);

        Intent intent = getIntent();
        Bundle posXY = intent.getBundleExtra(getView().getActivityContext().getString(R.string.activity_customer_main_xy));

        MapPoint mapPoint;
        if( posXY != null ) {
            mapPoint = MapPoint.mapPointWithGeoCoord(
                    posXY.getDouble(getView().getActivityContext().getString(R.string.activity_customer_main_latitude))
                    , posXY.getDouble(getView().getActivityContext().getString(R.string.activity_customer_main_longitude)));
        } else {
            MenuLocationCamera menuLocationCamera = getDataBinding().getMenuLocationCamera();
            mapPoint = MapPoint.mapPointWithGeoCoord(menuLocationCamera.getLatitude()
                                                    , menuLocationCamera.getLongitude());
        }
        createCustomMarker(mapPoint);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.removeAllPOIItems();
        getDataBinding().getMenuLocationCamera().setLatitude(mMapView.getMapCenterPoint()
                                                                        .getMapPointGeoCoord()
                                                                        .latitude);
        getDataBinding().getMenuLocationCamera().setLongitude(mMapView.getMapCenterPoint()
                                                                        .getMapPointGeoCoord()
                                                                        .longitude);
        getDataBinding().getMenuLocationCamera().setZoom(mMapView.getZoomLevel());
        getDataBinding().activityCustomerMainMvDaum.removeView(mMapView);
    }

    private void createCustomMarker(MapPoint mapPoint) {
        mCustomMarker = new MapPOIItem();
        mCustomMarker.setItemName(getView().getActivityContext().getString(R.string.app_name));
        mCustomMarker.setMapPoint(mapPoint);

        mCustomMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        mCustomMarker.setCustomImageResourceId(R.drawable.custom_marker_chicken);
        mCustomMarker.setCustomImageAutoscale(false);
        mCustomMarker.setCustomImageAnchor(0.5f, 1.0f);

        mMapView.addPOIItem(mCustomMarker);
        mMapView.selectPOIItem(mCustomMarker, true);

        circle = new MapCircle(
                MapPoint.mapPointWithGeoCoord(mapPoint.getMapPointGeoCoord().latitude
                                            , mapPoint.getMapPointGeoCoord().longitude), // center
                50, // radius
                Color.argb(0, 255, 120, 120), // strokeColor
                Color.argb(128, 255, 120, 120) // fillColor
        );

        circle.setCenter(mapPoint);
        circle.setRadius((int)getRadius());
        mMapView.addCircle(circle);

        mMapView.setMapCenterPointAndZoomLevel(mapPoint
                                                , getDataBinding().getMenuLocationCamera().getZoom()
                                                , false);
    }

    private void setUpActivityToDataBinding() {
        getDataBinding().setActivity(this);
    }

    private void setUpPresenterToDataBinding() { getDataBinding().setPresenter(getPresenter()); }

    private void setUpSearchToolBar() {
        Toolbar toolbar = getDataBinding().toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setRadiusButtonList() {
        radiusButton.add(getDataBinding().activityCustomerMainIvRadius1);
        radiusButton.add(getDataBinding().activityCustomerMainIvRadius2);
        radiusButton.add(getDataBinding().activityCustomerMainIvRadius3);
        radiusButton.add(getDataBinding().activityCustomerMainIvRadius4);
        radiusButton.add(getDataBinding().activityCustomerMainIvRadius5);
        getDataBinding().activityCustomerMainIvRadius1.setSelected(true);
    }

    private void setUpCategoryRecyclerView() {
        MenuCategoryAdapter menuCategoryAdapter = new MenuCategoryAdapter();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this
                                                                    , LinearLayoutManager.HORIZONTAL
                                                                    , false);
        getDataBinding().activityCustomerMainRcCategory.setLayoutManager(layoutManager);
        getDataBinding().activityCustomerMainRcCategory.setAdapter(menuCategoryAdapter);

        setUpAdapterToPresenter(menuCategoryAdapter);
        setUpAdapterToThis(menuCategoryAdapter);
    }

    private void setUpAdapterToPresenter(MenuCategoryAdapter menuCategoryAdapter) {
        getPresenter().setAdapterModel(menuCategoryAdapter);
    }

    private void setUpAdapterToThis(MenuCategoryAdapter menuCategoryAdapter) {
        menuCategoryAdapterView = menuCategoryAdapter;
        menuCategoryAdapterView.setOnMenuCategoryClickListener(new OnMenuCategoryClickListener() {
            @Override
            public void onMenuCategoryItemClick(@NonNull View view, int position) {
                getPresenter().handlingMenuCategoryItemClick(position);
            }
        });
    }

    public void onGPSButtonClicked() {
       if ( Build.VERSION.SDK_INT >= 23
               && ContextCompat.checkSelfPermission( getApplicationContext()
                                                    , android.Manifest.permission.ACCESS_FINE_LOCATION )
               != PackageManager.PERMISSION_GRANTED ) {
           ActivityCompat.requestPermissions( CustomerMainActivity.this
                                            , new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION  }
                                            ,0 );
       } else {
           Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
           if (location != null) {
               Log.d(LOG_TAG,"GPS location is not null");
               mMapView.moveCamera(CameraUpdateFactory
                                    .newMapPoint(MapPoint.mapPointWithGeoCoord(location.getLatitude()
                                                                                , location.getLongitude())
                                                                                , 2));
               requestNewMenuList(MapPoint.mapPointWithGeoCoord(location.getLatitude()
                                                                , location.getLongitude()));
           } else {
               Log.d(LOG_TAG,"GPS location is null");
               locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER
                                                        , 100
                                                        , 0
                                                        , gpsLocationListener);
           }
       }
   }

    private LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.d(LOG_TAG,location.getProvider()+"location changed");
            locationManager.removeUpdates(this);
            mMapView.moveCamera(CameraUpdateFactory
                                .newMapPoint(MapPoint.mapPointWithGeoCoord(location.getLatitude()
                                                                            , location.getLongitude())
                                                                            , 2));
            requestNewMenuList(MapPoint.mapPointWithGeoCoord(location.getLatitude()
                                                            , location.getLongitude()));
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(LOG_TAG,"status changed");
        }

        public void onProviderEnabled(String provider) {
            Log.d(LOG_TAG,"provider enabled");
        }

        public void onProviderDisabled(String provider) {
            Log.d(LOG_TAG,"provider disabled");
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onMapViewInitialized(MapView mapView) { }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapCenterPoint) {
        mapView.removeAllCircles();
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
        requestNewMenuList(mapPoint);
    }

    private void requestNewMenuList(MapPoint mapPoint) {
        mMapView.removeAllCircles();
        circle.setCenter(mapPoint);
        mMapView.addCircle(circle);
        mMapView.removeAllPOIItems();

        mCustomMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(mapPoint.getMapPointGeoCoord().latitude
                                                                , mapPoint.getMapPointGeoCoord().longitude));
        mMapView.addPOIItem(mCustomMarker);
        double radius = getRadius();
        getPresenter().requestMenuList(mapPoint.getMapPointGeoCoord().latitude
                                        , mapPoint.getMapPointGeoCoord().longitude
                                        , radius);
    }

    private double getRadius() {
        double radius = 50;
        View radiusView;
        for(int i =0; i < radiusButton.size(); i++) {
            radiusView = radiusButton.get(i);
            if(radiusView.isSelected()) {
                radius = Double.valueOf((String)radiusView.getTag());
                break;
            }
        }
        return radius;
    }

    public void updateMapByRadiusCategory() {
        double radius = getRadius();
        mMapView.removeAllPOIItems();
        MapPoint.GeoCoordinate mapPointGeo = mMapView.getMapCenterPoint().getMapPointGeoCoord();
        getPresenter().getMenuCountFiltered(mapPointGeo.latitude, mapPointGeo.longitude, radius);
    }

    @Override
    public void onRadiusButtonClicked(View view) {
        double radius = 50;
        View radiusView;
        for(int i =0; i < radiusButton.size(); i++) {
            radiusView = radiusButton.get(i);
            radiusView.setSelected(false);
            if(radiusView == view ) {
                radiusView.setSelected(true);
                radius = Double.valueOf((String)radiusView.getTag());
            }
        }

        mMapView.removeAllCircles();
        circle.setRadius((int)radius);
        mMapView.addCircle(circle);

        updateMapByRadiusCategory();
    }

    public void setMarkerAtNewLocation(double lat, double lon, List<MenuLocation> closerDistanceList) {
        int len = 0;
        if(closerDistanceList != null) {
            len = closerDistanceList.size();
        }
        mCustomMarker.setItemName(len+"");
        mCustomMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(lat, lon));
        mMapView.addPOIItem(mCustomMarker);
        mMapView.selectPOIItem(mCustomMarker, true);

        MapPOIItem closerMenu;
        for(int i = 0; i < len; i++) {
            closerMenu = new MapPOIItem();
            closerMenu.setItemName("");
            closerMenu.setMapPoint(MapPoint.mapPointWithGeoCoord(closerDistanceList.get(i).getLatitude()
                                                                , closerDistanceList.get(i).getLongitude()));
            closerMenu.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            closerMenu.setCustomImageResourceId(R.drawable.custom_marker_pin);
            closerMenu.setCustomImageAutoscale(false);
            closerMenu.setCustomImageAnchor(0.5f, 1.0f);
            closerMenu.setShowCalloutBalloonOnTouch(false);
            mMapView.addPOIItem(closerMenu);
        }

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) { }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) { }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) { }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) { }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) { }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) { }

    @Override
    protected int getLayoutId() { return R.layout.activity_customer_main; }

    @NonNull
    @Override
    protected CustomerMainContract.Presenter setUpPresenter() { return new CustomerMainPresenter(); }

    @Override
    protected CustomerMainContract.View getView() { return this; }

    public void moveToLocationSearchPage() {
        mMapView.removeAllCircles();

        Intent locationSearchIntent = new Intent(CustomerMainActivity.this
                                                , LocationSearchActivity.class);
        locationSearchIntent.putExtra(getView().getActivityContext().getString(R.string.activity_store_edit_request_location_search)
                , getView().getActivityContext().getString(R.string.activity_customer_main_descriptor));
        startActivityForResult(locationSearchIntent,LOCATION_DATA);
    }

    public void moveToMenuSelectPage() {
        if(mMapView.getPOIItems().length > 1) {
            mMapView.removeAllCircles();
            Intent menuSelectIntent = new Intent(CustomerMainActivity.this
                    , MenuSelectActivity.class);
            int radius = (int) getRadius();
            String category = getPresenter().getSelectedCategory();

            if(getView().getActivityContext().getString(R.string.activity_customer_main_all_category).equals(category)) {
                category = "";
            }

            MapPoint.GeoCoordinate mapPointGeo = mMapView.getMapCenterPoint().getMapPointGeoCoord();
            menuSelectIntent.putExtra(getView().getActivityContext().getString(R.string.activity_customer_main_extra_menu_data)
                                    , new MenuSearchRequest(mapPointGeo.latitude
                                    , mapPointGeo.longitude
                                    , radius
                                    , category));

            startActivity(menuSelectIntent);
        } else {
            showToastMessage(getView().getActivityContext().getString(R.string.activity_customer_main_request_error));
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case LOCATION_DATA:
                    setIntent(data);
                    Bundle posXY = data.getBundleExtra(getView().getActivityContext().getString(R.string.activity_customer_main_xy));
                    if( posXY == null ) {
                        showToastMessage(getView().getActivityContext().getString(R.string.activity_customer_main_empty_result));
                        return;
                    }
                    mMapView.setMapCenterPointAndZoomLevel(
                            MapPoint.mapPointWithGeoCoord(posXY.getDouble(getView().getActivityContext().getString(R.string.activity_customer_main_latitude))
                                                        , posXY.getDouble(getView().getActivityContext().getString(R.string.activity_customer_main_longitude)))
                                                        ,1
                                                        ,true);
            }
        }
    }

}
