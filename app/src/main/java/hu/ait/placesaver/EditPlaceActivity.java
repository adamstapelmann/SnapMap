package hu.ait.placesaver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;
import java.util.UUID;

import hu.ait.placesaver.data.Place;
import io.realm.Realm;

public class EditPlaceActivity extends AppCompatActivity implements PlacesLocationManager.OnNewLocationAvailable, OnMapReadyCallback {
    public static final String KEY_PLACE = "KEY_PLACE";
    public static final int RESULT_CODE_DELETE = 1002;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private EditText etLocTitle;
    private EditText etLocDate;
    private EditText etLocTime;
    private EditText etLocDescription;
    private ImageView latlongIv;
    private ImageView openCameraIv;
    private ImageView viewPicture;
    private Place placeToEdit = null;

    private PlacesLocationManager placesLocationManager = new PlacesLocationManager(this);
    private double lat, lng;

    private GoogleMap mMap;

    private boolean canCreate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_place);
        requestNeededPermission();

        setupUI();


        setUpCameraImageViewBtn();
        setUpViewPictureImageViewBtn();


        if (getIntent().getSerializableExtra(MainActivity.KEY_EDIT) != null) {
            initEdit();
        } else {
            canCreate = true;
        }

        latlongIv = (ImageView) findViewById(R.id.lat_long_iv);
        latlongIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("IMAGE VIEW TOAST","Toast should pop up");
                Toast.makeText(EditPlaceActivity.this, "Lat" + lat + "\n" + "lng: " + lng, Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void setUpCameraImageViewBtn(){

        openCameraIv = (ImageView) findViewById(R.id.cameraIcon);
        openCameraIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();

            }
        });
    }

    private void setUpViewPictureImageViewBtn(){
        viewPicture = (ImageView) findViewById(R.id.viewPic);
        viewPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void initCreate() {
        getRealm().beginTransaction();
        placeToEdit = getRealm().createObject(Place.class, UUID.randomUUID().toString());
        placeToEdit.setPickUpDate(new Date(System.currentTimeMillis()));
        getRealm().commitTransaction();
    }

    private void initEdit() {
        String placeID = getIntent().getStringExtra(MainActivity.KEY_EDIT);
        placeToEdit = getRealm().where(Place.class)
                .equalTo("placeID", placeID)
                .findFirst();

        etLocTitle.setText(placeToEdit.getLocTitle());
        etLocDate.setText(placeToEdit.getLocDate());
        etLocTime.setText(placeToEdit.getLocTime());
        etLocDescription.setText(placeToEdit.getLocDescription());
    }

    private void setupUI() {

        etLocTitle = (EditText) findViewById(R.id.etLocTitle);
        etLocDate = (EditText) findViewById(R.id.etLocDate);
        etLocTime = (EditText) findViewById(R.id.etLocTime);
        etLocDescription = (EditText) findViewById(R.id.etLocDescription);




        Button btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePlace();
            }
        });

        Button btnDelete = (Button) findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String placeId = placeToEdit.getPlaceID();

//                Intent intentResult = new Intent();
//                intentResult.putExtra(KEY_PLACE, placeId);
//                setResult(RESULT_CODE_DELETE, intentResult);

                getRealm().beginTransaction();
                placeToEdit.deleteFromRealm();
                getRealm().commitTransaction();
                finish();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public Realm getRealm() {
        return ((MainApplication)getApplication()).getRealmPlaces();
    }

    private void savePlace() {
        if (canCreate) {
            initCreate();
        }

        getRealm().beginTransaction();

        placeToEdit.setLocTitle(etLocTitle.getText().toString());
        placeToEdit.setLocDate(etLocDate.getText().toString());
        placeToEdit.setLocTime(etLocTime.getText().toString());
        placeToEdit.setLocDescription(etLocDescription.getText().toString());

        placeToEdit.setLat(lat);
        placeToEdit.setLng(lng);

        getRealm().commitTransaction();

        if (canSave()) {
            Intent intentResult = new Intent();
            intentResult.putExtra(KEY_PLACE, placeToEdit.getPlaceID());
            setResult(RESULT_OK, intentResult);
            finish();
        }
    }

    private boolean canSave() {
        boolean canSave = true;

        if (etLocTitle.getText().toString().equals("")) {
            canSave = false;
            etLocTitle.setError("Please input a title");
        }
        if (etLocDate.getText().toString().equals("")) {
            canSave = false;
            etLocDate.setError("Please input a valid date");
        }
        if (etLocTime.getText().toString().equals("")) {
            canSave = false;
            etLocTime.setError("Please input a valid date");
        }

        return canSave;
    }

    private void requestNeededPermission() {

        if ( (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED)

                && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED)) {
            placesLocationManager.startLocationMonitoring(this);
            Log.v("MONITORING BEGINS","true");
            Log.v("CAMERA GRANTER", Integer.toString(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)));

            Log.v("PERM GRANTED CODE",Integer.toString(PackageManager.PERMISSION_GRANTED));
        }else{

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.ACCESS_COARSE_LOCATION},
                    101);
            Log.v("REQYEST PERMISI","request permission");
        }

    }

    private void openCamera(){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }
    @Override
    public void onNewLocation(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
    }

    @Override
    protected void onDestroy() {
        if (placesLocationManager != null) {
            placesLocationManager.stopLocationMonitoring();
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
                placesLocationManager.startLocationMonitoring(this);
                Log.v("PERMISSIONS GRANTER", "granted");

            } else {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        LatLng sydney = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Your current location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
