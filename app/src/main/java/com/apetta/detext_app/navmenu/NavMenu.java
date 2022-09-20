package com.apetta.detext_app.navmenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.apetta.detext_app.R;
import com.apetta.detext_app.navmenu.account.AccountFragment;
import com.apetta.detext_app.navmenu.detection.MainDetectionFragment;
import com.apetta.detext_app.navmenu.translation.TranslatorFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class NavMenu extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_menu);
        bottomNavigationView = findViewById(R.id.bottomNavigationMenu);
        setFragment(new MainDetectionFragment(), getString(R.string.detect_img));
        setListeners();
    }

    public void setListeners() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.toString().equals(getString(R.string.detect_img)))
                    setFragment(new MainDetectionFragment(), getString(R.string.detect_img));
                else if(item.toString().equals(getString(R.string.translator)))
                    setFragment(new TranslatorFragment(), getString(R.string.translator));
                else if(item.toString().equals(getString(R.string.acc)))
                    setFragment(new AccountFragment(), getString(R.string.acc));
                return true;
            }
        });
    }

    public void setFragment(Fragment fragment,String fragmentTitle) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayoutNavMenu, fragment).commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(fragmentTitle);
        }
    }
}