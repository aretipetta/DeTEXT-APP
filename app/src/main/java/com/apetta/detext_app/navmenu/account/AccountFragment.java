package com.apetta.detext_app.navmenu.account;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.apetta.detext_app.R;
import com.apetta.detext_app.alertDialogs.TemperatureAlertDialog;
import com.apetta.detext_app.login.SignInActivity;
import com.google.firebase.auth.FirebaseAuth;

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
        // just like onCreate method
        mAuth = FirebaseAuth.getInstance();

        signOutButton = view.findViewById(R.id.signOutButton);
        viewHistoryButton = view.findViewById(R.id.viewHistoryBtnAccount);
        weatherButton = view.findViewById(R.id.weatherImgBtn);
        userEmailEditText = view.findViewById(R.id.emailEditTextAccount);
        userEmailEditText.setText(mAuth.getCurrentUser().getEmail());
        setListeners();

    }

    public void setListeners() {
        signOutButton.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(getContext(), SignInActivity.class));
        });

        viewHistoryButton.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), HistoryActivity.class));
        });

        weatherButton.setOnClickListener(view -> {
            sensorManager = (SensorManager) getActivity().getSystemService(getContext().SENSOR_SERVICE);
            if(sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
                Sensor tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
                SensorEventListener tempListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent sensorEvent) {
                        String temperature = sensorEvent.values[0] + " °C";
                        TemperatureAlertDialog temperatureAlertDialog = new TemperatureAlertDialog(requireContext(), temperature, "location");
                        temperatureAlertDialog.show();
                        sensorManager.unregisterListener(this);
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int i) {}
                };
                sensorManager.registerListener(tempListener, tempSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
            else {
                String temperature = "Not available"; // getString(R.string.notAvailable);
                TemperatureAlertDialog temperatureAlertDialog = new TemperatureAlertDialog(requireContext(), temperature, "location");
                temperatureAlertDialog.show();
            }
        });
    }

}