package org.inventivetalent.trashapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.settings, new SettingsFragment())
				.commit();
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	public static class SettingsFragment extends PreferenceFragmentCompat {
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.root_preferences, rootKey);

			Preference aboutPreference = findPreference("dummy_about");
			if (aboutPreference != null) {
				aboutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						showAbout();
						return true;
					}
				});
			}

			Preference osmPreference = findPreference("dummy_osm_info");
			if (osmPreference != null) {
				osmPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openWebView("file:///android_asset/about_osm_info.html");
						return true;
					}
				});
			}

			Preference adsPreference = findPreference("dummy_remove_ads");
			if (adsPreference != null) {
				adsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						if (TabActivity.SKU_INFO_PREMIUM != null) {
							TabActivity.SKU_INFO_PREMIUM.launchBilling();
						}else{
							Toast.makeText(getActivity(),"Product not ready!",Toast.LENGTH_SHORT).show();
						}
						return true;
					}
				});
			}
		}

		void showAbout() {
			Intent settingsIntent = new Intent(getActivity(), AboutActivity.class);
			startActivity(settingsIntent);
		}

		void openWebView(String uri) {
			Intent intent = new Intent(getActivity(), HtmlActivity.class);
			intent.setAction(Intent.ACTION_VIEW);
			intent.putExtra("uri", uri);
			startActivity(intent);
		}
	}

}