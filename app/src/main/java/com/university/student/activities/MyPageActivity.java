package com.university.student.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;
import com.university.student.R;

public class MyPageActivity extends AppCompatActivity {

    private TextView tvCollege;
    private TextView tvCollege2;
    private MaterialButton btnNotVerified;
    private MaterialButton btnVerify;
    private MaterialButton btnVerifyStudent;
    private CardView verificationCard;
    private TextView tvBookmarkCount;
    private TextView tvReviewCount;

    // Bottom navigation
    private LinearLayout navHome;
    private LinearLayout navFavorites;
    private LinearLayout navMyPage;
    private LinearLayout navSettings;

    // User data - will be updated after verification
    private boolean isVerified = false;
    private String collegeName = "IT대학"; // Default, will be set from verification

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        // Initialize views
        tvCollege = findViewById(R.id.tvCollege);
        tvCollege2 = findViewById(R.id.tvCollege2);
        btnNotVerified = findViewById(R.id.btnNotVerified);
        btnVerify = findViewById(R.id.btnVerify);
        btnVerifyStudent = findViewById(R.id.btnVerifyStudent);
        verificationCard = findViewById(R.id.verificationCard);
        tvBookmarkCount = findViewById(R.id.tvBookmarkCount);
        tvReviewCount = findViewById(R.id.tvReviewCount);

        // Bottom navigation
        navHome = findViewById(R.id.navHome);
        navFavorites = findViewById(R.id.navFavorites);
        navMyPage = findViewById(R.id.navMyPage);
        navSettings = findViewById(R.id.navSettings);

        // Get college name from intent or SharedPreferences
        if (getIntent().hasExtra("college_name")) {
            collegeName = getIntent().getStringExtra("college_name");
        } else {
            // Load from SharedPreferences
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            collegeName = prefs.getString("selected_college", "IT대학");
        }

        // Set up click listeners
        btnVerify.setOnClickListener(v -> navigateToVerification());
        btnVerifyStudent.setOnClickListener(v -> navigateToVerification());

        // Bottom navigation click listeners
        navHome.setOnClickListener(v -> {
            // TODO: Navigate to home
        });

        navFavorites.setOnClickListener(v -> {
            // TODO: Navigate to favorites
        });

        navSettings.setOnClickListener(v -> {
            // TODO: Navigate to settings
        });

        // Load user data
        loadUserData();
    }

    private void loadUserData() {
        // Load user data from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        isVerified = prefs.getBoolean("is_verified", false);

        if (isVerified) {
            // Hide verification warning card
            verificationCard.setVisibility(View.GONE);
            btnNotVerified.setVisibility(View.GONE);
        } else {
            // Show verification warning card
            verificationCard.setVisibility(View.VISIBLE);
            btnNotVerified.setVisibility(View.VISIBLE);
        }

        // Set college name
        tvCollege.setText(collegeName);
        tvCollege2.setText(collegeName);

        // Set activity counts
        // TODO: Load from database
        tvBookmarkCount.setText("0");
        tvReviewCount.setText("0");
    }

    private void navigateToVerification() {
        // TODO: Navigate to verification activity
        // Intent intent = new Intent(MyPageActivity.this, VerificationActivity.class);
        // startActivity(intent);
    }

    public void updateCollegeName(String college) {
        this.collegeName = college;
        tvCollege.setText(college);
        tvCollege2.setText(college);
    }

    public void setVerificationStatus(boolean verified) {
        this.isVerified = verified;
        loadUserData();
    }
}
