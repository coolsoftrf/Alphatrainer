package ru.coolsoft.alphatrainer;

import androidx.appcompat.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

import ru.coolsoft.alphatrainer.data.GridData;
import ru.coolsoft.alphatrainer.ui.controls.BarAdapter;


public class GridActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks
        , ActionBar.OnNavigationListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private static final String STATE_SELECTED_MODE = "selected_navigation_bar_mode";
    private static final String TAG = "activity";

    enum TransitParams {
        TRANSIT_PARAM_MODE,
        TRANSIT_PARAM_ENTER_ANIMATION,
        TRANSIT_PARAM_EXIT_ANIMATION,

        TRANSIT_PARAMS_LEN
    }

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private PlaceholderFragment mCurrentGrid;
    private int mCurrentSelectedMode = -1;
    private Timer mTimer;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        if (savedInstanceState != null) {
            mCurrentSelectedMode = savedInstanceState.getInt(STATE_SELECTED_MODE);
        } else {
            mCurrentSelectedMode = PreferenceManager
                    .getDefaultSharedPreferences(this).getInt(STATE_SELECTED_MODE, 0);
        }
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onPostCreate");
        mNavigationDrawerFragment.sync();
        Log.d(TAG, "invalidateOptionsMenu");
        invalidateOptionsMenu();//сцуко-андроид 4.4.2 без этого не рисует панель пока не повернёшь экран
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_MODE, mCurrentSelectedMode);
    }

    private void onSectionAttached(int number) {
        //Log.d(TAG, "SA");
        mTitle = GridData.getTrainables().get(number);
    }

    private void restoreActionBar() {
        //Log.d(TAG, "RAB");
        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);

        ArrayAdapter<CharSequence> barAdapter = BarAdapter.createFromResource(
                actionBar.getThemedContext()
                , R.array.mode_names
                , R.array.mode_pics
                , android.R.layout.activity_list_item
                , android.R.id.text1, android.R.id.icon);
        barAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item
        );
        actionBar.setListNavigationCallbacks(barAdapter, this);
        actionBar.setSelectedNavigationItem(mCurrentSelectedMode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        // Only show items in the action bar relevant to this screen
        // if the drawer is not showing. Otherwise, let the drawer
        // decide what to show in the action bar.
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.grid, menu);
            MenuItem timeItem = menu.findItem(R.id.time);
            if (timeItem != null && mCurrentGrid != null && mCurrentGrid._data != null) {
                timeItem.setTitle(mCurrentGrid._data.sessionTime());
            }

            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.action_settings: {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            case R.id.shuffle: {
                mCurrentGrid.shuffle();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        if (!PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_key_eula), false)
        ) {
            SettingsActivity.showEula(getSupportFragmentManager(), this);
        }
        if (mTimer == null) {
            mTimer = new Timer("sessionTime");

            mTimer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            //Log.d("timer", "iom");
                            invalidateOptionsMenu();
                        }
                    }
                    , 0, 1000
            );
        }
        super.onResume();
    }


    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
        super.onPause();
    }

    private void gotoFragment(int lang, int mode) {
        //Log.d(TAG, "goF");
        int[] transition = new int[TransitParams.TRANSIT_PARAMS_LEN.ordinal()];
        PlaceholderFragment newGrid = PlaceholderFragment.newInstance(
                lang, mode, transition
        );
        if (mCurrentGrid != newGrid) {
            // update the main content by replacing fragments
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .setCustomAnimations(
                            transition[TransitParams.TRANSIT_PARAM_ENTER_ANIMATION.ordinal()]
                            , transition[TransitParams.TRANSIT_PARAM_EXIT_ANIMATION.ordinal()]
                    )
                    .setTransition(transition[TransitParams.TRANSIT_PARAM_MODE.ordinal()])
                    .replace(R.id.container, mCurrentGrid = newGrid)
                    .commit();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Log.d(TAG, "onNavigationDrawerItemSelected (p " + position + " m " + mCurrentSelectedMode + ")");
        if (mCurrentSelectedMode < 0) {
            return;
        }
        gotoFragment(position, mCurrentSelectedMode);
    }

    @Override
    public boolean onNavigationItemSelected(int idx, long id) {
        Log.d(TAG, "onNavigationItemSelected " + idx);
        mCurrentSelectedMode = idx;
        SharedPreferences.Editor ed = PreferenceManager.getDefaultSharedPreferences(this).edit();
        ed.putInt(STATE_SELECTED_MODE, idx).apply();
        ed.apply();
        gotoFragment(mNavigationDrawerFragment.getCurrentSelectedPosition(), idx);
        return true;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_LANGUAGE_NUMBER = "section_number";
        private static final String ARG_MODE_NUMBER = "mode_number";

        private static final String TAG = "fragment";

        private GridData _data;

        private static int _lastLang = -1;
        private static int _lastMode = -1;
        private static PlaceholderFragment _lastFragment = null;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, int modeNumber
                , final int[] outTransitParams) {
            Log.d(TAG, "ni");
            if (sectionNumber == _lastLang && modeNumber == _lastMode && _lastFragment != null) {
                return _lastFragment;
            }

            if (_lastMode == modeNumber) {
                outTransitParams[TransitParams.TRANSIT_PARAM_MODE.ordinal()]
                        = FragmentTransaction.TRANSIT_FRAGMENT_OPEN;
                outTransitParams[TransitParams.TRANSIT_PARAM_ENTER_ANIMATION.ordinal()]
                        = R.animator.slide_in_left;

            } else {
                outTransitParams[TransitParams.TRANSIT_PARAM_MODE.ordinal()]
                        = FragmentTransaction.TRANSIT_FRAGMENT_CLOSE;
                outTransitParams[TransitParams.TRANSIT_PARAM_EXIT_ANIMATION.ordinal()]
                        = R.animator.slide_out_down;
            }
            _lastLang = sectionNumber;
            _lastMode = modeNumber;
            _lastFragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_LANGUAGE_NUMBER, sectionNumber);
            args.putInt(ARG_MODE_NUMBER, modeNumber);
            _lastFragment.setArguments(args);
            return _lastFragment;
        }

        /*
                public PlaceholderFragment() {
                }
        */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            Log.d(TAG, "onCreate, invalidateOptionsMenu");
            super.onCreate(savedInstanceState);
            //setHasOptionsMenu(true);
            requireActivity().invalidateOptionsMenu();
        }

        @Override
        public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d(TAG, "onCreateView");
            final int mode = getArguments().getInt(ARG_MODE_NUMBER);
            int _grdWidth;
            if (mode == GridData.MODE_ALPHABET)
                _grdWidth = 4;
            else
                _grdWidth = 5;

            final View rootView = inflater.inflate(R.layout.fragment_grid, container, false);
            final GridView gv = (GridView) rootView.findViewById(R.id.grid);

            gv.setNumColumns(_grdWidth);

            _data = GridData.getInstance(
                    new GridData.IMatchListener() {
                        @Override
                        public Context getContext() {
                            return requireActivity();
                        }

                        @Override
                        public void onMatched() {
                            gv.setItemChecked(_data.clicked1(), false);
                            gv.setItemChecked(_data.clicked2(), false);

                            _data.checkFinished();
                            if (mode != GridData.MODE_ALPHABET) {
                                matchGuess(rootView);
                            }

                            if (_data.isFinished()) {
                                String msg;
                                if (mode == GridData.MODE_ALPHABET) {
                                    msg = String.format(
                                            getString(R.string.result_alpha)
                                            , _data.sessionTime(), _data.items().size() / 2, _data.mistakes()
                                    );
                                } else {
                                    msg = String.format(
                                            getString(R.string.result_words)
                                            , _data.sessionTime(), _data.hints()
                                    );
                                }
                                new AlertDialog.Builder(rootView.getContext())
                                        .setTitle(R.string.congratulations)
                                        .setMessage(msg)
                                        .setPositiveButton(android.R.string.ok, (dialog, which) -> shuffle())
                                        .create().show();
                            }
                        }
                    }
                    , getArguments().getInt(ARG_LANGUAGE_NUMBER)
                    , mode
            );

            rootView.setLayoutDirection(_data.isRightToLeft()
                    ? View.LAYOUT_DIRECTION_RTL
                    : View.LAYOUT_DIRECTION_LTR
            );
            gv.setAdapter(new ArrayAdapter<>(requireActivity(),
                    R.layout.grid_item, android.R.id.text1, _data.items()));
            gv.setOnItemClickListener((parent, view, position, id) -> {
                if (_data.items().get(position).length() == 0) {
                    ((GridView) parent).setItemChecked(position, false);
                    return;
                }
                ((GridView) parent).setItemChecked(position, true);

                new DelayedClickHandler(_data, position).execute(requireActivity());
            });

            final EditText guess = (EditText) rootView.findViewById(android.R.id.text2);
            ImageButton h = (ImageButton) rootView.findViewById(R.id.help);
            ImageButton x = (ImageButton) rootView.findViewById(R.id.x);
            ImageButton c = (ImageButton) rootView.findViewById(R.id.c);

            if (mode == GridData.MODE_ALPHABET) {
                x.setVisibility(View.GONE);
                c.setVisibility(View.GONE);
                h.setVisibility(View.GONE);
            } else {
                guess.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        _data.handleGuessChanged(s.toString());
                    }
                });

                x.setOnClickListener(v -> _data.clearGuess());
                h.setOnClickListener(v -> {
                    String word = _data.hint();
                    if (word.length() == 0) {
                        return;
                    }

                    Rect rect = new Rect();
                    guess.getPaint().getTextBounds(word, 0, word.length() - 1, rect);
                    int left = rect.width();
                    guess.getPaint().getTextBounds(word, 0, word.length(), rect);
                    int right = rect.width();

                    final ImageView hint = (ImageView) rootView.findViewById(R.id.highlight);
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) hint.getLayoutParams();
                    lp.setMarginStart(left + guess.getTotalPaddingLeft());
                    hint.setLayoutParams(lp);

                    hint.setMinimumWidth(right - left + 20);

                    AlphaAnimation alpha = new AlphaAnimation(1, 0);
                    alpha.setDuration(1000);
                    alpha.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            hint.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            hint.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    hint.startAnimation(alpha);
                });
                c.setOnClickListener(v -> _data.undoGuess());
            }

            if (_data.isRightToLeft()) {
                CharSequence hint = GridData.RTL + guess.getHint();
                guess.setHint(hint);
            }
            updateView(rootView, mode);

            AdView adView = (AdView) rootView.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("CC2C0CC7EBDCE3406C94D60D1B89504A")
                    .build();
            adView.loadAd(adRequest);
            return rootView;
        }

        public void shuffle() {
            _data.shuffle();

            View rootView = getView();
            assert rootView != null;

            ((BaseAdapter) ((GridView) rootView.findViewById(R.id.grid)).getAdapter()).notifyDataSetChanged();
            updateView(rootView, getArguments().getInt(ARG_MODE_NUMBER));
        }

        private void matchGuess(View rootView) {
            String g = _data.guess();
            EditText edit = ((EditText) rootView.findViewById(android.R.id.text2));
            if (!edit.getText().toString().equals(g)) {
                edit.setText(g);
            }
            rootView.findViewById(R.id.correct).setVisibility(
                    _data.isFinished() ? View.VISIBLE : View.GONE);
        }

        private void updateView(View rootView, int mode) {
            TextView word = (TextView) rootView.findViewById(android.R.id.text1);
            EditText guess = (EditText) rootView.findViewById(android.R.id.text2);

            if (mode == GridData.MODE_ALPHABET) {
                word.setVisibility(View.GONE);
                guess.setVisibility(View.GONE);
            } else {
                word.setText(_data.word());
                matchGuess(rootView);
            }

            if (mode != GridData.MODE_TRANSLATE) {
                guess.setFocusableInTouchMode(false);

                InputMethodManager inputManager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                View view = requireActivity().getCurrentFocus();
                if (view != null) {
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
            /* else {
                guess.setFilters(new InputFilter[] {
                        new InputFilter() {
                            LengthFilter lf = new LengthFilter(32);
                            public CharSequence filter(CharSequence src, int start, int end,
                                                       Spanned dst, int dstart, int dend) {
                                if (mode == GridData.MODE_TRANSLATE){
                                //TODO: Filter by appropriate char range
                                    return lf.filter(src, start, end, dst, dstart, dend);
                                }
                                return src.length() < 1 ? dst.subSequence(dstart, dend) : "";
                            }
                        }
                });
            }
*/
        }

        @Override
        public void onAttach(@NotNull Activity activity) {
            Log.d(TAG, "onAttach");
            super.onAttach(activity);
            ((GridActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_LANGUAGE_NUMBER));
        }

        @Override
        public void onResume() {
            Log.d(TAG, "onResume");
            View rootView = getView();
            assert rootView != null;

            ((GridView) rootView.findViewById(R.id.grid)).setItemChecked(_data.clicked1(), true);
            super.onResume();
        }


        static class DelayedClickHandler extends AsyncTask<Activity, Void, Void> {

            GridData data;
            int position;

            DelayedClickHandler(GridData data, int position) {
                //this.activity = activity;
                this.data = data;
                this.position = position;
            }

            @Override
            protected Void doInBackground(final Activity... params) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Log.w(TAG, "delay interrupted");
                }
                params[0].runOnUiThread(() -> data.handleClick(position));
                return null;
            }
        }
    }
}
