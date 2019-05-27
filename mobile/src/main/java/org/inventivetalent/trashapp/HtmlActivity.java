package org.inventivetalent.trashapp;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import org.inventivetalent.trashapp.common.Util;

public class HtmlActivity extends AppCompatActivity {

	WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Util.applyTheme(this);

		setContentView(R.layout.activity_html);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

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
