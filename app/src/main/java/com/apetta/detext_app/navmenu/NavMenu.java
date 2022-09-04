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
        setFragment(new MainDetectionFragment(), "Detect Image");
        setListeners();
    }

    public void setListeners() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.toString().equals("Detect Image"))
                    setFragment(new MainDetectionFragment(), "Detect Image");
                else if(item.toString().equals("Translator"))
                    setFragment(new TranslatorFragment(), "Translator");
                else if(item.toString().equals("Account"))
                    setFragment(new AccountFragment(), "My Account");
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