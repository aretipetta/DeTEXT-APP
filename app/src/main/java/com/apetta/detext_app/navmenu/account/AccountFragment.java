package com.apetta.detext_app.navmenu.account;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.apetta.detext_app.R;
import com.apetta.detext_app.alertDialog.TemperatureAlertDialog;
import com.apetta.detext_app.login.SignInActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {
    FirebaseAuth mAuth;

    Button signOutButton, viewHistoryButton;
    ImageButton weatherButton;
    EditText userEmailEditText;
    SensorManager sensorManager;

    LocationManager locationManager;
    LocationListener locationListener;
    private String country, locality;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        signOutButton = view.findViewById(R.id.signOutButton);
        viewHistoryButton = view.findViewById(R.id.viewHistoryBtnAccount);
        weatherButton = view.findViewById(R.id.weatherImgBtn);
        userEmailEditText = view.findViewById(R.id.emailEditTextAccount);
        userEmailEditText.setText(mAuth.getCurrentUser().getEmail());
        setListeners();

    }

    /* Sets listeners to activity's widgets */
    public void setListeners() {
        signOutButton.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(getContext(), SignInActivity.class));
        });

        viewHistoryButton.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), HistoryActivity.class));
        });

        weatherButton.setOnClickListener(view -> {
            getCountryAndLocality();
        });
    }

    private void getCountryAndLocality() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            openTemperatureDialog();
            return;
        }
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                country = null;
                locality = null;
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                List<Address> addressList;
                try {
                    addressList = geocoder.getFromLocation(location.getLatitude(), location.getLatitude(), 1);
                    country = addressList.get(0).getCountryName();
                    locality = addressList.get(0).getLocality();
                    openTemperatureDialog();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}

            @Override
            public void onProviderEnabled(String s) {}

            @Override
            public void onProviderDisabled(String s) {}
        };

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return;
        }
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, Looper.myLooper());
        else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, Looper.myLooper());
    }

    private void openTemperatureDialog() {
        sensorManager = (SensorManager) getActivity().getSystemService(getContext().SENSOR_SERVICE);
        if(sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
            Sensor tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            SensorEventListener tempListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    String temperature = sensorEvent.values[0] + " °C";
                    String location;
                    if(country != null && locality != null) location = country + ", " + locality;
                    else if(country == null && locality == null) location = getString(R.string.not_available);
                    else location = (country != null) ? country : locality;
                    TemperatureAlertDialog temperatureAlertDialog = new TemperatureAlertDialog(requireContext(), temperature, location);
                    temperatureAlertDialog.show();
                    sensorManager.unregisterListener(this);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {}
            };
            sensorManager.registerListener(tempListener, tempSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            String temperature = getString(R.string.not_available);
            TemperatureAlertDialog temperatureAlertDialog = new TemperatureAlertDialog(requireContext(), temperature, "location");
            temperatureAlertDialog.show();
        }
    }
}