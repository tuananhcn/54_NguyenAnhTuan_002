package com.example.quiz;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView questionTextView;
    private Button optionButton1, optionButton2, optionButton3, optionButton4;
    private List<Question> questions = new ArrayList<>();
    private int currentIndex = 0;
    private Button backButton, nextButton;
    private int score = 0;
    private SharedPreferences sharedPreferences;
    private boolean isOptionSelected = false; // Biến để kiểm tra xem phương án đã được chọn hay chưa
    private Button selectedButton = null; // Biến để lưu trữ nút được chọn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        questionTextView = findViewById(R.id.questionTextView);
        optionButton1 = findViewById(R.id.optionButton1);
        optionButton2 = findViewById(R.id.optionButton2);
        optionButton3 = findViewById(R.id.optionButton3);
        optionButton4 = findViewById(R.id.optionButton4);
        backButton = findViewById(R.id.backButton);
        nextButton = findViewById(R.id.nextButton);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("QuizApp", Context.MODE_PRIVATE);

        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());

        new Thread(() -> {
            questions.addAll(db.questionDao().getAll());
            runOnUiThread(() -> {
                restoreState();
                showQuestion(questions.get(currentIndex));
            });
        }).start();

        nextButton.setOnClickListener(v -> {
            if (isOptionSelected) { // Kiểm tra nếu phương án đã được chọn
                if (currentIndex < questions.size() - 1) {
                    currentIndex++;
                    showQuestion(questions.get(currentIndex));
                    saveState();
                    isOptionSelected = false; // Đặt lại biến kiểm tra khi chuyển sang câu hỏi mới
                    if (selectedButton != null) {
                        selectedButton.setBackgroundColor(getResources().getColor(R.color.defaultOptionColor));
                        selectedButton = null;
                    }
                } else {
                    questionTextView.setText("Quiz Finished! Your score: " + score);
                    optionButton1.setVisibility(View.GONE);
                    optionButton2.setVisibility(View.GONE);
                    optionButton3.setVisibility(View.GONE);
                    optionButton4.setVisibility(View.GONE);
                    nextButton.setVisibility(View.GONE);
                    backButton.setVisibility(View.GONE);
                }
            } else {
                // Hiển thị thông báo hoặc làm gì đó khi người dùng chưa chọn phương án
                questionTextView.setText("Please select an option before moving to the next question.");
            }
        });

        backButton.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                showQuestion(questions.get(currentIndex));
                saveState();
                isOptionSelected = false; // Đặt lại biến kiểm tra khi quay lại câu hỏi trước
                if (selectedButton != null) {
                    selectedButton.setBackgroundColor(getResources().getColor(R.color.defaultOptionColor));
                    selectedButton = null;
                }
            }
        });

        View.OnClickListener optionListener = view -> {
            Button b = (Button) view;
            String answer = b.getText().toString();
            if (answer.equals(questions.get(currentIndex).answer)) {
                score++;
            }
            isOptionSelected = true; // Đánh dấu rằng phương án đã được chọn
            if (selectedButton != null) {
                selectedButton.setBackgroundColor(getResources().getColor(R.color.defaultOptionColor));
            }
            b.setBackgroundColor(getResources().getColor(R.color.selectedOptionColor));
            selectedButton = b;
            saveState();
        };
        optionButton1.setOnClickListener(optionListener);
        optionButton2.setOnClickListener(optionListener);
        optionButton3.setOnClickListener(optionListener);
        optionButton4.setOnClickListener(optionListener);
    }

    private void showQuestion(Question question) {
        Type type = new TypeToken<List<String>>() {}.getType();
        List<String> options = new Gson().fromJson(question.options, type);
        optionButton1.setVisibility(View.VISIBLE);
        optionButton2.setVisibility(View.VISIBLE);
        optionButton3.setVisibility(View.VISIBLE);
        optionButton4.setVisibility(View.VISIBLE);

        questionTextView.setText(question.content);
        optionButton1.setText(options.get(0));
        optionButton2.setText(options.get(1));
        optionButton3.setText(options.get(2));
        optionButton4.setText(options.get(3));

        if (currentIndex > 0) {
            backButton.setVisibility(View.VISIBLE);
        } else {
            backButton.setVisibility(View.GONE);
        }
    }

    public void saveState() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("currentIndex", currentIndex);
        editor.putInt("score", score);
        editor.putBoolean("isOptionSelected", isOptionSelected);
        if (selectedButton != null) {
            editor.putInt("selectedButtonId", selectedButton.getId());
        }
        editor.apply();
    }

    private void restoreState() {
        if (sharedPreferences != null && sharedPreferences.contains("currentIndex")) {
            currentIndex = sharedPreferences.getInt("currentIndex", 0);
            score = sharedPreferences.getInt("score", 0);
            isOptionSelected = sharedPreferences.getBoolean("isOptionSelected", false);
            int selectedButtonId = sharedPreferences.getInt("selectedButtonId", -1);
            if (selectedButtonId != -1) {
                selectedButton = findViewById(selectedButtonId);
                if (selectedButton != null) {
                    selectedButton.setBackgroundColor(getResources().getColor(R.color.selectedOptionColor));
                }
            }
        }
    }
}
