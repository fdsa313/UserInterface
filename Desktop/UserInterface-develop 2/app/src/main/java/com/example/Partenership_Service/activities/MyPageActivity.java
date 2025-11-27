package com.example.Partenership_Service.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;
import com.example.Partenership_Service.R;

public class MyPageActivity extends AppCompatActivity {

    private TextView tvCollege;
    private TextView tvCollege2;
    private MaterialButton btnNotVerified;
    private MaterialButton btnVerify;
    private MaterialButton btnVerifyStudent;
    private CardView verificationCard;
    private CardView verificationInfoCard;
    private TextView tvVerificationMethod;
    private TextView tvVerificationName;
    private TextView tvVerificationCollege;
    private TextView tvVerificationDepartment;
    private TextView tvVerificationStudentNumber;
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
        verificationInfoCard = findViewById(R.id.verificationInfoCard);
        tvVerificationMethod = findViewById(R.id.tvVerificationMethod);
        tvVerificationName = findViewById(R.id.tvVerificationName);
        tvVerificationCollege = findViewById(R.id.tvVerificationCollege);
        tvVerificationDepartment = findViewById(R.id.tvVerificationDepartment);
        tvVerificationStudentNumber = findViewById(R.id.tvVerificationStudentNumber);
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
        // Get current user ID
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : "default";

        // Load user-specific verification data
        String prefsName = "StudentVerification_" + userId;
        SharedPreferences prefs = getSharedPreferences(prefsName, MODE_PRIVATE);
        isVerified = prefs.getBoolean("is_verified", false);

        if (isVerified) {
            // Hide verification warning card
            verificationCard.setVisibility(View.GONE);
            btnNotVerified.setVisibility(View.GONE);

            // Show verification info card
            verificationInfoCard.setVisibility(View.VISIBLE);

            // Load and display verification info
            String verificationMethod = prefs.getString("verification_method", "학생증");
            String name = prefs.getString("student_name", "정보 없음");
            String college = prefs.getString("student_college", "정보 없음");
            String department = prefs.getString("student_department", "정보 없음");
            String studentNumber = prefs.getString("student_number", "정보 없음");

            tvVerificationMethod.setText(verificationMethod);
            tvVerificationName.setText(name);
            tvVerificationCollege.setText(college);
            tvVerificationDepartment.setText(department);
            tvVerificationStudentNumber.setText(studentNumber);

            // Update college name from verification info
            if (!TextUtils.isEmpty(college) && !"정보 없음".equals(college)) {
                collegeName = college;
            } else {
                collegeName = department;
            }
        } else {
            // Show verification warning card
            verificationCard.setVisibility(View.VISIBLE);
            btnNotVerified.setVisibility(View.VISIBLE);

            // Hide verification info card
            verificationInfoCard.setVisibility(View.GONE);
        }

        // Set college name
        tvCollege.setText(collegeName);
        tvCollege2.setText(collegeName);

        // Set activity counts
        // TODO: Load from database
        tvBookmarkCount.setText("0");
        tvReviewCount.setText("0");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 인증 화면에서 돌아왔을 때 데이터 다시 로드
        loadUserData();
    }

    private void navigateToVerification() {
        Intent intent = new Intent(MyPageActivity.this, VerificationActivity.class);
        startActivity(intent);
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
