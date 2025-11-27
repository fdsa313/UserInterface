package com.example.Partenership_Service.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;
import com.example.Partenership_Service.R;

import java.io.InputStream;

public class Student_card extends AppCompatActivity {
    private static final String TAG = "Student_card";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private TextRecognizer mlKitRecognizer;

    private ImageView imageView;
    private Button btnSelectImage;
    private Button btnProcessOCR;
    private TextView tvResult;
    private TextView tvStatus;

    private Bitmap selectedBitmap;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_card);

        initViews();
        checkPermissions();
        initMLKit();
        setupImagePicker();
        setupListeners();

        // 자동으로 이미지 선택 화면 열기
        selectImage();
    }

    private void initViews() {
        imageView = findViewById(R.id.imageView);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnProcessOCR = findViewById(R.id.btnProcessOCR);
        tvResult = findViewById(R.id.tvResult);
        tvStatus = findViewById(R.id.tvStatus);

        btnProcessOCR.setEnabled(false);
    }

    private void checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    PERMISSION_REQUEST_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void initMLKit() {
        try {
            updateStatus("ML Kit 초기화 중...");
            mlKitRecognizer = TextRecognition.getClient(new KoreanTextRecognizerOptions.Builder().build());
            updateStatus("준비 완료");
        } catch (Exception e) {
            Log.e(TAG, "ML Kit 초기화 오류", e);
            updateStatus("초기화 오류: " + e.getMessage());
            Toast.makeText(this, "OCR 초기화 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        selectedBitmap = BitmapFactory.decodeStream(inputStream);
                        imageView.setImageBitmap(selectedBitmap);
                        btnProcessOCR.setEnabled(true);
                        updateStatus("이미지 선택 완료");

                        // 자동으로 OCR 실행
                        processOCR();
                    } catch (Exception e) {
                        Log.e(TAG, "이미지 로드 실패", e);
                        Toast.makeText(this, "이미지 로드 실패", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 이미지 선택을 취소한 경우 액티비티 종료
                    finish();
                }
            }
        );
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnProcessOCR.setOnClickListener(v -> processOCR());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void processOCR() {
        if (selectedBitmap == null) {
            Toast.makeText(this, "먼저 이미지를 선택하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mlKitRecognizer == null) {
            Toast.makeText(this, "ML Kit이 초기화되지 않았습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        updateStatus("OCR 처리 중...");
        btnProcessOCR.setEnabled(false);

        try {
            InputImage image = InputImage.fromBitmap(selectedBitmap, 0);

            mlKitRecognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String recognizedText = visionText.getText();

                    if (recognizedText != null && !recognizedText.isEmpty()) {
                        tvResult.setText(recognizedText);
                        updateStatus("OCR 완료");
                        Toast.makeText(this, "OCR 처리 완료", Toast.LENGTH_SHORT).show();

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("ocr_result", recognizedText);
                        setResult(RESULT_OK, resultIntent);

                        new android.os.Handler().postDelayed(() -> finish(), 1500);
                    } else {
                        tvResult.setText("텍스트를 인식할 수 없습니다");
                        updateStatus("텍스트 인식 실패");
                        Toast.makeText(this, "텍스트 인식 실패", Toast.LENGTH_SHORT).show();
                    }
                    btnProcessOCR.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "ML Kit OCR 처리 오류", e);
                    tvResult.setText("오류: " + e.getMessage());
                    updateStatus("OCR 오류");
                    Toast.makeText(this, "OCR 처리 오류", Toast.LENGTH_SHORT).show();
                    btnProcessOCR.setEnabled(true);
                });
        } catch (Exception e) {
            Log.e(TAG, "ML Kit 처리 오류", e);
            tvResult.setText("오류: " + e.getMessage());
            updateStatus("OCR 오류");
            Toast.makeText(this, "OCR 처리 오류", Toast.LENGTH_SHORT).show();
            btnProcessOCR.setEnabled(true);
        }
    }

    private void updateStatus(String status) {
        runOnUiThread(() -> tvStatus.setText("상태: " + status));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mlKitRecognizer != null) {
            mlKitRecognizer.close();
        }
    }
}
