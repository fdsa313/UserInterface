package com.example.lonin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText inputUsername;
    private EditText inputPassword;
    private Button btnLogin;
    private Button btnKakaoLogin;
    private CheckBox checkboxAutoLogin;
    private TextView tabLogin;
    private TextView tabSignup;
    
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_AUTO_LOGIN = "auto_login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        bindViews();
        initTabs();
        initTextWatchers();
        initClickListeners();
        
        // 자동 로그인 체크
        checkAutoLogin();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // 현재 로그인된 사용자 확인 (Firebase 세션 유지)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // 이미 로그인되어 있으면 메인 화면으로 이동
            // TODO: 메인 Activity 생성 후 아래 주석 해제
            // showToast("이미 로그인되어 있습니다.");
            // Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            // startActivity(intent);
            // finish();
        }
    }
    
    private void checkAutoLogin() {
        boolean autoLogin = sharedPreferences.getBoolean(KEY_AUTO_LOGIN, false);
        if (autoLogin) {
            String savedEmail = sharedPreferences.getString(KEY_EMAIL, "");
            
            if (!savedEmail.isEmpty()) {
                checkboxAutoLogin.setChecked(true);
                inputUsername.setText(savedEmail);
                // 비밀번호는 저장하지 않으므로 사용자가 직접 입력해야 함
            }
        }
    }

    private void bindViews() {
        inputUsername = findViewById(R.id.input_username);
        inputPassword = findViewById(R.id.input_password);
        btnLogin = findViewById(R.id.btn_login);
        btnKakaoLogin = findViewById(R.id.btn_kakao_login);
        checkboxAutoLogin = findViewById(R.id.checkbox_auto_login);
        tabLogin = findViewById(R.id.tab_login);
        tabSignup = findViewById(R.id.tab_signup);
    }

    private void initTabs() {
        tabLogin.setOnClickListener(view -> {
            // 현재 로그인 화면이므로 추가 동작 없음
        });
        tabSignup.setOnClickListener(view -> {
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
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
                updateLoginButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        };

        inputUsername.addTextChangedListener(watcher);
        inputPassword.addTextChangedListener(watcher);

        updateLoginButtonState();
    }

    private void updateLoginButtonState() {
        boolean enabled = inputUsername.getText().length() > 0
                && inputPassword.getText().length() > 0;
        btnLogin.setEnabled(enabled);
        btnLogin.setAlpha(enabled ? 1f : 0.5f);
    }

    private void initClickListeners() {
        btnLogin.setOnClickListener(view -> {
            String email = inputUsername.getText().toString().trim();
            String password = inputPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                showToast("이메일과 비밀번호를 입력하세요.");
                return;
            }

            performLogin(email, password, checkboxAutoLogin.isChecked());
        });

        btnKakaoLogin.setOnClickListener(view ->
                showToast("카카오 로그인은 준비 중입니다.")
        );
    }

    private void performLogin(String email, String password, boolean autoLogin) {
        btnLogin.setEnabled(false);
        btnLogin.setAlpha(0.5f);

        // Firebase Authentication으로 로그인
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 로그인 성공
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // 자동 로그인 설정 저장
                                saveAutoLogin(email, password, autoLogin);
                                
                                showToast("로그인 성공");
                                // TODO: 메인 Activity로 이동
                                // Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                // startActivity(intent);
                                // finish();
                            }
                        } else {
                            // 로그인 실패
                            btnLogin.setEnabled(true);
                            btnLogin.setAlpha(1f);
                            String errorMessage = "로그인 실패";
                            if (task.getException() != null) {
                                String errorCode = task.getException().getMessage();
                                if (errorCode != null) {
                                    if (errorCode.contains("user-not-found")) {
                                        errorMessage = "등록되지 않은 이메일입니다.";
                                    } else if (errorCode.contains("wrong-password")) {
                                        errorMessage = "비밀번호가 잘못되었습니다.";
                                    } else if (errorCode.contains("invalid-email")) {
                                        errorMessage = "이메일 형식이 올바르지 않습니다.";
                                    } else if (errorCode.contains("user-disabled")) {
                                        errorMessage = "비활성화된 계정입니다.";
                                    } else {
                                        errorMessage = "로그인 실패: " + errorCode;
                                    }
                                }
                            }
                            showToast(errorMessage);
                        }
                    }
                });
    }
    
    private void saveAutoLogin(String email, String password, boolean autoLogin) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (autoLogin) {
            // 이메일만 저장 (비밀번호는 저장하지 않음 - 보안상 위험)
            editor.putString(KEY_EMAIL, email);
            editor.putBoolean(KEY_AUTO_LOGIN, true);
        } else {
            editor.remove(KEY_EMAIL);
            editor.remove(KEY_PASSWORD);
            editor.putBoolean(KEY_AUTO_LOGIN, false);
        }
        editor.apply();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}