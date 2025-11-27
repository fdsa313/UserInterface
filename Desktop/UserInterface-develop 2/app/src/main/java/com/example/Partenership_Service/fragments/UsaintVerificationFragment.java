package com.example.Partenership_Service.fragments;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.Partenership_Service.R;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UsaintVerificationFragment extends Fragment {

    private static final String TAG = "UsaintVerification";
    private static final String USAINT_LOGIN_URL = "https://saint.ssu.ac.kr/irj/portal";

    private EditText etUsaintId;
    private EditText etUsaintPassword;
    private Button btnVerify;
    private TextView tabStudentCard;
    private TextView tabUsaint;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_usaint_verification, container, false);

        // Initialize views
        etUsaintId = view.findViewById(R.id.et_usaint_id);
        etUsaintPassword = view.findViewById(R.id.et_usaint_password);
        btnVerify = view.findViewById(R.id.btn_verify);
        tabStudentCard = view.findViewById(R.id.tab_student_card);
        tabUsaint = view.findViewById(R.id.tab_usaint);

        // Set up tab clicks
        tabStudentCard.setOnClickListener(v -> switchToStudentCardTab());

        // Set up verify button
        btnVerify.setOnClickListener(v -> performUsaintVerification());

        return view;
    }

    private void switchToStudentCardTab() {
        if (getActivity() != null) {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new StudentCardVerificationFragment())
                    .commit();
        }
    }

    private void performUsaintVerification() {
        String usaintId = etUsaintId.getText().toString().trim();
        String password = etUsaintPassword.getText().toString().trim();

        if (usaintId.isEmpty()) {
            etUsaintId.setError("유세인트 ID를 입력하세요");
            etUsaintId.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etUsaintPassword.setError("비밀번호를 입력하세요");
            etUsaintPassword.requestFocus();
            return;
        }

        // Disable button during verification
        btnVerify.setEnabled(false);
        btnVerify.setText("인증 중...");

        // Perform verification in background
        verifyUsaintCredentials(usaintId, password);
    }

    private void verifyUsaintCredentials(String usaintId, String password) {
        new Thread(() -> {
            try {
                // Connect to U-SAINT portal
                URL url = new URL(USAINT_LOGIN_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Prepare login data
                String postData = "sap-user=" + usaintId + "&sap-password=" + password;
                connection.getOutputStream().write(postData.getBytes("UTF-8"));

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    // Read response
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Check if login was successful
                    String responseBody = response.toString();
                    if (responseBody.contains("Main") || responseBody.contains("main") ||
                        !responseBody.contains("error") && !responseBody.contains("failed")) {

                        // Login successful - fetch student info
                        fetchStudentInfo(usaintId, connection);
                    } else {
                        requireActivity().runOnUiThread(() -> {
                            showError("로그인 실패", "유세인트 ID 또는 비밀번호가 올바르지 않습니다.");
                            resetVerifyButton();
                        });
                    }
                } else {
                    requireActivity().runOnUiThread(() -> {
                        showError("연결 실패", "유세인트 서버에 연결할 수 없습니다.");
                        resetVerifyButton();
                    });
                }

                connection.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "Verification error", e);
                requireActivity().runOnUiThread(() -> {
                    showError("오류 발생", "인증 중 오류가 발생했습니다: " + e.getMessage());
                    resetVerifyButton();
                });
            }
        }).start();
    }

    private void fetchStudentInfo(String usaintId, HttpURLConnection connection) {
        requireActivity().runOnUiThread(() -> {
            // For now, save with student ID
            // In a real implementation, you would parse the actual student info from U-SAINT
            saveVerificationInfo(usaintId, "숭실대학교", "정보통신전자공학부", usaintId);

            Toast.makeText(getContext(), "유세인트 인증이 완료되었습니다!", Toast.LENGTH_SHORT).show();

            // Navigate back
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private void saveVerificationInfo(String name, String college, String department, String studentNumber) {
        // Get current user ID
        com.google.firebase.auth.FirebaseUser currentUser =
            com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : "default";

        // Use user-specific SharedPreferences
        String prefsName = "StudentVerification_" + userId;
        SharedPreferences prefs = requireContext().getSharedPreferences(prefsName, android.content.Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("is_verified", true);
        editor.putString("verification_method", "유세인트");
        editor.putString("student_name", name);
        editor.putString("student_college", college);
        editor.putString("student_department", department);
        editor.putString("student_number", studentNumber);
        editor.apply();

        // Update Firestore with uSaint ID
        com.google.firebase.firestore.FirebaseFirestore db =
            com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .update("uSaint", studentNumber)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "uSaint field updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update uSaint field", e);
                });
    }

    private void showError(String title, String message) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("확인", null)
                .show();
    }

    private void resetVerifyButton() {
        btnVerify.setEnabled(true);
        btnVerify.setText("인증하기");
    }
}
