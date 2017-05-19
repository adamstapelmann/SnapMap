package hu.ait.placesaver;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import hu.ait.placesaver.adapter.PlacesAdapter;
import hu.ait.placesaver.data.Place;
import io.realm.Realm;
import io.realm.RealmResults;

public class MapsActivityAllPoints extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_all_points);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public Realm getRealm() {
        return ((MainApplication)getApplication()).getRealmPlaces();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        RealmResults<Place> allPlaces = getRealm().where(Place.class).findAll();

        if (allPlaces.size()>0) {

            LatLng marker = new LatLng(allPlaces.get(0).getLat(), allPlaces.get(0).getLng());

            for (Place place : allPlaces) {
                marker = new LatLng(place.getLat(), place.getLat());
                mMap.addMarker(new MarkerOptions().position(marker).title(place.getLocTitle()));
            }

            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));

        }
    }
}
