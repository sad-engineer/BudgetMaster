package com.sadengineer.budgetmaster;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    
    private ImageView calculatorImage;
    private TextView displayText;
    private String[] numbers = {"0", "123", "456", "789", "1000", "2500", "5000", "10000"};
    private int currentNumberIndex = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        calculatorImage = findViewById(R.id.calculator_image);
        displayText = findViewById(R.id.display_text);
        
        // Начинаем анимацию
        startCalculatorAnimation();
        
        // Переход к MainActivity через 4 секунды
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 4000);
    }
    
    private void startCalculatorAnimation() {
        // Анимация появления калькулятора
        calculatorImage.setScaleX(0.5f);
        calculatorImage.setScaleY(0.5f);
        calculatorImage.setAlpha(0f);
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(calculatorImage, "scaleX", 0.5f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(calculatorImage, "scaleY", 0.5f, 1.0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(calculatorImage, "alpha", 0f, 1f);
        
        AnimatorSet entranceAnim = new AnimatorSet();
        entranceAnim.playTogether(scaleX, scaleY, alpha);
        entranceAnim.setDuration(1000);
        entranceAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        
        entranceAnim.start();
        
        // Начинаем анимацию кнопок и чисел
        entranceAnim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                startButtonAnimation();
            }
        });
    }
    
    private void startButtonAnimation() {
        // Анимация мигания кнопок и изменения чисел
        ValueAnimator buttonAnimator = ValueAnimator.ofFloat(1.0f, 1.2f, 1.0f);
        buttonAnimator.setDuration(800);
        buttonAnimator.setRepeatCount(ValueAnimator.INFINITE);
        buttonAnimator.setRepeatMode(ValueAnimator.RESTART);
        
        buttonAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = (Float) animation.getAnimatedValue();
                calculatorImage.setScaleX(scale);
                calculatorImage.setScaleY(scale);
            }
        });
        
        buttonAnimator.start();
        
        // Анимация изменения чисел на дисплее
        startNumberAnimation();
    }
    
    private void startNumberAnimation() {
        final Handler numberHandler = new Handler();
        final Runnable numberRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentNumberIndex < numbers.length) {
                    // Анимация появления нового числа
                    displayText.setAlpha(0f);
                    displayText.setText(numbers[currentNumberIndex]);
                    
                    ObjectAnimator fadeIn = ObjectAnimator.ofFloat(displayText, "alpha", 0f, 1f);
                    fadeIn.setDuration(300);
                    fadeIn.start();
                    
                    currentNumberIndex++;
                    
                    // Следующее число через 500мс
                    numberHandler.postDelayed(this, 500);
                }
            }
        };
        
        numberHandler.post(numberRunnable);
    }
}
