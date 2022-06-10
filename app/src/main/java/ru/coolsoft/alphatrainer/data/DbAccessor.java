package ru.coolsoft.alphatrainer.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

import ru.coolsoft.alphatrainer.TrainerApplication;
import ru.coolsoft.alphatrainer.db.TrainerContract;
import ru.coolsoft.alphatrainer.db.TrainerDbHelper;

import android.util.*;

/**
 * Created by Bobby© on 17.04.2015.
 * Database accessor class
 */
public class DbAccessor {
	private final static String TAG ="DbAccessor";
    private static SQLiteDatabase _db;
    //public final static int LANG_DETECT = -1;

    private static void fCopy(File fileSource, File fileTarget){
        FileInputStream streamSource;
        FileOutputStream streamTarget;
        try {
            streamSource = new FileInputStream(fileSource);
            streamTarget = new FileOutputStream(fileTarget);
            fCopyFstreamAndClose(streamSource, streamTarget);
        } catch (Exception e){
            Log.w(TAG, "failed to copy DB file", e);
        }
    }
    private static void fCopyFd(FileDescriptor fileSource, FileOutputStream streamTarget){
        FileInputStream streamSource;
        try {
            streamSource = new FileInputStream(fileSource);
            fCopyFstreamAndClose(streamSource, streamTarget);
        } catch (Exception e){
            Log.w(TAG, "failed to copy DB from descriptor", e);
        }
    }
    private static void fCopyFstreamAndClose(FileInputStream streamSource, FileOutputStream streamTarget){
        try {
            FileChannel chSource = streamSource.getChannel();
            FileChannel chTarget = streamTarget.getChannel();

            long start = 0;
            final long size = chSource.size();
            while (start < size) {
                start = chSource.transferTo(start, size, chTarget);
            }
        } catch (Exception e){
            Log.w(TAG, "failed to copy streamed DB", e);
        } finally {
            try {
                streamTarget.close();
            } catch (IOException e) {
                Log.d(TAG, "failed to close target stream", e);
            }
            try {
                streamSource.close();
            } catch (IOException e) {
                Log.d(TAG, "failed to close source stream", e);
            }
        }
    }
    private static void fCopyStreamAndClose(InputStream streamSource, FileOutputStream streamTarget){
        final int kBufferSize = 4096;
        try {
            byte[] buf = new byte[kBufferSize];
            int bytesRead;
            while((bytesRead = streamSource.read(buf)) > -1){
                streamTarget.write(buf, 0, bytesRead);
            }
        } catch (Exception e){
            Log.w(TAG, "failed to copy streamed DB", e);
        } finally {
            try {
                streamTarget.close();
            } catch (IOException e) {
                Log.d(TAG, "failed to close target stream", e);
            }
            try {
                streamSource.close();
            } catch (IOException e) {
                Log.d(TAG, "failed to close source stream", e);
            }
        }
    }
    private static SQLiteDatabase db(){
        if (_db == null) {
            Context c = TrainerApplication.app();
            File fileExt = new File(c.getExternalFilesDir(null), TrainerDbHelper.DB_NAME);
            File fileInt = c.getDatabasePath(TrainerDbHelper.DB_NAME);

            if (fileExt.exists()) {
                //restore custom DB if it's newer
                if (fileExt.lastModified() > fileInt.lastModified()) {
                    fCopy(fileExt, fileInt);
                }
            } else if (!fileInt.exists()){
                //restore DB from assets if exists
                try{
                    fileInt.getParentFile().mkdirs();
                    //fileInt.createNewFile();
                    fCopyStreamAndClose(
                            TrainerApplication.app().getAssets().open(TrainerDbHelper.DB_NAME)
                            , new FileOutputStream(fileInt));

                    fileExt.getParentFile().mkdirs();
                    //fileExt.createNewFile();
                    fCopyStreamAndClose(
                            TrainerApplication.app().getAssets().open(TrainerDbHelper.DB_NAME)
                            , new FileOutputStream(fileExt));
                } catch (Exception e){
                    Log.w(TAG, "failed to open asset", e);
                }
            }

            _db = new TrainerDbHelper(c).getReadableDatabase();
            //backup generated DB if backup lacks - EVEN FOR DEBUG VERSION
            if (_db != null && !fileExt.exists()) {
                fCopy(new File(_db.getPath()), fileExt);
            }
        }
        return  _db;
    }

