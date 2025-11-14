package com.example.lonin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText inputUsername;
    private EditText inputEmail;
    private EditText inputPassword;
    private EditText inputPasswordConfirm;
    private Button btnSignup;
    private Button btnKakaoSignup;
    private TextView tabLogin;
    private TextView tabSignup;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        bindViews();
        initTabs();
        initTextWatchers();
        initClickListeners();
    }

    private void bindViews() {
        inputUsername = findViewById(R.id.input_username);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        inputPasswordConfirm = findViewById(R.id.input_password_confirm);
        btnSignup = findViewById(R.id.btn_signup);
        btnKakaoSignup = findViewById(R.id.btn_kakao_signup);
        tabLogin = findViewById(R.id.tab_login);
        tabSignup = findViewById(R.id.tab_signup);
    }

    private void initTabs() {
        tabLogin.setOnClickListener(view -> {
            finish();
            overridePendingTransition(0, 0);
        });
        tabSignup.setOnClickListener(view -> {
            // 현재 회원가입 화면. 추가 동작 없음.
        });
    }

    private void initTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSignupButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        };

        inputUsername.addTextChangedListener(watcher);
        inputEmail.addTextChangedListener(watcher);
        inputPassword.addTextChangedListener(watcher);
        inputPasswordConfirm.addTextChangedListener(watcher);

        updateSignupButtonState();
    }

    private void updateSignupButtonState() {
        boolean filled = inputUsername.getText().length() > 0
                && inputEmail.getText().length() > 0
                && inputPassword.getText().length() > 0
                && inputPasswordConfirm.getText().length() > 0;
        btnSignup.setEnabled(filled);
        btnSignup.setAlpha(filled ? 1f : 0.5f);
    }

    private void initClickListeners() {
        btnSignup.setOnClickListener(view -> {
            String id = inputUsername.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString();
            String confirmPassword = inputPasswordConfirm.getText().toString();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast("이메일 형식을 확인하세요.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showToast("비밀번호가 일치하지 않습니다.");
                return;
            }

            performSignup(id, email, password);
        });

        btnKakaoSignup.setOnClickListener(view ->
                showToast("카카오 회원가입은 준비 중입니다.")
        );
    }

    private void performSignup(String id, String email, String password) {
        if (password.length() < 6) {
            showToast("비밀번호는 6자 이상이어야 합니다.");
            return;
        }

        btnSignup.setEnabled(false);
        btnSignup.setAlpha(0.5f);

        // Firebase Authentication으로 회원가입
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 회원가입 성공
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Firestore에 사용자 정보 저장 (일반 사용자로 저장)
                                saveUserToFirestore(user.getUid(), id, email, false);
                            }
                        } else {
                            // 회원가입 실패
                            btnSignup.setEnabled(true);
                            btnSignup.setAlpha(1f);
                            String errorMessage = "회원가입 실패";
                            if (task.getException() != null) {
                                String errorCode = task.getException().getMessage();
                                if (errorCode != null) {
                                    if (errorCode.contains("email-already-in-use")) {
                                        errorMessage = "이미 사용 중인 이메일입니다.";
                                    } else if (errorCode.contains("weak-password")) {
                                        errorMessage = "비밀번호가 너무 약합니다.";
                                    } else if (errorCode.contains("invalid-email")) {
                                        errorMessage = "이메일 형식이 올바르지 않습니다.";
                                    } else if (errorCode.contains("CONFIGURATION_NOT_FOUND")) {
                                        errorMessage = "Firebase 설정 오류: SHA-1 인증서 지문을 Firebase Console에 등록해주세요.";
                                    } else {
                                        errorMessage = "회원가입 실패: " + errorCode;
                                    }
                                }
                            }
                            showToast(errorMessage);
                        }
                    }
                });
    }

    private void saveUserToFirestore(String uid, String id, String email, boolean isPartner) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", id);
        userData.put("email", email);
        userData.put("passwd", ""); // Firebase Auth로 관리하므로 비밀번호는 저장하지 않음
        userData.put("uSaint", "");
        userData.put("isManager", false);
        userData.put("isPartner", isPartner);
        userData.put("coupon", 0);
        userData.put("points", 0);
        userData.put("favorites", new HashMap<String, Boolean>());
        userData.put("history", new HashMap<String, Boolean>());

        db.collection("users")
                .document(uid)
                .set(userData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            showToast("회원가입이 완료되었습니다.");
                            // 로그인 화면으로 이동
                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(0, 0);
                        } else {
                            btnSignup.setEnabled(true);
                            btnSignup.setAlpha(1f);
                            showToast("사용자 정보 저장에 실패했습니다.");
                        }
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

