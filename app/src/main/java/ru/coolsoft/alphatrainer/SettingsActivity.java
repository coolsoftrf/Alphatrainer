package ru.coolsoft.alphatrainer;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import ru.coolsoft.alphatrainer.data.DbAccessor;
import ru.coolsoft.alphatrainer.ui.dialogs.AlertDialogFragment;
import ru.coolsoft.alphatrainer.ui.dialogs.SortableListDialogFragment;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatActivity
        implements SortableListDialogFragment.SortableListDialogListener {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    //private static final boolean ALWAYS_SIMPLE_PREFS = false;
    private static Preference.OnPreferenceClickListener opclAlert(final FragmentManager fm, final Context context) {
        return preference -> {
            if (fm.findFragmentByTag(preference.getKey()) != null) {
                return true;
            }
            boolean isEULA = preference.getKey().equals(
                    context.getString(R.string.pref_key_eula)
            );
            boolean isEulaAccepted = PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .getBoolean(preference.getKey(), false);

            DialogFragment alertDialogFragment = new AlertDialogFragment();

            Bundle args = new Bundle();
            args.putString(AlertDialogFragment.ITEM_PREFERENCE_KEY, preference.getKey());
            args.putString(AlertDialogFragment.ITEM_PREFERENCE_TITLE, preference.getTitle().toString());
            args.putBoolean(AlertDialogFragment.ITEM_IS_EULA, isEULA);
            args.putBoolean(AlertDialogFragment.ITEM_IS_EULA_ACCEPTED, isEulaAccepted);
            alertDialogFragment.setArguments(args);

            if (isEULA && !isEulaAccepted) {
                alertDialogFragment.setCancelable(false);
            }

            alertDialogFragment.show(fm, preference.getKey());
            return true;
        };
    }

    public static void showEula(final FragmentManager fm, Context context) {
        Preference preference = new Preference(context);

        //these 2 attributes are used in #onPreferenceClick to show the dialog
        preference.setKey(context.getString(R.string.pref_key_eula));
        preference.setTitle(R.string.pref_title_eula);

        opclAlert(fm, context).onPreferenceClick(preference);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (getIntent().getAction() == null) {
            setupSimplePreferencesScreen();
        }
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
/*
        if (isCompositePreferences(this)) {
            return;
        }
*/

        //Populate General fragment with additionally all other settings
        GeneralPreferenceFragment gpf = new GeneralPreferenceFragment();

        Bundle b = new Bundle();
        b.putBoolean(GeneralPreferenceFragment.ARG_INCLUDE_ALL, true);

        gpf.setArguments(b);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, gpf)
                .commit();
    }


    /**
     * {@inheritDoc}
     */
/*
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && isCompositePreferences(this);
    }
*/

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
/*
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }
*/

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via ALWAYS_SIMPLE_PREFS (deprecated), or the device
     * doesn't have newer APIs like {@link PreferenceFragmentCompat}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
/*
    private static boolean isCompositePreferences(Context context) {
        return //ALWAYS_SIMPLE_PREFS ||
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                        && isXLargeTablet(context);
    }
*/

    /**
     * {@inheritDoc}
     */
