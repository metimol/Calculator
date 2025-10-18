package com.metimol.calculator;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.HorizontalScrollView;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvDisplay;
    private HorizontalScrollView horizontalScrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        var main_layout = findViewById(R.id.main_screen);
        ViewCompat.setOnApplyWindowInsetsListener(main_layout, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            view.setPadding(
                    systemBars.left,
                    0,
                    systemBars.right,
                    systemBars.bottom
            );

            return WindowInsetsCompat.CONSUMED;
        });

        initializeViews();
        setupClickListeners();
        tvDisplay.setText("0");
    }

    private void initializeViews() {
        tvDisplay = findViewById(R.id.text_view);
        horizontalScrollView = findViewById(R.id.hsv_display);
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_ac).setOnClickListener(v -> onClearClick());
        findViewById(R.id.btn_equals).setOnClickListener(v -> onEqualsClick());
        findViewById(R.id.btn_dot).setOnClickListener(v -> onDotClick());
        findViewById(R.id.btn_brackets).setOnClickListener(v -> onBracketsClick());
        findViewById(R.id.btn_percent).setOnClickListener(v -> onPercentClick());

        List<Integer> numberButtonIds = Arrays.asList(
                R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9
        );
        for (int id : numberButtonIds) {
            findViewById(id).setOnClickListener(v -> onNumberClick(((Button) v).getText().toString()));
        }

        List<Integer> operatorButtonIds = Arrays.asList(
                R.id.btn_add, R.id.btn_subtract, R.id.btn_multiply, R.id.btn_divide
        );
        for (int id : operatorButtonIds) {
            findViewById(id).setOnClickListener(v -> onOperatorClick(((Button) v).getText().toString()));
        }
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null && vibrator.hasVibrator()) {
            VibrationEffect effect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE);
            vibrator.vibrate(effect);
        }
    }

    private void scrollToRight() {
        horizontalScrollView.post(() -> {
            horizontalScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
        });
    }

    private void scrollToLeft() {
        horizontalScrollView.post(() -> {
            horizontalScrollView.fullScroll(HorizontalScrollView.FOCUS_LEFT);
        });
    }

    private void onNumberClick(String number) {
        String currentText = tvDisplay.getText().toString();
        if (currentText.equals("0") || currentText.equals("ERROR")) {
            tvDisplay.setText(number);
        } else {
            tvDisplay.append(number);
        }

        vibrate();
        scrollToRight();
    }

    private void onOperatorClick(String operator) {
        String currentText = tvDisplay.getText().toString();
        if (currentText.equals("ERROR")) {
            return;
        }

        if (!currentText.isEmpty()) {
            char lastChar = currentText.charAt(currentText.length() - 1);
            if (isOperator(lastChar)) {
                tvDisplay.setText(currentText.substring(0, currentText.length() - 1) + operator);
            } else {
                tvDisplay.append(operator);
            }
        }

        vibrate();
        scrollToRight();
    }

    private void onDotClick() {
        String currentText = tvDisplay.getText().toString();
        if (currentText.equals("ERROR")) {
            return;
        }

        int lastOperatorIndex = -1;
        for (int i = currentText.length() - 1; i >= 0; i--) {
            if (isOperator(currentText.charAt(i))) {
                lastOperatorIndex = i;
                break;
            }
        }

        String lastNumber = currentText.substring(lastOperatorIndex + 1);
        if (!lastNumber.contains(".")) {
            tvDisplay.append(".");
        }

        vibrate();
        scrollToRight();
    }

    private void onBracketsClick() {
        String currentText = tvDisplay.getText().toString();
        if (currentText.equals("ERROR")) {
            tvDisplay.setText("(");
            return;
        }

        long openBrackets = currentText.chars().filter(ch -> ch == '(').count();
        long closedBrackets = currentText.chars().filter(ch -> ch == ')').count();
        char lastChar = currentText.isEmpty() ? ' ' : currentText.charAt(currentText.length() - 1);

        if (openBrackets > closedBrackets && (Character.isDigit(lastChar) || lastChar == ')')) {
            tvDisplay.append(")");
        } else {
            if (currentText.equals("0")) {
                tvDisplay.setText("(");
            } else if (Character.isDigit(lastChar) || lastChar == ')') {
                tvDisplay.append("*(");
            } else {
                tvDisplay.append("(");
            }
        }

        vibrate();
        scrollToRight();
    }

    private void onPercentClick() {
        onEqualsClick();
        String currentText = tvDisplay.getText().toString();
        if (currentText.equals("ERROR")) {
            return;
        }
        try {
            double value = Double.parseDouble(currentText) / 100.0;
            String resultString = (value == (long) value)
                    ? String.format("%d", (long) value)
                    : String.valueOf(value);
            tvDisplay.setText(resultString);
        } catch (NumberFormatException e) {
            displayError(e.toString());
        }

        vibrate();
        scrollToLeft();
    }

    private void onEqualsClick() {
        String expression = tvDisplay.getText().toString();
        expression = expression.replace('×', '*').replace('÷', '/');
        if (expression.equals("ERROR")) {
            return;
        }

        try {
            Expression exp = new ExpressionBuilder(expression).build();
            double result = exp.evaluate();

            if (Double.isInfinite(result) || Double.isNaN(result)) {
                displayError("Invalid expression");
                return;
            }

            String resultString = (result == (long) result)
                    ? String.format("%d", (long) result)
                    : String.valueOf(result);

            tvDisplay.setText(resultString);

        } catch (Exception e) {
            displayError(e.toString());
        }

        vibrate();
        scrollToLeft();
    }

    private void onClearClick() {
        vibrate();
        tvDisplay.setText("0");
        scrollToRight();
    }

    private void displayError(String error) {
        Log.e("Calculator", error);
        tvDisplay.setText("ERROR");
        scrollToLeft();
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }
}