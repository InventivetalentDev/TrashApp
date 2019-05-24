package org.inventivetalent.trashapp;

import android.content.Intent;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class HtmlActivity extends AppCompatActivity {

	WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_html);

		webView = findViewById(R.id.webView);

		Intent intent = getIntent();
		if (intent != null) {
			if (Intent.ACTION_VIEW.equals(intent.getAction())) {
				String uriString = intent.getStringExtra("uri");
				if (uriString != null) {
					webView.loadUrl(uriString);
				}
			}
		}
	}

}
