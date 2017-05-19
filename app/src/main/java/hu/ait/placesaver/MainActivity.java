package hu.ait.placesaver;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hu.ait.placesaver.adapter.PlacesAdapter;
import hu.ait.placesaver.data.Place;
import hu.ait.placesaver.touch.PlacesTouchHelperAdapter;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_NEW_PLACE = 101;
    public static final int REQUEST_EDIT_PLACE = 102;
    public static final String KEY_EDIT = "KEY_EDIT";
    private PlacesAdapter placesAdapter;
    private CoordinatorLayout layoutContent;
    private DrawerLayout drawerLayout;
    private int placeToEditPosition = -1;

    StorageReference storageRef ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storageRef = FirebaseStorage.getInstance().getReference();

        setUpRecyclerAdapter();

        setUpUI();

    }

    public Realm getRealm() {
        return ((MainApplication)getApplication()).getRealmPlaces();
    }

    private void setUpRecyclerAdapter() {
        ((MainApplication)getApplication()).openRealm();

        RealmResults<Place> allPlaces = getRealm().where(Place.class).findAll();
        Place placesArray[] = new Place[allPlaces.size()];
        List<Place> placesResult = new ArrayList<Place>(Arrays.asList(allPlaces.toArray(placesArray)));

        placesAdapter = new PlacesAdapter(placesResult, this);
        RecyclerView recyclerViewPlaces = (RecyclerView) findViewById(
                R.id.recyclerViewPlaces);
        recyclerViewPlaces.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPlaces.setAdapter(placesAdapter);

        PlacesTouchHelperAdapter touchHelperCallback = new PlacesTouchHelperAdapter(
                placesAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(
                touchHelperCallback);
        touchHelper.attachToRecyclerView(recyclerViewPlaces);
    }

    private void setUpUI() {
        layoutContent = (CoordinatorLayout) findViewById(
                R.id.layoutContent);

        setUpFAB();
        setUpNavigationWindow();
        setUpToolBar();
    }

    private void setUpFAB() {
        FloatingActionButton fabAdd = (FloatingActionButton) findViewById(R.id.btnAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreatePlaceActivity();
            }
        });
    }

    private void setUpToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.mipmap.ic_launcher);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        
        toolbar.setOnMenuItemClickListener(
            new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_sort_name:
                            placesAdapter.sortByName();
                            showSnackBarMessage("Sorted by name");
                            break;
                        case R.id.action_sort_date:
                            placesAdapter.sortByDate();
                            showSnackBarMessage("Sorted by date");
                            break;
//                        case R.id.action_add_from_toolbar:
//                            showSnackBarMessage("Click the dots in the upper right corner");
//                            break;
                        default:
                            break;
                    }
                    return false;
                }
        });

    }

    private void setUpNavigationWindow() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        switch (menuItem.getItemId()) {
                            case R.id.action_add:
                                showMapActivity();
                                drawerLayout.closeDrawer(GravityCompat.START);
                                break;
                            case R.id.action_about:
                                showSnackBarMessage(getString(R.string.txt_about));
                                drawerLayout.closeDrawer(GravityCompat.START);
                                break;
                            case R.id.action_help:
                                showSnackBarMessage(getString(R.string.txt_help));
                                drawerLayout.closeDrawer(GravityCompat.START);
                                break;
                        }

                        return false;
                    }
                });
    }

    private void showCreatePlaceActivity() {
        Intent intentStart = new Intent(MainActivity.this,
                EditPlaceActivity.class);
        startActivityForResult(intentStart, REQUEST_NEW_PLACE);
    }

    public void showEditPlaceActivity(String placeID, int position) {
        Intent intentStart = new Intent(MainActivity.this,
                EditPlaceActivity.class);
        placeToEditPosition = position;

        intentStart.putExtra(KEY_EDIT, placeID);
        startActivityForResult(intentStart, REQUEST_EDIT_PLACE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                String placeID  = data.getStringExtra(
                        EditPlaceActivity.KEY_PLACE);

                Place place = getRealm().where(Place.class)
                        .equalTo("placeID", placeID)
                        .findFirst();

                if (requestCode == REQUEST_NEW_PLACE) {
                    placesAdapter.addPlace(place);
                    showSnackBarMessage(getString(R.string.txt_place_added));
                } else if (requestCode == REQUEST_EDIT_PLACE) {

                    placesAdapter.updatePlace(placeToEditPosition, place);
                    showSnackBarMessage(getString(R.string.txt_place_edited));
                }
                break;
            case RESULT_CANCELED:
                showSnackBarMessage(getString(R.string.txt_add_cancel));
                break;
            case EditPlaceActivity.RESULT_CODE_DELETE:
                String delPlaceId  = data.getStringExtra(
                        EditPlaceActivity.KEY_PLACE);

                if (placesAdapter.getPlaceByKey(delPlaceId).hasPlacePicture()){

                    removePlacePictureFromFirebase(delPlaceId);
                }

                placesAdapter.removePlaceByKey(delPlaceId);

                break;
        }
    }


    private void removePlacePictureFromFirebase(String id){

        StorageReference pictureReference = storageRef.child("images/"+id+".jpg");
        pictureReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Log.v("ITEM DELETED","deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

                Log.v("ITEM DELETED","not deleted");

            }
        });


    }
    public void deletePlace(Place place) {

        if (place.hasPlacePicture()){

            removePlacePictureFromFirebase(place.getPlaceID());

        }

        getRealm().beginTransaction();
        place.deleteFromRealm();
        getRealm().commitTransaction();


    }


    private void showSnackBarMessage(String message) {
        Snackbar.make(layoutContent,
                message,
                Snackbar.LENGTH_LONG
        ).setAction(R.string.action_hide, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //...
            }
        }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                showCreatePlaceActivity();
                return true;
            default:
                showCreatePlaceActivity();
                return true;
        }
    }

    private void showMapActivity() {
        Intent intentStart = new Intent(MainActivity.this,
                MapsActivityAllPoints.class);
        startActivity(intentStart);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((MainApplication)getApplication()).closeRealm();
    }


}

