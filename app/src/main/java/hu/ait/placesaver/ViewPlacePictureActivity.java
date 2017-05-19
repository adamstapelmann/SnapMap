package hu.ait.placesaver;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ViewPlacePictureActivity extends AppCompatActivity {



    public static final String PICTURE_URL = "PICTURE_URL";
    public static final String PICTURE_BITMAP ="PICTURE_BITMAP";

    ImageView placePicture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_place_picture);


        placePicture = (ImageView) findViewById(R.id.placePicture);


        if (getIntent().getExtras() !=null){

            if (getIntent().hasExtra(PICTURE_BITMAP)){


                Bitmap bitmap = (getIntent().getParcelableExtra(PICTURE_BITMAP));
                placePicture.setImageBitmap(bitmap);
            }else{

                String url = getIntent().getStringExtra(PICTURE_URL);

                Glide.with(this).load(url)
                        .into(placePicture);

            }


        }


    }



}