    @SafeVarargs
    private static ArrayList<String> getArray(String sql
            /*, StringBuffer outParam*/, final ArrayList<String>... outArrays
            /*, String ... args*/){
        Cursor c = db().rawQuery(sql, null/* args*/);
		if(c.getCount()==0){
			Log.d(TAG, "No items in " + sql);
		}

        ArrayList<String> array = new ArrayList<>();
        if(c.moveToFirst()){
            /*if (outParam != null){
                outParam.append(c.getString(1));
            }*/
            do {
                if (outArrays != null) {
                    for (int i = 0; i < outArrays.length; i++) {
                        if (outArrays[i] != null) {
                            outArrays[i].add(c.getString(i + 1));
                        }
                    }
                }
                array.add(c.getString(0));
            } while (c.moveToNext());
        }
        c.close();
        return array;
    }

    public static ArrayList<String> trainables(
            final ArrayList<String> outIDs
            , final ArrayList<String> outDescriptions){
		//Log.d(TAG,"trainables");

        //noinspection unchecked
        return getArray(
                "SELECT " + TrainerContract.Entities.COLUMN_LANG_NAME
                        + ", l." + TrainerContract.Entities._ID
                        + ", " + TrainerContract.Spells.COLUMN_SPELL
                + " FROM " + TrainerContract.Entities.TABLE_NAME
                    + " l LEFT JOIN (SELECT " + TrainerContract.Spells.COLUMN_TARGET_LANG_ID
                        + ", " + TrainerContract.Spells.COLUMN_SPELL
                        + ", min(" + TrainerContract.Entities.COLUMN_LANG_DESCRIPTIVE
                    + ") FROM " + TrainerContract.Spells.TABLE_NAME
                        + " mins INNER JOIN " + TrainerContract.Entities.TABLE_NAME
                        + " minl ON minl." + TrainerContract.Entities._ID + " = mins." + TrainerContract.Spells.COLUMN_SPELL_LANG_ID
                        + " AND mins." + TrainerContract.Spells._ID + " = -1"
                        + " GROUP BY " + TrainerContract.Spells.COLUMN_TARGET_LANG_ID
                    + ") s on s." + TrainerContract.Spells.COLUMN_TARGET_LANG_ID + " = l._id"
                + " WHERE " + TrainerContract.Entities.COLUMN_LANG_TRAINABLE + " = 1 "
                /*, null*/, outIDs, outDescriptions);
    }

    public static ArrayList<String> descriptives(final ArrayList<String> outIDs){
		//Log.d(TAG,"descriptives");

        //noinspection unchecked
        return getArray("SELECT " + TrainerContract.Entities.COLUMN_LANG_NAME
                + ", " + TrainerContract.Entities._ID
                + " FROM " + TrainerContract.Entities.TABLE_NAME
                + " WHERE " + TrainerContract.Entities.COLUMN_LANG_DESCRIPTIVE + " <> 0"
                + " ORDER BY " + TrainerContract.Entities.COLUMN_LANG_DESCRIPTIVE/*, null*/, outIDs);
    }

