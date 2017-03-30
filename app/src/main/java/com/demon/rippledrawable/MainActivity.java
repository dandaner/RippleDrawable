package com.demon.rippledrawable;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.demon.rippledrawablelibrary.RippleDrawable;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        applyRipple();
    }

    private void applyRipple() {
        RippleDrawable.RippleEffect.applyRippleOfCircle(findViewById(R.id.circle),
                getResources().getDimensionPixelOffset(R.dimen.ripple_radius));

    }
}
