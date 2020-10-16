package apps.java.suvorovoleg;


import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private TextView mTextViewResult;
    LoadingDialog loadingDialog;

    WebView webView;
    Intent intent;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = new Intent(this, apps.java.suvorovoleg.MWebView.class);
        mTextViewResult = findViewById(R.id.text_view_result);
        final OkHttpClient client = new OkHttpClient();
        String url = "https://ohsuvorov.pythonanywhere.com/";
        final Request request = new Request.Builder()
                .url(url)
                .build();

        loadingDialog = new LoadingDialog(MainActivity.this);
        loadingDialog.startLoadingDialog();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.dismissDialog();
                // Хочу вывести данное окно. Однако, если ответ приходит тру, на это уходит секунды 3
                // А затем вылазит это сообщение. Т.е. мне нужно сделать это
                // mTextViewResult.setText("Упс.. Что-то пошло не так\nЖдем соединение с сервером");

            }
        }, 5000);
        Callback call = new DataCheckCallback();
        client.newCall(request).enqueue(call);

    }

    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public static class MyWebViewClient extends WebViewClient {
        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

    }

    class DataCheckCallback implements Callback {
        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            MainActivity.this.runOnUiThread
                    (new Runnable() {
                        public void run() {
                            mTextViewResult.setText("Упс.. Что-то пошло не так\nСервер жмотится на ответ");
                        }
                    });

        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
            if (response.isSuccessful()) {
                final String msg = response.body().string();
                final boolean result = msg.contains("true");
                loadingDialog.dismissDialog();
                MainActivity.this.runOnUiThread
                        (new Runnable() {
                            @RequiresApi(api = Build.VERSION_CODES.P)
                            public void run() {
                                if (result) {
                                    startActivity(intent);

                                } else {
                                    mTextViewResult.setText("ЗАГЛУШКА");
                                }
                            }
                        });
            }
        }
    }
}