    static ArrayList<String> alphas(int trainedLang, boolean withObsolete, boolean aux
            , final ArrayList<String> outLetterNames) {
		//Log.d(TAG,"alphas");
        StringBuilder sql = new StringBuilder("SELECT a." + TrainerContract.Alphabets.COLUMN_SYMBOL);
        if (outLetterNames != null) {
            sql.append(", mins." + TrainerContract.Alphabets.COLUMN_SYMBOL);//for outLetterNames
        }
        sql.append(" FROM " + TrainerContract.Alphabets.TABLE_NAME)
                .append(" a INNER JOIN " + TrainerContract.Entities.TABLE_NAME)
                .append(" l ON a." + TrainerContract.Alphabets.COLUMN_TARGET_LANG_ID)
                .append("=l." + TrainerContract.Entities._ID);

        if (outLetterNames != null) {
            sql.append(" INNER JOIN (SELECT mina." + TrainerContract.Alphabets._ID)
                    .append(", mina." + TrainerContract.Alphabets.COLUMN_TARGET_LANG_ID)
                    .append(    ", min(minl." + TrainerContract.Entities.COLUMN_LANG_DESCRIPTIVE)
                    .append(    ") AS d, mina." + TrainerContract.Alphabets.COLUMN_SYMBOL)
                    .append(" FROM " + TrainerContract.Alphabets.TABLE_NAME)
                    .append(    " mina INNER JOIN " + TrainerContract.Entities.TABLE_NAME)
                    .append(    " minl ON mina." + TrainerContract.Alphabets.COLUMN_TRANSCRIPTION_LANG_ID)
                    .append(    "=minl." + TrainerContract.Entities._ID)
                    .append(    " AND mina." + TrainerContract.Alphabets.COLUMN_TRANSCRIPTION_LANG_ID)
                    .append(    " <> ").append(trainedLang)
                    .append(    " GROUP BY mina." + TrainerContract.Alphabets._ID)
                    .append(    ", mina." + TrainerContract.Alphabets.COLUMN_TARGET_LANG_ID)
                    .append(") AS mins ON mins." + TrainerContract.Alphabets._ID)
                    .append(" = a." + TrainerContract.Alphabets._ID)
                    .append(" AND a." + TrainerContract.Alphabets.COLUMN_TARGET_LANG_ID)
                    .append(" = mins." + TrainerContract.Alphabets.COLUMN_TARGET_LANG_ID)
                    .append(" AND a.");
        } else {
            sql.append(" WHERE a.");
        }

        sql.append(TrainerContract.Alphabets.COLUMN_TRANSCRIPTION_LANG_ID)
                .append("=").append(trainedLang);

        sql.append(" AND (a." + TrainerContract.Alphabets.COLUMN_SYMBOL_AUX)
                .append(aux ? " < 2 " : " = 0");
        if (withObsolete){
            sql.append(" OR a." + TrainerContract.Alphabets.COLUMN_SYMBOL_AUX + " = 2");
        }

        sql.append(") ORDER BY a." + TrainerContract.Alphabets._ID);

        //noinspection NullArgumentToVariableArgMethod
        return getArray(sql.toString()/*, outLangBase*/, outLetterNames);
    }

    /**
     *
     * @param langTrain
     * @param answers
     * @return
     */
    static ArrayList<String> spell(int langTrain, final ArrayList<String> answers){
        //Log.d(TAG, "spell");

        //noinspection unchecked
        return getArray("SELECT spell." + TrainerContract.Spells.COLUMN_SPELL
                + ", quest." + TrainerContract.Dictionary.COLUMN_WORD
                + " FROM " + TrainerContract.Dictionary.TABLE_NAME
                + " AS quest INNER JOIN (SELECT s." + TrainerContract.Spells._ID
                + ", s." + TrainerContract.Spells.COLUMN_TARGET_LANG_ID
                + ", s." + TrainerContract.Spells.COLUMN_SPELL
                + ", min(l." + TrainerContract.Entities.COLUMN_LANG_DESCRIPTIVE
                + ") FROM " + TrainerContract.Spells.TABLE_NAME
                + " s INNER JOIN " + TrainerContract.Entities.TABLE_NAME
                + " l ON s." + TrainerContract.Spells.COLUMN_SPELL_LANG_ID + "=l." + TrainerContract.Entities._ID
                + " AND s." + TrainerContract.Spells.COLUMN_TARGET_LANG_ID + "<>s." + TrainerContract.Spells.COLUMN_SPELL_LANG_ID
                + " GROUP BY s." + TrainerContract.Spells._ID
                + ", s." + TrainerContract.Spells.COLUMN_TARGET_LANG_ID
                + ") AS spell ON quest." + TrainerContract.Dictionary._ID + "=spell." + TrainerContract.Spells._ID
                + " AND spell." + TrainerContract.Spells._ID + ">=0"
                + " AND quest." + TrainerContract.Dictionary.COLUMN_LANG_ID + "=" + "spell." + TrainerContract.Spells.COLUMN_TARGET_LANG_ID
//                + " AND quest." + TrainerContract.Spells.COLUMN_TARGET_LANG_ID + "=" + "quest." + TrainerContract.Spells.COLUMN_SPELL_LANG_ID
                + " AND quest." + TrainerContract.Dictionary.COLUMN_LANG_ID + "=" + String.valueOf(langTrain)/*, null*/, answers);
    }

