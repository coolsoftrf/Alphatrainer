package ru.coolsoft.alphatrainer.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.google.android.gms.ads.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.coolsoft.alphatrainer.R;
import ru.coolsoft.alphatrainer.TrainerApplication;

/**
 * Created by Bobby© on 29.03.2015.
 * Manages UI-independent data for the application
 */
public class GridData {
    /////////////////// CONSTANTS ///////////////////
    public static final String RTL = "\u200F";
    private static final int NONE = -1;
    private static final String VOICING_PLACEHOLDER = "ב";//RTL + "◌";//'◦';

    public static final int MODE_ALPHABET = 0;
    private static final int MODE_SPELL = 1;
    public static final int MODE_TRANSLATE = 2;

    /////////////////// FIELDS ///////////////////
    //all modes
    private boolean _rtl = false;
    private long _timeStarted;
    private long _timeFinished;
    private int _clicked1 = NONE;

    private int _clicked2 = NONE;
    private int _section = NONE;
    private int _mode = NONE;
    private final ArrayList<String> _items = new ArrayList<>();
    private ArrayList<Integer> _matches;
    private int _matched;
    private int _mistaken;
    private int _hints;
    private boolean _finished;

    //spell & translation modes
    private Set<Integer> _wordsUsed = new HashSet<>();
    private String _word = null;
    private String _answer = null;
    private String _guess = "";

    // Ads
    private InterstitialAd _interstitial;
    private boolean _loadFailed;
    private boolean _loadStarted;
    private int _ads2Skip = 5;
    private int _adCountDown = _ads2Skip;

    /////////////////// GETTERS ///////////////////
    public int clicked1() {
        return _clicked1;
    }
    public int clicked2() {
        return _clicked2;
    }

    public String word() {
        return _word;
    }
    public String guess() {
        return _guess;
    }
    public int mistakes() {
        return _mistaken;
    }
    public int hints() {
        return _hints;
    }

    public ArrayList<String> items(){
        return _items;
    }

    /////////////////// INTERFACES ///////////////////
    public interface IMatchListener{
        Context getContext();
        void onMatched();
    }

    /////////////////// STATIC DATA ///////////////////
    private static ArrayList<String> mTrainables;
    private static ArrayList<String> mTrainableIDs;
    private static String mCurrentScript;
    private static ArrayList<String> mTrainableDescriptions;

    private static void initTrainables(){
        String spellID = DbAccessor.firstScript();
        if (mTrainables == null || !mCurrentScript.equals(spellID)){
            mTrainableIDs = new ArrayList<>();
            mTrainableDescriptions = new ArrayList<>();
            mTrainables = DbAccessor.trainables(mTrainableIDs, mTrainableDescriptions);
            mCurrentScript = spellID;
        }
    }
    public static ArrayList<String> getTrainables(){
        initTrainables();
        return mTrainables;
    }
    private static int getTrainableId(int ordinal){
        initTrainables();
        return Integer.parseInt(mTrainableIDs.get(ordinal));
    }
    public static String getTrainableDescription(int ordinal){
        initTrainables();
        return mTrainableDescriptions.get(ordinal);
    }

    /////////////////// INSTANCE CONTROL ///////////////////
    private IMatchListener _listener;
    private final static GridData ourInstance = new GridData();

    public static GridData getInstance(IMatchListener listener, int section, int mode) {
        ourInstance._listener = listener;
        if ((ourInstance._section != section) || (ourInstance._mode != mode)){
            ourInstance._section = section;
            ourInstance._mode = mode;
            ourInstance.shuffle();
        }
        return ourInstance;
    }

