package hu.ait.placesaver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ViewPlacePictureActivity extends AppCompatActivity {



    public static final String PICTURE_URL = "PICTURE_URL";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_place_picture);


        ImageView placePicture = (ImageView) findViewById(R.id.placePicture);

        if(getIntent().getExtras() !=null) {
            String url = getIntent().getExtras().getString(PICTURE_URL);

            Glide.with(this).load(url)
                    .into(placePicture);
        }

    }
}
