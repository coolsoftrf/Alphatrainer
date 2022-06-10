package ru.coolsoft.alphatrainer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
//import android.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by Bobby© on 12.04.2015.
 * Database helper class. Implements getters and setters for application data entities
 */
public class TrainerDbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "Trainer.db";
    public static final int DEFAULT_LEVEL = 0;

    //private static int DB_VER = 1; //pre-release
    //private static final int DB_VER = 2; //first release
    private static final int DB_VER = 3; //training levels, spells refer to Dictionary (former Translations)

    private static final int LANG_RU = 1;
    private static final int LANG_EN = 2;
    private static final int LANG_KANA = 3;
    private static final int LANG_HIRAGANA = 4;
    private static final int LANG_KATAKANA = 5;
    private static final int LANG_HEBREW = 6;
    private static final int LANG_GEO = 7;
    private static final int LANG_GEO_CAP = 8;
    private static final int LANG_GEO_LOW = 9;

    private static final int[] TRAINABLES = new int[]{
            0, 0,   0, 1, 1,  1,   0, 1, 1
    };

    public TrainerDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    private class DbFieldInfo{
        final boolean _primary;
        final String _type;
        final String _name;
        final boolean _notnull;
        final String _default;

        DbFieldInfo(boolean primary, String type, String name, boolean notNull, String defVal){
            _primary = primary;
            _type = type;
            _name = name;
            _notnull = notNull;
            _default = defVal;
        }
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        initStructure(db);
        initData(db);
    }

    private void initStructure(SQLiteDatabase db) {
        //final String TAG = "db_structure";
        //Class contract = TrainerContract.class;
        Class[] tables = TrainerContract.class.getDeclaredClasses();
        //Log.d(TAG, "declared classes found " + tables.length);
        for (Class table : tables) {
            //Log.d(TAG, "table class " + table.getSimpleName());
            Field[] fields = table.getDeclaredFields();
            String tName = "";
            ArrayList<DbFieldInfo> dbFields = new ArrayList<>();
            StringBuilder key = new StringBuilder();

            for (Field field : fields) {
                Annotation ans[] = field.getDeclaredAnnotations();
                for (Annotation an : ans) {
                    //Log.d(TAG, "annotation " + an.toString());
                    if (an instanceof DbTable){
                        try {
                            tName = (String) field.get(null);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else if (an instanceof DbField){
                        try {
                            dbFields.add(new DbFieldInfo(
                                            ((DbField) an).primary()
                                            ,((DbField) an).type()
                                            , (String)field.get(null)
                                            ,((DbField) an).notnull()
                                            , ((DbField) an).default_value())
                            );
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            //Log.d(TAG, "tname " + tName + " fields " + dbFields.size());
            if (!tName.equals("") && dbFields.size() > 0){
                StringBuilder sql = new StringBuilder("CREATE TABLE ")
                        .append(tName)
                        .append(" (")
                        .append(BaseColumns._ID)
                        .append(" INTEGER");
                for (DbFieldInfo dbField : dbFields) {
                    sql.append(", ")
                            .append(dbField._name)
                            .append(" ")
                            .append(dbField._type);
                    if (dbField._primary){
                        key.append(", ").append(dbField._name);
                    }

                    if (!dbField._default.equals("")) {
                        sql.append(" DEFAULT ")
                                .append(dbField._default);
                    } else if (dbField._notnull){
                        sql.append(" NOT NULL");
                    }
                }
                sql.append(", PRIMARY KEY (")
                    .append(BaseColumns._ID)
                    .append(key)
                    .append("))");
                //Log.d("sql", sql.toString());
                db.execSQL(sql.toString());
            }//else the table definition is incorrect
        }
    }

    private void initData(SQLiteDatabase db) {

        initHackers(db);

        /**
         * Lang order Must comply to LANG_ constants
         */
        final String[] langs = new String[]{
                "Русский", "English"
                , "日本語の仮名","ひらがな", "カタカナ"
                , "עברית"
                , "Georgian", "ႵႠႰႧႳႪႨ", "ქართული"
        };
        final int[] descriptives = new int[]{
                1, 2,   0, 3, 4,  5,   0, 6, 7
        };

        ContentValues values;
        //Languages
        for (int i = 0; i < langs.length; i++) {
            values = new ContentValues();
            values.put(BaseColumns._ID, i + 1);
            values.put(TrainerContract.Entities.COLUMN_LANG_NAME, langs[i]);
            values.put(TrainerContract.Entities.COLUMN_LANG_TRAINABLE, TRAINABLES[i]);
            values.put(TrainerContract.Entities.COLUMN_LANG_DESCRIPTIVE, descriptives[i]);

            db.insert(TrainerContract.Entities.TABLE_NAME, null, values);
        }

        initAlphabets(db);
        initLevels(db);
        initDictionary(db);
        initSpells(db);
    }

    private void initHackers(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(TrainerContract.Hackers.COLUMN_INFO
                , "If you read this please contact me. I'd like to work with those who hep to improve this and other apps");
        db.insert(TrainerContract.Hackers.TABLE_NAME, null, values);
    }

    private void initAlphabets(SQLiteDatabase db) {
        initJapanese(db);
        initHebrew(db);
        initGeorgian(db);
    }

    private void initJapanese(SQLiteDatabase db) {
        int i;
        ContentValues values;

        final String ROSIAGOJI[] = new String[]{
                "а", "и", "у", "э", "o",
                "кa", "ки", "ку", "кэ", "кo",
                "сa", "cи", "су", "сэ", "сo",
                "тa", "ти", "цу", "тэ", "тo",
                "нa", "ни", "ну", "нэ", "нo",
                "хa", "хи", "фу", "хэ", "хo",
                "мa", "ми", "му", "мэ", "мo",
                "рa", "ри", "ру", "рэ", "рo",
                "я", "ю", "ё",
                "вa", "вo", "н", "ву"
        };
        for (i = 0; i < ROSIAGOJI.length; i++) {
            values = new ContentValues();
            values.put(BaseColumns._ID, i);
            values.put(TrainerContract.Alphabets.COLUMN_TARGET_LANG_ID, LANG_KANA);
            values.put(TrainerContract.Alphabets.COLUMN_TRANSCRIPTION_LANG_ID, LANG_RU);
            values.put(TrainerContract.Alphabets.COLUMN_SYMBOL, ROSIAGOJI[i]);

            db.insert(TrainerContract.Alphabets.TABLE_NAME, null, values);
        }

        final String ROMAJI[] = new String[]{
                "a", "i", "u", "e", "o",
                "ka", "ki", "ku", "ke", "ko",
                "sa", "si", "su", "se", "so",
                "ta", "ti", "tu", "te", "to",
                "na", "ni", "nu", "ne", "no",
                "ha", "hi", "fu", "he", "ho",
                "ma", "mi", "mu", "me", "mo",
                "ra", "ri", "ru", "re", "ro",
                "ya", "yu", "yo",
                "wa", "wo", "n", "vu"
        };
        for (i = 0; i < ROMAJI.length; i++) {
            values = new ContentValues();
            values.put(BaseColumns._ID, i);
            values.put(TrainerContract.Alphabets.COLUMN_TARGET_LANG_ID, LANG_KANA);
            values.put(TrainerContract.Alphabets.COLUMN_TRANSCRIPTION_LANG_ID, LANG_EN);
            values.put(TrainerContract.Alphabets.COLUMN_SYMBOL, ROMAJI[i]);

            db.insert(TrainerContract.Alphabets.TABLE_NAME, null, values);
        }

        final String HIRAGANA[] = new String[]{
                "あ", "い", "う", "え", "お",
                "か", "き", "く", "け", "こ",
                "さ", "し", "す", "せ", "そ",
                "た", "ち", "つ", "て", "と",
                "な", "に", "ぬ", "ね", "の",
                "は", "ひ", "ふ", "へ", "ほ",
                "ま", "み", "む", "め", "も",
                "ら", "り", "る", "れ", "ろ",
                "や", "ゆ", "よ",
                "わ", "を", "ん"
        };
        for (i = 0; i < HIRAGANA.length; i++) {
            values = new ContentValues();
            values.put(BaseColumns._ID, i);
            values.put(TrainerContract.Alphabets.COLUMN_TARGET_LANG_ID, LANG_KANA);
            values.put(TrainerContract.Alphabets.COLUMN_TRANSCRIPTION_LANG_ID, LANG_HIRAGANA);
            values.put(TrainerContract.Alphabets.COLUMN_SYMBOL, HIRAGANA[i]);

            db.insert(TrainerContract.Alphabets.TABLE_NAME, null, values);
        }
        int max = i;

        final String HIRAGANA_PHONETICS[] = new String[]{
                "ぁ", "ぃ", "ぅ", "ぇ", "ぉ", "っ", "ゃ", "ゅ", "ょ", "ゎ", "゛", "゜"
        };
        for (i = 0; i < HIRAGANA_PHONETICS.length; i++) {
            values = new ContentValues();
            values.put(BaseColumns._ID, i + max);
            values.put(TrainerContract.Alphabets.COLUMN_TARGET_LANG_ID, LANG_KANA);
            values.put(TrainerContract.Alphabets.COLUMN_TRANSCRIPTION_LANG_ID, LANG_HIRAGANA);
            values.put(TrainerContract.Alphabets.COLUMN_SYMBOL, HIRAGANA_PHONETICS[i]);
            values.put(TrainerContract.Alphabets.COLUMN_SYMBOL_AUX, 1);

            db.insert(TrainerContract.Alphabets.TABLE_NAME, null, values);
        }

        final String KATAKANA[] = new String[]{
                "ア", "イ", "ウ", "エ", "オ",
                "カ", "キ", "ク", "ケ", "コ",
                "サ", "シ", "ス", "セ", "ソ",
                "タ", "チ", "ツ", "テ", "ト",
                "ナ", "ニ", "ヌ", "ネ", "ノ",
                "ハ", "ヒ", "フ", "ヘ", "ホ",
                "マ", "ミ", "ム", "メ", "モ",
                "ラ", "リ", "ル", "レ", "ロ",
                "ヤ", "ユ", "ヨ",
                "ワ", "ヲ", "ン", "ヴ"
        };
        for (i = 0; i < KATAKANA.length; i++) {
            values = new ContentValues();
            values.put(BaseColumns._ID, i);
            values.put(TrainerContract.Alphabets.COLUMN_TARGET_LANG_ID, LANG_KANA);
            values.put(TrainerContract.Alphabets.COLUMN_TRANSCRIPTION_LANG_ID, LANG_KATAKANA);
            values.put(TrainerContract.Alphabets.COLUMN_SYMBOL, KATAKANA[i]);

            db.insert(TrainerContract.Alphabets.TABLE_NAME, null, values);
        }
        max = i;

        final String KATAKANA_PHONETICS[] = new String[]{
                "ァ", "ィ", "ゥ", "ェ", "ォ", "ッ", "ャ", "ュ", "ョ", "ヮ", "ヵ", "ヶ", "゛", "゜", "ー"
        };
        for (i = 0; i < KATAKANA_PHONETICS.length; i++) {
            values = new ContentValues();
            values.put(BaseColumns._ID, i + max);
            values.put(TrainerContract.Alphabets.COLUMN_TARGET_LANG_ID, LANG_KANA);
            values.put(TrainerContract.Alphabets.COLUMN_TRANSCRIPTION_LANG_ID, LANG_KATAKANA);
            values.put(TrainerContract.Alphabets.COLUMN_SYMBOL, KATAKANA_PHONETICS[i]);
            values.put(TrainerContract.Alphabets.COLUMN_SYMBOL_AUX, 1);

            db.insert(TrainerContract.Alphabets.TABLE_NAME, null, values);
        }
    }

    private void initHebrew(SQLiteDatabase db) {
        int i;
        ContentValues values;

        final String HEBREW_RU[] = new String[]{
                "алеф", "бет", "гимель", "далет", "хэй", "вав", "зайн", "хет", "тет", "юд", "каф",
                "лямэд", "мем", "нун", "самех", "айн", "пэ", "цади", "куф", "реш", "шин", "тав"
        };
        for (i = 0; i < HEBREW_RU.length; i++) {
            values = new ContentValues();
            values.put(BaseColumns._ID, i);
            values.put(TrainerContract.Alphabets.COLUMN_TARGET_LANG_ID, LANG_HEBREW);
            values.put(TrainerContract.Alphabets.COLUMN_TRANSCRIPTION_LANG_ID, LANG_RU);
            values.put(TrainerContract.Alphabets.COLUMN_SYMBOL, HEBREW_RU[i]);

            db.insert(TrainerContract.Alphabets.TABLE_NAME, null, values);
        }

        final String HEBREW[] = new String[]{
                "א", "ב", "ג", "ד", "ה", "ו", "ז", "ח", "ט", "י", "כ",
                "ל", "מ", "נ", "ס", "ע", "פ", "צ", "ק", "ר", "ש", "ת"
        };
        for (i = 0; i < HEBREW.length; i++) {
            values = new ContentValues();
            values.put(BaseColumns._ID, i);
            values.put(TrainerContract.Alphabets.COLUMN_TARGET_LANG_ID, LANG_HEBREW);
            values.put(TrainerContract.Alphabets.COLUMN_TRANSCRIPTION_LANG_ID, LANG_HEBREW);
            values.put(TrainerContract.Alphabets.COLUMN_SYMBOL, HEBREW[i]);

            db.insert(TrainerContract.Alphabets.TABLE_NAME, null, values);
        }
        int max = i;

        final String HEBREW_PHONETICS[] = new String[]{
                "ך", "ם", "ן", "ף", "ץ"
        };
        for (i = 0; i < HEBREW_PHONETICS.length; i++) {
            values = new ContentValues();
            values.put(BaseColumns._ID, i + max);
            values.put(TrainerContract.Alphabets.COLUMN_TARGET_LANG_ID, LANG_HEBREW);
            values.put(TrainerContract.Alphabets.COLUMN_TRANSCRIPTION_LANG_ID, LANG_HEBREW);
            values.put(TrainerContract.Alphabets.COLUMN_SYMBOL, HEBREW_PHONETICS[i]);
            values.put(TrainerContract.Alphabets.COLUMN_SYMBOL_AUX, 1);

            db.insert(TrainerContract.Alphabets.TABLE_NAME, null, values);
        }
    }

    private void initGeorgian(SQLiteDatabase db) {
        int i;
        ContentValues values;

        final String GEO_RU[] = new String[]{
                "ан", "бан", "ган", "дон", "эн", "вин", "зэн", "тан", "ин", "к'ан", "лас", "ман", "нар",
                "он", "п'ар", "жар", "раэ", "сан", "т'ар", "ун", "пар", "кан", "ѓан", "qар", "шин", "чин",
                "цан", "дзил", "ц'ил", "ч'ар", "хан", "јан", "hаэ", "хэ", "хе", "ўe", "хар", "hоэ"
        };
        for (i = 0; i < GEO_RU.length; i++) {
            values = new ContentValues();
            values.put(BaseColumns._ID, i);
            values.put(TrainerContract.Alphabets.COLUMN_TARGET_LANG_ID, LANG_GEO);
            values.put(TrainerContract.Alphabets.COLUMN_TRANSCRIPTION_LANG_ID, LANG_RU);
            values.put(TrainerContract.Alphabets.COLUMN_SYMBOL, GEO_RU[i]);

            db.insert(TrainerContract.Alphabets.TABLE_NAME, null, values);
        }
        final String GEO_CAP[] = new String[]{
                "Ⴀ", "Ⴁ", "Ⴂ", "Ⴃ", "Ⴄ", "Ⴅ", "Ⴆ", "Ⴇ", "Ⴈ", "Ⴉ", "Ⴊ", "Ⴋ", "Ⴌ",
                "Ⴍ", "Ⴎ", "Ⴏ", "Ⴐ", "Ⴑ", "Ⴒ", "Ⴓ", "Ⴔ", "Ⴕ", "Ⴖ", "Ⴗ", "Ⴘ", "Ⴙ",
                "Ⴚ", "Ⴛ", "Ⴜ", "Ⴝ", "Ⴞ", "Ⴟ", "Ⴠ", "Ⴡ", "Ⴢ", "Ⴣ", "Ⴤ", "Ⴥ"
        };
        for (i = 0; i < GEO_CAP.length; i++) {
            values = new ContentValues();
            values.put(BaseColumns._ID, i);
            values.put(TrainerContract.Alphabets.COLUMN_TARGET_LANG_ID, LANG_GEO);
            values.put(TrainerContract.Alphabets.COLUMN_TRANSCRIPTION_LANG_ID, LANG_GEO_CAP);
            values.put(TrainerContract.Alphabets.COLUMN_SYMBOL, GEO_CAP[i]);

            db.insert(TrainerContract.Alphabets.TABLE_NAME, null, values);
        }
        final String GEO_LOW[] = new String[]{
                "ა", "ბ", "გ", "დ", "ე", "ვ", "ზ", "თ", "ი", "კ", "ლ", "მ", "ნ",
                "ო", "პ", "ჟ", "რ", "ს", "ტ", "უ", "ფ", "ქ", "ღ", "ყ", "შ", "ჩ",
                "ც", "ძ", "წ", "ჭ", "ხ", "ჯ", "ჰ", "ჱ", "ჲ", "ჳ", "ჴ", "ჵ"
        };
        for (i = 0; i < GEO_LOW.length; i++) {
            values = new ContentValues();
            values.put(BaseColumns._ID, i);
            values.put(TrainerContract.Alphabets.COLUMN_TARGET_LANG_ID, LANG_GEO);
            values.put(TrainerContract.Alphabets.COLUMN_TRANSCRIPTION_LANG_ID, LANG_GEO_LOW);
            values.put(TrainerContract.Alphabets.COLUMN_SYMBOL, GEO_LOW[i]);

            db.insert(TrainerContract.Alphabets.TABLE_NAME, null, values);
        }
    }

    private void initLevels(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        //Define a single test Level
        cv.put(TrainerContract.Entities._ID, DEFAULT_LEVEL); //referenced in Dictionary
        cv.put(TrainerContract.Entities.COLUMN_LANG_NAME, "Test");
        cv.put(TrainerContract.Entities.COLUMN_LEVEL, 1); //this record IS level

        db.insert(TrainerContract.Entities.TABLE_NAME, null, cv);

        //Provide name for the test Level
        cv = new ContentValues();
        cv.put(TrainerContract.Spells._ID, -1); //Self description ID
        cv.put(TrainerContract.Spells.COLUMN_TARGET_LANG_ID, DEFAULT_LEVEL);
        cv.put(TrainerContract.Spells.COLUMN_SPELL_LANG_ID, LANG_EN);
        cv.put(TrainerContract.Spells.COLUMN_SPELL, "Default mini");

        db.insert(TrainerContract.Spells.TABLE_NAME, null, cv);

        //Add the level [hierarchy] to Levels for each language
        for (int i = 0; i < TRAINABLES.length; i++) {
            if (TRAINABLES[i] == 1){
                ContentValues values = new ContentValues();
                values.put(TrainerContract.Levels._ID, 1);
                values.put(TrainerContract.Levels.COLUMN_PARENT_ID, 0);
                values.put(TrainerContract.Levels.COLUMN_LANG_ID, i + 1);
                values.put(TrainerContract.Levels.COLUMN_LEVEL, DEFAULT_LEVEL);

                db.insert(TrainerContract.Levels.TABLE_NAME, null, values);
            }
        }
    }

    private void initDictionary(SQLiteDatabase db) {
        final String WORDS[][][] = new String[][][]{
                //digits must correspond to descriptive language IDs
                {{"2", "good afternoon"}, {"4", "こんにちは"}},
                {{"1", "добрый вечер"}, {"4", "こんばんは"}},
                {{"1", "привет"}, {"2", "hello"}, {"5", "ハロー"}, {"6", "שלום"}, {"9", "გამარჯობა"}},
                {{"1", "солнце"}, {"2", "sun"}, {"4", "ひ"}, {"6", "חמה"}, {"9", "მზე"}},
                {{"1", "я"}, {"2", "I"},{"4", "わたし"},{"6", "אני"},{"9", "მე"}},
                {{"1", "Россия"}, {"2", "Russia"}, {"5", "ロシア"}}
        };
        ContentValues values;
        for (int word = 0; word < WORDS.length; word++) {
            String langs[][] = WORDS[word];
            for (String[] lang : langs) {
                values = new ContentValues();
                values.put(BaseColumns._ID, word);
                values.put(TrainerContract.Dictionary.COLUMN_LANG_ID, lang[0]);
                values.put(TrainerContract.Dictionary.COLUMN_WORD, lang[1]);
                values.put(TrainerContract.Dictionary.COLUMN_LEVEL, DEFAULT_LEVEL);

                db.insert(TrainerContract.Dictionary.TABLE_NAME, null, values);
            }
        }
    }

    private void initSpells(SQLiteDatabase db) {
        /**
         * Lang order must comply to "1" languages in TRAINABLES array
         * Digits must correspond to descriptive language IDs
         * Order of words must match the one in Dictionary
         */
       final String SPELL[][][][] = new String[][][][]{
                {
                        {{"1", "Японская хирагана"}, {"2", "Japanese hiragana"}} // id=-1
                        , {{"2", "konnichiwa"}, {"1", "коннитива"}}
                        , {{"2", "kombanwa"}}
                        , {}
                        , {{"1", "хи"}}
                        , {{"1", "ватаcи"}, {"2", "watashi"}}
                },
                {
                        {{"1", "Японская катакана"}, {"2", "Japanese katakana"}} // id=-1
                        , {}
                        , {}
                        , {{"2", "haro:"}}
                        , {}
                        , {}
                        , {{"2", "Roshia"}}
                },
                {
                        {{"1", "Иврит"}, {"2", "Hebrew"}} // id=-1
                        , {}
                        , {}
                        , {{"2", "shalom"}}
                        , {{"1", "хама"}}
                        , {{"2", "ani"}, {"4", "あに"}}
                },
                {
                        {{"1","Грузинский (заглавные)"}, {"2", "Georgian (capitals)"}}// id=-1
                },
                {
                        {{"1","Грузинский (строчные)"}, {"2", "Georgian (lowercase)"}} // id=-1
                        , {}
                        , {}
                        , {{"2","gamarjoba"}}
                        , {{"1","мзэ"}}
                        , {{"1","мэ"}, {"2","me"}}
                }
        };
        ContentValues values;
        int iTrainable = -1;
        for (String[][][] words : SPELL) {
            do iTrainable++; while (TRAINABLES[iTrainable] == 0);

            for (int word = 0; word < words.length; word++) {
                for (int spell = 0; spell < words[word].length; spell++) {
                    values = new ContentValues();
                    values.put(BaseColumns._ID, word - 1);//Start with id = -1 that identifies the language self-name
                    values.put(TrainerContract.Spells.COLUMN_TARGET_LANG_ID, iTrainable + 1);
                    values.put(TrainerContract.Spells.COLUMN_SPELL_LANG_ID, words[word][spell][0]);
                    values.put(TrainerContract.Spells.COLUMN_SPELL, words[word][spell][1]);

                    db.insert(TrainerContract.Spells.TABLE_NAME, null, values);
                }
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2){
            //Rename Languages to prevent backward compatibility to db version 1
            db.execSQL("ALTER TABLE Languages RENAME TO " + TrainerContract.Entities.TABLE_NAME);

            //Add hackers' info
            db.execSQL("CREATE TABLE " + TrainerContract.Hackers.TABLE_NAME
                    + " (" + TrainerContract.Hackers.COLUMN_INFO + " TEXT)");
            initHackers(db);
        }
        if (oldVersion < 3){
            if (oldVersion == 2) {
                //Rename Langs into Entities to reflect new semantics
                db.execSQL("ALTER TABLE Langs RENAME TO " + TrainerContract.Entities.TABLE_NAME);
            }
            //Add Level column
            db.execSQL("ALTER TABLE " + TrainerContract.Entities.TABLE_NAME
                    + " ADD COLUMN " + TrainerContract.Entities.COLUMN_LEVEL + " INTEGER DEFAULT " + DEFAULT_LEVEL);

            //Rename Translations into Dict to reflect new semantics
            db.execSQL("ALTER TABLE Translations RENAME TO " + TrainerContract.Dictionary.TABLE_NAME);

            //Add Training Level column
            db.execSQL("ALTER TABLE " + TrainerContract.Dictionary.TABLE_NAME
                    + " ADD COLUMN " + TrainerContract.Dictionary.COLUMN_LEVEL + " INTEGER DEFAULT " + DEFAULT_LEVEL);

            //Refactor Spells
            Cursor c = db.rawQuery("SELECT s." + TrainerContract.Spells._ID
                    + ", s." + TrainerContract.Spells.COLUMN_TARGET_LANG_ID
                    + ", s." + TrainerContract.Spells.COLUMN_SPELL_LANG_ID
                    + ", s." + TrainerContract.Spells.COLUMN_SPELL
                    + ", d." + TrainerContract.Dictionary._ID
                    + " FROM " + TrainerContract.Spells.TABLE_NAME
                    + " s INNER JOIN " + TrainerContract.Spells.TABLE_NAME
                    + " base ON base." + TrainerContract.Spells.COLUMN_SPELL_LANG_ID
                    + "=base." + TrainerContract.Spells.COLUMN_TARGET_LANG_ID
                    + " AND base." + TrainerContract.Spells.COLUMN_TARGET_LANG_ID
                    + "=s." + TrainerContract.Spells.COLUMN_TARGET_LANG_ID
                    + " AND base." + TrainerContract.Spells._ID
                    + "=s." + TrainerContract.Spells._ID
                    + " LEFT JOIN " + TrainerContract.Dictionary.TABLE_NAME
                    + " d ON d." + TrainerContract.Dictionary.COLUMN_WORD
                    + "=base." + TrainerContract.Spells.COLUMN_SPELL
                    + " ORDER BY d." + TrainerContract.Dictionary._ID
                    + ", s." + TrainerContract.Spells.COLUMN_TARGET_LANG_ID
                    + ", s." + TrainerContract.Spells.COLUMN_TARGET_LANG_ID
                    + "<>s." + TrainerContract.Spells.COLUMN_SPELL_LANG_ID
                    , null);

            Cursor cMaxId = db.rawQuery("SELECT MAX(" + TrainerContract.Dictionary._ID
                    + ") FROM " + TrainerContract.Dictionary.TABLE_NAME
                    , null
            );
            int maxId = -1;
            if (cMaxId.moveToFirst()){
                maxId = cMaxId.getInt(0);
            }
            cMaxId.close();

            if(c.moveToFirst()){
                do {
                    //At this moment the number of Spell _id"s must be less than the one of Dict
                    // or primary key violation occurs
                    int id; //Dictionary word id
                    int spellId = c.getInt(0);
                    int langId = c.getInt(1);
                    int spellLangId = c.getInt(2);
                    boolean isBase = langId == spellLangId;

                    if(c.isNull(4)){
                        if (isBase) {
                            ++maxId;
                            //This word is missing in Dictionary. Add it
                            ContentValues cv = new ContentValues();
                            cv.put(TrainerContract.Dictionary._ID, maxId);
                            cv.put(TrainerContract.Dictionary.COLUMN_LANG_ID, langId);
                            cv.put(TrainerContract.Dictionary.COLUMN_LEVEL, DEFAULT_LEVEL); //user level
                            cv.put(TrainerContract.Dictionary.COLUMN_WORD, c.getString(3));

                            db.insert(TrainerContract.Dictionary.TABLE_NAME, null, cv);
                        }
                        id = maxId;
                    } else {
                        id = c.getInt(4);
                    }

                    final String where = String.format(TrainerContract.Spells.COLUMN_TARGET_LANG_ID
                                    + "=%d AND " + TrainerContract.Spells.COLUMN_SPELL_LANG_ID
                                    + "=%d AND " + TrainerContract.Spells._ID + "=%d"
                            , langId, spellLangId, spellId
                    );
                    if (isBase){
                        //The word should already be in Dictionary. Remove it from Spells
                        db.delete(TrainerContract.Spells.TABLE_NAME, where, null);
                    } else {
                        //Update _id in Spells
                        ContentValues cv = new ContentValues();
                        cv.put(TrainerContract.Spells._ID, id);

                        db.update(TrainerContract.Spells.TABLE_NAME, cv, where, null);
                    }
                } while (c.moveToNext());
            }
            c.close();

            initLevels(db);
        }
        db.setVersion(DB_VER);
    }
}