    private GridData() {
        _interstitial = new InterstitialAd(TrainerApplication.app());
        _interstitial.setAdUnitId("ca-app-pub-7672444295949563/5634931735");
        _interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdOpened();
                _adCountDown = /*++*/_ads2Skip;
                prepareAd();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                _loadFailed = true;
            }
        });

        prepareAd();
    }

    private void prepareAd() {
        Context ctx = TrainerApplication.app();
        NetworkInfo info = ((ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        boolean adsOverWifiOnly = PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(ctx.getString(R.string.pref_key_interstitial)
                        , ctx.getResources().getBoolean(R.bool.pref_default_interstitial));
        if (info != null && info.getType() != ConnectivityManager.TYPE_MOBILE
                || !adsOverWifiOnly) {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("CC2C0CC7EBDCE3406C94D60D1B89504A")
                    .build();

            _loadStarted = true;
            _loadFailed = false;
            _interstitial.loadAd(adRequest);
        } else {
            _loadStarted = false;
        }
    }

    private void dispatchAd() {
        if(--_adCountDown <= 0 && _interstitial.isLoaded()) {
            _interstitial.show();
        } else if(!_loadStarted || _loadFailed) {
            prepareAd();
        }
    }

    /////////////////// METHODS ///////////////////
    private int getFreeLetter(List<String> letters) {
        int n = NONE;
        int curLetter = _items.size();
        String nextMandatory = "";

        if (_mode == MODE_SPELL && curLetter < _answer.length()){
            for (; curLetter < _answer.length(); curLetter++){
                //Combined letters require a tricky search
                for (int i = 0; i < letters.size(); i++){
                    String ltr = letters.get(i);
                    if (_answer.length() >= (curLetter + ltr.length())) {
                        String sub = _answer.substring(curLetter, curLetter + ltr.length());
                        if (ltr.equals(sub)) {
                            nextMandatory = sub;
                            n = i;
                            break;
                        }
                    }
                }
                if(!_items.contains(checkVoicing(nextMandatory))){
                    break;
                }
            }
            if(curLetter >= _answer.length())
            {
                nextMandatory = "";
            }
        }

        if (/*!*/nextMandatory.equals("")) /*{
            n = letters.indexOf(nextMandatory);
        } else */{
            do {
                n = (int) (Math.random() * letters.size());
            } while (_items.contains(checkVoicing(letters.get(n))));
        }
        return n;
    }

    private String getSimplifiedText (String complexText){
        String answer = complexText;
        for (int i = 0; i < answer.length(); i++) {
            char c = answer.charAt(i);
            //process japanese voiced kana
            if((c > 'か' && c <= 'ぢ' || c > 'カ' && c <= 'ヂ') && (c % 2 == 0)
                    || (c > 'つ' && c <= 'ど' || c > 'ツ' && c <= 'ド') && (c % 2 == 1)
                    || (c > 'は' && c <= 'ぽ' || c > 'ハ' && c <= 'ポ') && (c % 3 == 1)
                    ){
                answer = answer.substring(0, i) + (char)(c - 1) + "゛" + answer.substring(i + 1);
            }
            else
                //and semi-voiced
                if((c > 'は' && c <= 'ぽ' || c > 'ハ' && c <= 'ポ') && (c % 3 == 2)){
                    answer = answer.substring(0, i) + (char)(c - 2) + "゜" + answer.substring(i + 1);
                }
                else
                    //remove spaces
                    if((c  == ' ')){
                        answer = answer.substring(0, i) + answer.substring(i + 1);
                    }
        }
        return answer;
    }

    private boolean isShowPhonetics(){
        Context ctx = _listener.getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(ctx.getString(R.string.pref_key_phonetic), false);
    }
    private boolean isShowObsolete(){
        Context ctx = _listener.getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(ctx.getString(R.string.pref_key_obsolete), false);
    }

    public void shuffle(){
        resetClicks();
        Context ct = _listener.getContext();

        _items.clear();
        _matches = new ArrayList<>();
        _matched = 0;
        _mistaken = 0;
        _hints = 0;
        _finished = false;

        ArrayList<String> letterNames = new ArrayList<>();

        int trainable = getTrainableId(_section);
        final boolean isAlpha = _mode == MODE_ALPHABET;
        ArrayList<String> array1 = DbAccessor.alphas(trainable
                , isShowObsolete()
                , isShowPhonetics() || !isAlpha
                , isAlpha ? letterNames : null);
        int dir = Character.getDirectionality(array1.get(0).charAt(0));
        _rtl = dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT
                || dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
                || dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING
                || dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE;

        int maxLetters = 0;
        final int mul;
        switch (_mode) {
            case MODE_ALPHABET:
            case MODE_SPELL:
                maxLetters = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(ct).getString(
                        ct.getString(R.string.pref_key_quantity), ct.getString(R.string.pref_default_quantity)))
                        * (_mode + 1);

                if (_mode == MODE_ALPHABET) {
                    mul = 2;
                    break;
                }
            default://case MODE_TRANSLATE:
                if (_mode == MODE_TRANSLATE) {
                    maxLetters = array1.size();
                }
                mul = 1;
        }

        ArrayList<String> langAnswers = new ArrayList<>();
        ArrayList<String> langTasks = null;
        switch (_mode) {
            case MODE_ALPHABET:
                _word = _answer = _guess = "";
                break;
            case MODE_SPELL:
                langTasks = DbAccessor.spell(trainable, langAnswers);
            case MODE_TRANSLATE:
                if (langTasks == null) {
                    langTasks = DbAccessor.tran(trainable, langAnswers);
                }
                if (langTasks.size() == 0){
                    _word = _answer = "";
                } else {
                    int n;
                    if (_wordsUsed.size() >= langTasks.size()){
                        _wordsUsed = new HashSet<>();
                    }
                    do {
                        n = (int) Math.floor(Math.random() * langTasks.size());
                    } while (_wordsUsed.contains(n));
                    _wordsUsed.add(n);
                    _word = langTasks.get(n);
                    _answer = getSimplifiedText(langAnswers.get(n));

                    _guess = "";
                }
                break;
        }

        for (int i = 0; i < maxLetters && i < array1.size(); i++) {
            int letter;
            int n;
            if (_mode != MODE_TRANSLATE) {
                letter = getFreeLetter(array1);
                n = (int) Math.floor(Math.random() * (i * mul + 1));
            } else {
                letter = n = i;
            }
            _items.add(n, checkVoicing(array1.get(letter)));
            _matches.add(n, letter);
            if (_mode == MODE_ALPHABET) {
                n = (int) Math.floor(Math.random() * (i * mul + 2));
                assert letterNames!= null;
                _items.add(n, letterNames.get(letter));
                _matches.add(n, letter);
            }
        }

        _timeFinished = 0;
        _timeStarted = SystemClock.elapsedRealtime();
    }

    private String checkVoicing(String s){
        if (s.length() == 1 && Character.getType(s.charAt(0)) == Character.NON_SPACING_MARK){
            s = VOICING_PLACEHOLDER + s;
        }
        return s;
    }

    public String sessionTime(){
        long time = ((_timeFinished == 0 ? SystemClock.elapsedRealtime() : _timeFinished) - _timeStarted) / 1000;
        return String.format("%02d:%02d", time / 60, time % 60);
    }

    public void handleClick(int pos) {
            if (_clicked1 == NONE) {
                _clicked1 = pos;
            } else if (_clicked2 == NONE && pos != _clicked1) {
                _clicked2 = pos;
            }
            match();
    }

    public void handleGuessChanged(String newGuess){
        if (!_guess.equals(newGuess)) {
            _guess = newGuess;
            doMatch();
        }
    }

    private void resetClicks() {
        _clicked1 = _clicked2 = NONE;
    }

    private void match() {
        if (_mode == MODE_ALPHABET) {
            if (_clicked2 != NONE &&  _matches.get(_clicked1).equals(_matches.get(_clicked2))) {
                _items.set(_clicked1, "");
                _items.set(_clicked2, "");

                _matched++;
            } else if (_clicked2 == NONE) {
                return;
            } else {
                _mistaken++;
            }

        } else {
            String s = _items.get(_clicked1);
            if (s.startsWith(VOICING_PLACEHOLDER)){
                s = s.substring(VOICING_PLACEHOLDER.length());
            }
            _guess += s;
        }

        doMatch();
    }

    private void doMatch(){
        _listener.onMatched();
        if (isFinished()){
            _timeFinished = SystemClock.elapsedRealtime();
        }
        resetClicks();
    }

    public String hint(){
        int i;
        for (i = 0; i < _answer.length(); i++) {
            if (_guess.length() == i || _guess.charAt(i) != _answer.charAt(i)){
                _guess = _answer.substring(0, i + 1);
                break;
            }
        }
        if (i == _answer.length()) {
            _guess = _answer;
        }
        _hints++;
        _listener.onMatched();
        return _guess;
    }

    public boolean isFinished() {
        return _finished;
    }
    public boolean checkFinished(){
        if (_mode == MODE_ALPHABET){
            _finished = _matched == _matches.size() / 2;
        } else {
            _finished = getSimplifiedText(_guess).equals(_answer);
        }

        if (_finished) {
            dispatchAd();
        }
        return _finished;
    }

    public void undoGuess(){
        if(_guess.length() == 0){
            return;
        }
        _guess = _guess.substring(0, _guess.length() - 1);
        _listener.onMatched();
    }

    public void clearGuess(){
        _guess = "";
        _listener.onMatched();
    }

    public boolean isRightToLeft(){
        return _rtl;
    }
}