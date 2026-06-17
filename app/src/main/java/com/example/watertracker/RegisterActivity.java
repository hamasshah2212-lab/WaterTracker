package com.example.watertracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private ProgressBar passwordStrengthBar;
    private TextView tvStrengthLabel, btnGoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        passwordStrengthBar = findViewById(R.id.passwordStrengthBar);
        tvStrengthLabel = findViewById(R.id.tvStrengthLabel);
        btnGoLogin = findViewById(R.id.btnGoLogin);

        passwordStrengthBar.setMax(100);

        // Update strength as user types
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrength(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnRegister.setOnClickListener(v -> attemptRegister());

        // Go back to login
        btnGoLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void attemptRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirm = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Enter your name");
            etName.requestFocus();
            return;
        }
        if (!isValidEmail(email)) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }
        if (!isStrongPassword(password)) {
            etPassword.setError("Password must be ≥ 8 characters and contain a special symbol");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(confirm)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Save user (demo – stored in SharedPreferences)
        SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
        sp.edit()
                .putString("user_name", name)
                .putString("user_email", email)
                .putString("user_password", password)
                .putBoolean("logged_in", false)
                .apply();

        Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_SHORT).show();

        // Go to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Enforce ≥ 8 chars + special symbol
    private boolean isStrongPassword(String password) {
        if (password == null) return false;
        if (password.length() < 8) return false;

        boolean hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                hasSpecial = true;
                break;
            }
        }
        return hasSpecial;
    }

    // Visual strength indicator (simple but effective)
    private void updatePasswordStrength(String password) {
        if (TextUtils.isEmpty(password)) {
            passwordStrengthBar.setProgress(0);
            tvStrengthLabel.setText("Password strength: -");
            return;
        }

        int score = 0;

        if (password.length() >= 8) score += 40;
        if (password.length() >= 12) score += 20;

        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        if (hasUpper) score += 10;
        if (hasLower) score += 10;
        if (hasDigit) score += 10;
        if (hasSpecial) score += 10;

        if (score > 100) score = 100;

        passwordStrengthBar.setProgress(score);

        if (!isStrongPassword(password)) {
            tvStrengthLabel.setText("Password strength: Weak");
        } else if (score < 80) {
            tvStrengthLabel.setText("Password strength: Medium");
        } else {
            tvStrengthLabel.setText("Password strength: Strong");
        }
    }
}