    static ArrayList<String> tran(int langTrain, final ArrayList<String> answers){
        //Log.d(TAG, "tran");
        String sLangTrain = String.valueOf(langTrain);
        String sql = new StringBuilder("SELECT DISTINCT meaning.")
                .append(TrainerContract.Dictionary.COLUMN_WORD)
                .append(", quest.")
                .append(TrainerContract.Dictionary.COLUMN_WORD)//for answers
                .append(" FROM ")
                .append(TrainerContract.Dictionary.TABLE_NAME)
                .append(" AS quest INNER JOIN (SELECT t.")
                .append(    TrainerContract.Dictionary._ID)
                .append(", t.")
                .append(    TrainerContract.Dictionary.COLUMN_WORD)
                .append(    ", min(l.")
                .append(TrainerContract.Entities.COLUMN_LANG_DESCRIPTIVE)
                .append(    ") FROM ")
                .append(    TrainerContract.Dictionary.TABLE_NAME)
                .append(    " t INNER JOIN ")
                .append(    TrainerContract.Entities.TABLE_NAME)
                .append(    " l ON t.")
                .append(    TrainerContract.Dictionary.COLUMN_LANG_ID)
                .append(    "=l.")
                .append(    TrainerContract.Entities._ID)
                .append(    " AND t.")
                .append(    TrainerContract.Dictionary.COLUMN_LANG_ID)
                .append(    "<>")
                .append(    sLangTrain)
                .append(    " AND l.")
                .append(    TrainerContract.Entities.COLUMN_LANG_DESCRIPTIVE)
                .append(    ">0")
                .append(" GROUP BY t.")
                .append(    TrainerContract.Dictionary._ID)
                .append(") AS meaning ON quest.")
                .append(TrainerContract.Dictionary._ID)
                .append("=meaning.")
                .append(TrainerContract.Dictionary._ID)
                //Probably another join by min() will be needed on some devices
                .append(" AND quest.")
                .append(TrainerContract.Dictionary.COLUMN_LANG_ID)
                .append("=")
                .append(sLangTrain)
                //Filer by enabled levels
                .append(" INNER JOIN ")
                .append(TrainerContract.Levels.TABLE_NAME)
                .append(" l1 ON quest.")
                .append(TrainerContract.Dictionary.COLUMN_LEVEL)
                .append(" = l1.")
                .append(TrainerContract.Levels.COLUMN_LEVEL)
                .append(" AND quest.")
                .append(TrainerContract.Dictionary.COLUMN_LANG_ID)
                .append(" = l1.")
                .append(TrainerContract.Dictionary.COLUMN_LANG_ID)
                .append(" AND l1.")
                .append(TrainerContract.Levels.COLUMN_ENABLED)
                .append(" LEFT JOIN ")
                .append(TrainerContract.Levels.TABLE_NAME)
                .append(" l2 ON l1.")
                .append(TrainerContract.Levels.COLUMN_PARENT_ID)
                .append(" = l2._id AND l1.")
                .append(TrainerContract.Dictionary.COLUMN_LANG_ID)
                .append(" = l2.")
                .append(TrainerContract.Dictionary.COLUMN_LANG_ID)
                .append(" LEFT JOIN ")
                .append(TrainerContract.Levels.TABLE_NAME)
                .append(" l3 ON l2.")
                .append(TrainerContract.Levels.COLUMN_PARENT_ID)
                .append(" = l3._id AND l2.")
                .append(TrainerContract.Dictionary.COLUMN_LANG_ID)
                .append(" = l3.")
                .append(TrainerContract.Dictionary.COLUMN_LANG_ID)
                .append(" WHERE (l2.")
                .append(TrainerContract.Levels.COLUMN_ENABLED)
                .append(" IS NULL OR l2.")
                .append(TrainerContract.Levels.COLUMN_ENABLED)
                .append(") AND (l3.")
                .append(TrainerContract.Levels.COLUMN_ENABLED)
                .append(" IS NULL OR l3.")
                .append(TrainerContract.Levels.COLUMN_ENABLED)
                .append(")")
                .toString();

        //noinspection unchecked
        return getArray(sql/*, null*/, answers);
    }

