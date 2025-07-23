package com.sadengineer.budgetmaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;
import com.google.android.material.navigation.NavigationView;

import com.sadengineer.budgetmaster.backend.BackendVersion;

public class VersionActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version);

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Настройка DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        
        // Настройка кнопок тулбара
        ImageButton backButton = findViewById(R.id.back_button);
        ImageButton menuButton = findViewById(R.id.menu_button);
        
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Возвращаемся на главный экран
                Intent intent = new Intent(VersionActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Открываем меню (если есть drawer layout)
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        // Получаем ссылки на TextView
        TextView frontendVersionText = findViewById(R.id.frontend_version_text);
        TextView backendVersionText = findViewById(R.id.backend_version_text);

        // Устанавливаем версию фронтенда из BuildConfig
        try {
            String frontendVersion = BuildConfig.APP_VERSION;
            frontendVersionText.setText(frontendVersion);
        } catch (Exception e) {
            frontendVersionText.setText("Неизвестно");
        }

        // Устанавливаем версию бекенда
        try {
            String backendVersion = BackendVersion.VERSION;
            backendVersionText.setText(backendVersion);
        } catch (Exception e) {
            backendVersionText.setText("Неизвестно");
        }
    }
} 