package org.inventivetalent.trashapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.*;
import com.google.android.material.snackbar.Snackbar;
import org.inventivetalent.trashapp.common.BillingConstants;
import org.inventivetalent.trashapp.common.PaymentHandler;
import org.inventivetalent.trashapp.common.PaymentReadyListener;
import org.inventivetalent.trashapp.common.Util;

public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

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

	@Override
	public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
		return false;
	}

	public static class SettingsFragment extends PreferenceFragmentCompat {

		@Override
		public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
			super.onCreateOptionsMenu(menu, inflater);
		}

		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.root_preferences, rootKey);
			setHasOptionsMenu(false);

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

			final Preference clearCachePreference = findPreference("dummy_clear_cache");
			if (clearCachePreference != null) {
				clearCachePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						new AlertDialog.Builder(getActivity())
								.setTitle(R.string.dialog_clear_cache_title)
								.setMessage(R.string.dialog_clear_cache_message)
								.setIcon(android.R.drawable.ic_dialog_alert)
								.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										AsyncTask.execute(new Runnable() {
											@Override
											public void run() {
												Log.i("SettingsActivity", "ClearCache positive onClick");

												TabActivity.instance.appDatabase.trashcanDao().deleteAll();
												//												Toast.makeText(getContext(), "Trashcan cache cleared", Toast.LENGTH_SHORT).show();
												Snackbar.make(getView(), "Trashcan cache cleared", Snackbar.LENGTH_SHORT).show();
											}
										});
									}
								}).setNegativeButton(android.R.string.no, null)
								.show();

						return true;
					}
				});
			}

			final ListPreference themePreference = findPreference("app_theme");
			if (themePreference != null) {
				themePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						if (!themePreference.isEnabled()) {
							if (TabActivity.SKU_INFO_PREMIUM != null) {
								TabActivity.SKU_INFO_PREMIUM.launchBilling();
							} else {
								Toast.makeText(getActivity(), "Product not ready!", Toast.LENGTH_SHORT).show();
							}
						}
						return true;
					}
				});
				themePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						getActivity().recreate();
						return true;
					}
				});
			}

			if (adsPreference != null) { adsPreference.setEnabled(true); }
			if (themePreference != null) { themePreference.setEnabled(false); }


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

	public static class FilterSettingsFragment extends PreferenceFragmentCompat {
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.filter_preferences, rootKey);
			setHasOptionsMenu(true);
		}

		@Override
		public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
			Log.i("SettingsActivity", "Filters onCreateOptionsMenu");
			inflater.inflate(R.menu.filters_menu, menu);
		}

		void selectAll() {
			Log.i("SettingsActivity", "Filters selectAll");
			setAll(getPreferenceScreen(), true);
		}

		void selectNone() {
			Log.i("SettingsActivity", "Filters selectNone");
			setAll(getPreferenceScreen(), false);
		}

		void setAll(Preference pref, boolean checked) {
			if (pref instanceof PreferenceCategory || pref instanceof PreferenceScreen) {
				for (int i = 0; i < ((PreferenceGroup) pref).getPreferenceCount(); i++) {
					setAll(((PreferenceGroup) pref).getPreference(i), checked);
				}
			} else if (pref instanceof TwoStatePreference) {
				((TwoStatePreference) pref).setChecked(checked);
			} else {
				Log.w("SettingsActivity", "Filters " + pref + " is not a two-state");
			}
		}

		@Override
		public boolean onOptionsItemSelected(@NonNull MenuItem item) {
			Log.i("SettingsActivity", "Filters onOptionsItemSelected");
			switch (item.getItemId()) {
				case R.id.select_all:
					selectAll();
					return true;
				case R.id.select_none:
					selectNone();
					return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}

}