    public static ArrayList<String> levels(String langId, final ArrayList<String> outIds, final Set<String> outEnabledLevels){
        ArrayList<String> ids = new ArrayList<>();
        ArrayList<String> parentIds = new ArrayList<>();
        ArrayList<String> enables = new ArrayList<>();
        ArrayList<String> names = getArray("SELECT " + TrainerContract.Spells.COLUMN_SPELL
                + ", l." + TrainerContract.Levels._ID
                + ", l." + TrainerContract.Levels.COLUMN_PARENT_ID
                + ", l." + TrainerContract.Levels.COLUMN_ENABLED
                + " FROM (SELECT sp." + TrainerContract.Spells._ID
                + "     , sp." + TrainerContract.Spells.COLUMN_TARGET_LANG_ID
                + "     , sp." + TrainerContract.Spells.COLUMN_SPELL
                + "     , MIN(e." + TrainerContract.Entities.COLUMN_LANG_DESCRIPTIVE
                + "     ) FROM " + TrainerContract.Spells.TABLE_NAME
                + "     sp INNER JOIN " + TrainerContract.Entities.TABLE_NAME
                + "     e ON sp." + TrainerContract.Spells.COLUMN_SPELL_LANG_ID
                + "     = e." + TrainerContract.Entities._ID
                + "     GROUP BY sp." + TrainerContract.Spells._ID
                + "     , sp." + TrainerContract.Spells.COLUMN_TARGET_LANG_ID
                + ") AS s INNER JOIN " + TrainerContract.Levels.TABLE_NAME
                + " l on l." + TrainerContract.Levels.COLUMN_LEVEL
                + " = s." + TrainerContract.Spells.COLUMN_TARGET_LANG_ID
                + " AND s." + TrainerContract.Spells._ID
                + " = -1 AND l." + TrainerContract.Levels.COLUMN_LANG_ID
                + " = " + langId

                + " LEFT JOIN " + TrainerContract.Levels.TABLE_NAME
                + " l2 on l2." + TrainerContract.Levels.COLUMN_LANG_ID
                + " = l." + TrainerContract.Levels.COLUMN_LANG_ID
                + " AND l2." + TrainerContract.Levels._ID
                + " = l." + TrainerContract.Levels.COLUMN_PARENT_ID

                + " WHERE l2." + TrainerContract.Levels.COLUMN_PARENT_ID
                + " IS NULL OR l2." + TrainerContract.Levels.COLUMN_PARENT_ID + " = 0"

                + " ORDER BY l." + TrainerContract.Levels.COLUMN_PARENT_ID
                + ", l." + TrainerContract.Levels._ID
                /*, null*/, ids, parentIds, enables
        );

        //convert Enables 1/0 into a list of Enabled 1s
        for (int i = 0; i < enables.size(); i++){
            if (enables.get(i).equals("1")) {
                outEnabledLevels.add(ids.get(i));
            }
        }

        //Sort hierarchically
        for (int i = 0; i < names.size() - 1; i++){
            int moved = 0;
            for (int j = i + 1; j < names.size(); j++){
                if (parentIds.get(j).equals(ids.get(i))){
                    //increment desired position only if it's a child
                    if (i + ++moved != j){
                        names.add(i + moved, "\t\t" + names.get(j));
                        names.remove(j + 1);

                        //for further cycles
                        ids.add(i + moved, ids.get(j));
                        ids.remove(j + 1);

                        parentIds.add(i + moved, parentIds.get(j));
                        parentIds.remove(j + 1);
                    } else {
                        names.set(j, "\t\t" + names.get(j));
                    }
                }
            }
        }

        outIds.addAll(ids);
        return names;
    }

    public static void updateDescriptives(LinkedList<Long> precedence) {
        int i = 0;
        for (Long id : precedence) {
            ContentValues values = new ContentValues();
            values.put(TrainerContract.Entities.COLUMN_LANG_DESCRIPTIVE, ++i);
            db().update(TrainerContract.Entities.TABLE_NAME
                    , values
                    , TrainerContract.Entities._ID + " = " + id
                    , null
            );
        }
    }

    public static void updateLevels(String langId, Set<String> values) {
        StringBuilder set = new StringBuilder("(");
        for (String id : values) {
            if (!set.toString().equals("(")){
                set.append(", ");
            }
            set.append(id);
        }
        set.append(")");

        ContentValues cv = new ContentValues();
        cv.put(TrainerContract.Levels.COLUMN_ENABLED, 1);
        db().update(TrainerContract.Levels.TABLE_NAME
                , cv
                , TrainerContract.Levels.COLUMN_LANG_ID + " = " + langId
                        + " AND " + TrainerContract.Levels._ID + " IN " + set
                , null
        );

        cv = new ContentValues();
        cv.put(TrainerContract.Levels.COLUMN_ENABLED, 0);
        db().update(TrainerContract.Levels.TABLE_NAME
                , cv
                , TrainerContract.Levels.COLUMN_LANG_ID + " = " + langId
                + " AND " + TrainerContract.Levels._ID + " NOT IN " + set
                , null
        );
    }

    static String firstScript() {
        return getArray("SELECT " + TrainerContract.Entities._ID
                + " FROM " + TrainerContract.Entities.TABLE_NAME
                + " WHERE " + TrainerContract.Entities.COLUMN_LANG_DESCRIPTIVE
                + " > 0 ORDER BY  " + TrainerContract.Entities.COLUMN_LANG_DESCRIPTIVE
                /*, null*/).get(0);
    }
}