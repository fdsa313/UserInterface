package com.example.Partenership_Service.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;
import com.example.Partenership_Service.R;
import com.example.Partenership_Service.databinding.ActivitySignupBinding;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up tab click listeners
        binding.tabLogin.setOnClickListener(v -> switchToLoginTab());
        binding.tabSignup.setOnClickListener(v -> {
            // Already on signup tab, do nothing
        });

        // Set up signup button click listener
        binding.btnSignup.setOnClickListener(v -> performSignup());

        // Set up Kakao signup button click listener
        binding.btnKakaoSignup.setOnClickListener(v -> performKakaoSignup());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void switchToLoginTab() {
        // Switch to login activity
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void performSignup() {
        String id = binding.etSignupId.getText().toString().trim();
        String email = binding.etSignupEmail.getText().toString().trim();
        String password = binding.etSignupPassword.getText().toString();
        String passwordConfirm = binding.etSignupPasswordConfirm.getText().toString();

        // Validate input
        if (TextUtils.isEmpty(id)) {
            binding.etSignupId.setError("아이디를 입력하세요");
            binding.etSignupId.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            binding.etSignupEmail.setError("이메일을 입력하세요");
            binding.etSignupEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etSignupEmail.setError("유효한 이메일 주소를 입력하세요");
            binding.etSignupEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etSignupPassword.setError("비밀번호를 입력하세요");
            binding.etSignupPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            binding.etSignupPassword.setError("비밀번호는 최소 6자 이상이어야 합니다");
            binding.etSignupPassword.requestFocus();
            return;
        }

        if (!password.equals(passwordConfirm)) {
            binding.etSignupPasswordConfirm.setError("비밀번호가 일치하지 않습니다");
            binding.etSignupPasswordConfirm.requestFocus();
            return;
        }

        // Disable button during signup
        binding.btnSignup.setEnabled(false);
        binding.btnSignup.setText("회원가입 중...");

        // Create user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Navigate immediately, save to Firestore in background
                        navigateToCollegeSelection();
                        // Save user information to Firestore in background (non-blocking)
                        saveUserToFirestoreInBackground(id, email);
                    } else {
                        // Handle signup error
                        handleSignupError(task.getException());
                        binding.btnSignup.setEnabled(true);
                        binding.btnSignup.setText(R.string.btn_signup);
                    }
                });
    }

    private void saveUserToFirestoreInBackground(String id, String email) {
        if (mAuth.getCurrentUser() == null) {
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("passwd", ""); // Firebase Auth handles password, leaving empty
        user.put("uSaint", ""); // Will be set when user verifies with U-SAINT
        user.put("isManager", false);
        user.put("isPartner", false);
        user.put("coupon", new java.util.ArrayList<>()); // Empty array for coupons
        user.put("points", 0); // Initial points
        user.put("favorites", new java.util.ArrayList<>()); // Empty array for favorites
        user.put("history", new java.util.ArrayList<>()); // Empty array for history

        // Save to Firestore in background (non-blocking)
        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    // User saved successfully
                })
                .addOnFailureListener(e -> {
                    // This is non-critical, user can continue using the app
                    // User info will be saved when they select college or update profile
                });
    }

    private void navigateToCollegeSelection() {
        // Clear previous user's verification data
        clearPreviousVerificationData();

        mainHandler.post(() -> {
            new AlertDialog.Builder(this)
                .setTitle("회원가입 완료")
                .setMessage("회원가입이 완료되었습니다")
                .setPositiveButton("확인", (dialog, which) -> {
                    Intent intent = new Intent(SignupActivity.this, CollegeSelectionActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
        });
    }

    private void clearPreviousVerificationData() {
        // Clear current user's verification data only
        com.google.firebase.auth.FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String prefsName = "StudentVerification_" + userId;
            getSharedPreferences(prefsName, MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();
        }
    }

    private void handleSignupError(Exception exception) {
        String errorMessage = "회원가입에 실패했습니다";

        if (exception instanceof FirebaseAuthUserCollisionException) {
            errorMessage = "이미 사용 중인 이메일입니다";
        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
            errorMessage = "비밀번호가 너무 약합니다";
        } else if (exception != null) {
            String message = exception.getMessage();
            if (message != null) {
                // Translate common Firebase error messages
                if (message.contains("network") || message.contains("Network")) {
                    errorMessage = "네트워크 연결을 확인해주세요";
                } else if (message.contains("permission") || message.contains("Permission")) {
                    errorMessage = "Firestore 권한이 없습니다. Firebase Console에서 권한을 확인해주세요";
                } else {
                    errorMessage = message;
                }
            }
        }
        // Make a final copy for lambda expression
        final String finalErrorMessage = errorMessage;
        mainHandler.post(() -> {
            binding.btnSignup.setEnabled(true);
            binding.btnSignup.setText(R.string.btn_signup);
            new AlertDialog.Builder(this)
                    .setTitle("회원가입 오류")
                    .setMessage(finalErrorMessage)
                    .setPositiveButton("확인", null)
                    .show();
        });
    }

    private void performKakaoSignup() {
        try {
            // Disable button during signup
            binding.btnKakaoSignup.setEnabled(false);
            binding.btnKakaoSignup.setText("연결 중...");

            // Check if KakaoTalk login is available
            if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(this)) {
                // KakaoTalk login
                UserApiClient.getInstance().loginWithKakaoTalk(this, (OAuthToken token, Throwable error) -> {
                    if (error != null) {
                        // If KakaoTalk login fails, try Kakao Account login
                        performKakaoAccountSignup();
                    } else if (token != null) {
                        // Success - get user info and save to Firestore
                        handleKakaoSignupSuccess();
                    }
                    return null;
                });
            } else {
                // Kakao Account login
                performKakaoAccountSignup();
            }
        } catch (Exception e) {
            binding.btnKakaoSignup.setEnabled(true);
            binding.btnKakaoSignup.setText(R.string.btn_kakao_signup);
            mainHandler.post(() ->
                new AlertDialog.Builder(this)
                    .setTitle("카카오 회원가입 오류")
                    .setMessage("카카오 회원가입 초기화 오류: " + e.getMessage())
                    .setPositiveButton("확인", null)
                    .show()
            );
        }
    }

    private void performKakaoAccountSignup() {
        UserApiClient.getInstance().loginWithKakaoAccount(this, (OAuthToken token, Throwable error) -> {
            if (error != null) {
                binding.btnKakaoSignup.setEnabled(true);
                binding.btnKakaoSignup.setText(R.string.btn_kakao_signup);
                mainHandler.post(() ->
                    new AlertDialog.Builder(this)
                        .setTitle("카카오 회원가입 실패")
                        .setMessage("카카오 회원가입에 실패했습니다: " + (error.getMessage() != null ? error.getMessage() : "알 수 없는 오류"))
                        .setPositiveButton("확인", null)
                        .show()
                );
            } else if (token != null) {
                // Success - get user info and save to Firestore
                handleKakaoSignupSuccess();
            }
            return null;
        });
    }

    private void handleKakaoSignupSuccess() {
        // Get user information from Kakao
        UserApiClient.getInstance().me((User user, Throwable error) -> {
            if (error != null) {
                binding.btnKakaoSignup.setEnabled(true);
                binding.btnKakaoSignup.setText(R.string.btn_kakao_signup);
                mainHandler.post(() ->
                    new AlertDialog.Builder(this)
                        .setTitle("사용자 정보 오류")
                        .setMessage("사용자 정보를 가져오는데 실패했습니다")
                        .setPositiveButton("확인", null)
                        .show()
                );
            } else if (user != null) {
                // Get Kakao user info
                String kakaoId = String.valueOf(user.getId());
                String email = user.getKakaoAccount() != null && user.getKakaoAccount().getEmail() != null
                        ? user.getKakaoAccount().getEmail()
                        : kakaoId + "@kakao.com";
                String nickname = user.getKakaoAccount() != null && user.getKakaoAccount().getProfile() != null
                        ? user.getKakaoAccount().getProfile().getNickname()
                        : "카카오 사용자";

                // Save user info in Firestore and navigate
                saveKakaoUserToFirestore(kakaoId, email, nickname);
            }
            return null;
        });
    }

    private void saveKakaoUserToFirestore(String kakaoId, String email, String nickname) {
        // Check if user already exists
        db.collection("users")
                .whereEqualTo("kakaoId", kakaoId)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // User already exists - show error message
                        binding.btnKakaoSignup.setEnabled(true);
                        binding.btnKakaoSignup.setText(R.string.btn_kakao_signup);
                        mainHandler.post(() ->
                            new AlertDialog.Builder(this)
                                .setTitle("이미 가입된 계정")
                                .setMessage("이미 가입된 카카오 계정입니다. 로그인 화면으로 이동합니다.")
                                .setPositiveButton("확인", (dialog, which) -> {
                                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .setCancelable(false)
                                .show()
                        );
                    } else {
                        // New user - create Firestore document
                        Map<String, Object> user = new HashMap<>();
                        user.put("kakaoId", kakaoId);
                        user.put("id", nickname);
                        user.put("passwd", ""); // Kakao login doesn't use password
                        user.put("uSaint", ""); // Will be set when user verifies with U-SAINT
                        user.put("isManager", false);
                        user.put("isPartner", false);
                        user.put("coupon", new java.util.ArrayList<>()); // Empty array for coupons
                        user.put("points", 0); // Initial points
                        user.put("favorites", new java.util.ArrayList<>()); // Empty array for favorites
                        user.put("history", new java.util.ArrayList<>()); // Empty array for history

                        // Save to Firestore
                        String userId = "kakao_" + kakaoId;
                        db.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    navigateToCollegeSelection();
                                })
                                .addOnFailureListener(e -> {
                                    // Even if Firestore save fails, navigate
                                    navigateToCollegeSelection();
                                });
                    }
                    binding.btnKakaoSignup.setEnabled(true);
                    binding.btnKakaoSignup.setText(R.string.btn_kakao_signup);
                });
    }

}
