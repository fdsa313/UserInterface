package com.example.Partenership_Service.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import com.example.Partenership_Service.R;
import com.example.Partenership_Service.fragments.StudentCardVerificationFragment;
import com.example.Partenership_Service.fragments.UsaintVerificationFragment;

public class VerificationActivity extends AppCompatActivity {

    private TextView tabStudentCard;
    private TextView tabUsaint;
    private android.view.View tabIndicator;
    private boolean isStudentCardTab = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        // Initialize tab views
        tabStudentCard = findViewById(R.id.tab_student_card);
        tabUsaint = findViewById(R.id.tab_usaint);
        tabIndicator = findViewById(R.id.tab_indicator);

        // Set up tab click listeners
        tabStudentCard.setOnClickListener(v -> switchToStudentCardTab());
        tabUsaint.setOnClickListener(v -> switchToUsaintTab());

        // Load initial fragment
        if (savedInstanceState == null) {
            switchToStudentCardTab();
        }
    }

    private void switchToStudentCardTab() {
        if (!isStudentCardTab) {
            isStudentCardTab = true;
            updateTabUI();

            StudentCardVerificationFragment fragment = new StudentCardVerificationFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
        }
    }

    private void switchToUsaintTab() {
        if (isStudentCardTab) {
            isStudentCardTab = false;
            updateTabUI();

            UsaintVerificationFragment fragment = new UsaintVerificationFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
        }
    }

    private void updateTabUI() {
        int primaryColor = ContextCompat.getColor(this, R.color.primary);
        int secondaryColor = ContextCompat.getColor(this, R.color.text_secondary);

        if (isStudentCardTab) {
            // Student Card tab selected
            tabStudentCard.setTextColor(primaryColor);
            tabUsaint.setTextColor(secondaryColor);

            // Move indicator
            ViewGroup.LayoutParams params = tabIndicator.getLayoutParams();
            params.width = tabStudentCard.getWidth();
            tabIndicator.setLayoutParams(params);
            tabIndicator.setX(tabStudentCard.getX());
        } else {
            // U-SAINT tab selected
            tabStudentCard.setTextColor(secondaryColor);
            tabUsaint.setTextColor(primaryColor);

            // Move indicator
            ViewGroup.LayoutParams params = tabIndicator.getLayoutParams();
            params.width = tabUsaint.getWidth();
            tabIndicator.setLayoutParams(params);
            tabIndicator.setX(tabUsaint.getX());
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Update tab indicator position after layout
            updateTabUI();
        }
    }
}
