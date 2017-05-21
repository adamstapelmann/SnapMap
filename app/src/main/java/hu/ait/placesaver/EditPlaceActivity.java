package hu.ait.placesaver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.UUID;

import hu.ait.placesaver.data.Place;
import io.realm.Realm;

public class EditPlaceActivity extends AppCompatActivity implements PlacesLocationManager.OnNewLocationAvailable, OnMapReadyCallback {
    public static final String KEY_PLACE = "KEY_PLACE";
    public static final int RESULT_CODE_DELETE = 1002;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int ZOOM_LEVEL= 10;
    public static final String PICTURE_URL = "PICTURE_URL";
    public static final String PICTURE_BITMAP = "PICTURE_BITMAP";



    private EditText etLocTitle;
    private EditText etLocDate;
    private EditText etLocTime;
    private EditText etLocDescription;
    private ImageView latlongIv;
    private ImageView openCameraIv;
    private ImageView viewPicture;
    private Place placeToEdit = null;
    private Bitmap pictureTakenBitmap=null;
    private boolean isUploadingPicture=false;


    private PlacesLocationManager placesLocationManager = new PlacesLocationManager(this);
    private double lat, lng;
    private double currentLat, currentLng;

    private GoogleMap mMap;
    private Marker mkr;

    private boolean canCreate = false;
    private boolean canGetCurrentLoc = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_place);


        requestNeededPermission();

        setupUI();


        if (getIntent().getSerializableExtra(MainActivity.KEY_EDIT) != null) {
            initEdit();
        } else {
            canCreate = true;
            canGetCurrentLoc = true;
        }



        setUpCameraImageViewBtn();
        setUpViewPictureImageViewBtn();

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

        if (placeToEdit!=null && placeToEdit.hasPlacePicture()){
            Glide.with(this).load(placeToEdit.getPlacePictureURL()).into(viewPicture);

        }else if (pictureTakenBitmap!=null){
            viewPicture.setImageBitmap(pictureTakenBitmap);
        }


        viewPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (placeToEdit !=null && placeToEdit.hasPlacePicture()){
                    openViewPictureActivityGlide(placeToEdit.getPlacePictureURL());
                }else if (pictureTakenBitmap!=null){

                    openViewPictureActivityBitmap(pictureTakenBitmap);
                }
            }
        });




    }


    private void openViewPictureActivityGlide(String imageURL){
        Intent i = new Intent(this, ViewPlacePictureActivity.class);
        i.putExtra(PICTURE_URL,imageURL);
        startActivity(i);

    }
    private void openViewPictureActivityBitmap(Bitmap bitmap){
        Intent i = new Intent(this, ViewPlacePictureActivity.class);
        i.putExtra(PICTURE_BITMAP,bitmap);
        startActivity(i);

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

        lat = placeToEdit.getLat();
        lng = placeToEdit.getLng();

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

                Intent intentResult = new Intent();
                intentResult.putExtra(KEY_PLACE, placeId);
                setResult(RESULT_CODE_DELETE, intentResult);

                finish();
            }
        });


        latlongIv = (ImageView) findViewById(R.id.lat_long_iv);
        latlongIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canGetCurrentLoc = true;
                Toast.makeText(EditPlaceActivity.this, "Lat" + lat + "\n" + "lng: " + lng, Toast.LENGTH_SHORT).show();
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

        if (pictureTakenBitmap!=null) {
            try {
                uploadImage();
                isUploadingPicture=true;
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            Log.v("ADDED ITEM","Adedd");

            if (!isUploadingPicture) {
                finish();
            }
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
        if (pictureTakenBitmap==null && (placeToEdit!=null && !placeToEdit.hasPlacePicture())){
            canSave = false;
            Toast.makeText(this, "Select camera button to take picture", Toast.LENGTH_SHORT).show();
        }

        return canSave;
    }

    private void requestNeededPermission() {

        Log.v("permissionmethod","method");

        if ( (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED)

                && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED)) {

            Log.v("START MONITORING","monitoring");
            placesLocationManager.startLocationMonitoring(this);

        }else{
            Log.v("REQUEST PERM","requesting");


            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.ACCESS_FINE_LOCATION},
                    101);
        }

    }

    private void openCamera(){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }
    @Override
    public void onNewLocation(Location location) {
        currentLat = location.getLatitude();
        currentLng = location.getLongitude();

        if (canGetCurrentLoc && mkr!=null) {
            LatLng placeCoords = new LatLng(currentLat, currentLng);
            mkr.setPosition(placeCoords);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(placeCoords));

            lat = currentLat;
            lng = currentLng;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (data.getExtras()!=null){
                pictureTakenBitmap = (Bitmap) data.getExtras().get("data");
                viewPicture.setImageBitmap(pictureTakenBitmap);
            }

        }
    }
    public void uploadImage() throws Exception {

        Bitmap bitmap = pictureTakenBitmap;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageInBytes = baos.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String newImage = placeToEdit.getPlaceID() +".jpg";
        StorageReference newImageRef = storageRef.child(newImage);
        StorageReference newImageImagesRef = storageRef.child("images/"+newImage);
        newImageRef.getName().equals(newImageImagesRef.getName());    // true
        newImageRef.getPath().equals(newImageImagesRef.getPath());    // false


        UploadTask uploadTask = newImageImagesRef.putBytes(imageInBytes);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(EditPlaceActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                getRealm().beginTransaction();
                Log.v("SET DOWNLAOD URL","url");
                placeToEdit.uploadedPicture();
                placeToEdit.setPlacePictureURL(taskSnapshot.getDownloadUrl().toString());
                getRealm().commitTransaction();
                finish();

            }
        });

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

                Log.v("PERMISSIONS GRANTED", "granted");

            } else {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final LatLng placeCoords;

        if (placeToEdit!=null){

            placeCoords= new LatLng(placeToEdit.getLat(), placeToEdit.getLng());
        }else{
            placeCoords= new LatLng(currentLat, currentLng);
        }

        MarkerOptions options = new MarkerOptions()
                .position(placeCoords)
                .title("Current location")
                .draggable(true);

        mkr = mMap.addMarker(options);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);


        mMap.moveCamera(CameraUpdateFactory.newLatLng(placeCoords));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                canGetCurrentLoc = false;
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                mkr.setPosition(marker.getPosition());
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(placeCoords));
            }


            @Override
            public void onMarkerDragEnd(Marker marker) {
                mkr.setPosition(marker.getPosition());
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(placeCoords));
                lat = marker.getPosition().latitude;
                lng = marker.getPosition().longitude;
            }
        });




    }



}


