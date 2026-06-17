package com.example.watertracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_WATER = "water_prefs";
    private static final String KEY_TARGET = "daily_target";
    private static final String KEY_INTAKE = "current_intake";

    private TextView tvProgressText, tvOverTarget;
    private ProgressBar progressBar;
    private EditText etTarget, etAmount;

    private int dailyTarget = 0;
    private int currentIntake = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Optional: gate by login status
        SharedPreferences authPrefs = getSharedPreferences("auth", MODE_PRIVATE);
        boolean loggedIn = authPrefs.getBoolean("logged_in", false);
        if (!loggedIn) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Find views
        tvProgressText = findViewById(R.id.tvProgressText);
        tvOverTarget = findViewById(R.id.tvOverTarget);
        progressBar = findViewById(R.id.progressBar);
        etTarget = findViewById(R.id.etTarget);
        etAmount = findViewById(R.id.etAmount);

        MaterialButton btnSaveTarget = findViewById(R.id.btnSaveTarget);
        MaterialButton btnAddWater = findViewById(R.id.btnAddWater);
        MaterialButton btnQuick250 = findViewById(R.id.btnQuick250);
        MaterialButton btnQuick500 = findViewById(R.id.btnQuick500);
        MaterialButton btnResetDay = findViewById(R.id.btnResetDay);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);

        // Load saved values
        SharedPreferences sp = getSharedPreferences(PREF_WATER, MODE_PRIVATE);
        dailyTarget = sp.getInt(KEY_TARGET, 2000);  // default target 2000 ml
        currentIntake = sp.getInt(KEY_INTAKE, 0);

        etTarget.setText(String.valueOf(dailyTarget));
        updateUI();

        // Save target
        btnSaveTarget.setOnClickListener(v -> {
            String targetStr = etTarget.getText().toString().trim();
            if (TextUtils.isEmpty(targetStr)) {
                Toast.makeText(this, "Enter a target in ml", Toast.LENGTH_SHORT).show();
                return;
            }
            int t;
            try {
                t = Integer.parseInt(targetStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
                return;
            }
            if (t <= 0) {
                Toast.makeText(this, "Target must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
            dailyTarget = t;
            sp.edit().putInt(KEY_TARGET, dailyTarget).apply();
            updateUI();
            Toast.makeText(this, "Daily target saved", Toast.LENGTH_SHORT).show();
        });

        // Add custom amount
        btnAddWater.setOnClickListener(v -> {
            String amtStr = etAmount.getText().toString().trim();
            if (TextUtils.isEmpty(amtStr)) {
                Toast.makeText(this, "Enter an amount in ml", Toast.LENGTH_SHORT).show();
                return;
            }
            int amt;
            try {
                amt = Integer.parseInt(amtStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
                return;
            }
            if (amt <= 0) {
                Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
            addWater(amt, sp);
            etAmount.setText("");
        });

        // Quick +250
        btnQuick250.setOnClickListener(v -> addWater(250, sp));

        // Quick +500
        btnQuick500.setOnClickListener(v -> addWater(500, sp));

        // Reset today
        btnResetDay.setOnClickListener(v -> {
            currentIntake = 0;
            sp.edit().putInt(KEY_INTAKE, currentIntake).apply();
            updateUI();
            Toast.makeText(this, "Today’s intake reset", Toast.LENGTH_SHORT).show();
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            // Clear auth
            authPrefs.edit().clear().apply();
            // Optionally also reset water for this example
            // sp.edit().clear().apply();

            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    private void addWater(int amount, SharedPreferences sp) {
        currentIntake += amount;
        sp.edit().putInt(KEY_INTAKE, currentIntake).apply();
        updateUI();
        Toast.makeText(this, "Added " + amount + " ml", Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        tvProgressText.setText(currentIntake + " / " + dailyTarget + " ml");

        if (dailyTarget <= 0) {
            progressBar.setMax(1);
            progressBar.setProgress(0);
            tvOverTarget.setVisibility(TextView.GONE);
            return;
        }

        progressBar.setMax(dailyTarget);
        // progress bar stops visually at the target,
        // but currentIntake can keep increasing (you can surpass target)
        progressBar.setProgress(Math.min(currentIntake, dailyTarget));

        if (currentIntake > dailyTarget) {
            int extra = currentIntake - dailyTarget;
            tvOverTarget.setText("You’ve exceeded your goal by " + extra + " ml!");
            tvOverTarget.setVisibility(TextView.VISIBLE);
        } else {
            tvOverTarget.setVisibility(TextView.GONE);
        }
    }
}
