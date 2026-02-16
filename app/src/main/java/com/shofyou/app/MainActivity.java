package com.shofyou.app;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.ValueCallback;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private SwipeRefreshLayout swipe;

    private static final String HOME_URL = "https://shofyou.com/";

    private ValueCallback<Uri[]> filePathCallback;
    private final static int FILE_CHOOSER_RESULT_CODE = 1;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // إزالة Splash فور بدء MainActivity
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipe = findViewById(R.id.swipe);
        webView = findViewById(R.id.webview);

        initWebView();

        swipe.setOnRefreshListener(() -> {

            if (!webView.getUrl().contains("/reels/")) {
                webView.reload();
            } else {
                swipe.setRefreshing(false);
            }

        });

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl(HOME_URL);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        // تحسين السرعة
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setLoadsImagesAutomatically(true);

        // تحسين الأداء
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {

                MainActivity.this.filePathCallback = filePathCallback;

                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/* video/*");

                startActivityForResult(intent, FILE_CHOOSER_RESULT_CODE);

                return true;
            }
        });

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                swipe.setRefreshing(false);

                if (url.contains("/reels/")) {
                    swipe.setEnabled(false);
                } else {
                    swipe.setEnabled(true);
                }

                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                swipe.setRefreshing(false);

                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.startsWith("https://shofyou.com")) {

                    view.loadUrl(url);
                    return true;

                } else {

                    Intent intent = new Intent(MainActivity.this, PopupActivity.class);
                    intent.putExtra("url", url);
                    startActivity(intent);

                    return true;
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

        if (webView.canGoBack()) {

            webView.goBack();

        } else {

            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == FILE_CHOOSER_RESULT_CODE) {

            if (filePathCallback == null) return;

            Uri[] results = null;

            if (resultCode == RESULT_OK && intent != null) {

                if (intent.getClipData() != null) {

                    int count = intent.getClipData().getItemCount();
                    results = new Uri[count];

                    for (int i = 0; i < count; i++) {

                        results[i] = intent.getClipData().getItemAt(i).getUri();
                    }

                } else if (intent.getData() != null) {

                    results = new Uri[]{intent.getData()};
                }
            }

            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }
}
