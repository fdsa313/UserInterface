package com.university.student.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;
import com.university.student.R;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private TextView tabLogin;
    private TextView tabSignup;
    private CardView signupCard;
    private EditText etSignupId;
    private EditText etSignupEmail;
    private EditText etSignupPassword;
    private EditText etSignupPasswordConfirm;
    private MaterialButton btnSignup;
    private MaterialButton btnKakaoSignup;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        tabLogin = findViewById(R.id.tabLogin);
        tabSignup = findViewById(R.id.tabSignup);
        signupCard = findViewById(R.id.signupCard);
        etSignupId = findViewById(R.id.etSignupId);
        etSignupEmail = findViewById(R.id.etSignupEmail);
        etSignupPassword = findViewById(R.id.etSignupPassword);
        etSignupPasswordConfirm = findViewById(R.id.etSignupPasswordConfirm);
        btnSignup = findViewById(R.id.btnSignup);
        btnKakaoSignup = findViewById(R.id.btnKakaoSignup);

        // Set up tab click listeners
        tabLogin.setOnClickListener(v -> switchToLoginTab());
        tabSignup.setOnClickListener(v -> switchToSignupTab());

        // Set up signup button click listener
        btnSignup.setOnClickListener(v -> performSignup());

        // Set up Kakao signup button click listener
        btnKakaoSignup.setOnClickListener(v -> performKakaoSignup());
    }

    private void switchToLoginTab() {
        // Switch to login activity
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void switchToSignupTab() {
        // Update tab styles
        tabLogin.setBackgroundResource(R.drawable.tab_left_unselected);
        tabLogin.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tabLogin.setTypeface(null, android.graphics.Typeface.NORMAL);

        tabSignup.setBackgroundResource(R.drawable.tab_right_selected);
        tabSignup.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        tabSignup.setTypeface(null, android.graphics.Typeface.BOLD);

        // Show signup card
        signupCard.setVisibility(View.VISIBLE);
    }

    private void performSignup() {
        String id = etSignupId.getText().toString().trim();
        String email = etSignupEmail.getText().toString().trim();
        String password = etSignupPassword.getText().toString();
        String passwordConfirm = etSignupPasswordConfirm.getText().toString();

        // Validate input
        if (TextUtils.isEmpty(id)) {
            etSignupId.setError("아이디를 입력하세요");
            etSignupId.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etSignupEmail.setError("이메일을 입력하세요");
            etSignupEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etSignupEmail.setError("유효한 이메일 주소를 입력하세요");
            etSignupEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etSignupPassword.setError("비밀번호를 입력하세요");
            etSignupPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etSignupPassword.setError("비밀번호는 최소 6자 이상이어야 합니다");
            etSignupPassword.requestFocus();
            return;
        }

        if (!password.equals(passwordConfirm)) {
            etSignupPasswordConfirm.setError("비밀번호가 일치하지 않습니다");
            etSignupPasswordConfirm.requestFocus();
            return;
        }

        // Disable button during signup
        btnSignup.setEnabled(false);
        btnSignup.setText("회원가입 중...");

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
                        btnSignup.setEnabled(true);
                        btnSignup.setText(R.string.btn_signup);
                    }
                });
    }

    private void saveUserToFirestoreInBackground(String id, String email) {
        if (mAuth.getCurrentUser() == null) {
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> user = new HashMap<>();
        user.put("userId", userId);
        user.put("id", id);
        user.put("email", email);
        user.put("createdAt", com.google.firebase.Timestamp.now());
        user.put("college", ""); // Will be set later in CollegeSelectionActivity

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
        runOnUiThread(() -> {
            Toast.makeText(SignupActivity.this, "회원가입이 완료되었습니다", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignupActivity.this, CollegeSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
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
        runOnUiThread(() -> {
            btnSignup.setEnabled(true);
            btnSignup.setText(R.string.btn_signup);
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
            btnKakaoSignup.setEnabled(false);
            btnKakaoSignup.setText("연결 중...");

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
            btnKakaoSignup.setEnabled(true);
            btnKakaoSignup.setText(R.string.btn_kakao_signup);
            Toast.makeText(this, "카카오 회원가입 초기화 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void performKakaoAccountSignup() {
        UserApiClient.getInstance().loginWithKakaoAccount(this, (OAuthToken token, Throwable error) -> {
            if (error != null) {
                btnKakaoSignup.setEnabled(true);
                btnKakaoSignup.setText(R.string.btn_kakao_signup);
                Toast.makeText(SignupActivity.this, "카카오 회원가입에 실패했습니다: " + (error.getMessage() != null ? error.getMessage() : "알 수 없는 오류"), Toast.LENGTH_SHORT).show();
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
                btnKakaoSignup.setEnabled(true);
                btnKakaoSignup.setText(R.string.btn_kakao_signup);
                Toast.makeText(SignupActivity.this, "사용자 정보를 가져오는데 실패했습니다", Toast.LENGTH_SHORT).show();
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
                        // User already exists - just navigate
                        navigateToCollegeSelection();
                    } else {
                        // New user - create Firestore document
                        Map<String, Object> user = new HashMap<>();
                        user.put("kakaoId", kakaoId);
                        user.put("email", email);
                        user.put("id", nickname);
                        user.put("createdAt", com.google.firebase.Timestamp.now());
                        user.put("college", "");
                        user.put("loginType", "kakao");

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
                    btnKakaoSignup.setEnabled(true);
                    btnKakaoSignup.setText(R.string.btn_kakao_signup);
                });
    }

}