/*
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (isCompositePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }
*/

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) -> {
        String stringValue = value.toString();

        preference.setSummary(stringValue);

        return true;
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public void onPositiveButtonClicked(LinkedList<Long> precedence) {
        DbAccessor.updateDescriptives(precedence);
/*        SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(this).edit();
        e.putString(getString(R.string.pref_key_script), DbAccessor.firstScript());
        e.commit();*/
    }

    /*
        @Override
        public void onNegativeButtonClicked() {

        }
    */
/*
    @Override
    protected boolean isValidFragment(String fragmentName) {
        return Arrays.asList(GeneralPreferenceFragment.class.getName()
                , AdsPreferenceFragment.class.getName()
                , InfoPreferenceFragment.class.getName()
                , LanguagesPreferenceFragment.class.getName()
        ).contains(fragmentName);
    }
*/

    ////////////////////////////////////////////
    //////////////// FRAGMENTS /////////////////
    ////////////////////////////////////////////
    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    //@TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat {

        public static final String ARG_INCLUDE_ALL = "include_all_preferences";

        @Override
        public void onCreatePreferences(
                @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState,
                @Nullable @org.jetbrains.annotations.Nullable String rootKey) {
            setupFragment(this);

            Bundle args = getArguments();
            if (args != null && args.getBoolean(ARG_INCLUDE_ALL, false)) {
                AdsPreferenceFragment.setupFragment(this);
                InfoPreferenceFragment.setupFragment(this);
            }
        }

        public static void setupFragment(final PreferenceFragmentCompat preferenceFragment) {
            Context ctx = preferenceFragment.requireActivity();
            preferenceFragment.addPreferencesFromResource(R.xml.pref_general);

            bindPreferenceSummaryToValue(preferenceFragment.findPreference(ctx.getString(R.string.pref_key_quantity)));

            Preference prefLangPriority = preferenceFragment.findPreference(ctx.getString(R.string.pref_key_script));
            if(prefLangPriority != null) {
                prefLangPriority.setOnPreferenceClickListener(preference -> {
                    ArrayList<String> langIDs = new ArrayList<>();
                    ArrayList<String> langs = DbAccessor.descriptives(langIDs);

                    SortableListDialogFragment sldf = new SortableListDialogFragment();
                    Bundle args = new Bundle();
                    args.putStringArrayList(SortableListDialogFragment.ITEM_NAMES, langs);
                    args.putStringArrayList(SortableListDialogFragment.ITEM_KEYS, langIDs);
                    sldf.setArguments(args);
                    sldf.show(preferenceFragment.getParentFragmentManager(), preference.getKey());
                    return true;
                });
            }
        }
    }

    /**
     * This fragment shows ad-related preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AdsPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState, @Nullable @org.jetbrains.annotations.Nullable String rootKey) {
            setupFragment(this);
        }

        public static void setupFragment(final PreferenceFragmentCompat preferenceFragment) {
            preferenceFragment.addPreferencesFromResource(R.xml.pref_ads);
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class InfoPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState, @Nullable @org.jetbrains.annotations.Nullable String rootKey) {
            setupFragment(this);
        }

        public static void setupFragment(final PreferenceFragmentCompat preferenceFragment) {
            Context ctx = preferenceFragment.requireContext();
            FragmentManager fm = preferenceFragment.getFragmentManager();
            preferenceFragment.addPreferencesFromResource(R.xml.pref_info);

            Preference prefEULA = preferenceFragment.findPreference(ctx.getString(R.string.pref_key_eula));
            prefEULA.setOnPreferenceClickListener(opclAlert(fm, ctx));

            Preference prefAbout = preferenceFragment.findPreference(ctx.getString(R.string.pref_key_about));
            prefAbout.setOnPreferenceClickListener(opclAlert(fm, ctx));
        }
    }

    /**
     * This fragment shows Language list for Level preferences.
     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class LanguagesPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState, @Nullable @org.jetbrains.annotations.Nullable String rootKey) {
            addPreferencesFromResource(R.xml.pref_levels);
            final PreferenceScreen ps = getPreferenceScreen();

            final Context ctx = requireContext();
            final String keyPrefix = ctx.getString(R.string.pref_key_levels);
            final ArrayList<String> langIds = new ArrayList<>();
            final ArrayList<String> langDescriptions = new ArrayList<>();
            final ArrayList<String> langs = DbAccessor.trainables(langIds, langDescriptions);
            for (int i = 0; i < langs.size(); i++) {
                final String langId = langIds.get(i);
                ArrayList<String> levelIds = new ArrayList<>();
                Set<String> levelsEnabled = new HashSet<>();
                ArrayList<String> levelNames = DbAccessor.levels(
                        langId, levelIds, levelsEnabled
                );

                MultiSelectListPreference pref = new MultiSelectListPreference(ctx) {
                    @Override
                    public void setValues(Set<String> values) {
                        DbAccessor.updateLevels(langId, values);
                        super.setValues(values);
                    }
                };
                pref.setKey(keyPrefix + langId);
                pref.setTitle(langs.get(i));
                pref.setSummary(langDescriptions.get(i));

                String[] entries = new String[levelNames.size()];
                pref.setEntries(levelNames.toArray(entries));

                String[] ids = new String[levelNames.size()];
                pref.setEntryValues(levelIds.toArray(ids));

                pref.setValues(levelsEnabled);
//                pref.setPersistent(false);

                ps.addPreference(pref);
            }
            setPreferenceScreen(ps);
        }
    }
}
