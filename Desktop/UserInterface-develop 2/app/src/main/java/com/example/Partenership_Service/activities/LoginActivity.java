package com.example.Partenership_Service.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;
import com.example.Partenership_Service.R;
import com.example.Partenership_Service.databinding.ActivityLoginBinding;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        backgroundExecutor.execute(() -> {
            if (mAuth.getCurrentUser() != null) {
                checkAutoLogin();
            }
        });

        binding.tabLogin.setOnClickListener(v -> {
            // Already on login tab, do nothing
        });

        binding.tabSignup.setOnClickListener(v -> {
            // Switch to signup activity
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        });

        // Set up login button click listener
        binding.btnLogin.setOnClickListener(v -> performLogin());

        // Set up Kakao login button click listener
        binding.btnKakaoLogin.setOnClickListener(v -> performKakaoLogin());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void checkAutoLogin() {
        boolean autoLogin = sharedPreferences.getBoolean("auto_login", false);
        if (autoLogin) {
            // User is already logged in, navigate to appropriate screen
            navigateToNextScreen();
        }
    }

    private void performLogin() {
        String id = binding.etLoginId.getText().toString().trim();
        String password = binding.etLoginPassword.getText().toString().trim();
        boolean autoLogin = binding.cbAutoLogin.isChecked();

        // Validate input
        if (TextUtils.isEmpty(id)) {
            binding.etLoginId.setError("아이디 또는 이메일을 입력하세요");
            binding.etLoginId.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etLoginPassword.setError("비밀번호를 입력하세요");
            binding.etLoginPassword.requestFocus();
            return;
        }

        // Disable button during login
        binding.btnLogin.setEnabled(false);
        binding.btnLogin.setText("로그인 중...");

        // Determine if input is email or ID
        String loginEmail = id;
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(id).matches()) {
            // If it's not an email, try to find email from Firestore by ID
            loginWithId(id, password, autoLogin);
            return;
        }

        // Login with email and password
        mAuth.signInWithEmailAndPassword(loginEmail, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Save auto-login preference
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("auto_login", autoLogin);
                        editor.apply();

                        // Login successful, navigate to next screen
                        navigateToNextScreen();
                    } else {
                        // Handle login error
                        handleLoginError(task.getException());
                        binding.btnLogin.setEnabled(true);
                        binding.btnLogin.setText(R.string.btn_login);
                    }
                });
    }

    private void loginWithId(String id, String password, boolean autoLogin) {
        // Search for user in Firestore by ID
        db.collection("users")
                .whereEqualTo("id", id)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Found user, get email and login
                        String email = task.getResult().getDocuments().get(0).getString("email");
                        mAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(loginTask -> {
                                    if (loginTask.isSuccessful()) {
                                        // Save auto-login preference
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putBoolean("auto_login", autoLogin);
                                        editor.apply();

                                        // Login successful, navigate to next screen
                                        navigateToNextScreen();
                                    } else {
                                        handleLoginError(loginTask.getException());
                                        binding.btnLogin.setEnabled(true);
                                        binding.btnLogin.setText(R.string.btn_login);
                                    }
                                });
                    } else {
                        // User not found or error
                        new AlertDialog.Builder(this)
                                .setTitle("로그인 오류")
                                .setMessage("아이디 또는 비밀번호가 잘못되었습니다")
                                .setPositiveButton("확인", null)
                                .show();
                        binding.btnLogin.setEnabled(true);
                        binding.btnLogin.setText(R.string.btn_login);
                    }
                });
    }

    private void navigateToNextScreen() {
        // Check if user has selected college
        String college = sharedPreferences.getString("selected_college", "");
        Intent intent;
        
        if (college.isEmpty()) {
            // Navigate to college selection
            intent = new Intent(LoginActivity.this, CollegeSelectionActivity.class);
        } else {
            // Navigate to main page
            intent = new Intent(LoginActivity.this, MyPageActivity.class);
        }
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleLoginError(Exception exception) {
        String errorMessage = "로그인에 실패했습니다";

        if (exception instanceof FirebaseAuthInvalidUserException) {
            errorMessage = "존재하지 않는 계정입니다";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "아이디 또는 비밀번호가 잘못되었습니다";
        } else if (exception != null && exception.getMessage() != null) {
            errorMessage = exception.getMessage();
        }

        new AlertDialog.Builder(this)
                .setTitle("로그인 오류")
                .setMessage(errorMessage)
                .setPositiveButton("확인", null)
                .show();
    }

    private void performKakaoLogin() {
        try {
            // Disable button during login
            binding.btnKakaoLogin.setEnabled(false);
            binding.btnKakaoLogin.setText("연결 중...");

            // Check if KakaoTalk login is available
            if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(this)) {
                // KakaoTalk login
                UserApiClient.getInstance().loginWithKakaoTalk(this, (OAuthToken token, Throwable error) -> {
                    if (error != null) {
                        // If KakaoTalk login fails, try Kakao Account login
                        performKakaoAccountLogin();
                    } else if (token != null) {
                        // Success - get user info and login to Firebase
                        handleKakaoLoginSuccess();
                    }
                    return null;
                });
            } else {
                // Kakao Account login
                performKakaoAccountLogin();
            }
        } catch (Exception e) {
            binding.btnKakaoLogin.setEnabled(true);
            binding.btnKakaoLogin.setText(R.string.btn_kakao_login);
            mainHandler.post(() ->
                new AlertDialog.Builder(this)
                    .setTitle("카카오 로그인 오류")
                    .setMessage("카카오 로그인 초기화 오류: " + e.getMessage())
                    .setPositiveButton("확인", null)
                    .show()
            );
        }
    }

    private void performKakaoAccountLogin() {
        UserApiClient.getInstance().loginWithKakaoAccount(this, (OAuthToken token, Throwable error) -> {
            if (error != null) {
                binding.btnKakaoLogin.setEnabled(true);
                binding.btnKakaoLogin.setText(R.string.btn_kakao_login);
                mainHandler.post(() ->
                    new AlertDialog.Builder(this)
                        .setTitle("카카오 로그인 실패")
                        .setMessage("카카오 로그인에 실패했습니다: " + (error.getMessage() != null ? error.getMessage() : "알 수 없는 오류"))
                        .setPositiveButton("확인", null)
                        .show()
                );
            } else if (token != null) {
                handleKakaoLoginSuccess();
            }
            return null;
            
        });

    }

    private void handleKakaoLoginSuccess() {
        // Get user information from Kakao
        UserApiClient.getInstance().me((User user, Throwable error) -> {
            if (error != null) {
                binding.btnKakaoLogin.setEnabled(true);
                binding.btnKakaoLogin.setText(R.string.btn_kakao_login);
                mainHandler.post(() ->
                    new AlertDialog.Builder(this)
                        .setTitle("사용자 정보 오류")
                        .setMessage("사용자 정보를 가져오는데 실패했습니다").setPositiveButton("확인", null).show()
                );
            } else if (user != null) {
                // Save auto-login preference
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("auto_login", true);
                editor.apply();

                // Get Kakao user info
                String kakaoId = String.valueOf(user.getId());
                String email = user.getKakaoAccount() != null && user.getKakaoAccount().getEmail() != null
                        ? user.getKakaoAccount().getEmail()
                        : kakaoId + "@kakao.com";
                String nickname = user.getKakaoAccount() != null && user.getKakaoAccount().getProfile() != null
                        ? user.getKakaoAccount().getProfile().getNickname()
                        : "카카오 사용자";

                // Save or update user info in Firestore and navigate
                saveKakaoUserToFirestore(kakaoId, email, nickname);
            }
            return null;
        });
    }

    private void saveKakaoUserToFirestore(String kakaoId, String email, String nickname) {
        // Check if user exists in Firestore
        db.collection("users")
                .whereEqualTo("kakaoId", kakaoId)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // User exists - just navigate
                        navigateToNextScreen();
                    } else {
                        // New user - create account with custom token (simplified approach)
                        // For production, you should use Firebase Custom Authentication
                        // For now, we'll create a Firestore document with Kakao info
                        Map<String, Object> user = new HashMap<>();
                        user.put("kakaoId", kakaoId);
                        user.put("email", email);
                        user.put("id", nickname);
                        user.put("createdAt", com.google.firebase.Timestamp.now());
                        user.put("college", "");
                        user.put("loginType", "kakao");

                        // Save to Firestore (we'll use a document ID based on kakaoId)
                        String userId = "kakao_" + kakaoId;
                        db.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    navigateToNextScreen();
                                })
                                .addOnFailureListener(e -> {
                                    // Even if Firestore save fails, navigate
                                    navigateToNextScreen();
                                });
                    }
                    binding.btnKakaoLogin.setEnabled(true);
                    binding.btnKakaoLogin.setText(R.string.btn_kakao_login);
                });
    }

}
