package com.example.Partenership_Service.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.Partenership_Service.R;

public class CollegeSelectionActivity extends AppCompatActivity {

    private EditText etSearch;
    private View cardHumanities;
    private View cardNaturalScience;
    private View cardLaw;
    private View cardSocialScience;
    private View cardIT;
    private View cardEconomics;
    private View cardBusiness;
    private View cardEngineering;
    private View cardConvergence;
    private View cardBaird;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_college_selection);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        etSearch = findViewById(R.id.etSearch);
        cardHumanities = findViewById(R.id.cardHumanities);
        cardNaturalScience = findViewById(R.id.cardNaturalScience);
        cardLaw = findViewById(R.id.cardLaw);
        cardSocialScience = findViewById(R.id.cardSocialScience);
        cardIT = findViewById(R.id.cardIT);
        cardEconomics = findViewById(R.id.cardEconomics);
        cardBusiness = findViewById(R.id.cardBusiness);
        cardEngineering = findViewById(R.id.cardEngineering);
        cardConvergence = findViewById(R.id.cardConvergence);
        cardBaird = findViewById(R.id.cardBaird);

        cardHumanities.setOnClickListener(v -> selectCollege(getString(R.string.college_humanities)));
        cardNaturalScience.setOnClickListener(v -> selectCollege(getString(R.string.college_natural_science)));
        cardLaw.setOnClickListener(v -> selectCollege(getString(R.string.college_law)));
        cardSocialScience.setOnClickListener(v -> selectCollege(getString(R.string.college_social_science)));
        cardIT.setOnClickListener(v -> selectCollege(getString(R.string.college_it)));
        cardEconomics.setOnClickListener(v -> selectCollege(getString(R.string.college_economics)));
        cardBusiness.setOnClickListener(v -> selectCollege(getString(R.string.college_business)));
        cardEngineering.setOnClickListener(v -> selectCollege(getString(R.string.college_engineering)));
        cardConvergence.setOnClickListener(v -> selectCollege(getString(R.string.college_convergence)));
        cardBaird.setOnClickListener(v -> selectCollege(getString(R.string.college_baird)));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterColleges(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void selectCollege(String collegeName) {
        // Save selected college to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("selected_college", collegeName);
        editor.apply();

        // Save college to Firestore if user is logged in
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .update("college", collegeName)
                    .addOnCompleteListener(task -> {
                        // Navigate to MyPage or main activity
                        Intent intent = new Intent(CollegeSelectionActivity.this, MyPageActivity.class);
                        intent.putExtra("college_name", collegeName);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
        } else {
            Intent intent = new Intent(CollegeSelectionActivity.this, MyPageActivity.class);
            intent.putExtra("college_name", collegeName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void filterColleges(String query) {
        String lowerQuery = query.toLowerCase();

        // Show/hide cards based on search query
        cardHumanities.setVisibility(
            getString(R.string.college_humanities).toLowerCase().contains(lowerQuery)
            ? View.VISIBLE : View.GONE
        );
        cardNaturalScience.setVisibility(
            getString(R.string.college_natural_science).toLowerCase().contains(lowerQuery)
            ? View.VISIBLE : View.GONE
        );
        cardLaw.setVisibility(
            getString(R.string.college_law).toLowerCase().contains(lowerQuery)
            ? View.VISIBLE : View.GONE
        );
        cardSocialScience.setVisibility(
            getString(R.string.college_social_science).toLowerCase().contains(lowerQuery)
            ? View.VISIBLE : View.GONE
        );
        cardIT.setVisibility(
            getString(R.string.college_it).toLowerCase().contains(lowerQuery)
            ? View.VISIBLE : View.GONE
        );
        cardEconomics.setVisibility(
            getString(R.string.college_economics).toLowerCase().contains(lowerQuery)
            ? View.VISIBLE : View.GONE
        );
        cardBusiness.setVisibility(
            getString(R.string.college_business).toLowerCase().contains(lowerQuery)
            ? View.VISIBLE : View.GONE
        );
        cardEngineering.setVisibility(
            getString(R.string.college_engineering).toLowerCase().contains(lowerQuery)
            ? View.VISIBLE : View.GONE
        );
        cardConvergence.setVisibility(
            getString(R.string.college_convergence).toLowerCase().contains(lowerQuery)
            ? View.VISIBLE : View.GONE
        );
        cardBaird.setVisibility(
            getString(R.string.college_baird).toLowerCase().contains(lowerQuery)
            ? View.VISIBLE : View.GONE
        );
    }
}
