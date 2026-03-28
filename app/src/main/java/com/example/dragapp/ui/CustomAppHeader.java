package com.example.dragapp.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dragapp.R;

public class CustomAppHeader extends LinearLayout {

    private ImageView btnBack;
    private TextView headerTitle;
    private TextView headerSubtitle;
    private ImageView headerIcon;
    private View statusBarPlaceholder;

    public CustomAppHeader(Context context) {
        super(context);
        init(context);
    }

    public CustomAppHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomAppHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.custom_app_header, this);
        
        btnBack = findViewById(R.id.btn_back);
        headerTitle = findViewById(R.id.header_title);
        headerSubtitle = findViewById(R.id.header_subtitle);
        headerIcon = findViewById(R.id.header_icon);
        statusBarPlaceholder = findViewById(R.id.status_bar_placeholder);
        
        // تطبيق المسافة الآمنة من الـ status bar
        applyStatusBarInsets();
    }

    private void applyStatusBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(this, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int statusBarHeight = systemBars.top;
            
            if (statusBarHeight > 0 && statusBarPlaceholder != null) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) statusBarPlaceholder.getLayoutParams();
                params.height = statusBarHeight;
                statusBarPlaceholder.setLayoutParams(params);
            } else if (statusBarHeight == 0 && statusBarPlaceholder != null) {
                // للأجهزة القديمة، استخدم ارتفاع الـ status bar الافتراضي
                int defaultStatusBarHeight = getStatusBarHeight();
                if (defaultStatusBarHeight > 0) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) statusBarPlaceholder.getLayoutParams();
                    params.height = defaultStatusBarHeight;
                    statusBarPlaceholder.setLayoutParams(params);
                }
            }
            
            return insets;
        });
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void setTitle(String title) {
        if (headerTitle != null) {
            headerTitle.setText(title);
        }
    }

    public void setTitle(int titleResId) {
        if (headerTitle != null) {
            headerTitle.setText(titleResId);
        }
    }

    public void setSubtitle(String subtitle) {
        if (headerSubtitle != null) {
            if (subtitle != null && !subtitle.isEmpty()) {
                headerSubtitle.setText(subtitle);
                headerSubtitle.setVisibility(View.VISIBLE);
            } else {
                headerSubtitle.setVisibility(View.GONE);
            }
        }
    }

    public void setSubtitle(int subtitleResId) {
        if (headerSubtitle != null) {
            headerSubtitle.setText(subtitleResId);
            headerSubtitle.setVisibility(View.VISIBLE);
        }
    }

    public void showBackButton(boolean show) {
        if (btnBack != null) {
            btnBack.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public void setBackButtonListener(OnClickListener listener) {
        if (btnBack != null) {
            btnBack.setOnClickListener(listener);
        }
    }

    public void showHeaderIcon(boolean show) {
        if (headerIcon != null) {
            headerIcon.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public void setHeaderIcon(int iconResId) {
        if (headerIcon != null) {
            headerIcon.setImageResource(iconResId);
            headerIcon.setVisibility(View.VISIBLE);
        }
    }

    public void setHeaderIconListener(OnClickListener listener) {
        if (headerIcon != null) {
            headerIcon.setOnClickListener(listener);
        }
    }

    public void setHeaderIcon(Drawable drawable) {
        if (headerIcon != null && drawable != null) {
            headerIcon.setImageDrawable(drawable);
            headerIcon.setVisibility(View.VISIBLE);
        }
    }

    public void resetToDefault() {
        setTitle(R.string.app_name);
        setSubtitle("");
        showBackButton(false);
        showHeaderIcon(false);
    }
}
