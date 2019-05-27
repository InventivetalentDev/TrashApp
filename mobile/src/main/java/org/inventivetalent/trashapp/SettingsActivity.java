package org.inventivetalent.trashapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import org.inventivetalent.trashapp.common.BillingConstants;
import org.inventivetalent.trashapp.common.PaymentHandler;
import org.inventivetalent.trashapp.common.PaymentReadyListener;
import org.inventivetalent.trashapp.common.Util;

public class SettingsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Util.applyTheme(this);

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

			final Preference adsPreference = findPreference("dummy_remove_ads");
			if (adsPreference != null) {
				adsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						if (TabActivity.SKU_INFO_PREMIUM != null) {
							TabActivity.SKU_INFO_PREMIUM.launchBilling();
						} else {
							Toast.makeText(getActivity(), "Product not ready!", Toast.LENGTH_SHORT).show();
						}
						return true;
					}
				});
			}

			final ListPreference themePreference = findPreference("app_theme");
			if (themePreference != null) {
				themePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						//TODO: refresh theme
						return true;
					}
				});
			}

			final PaymentHandler paymentHandler = TabActivity.instance;
			paymentHandler.waitForManager(new PaymentReadyListener() {
				@Override
				public void ready() {
					boolean hasPremium = paymentHandler.isPurchased(BillingConstants.SKU_PREMIUM);
					Log.i("SettingsActivity", "hasPremium: " + hasPremium);

					if (adsPreference != null) { adsPreference.setEnabled(!hasPremium); }
					if (themePreference != null) { themePreference.setEnabled(hasPremium); }
				}
			});

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