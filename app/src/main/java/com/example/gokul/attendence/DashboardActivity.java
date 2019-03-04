package com.example.gokul.attendence;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.gokul.attendence.profile.ProfileFragment;
import com.example.gokul.attendence.report.ReportFragment;

public class DashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        bottomNavigationView = findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = HomeFragment.newInstance();
                        switch (item.getItemId()) {
                            case R.id.navigation_profile:
                                selectedFragment = ProfileFragment.newInstance();
                                break;
                            case R.id.navigation_home:
                                selectedFragment = HomeFragment.newInstance();
                                break;
                            case R.id.navigation_report:
                                selectedFragment = ReportFragment.newInstance();
                                break;
                        }
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_layout, selectedFragment);
                        transaction.commit();
                        return true;
                    }
                });

//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.frame_layout, HomeFragment.newInstance());
//        transaction.commit();

        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Confirmation")
                .setMessage("Do you really wish to exit?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void navigateToHomeFragment() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }
}






