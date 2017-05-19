package hu.ait.placesaver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class SplashActivity extends AppCompatActivity {


    private final int PAUSE_TIME = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View splashLayout = findViewById(R.id.splashLayout);

        final Animation animation = AnimationUtils.loadAnimation(SplashActivity.this,
                R.anim.play_anim);

        splashLayout.startAnimation(animation);

        splashLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent startMain = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(startMain);
                finish();
            }
        }, PAUSE_TIME);




    }
}
