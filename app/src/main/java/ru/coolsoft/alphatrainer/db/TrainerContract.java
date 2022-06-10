package ru.coolsoft.alphatrainer.db;

import android.provider.BaseColumns;

/**
 * Created by Bobby© on 12.04.2015.
 * Contract class that defines application database structure
 */
public final class TrainerContract {
    /**
     * Contains definitions of such entities as Languages and Levels
     */
    public class Entities implements BaseColumns {
        @DbTable
        public static final String TABLE_NAME = "Entities";

        @DbField(type = "TEXT")
        public static final String COLUMN_LANG_NAME = "Name";
        @DbField(type = "INTEGER", default_value = "0")//boolean
        public static final String COLUMN_LANG_TRAINABLE = "Trainable";
        @DbField(type = "INTEGER", default_value = "0")//boolean
        public static final String COLUMN_LANG_DESCRIPTIVE = "Descriptive";
        @DbField(type = "INTEGER", default_value = "0")//boolean
        public static final String COLUMN_LEVEL = "Level";
    }

    public class Alphabets implements BaseColumns {
        @DbTable
        public static final String TABLE_NAME = "Alphabets";

        @DbField (primary = true)
        public static final String COLUMN_TARGET_LANG_ID = "LangId";
        @DbField (primary = true)
        public static final String COLUMN_TRANSCRIPTION_LANG_ID = "ScriptLangId";
        @DbField (type = "TEXT")
        public static final String COLUMN_SYMBOL = "Symbol";
        @DbField (default_value = "" + TrainerDbHelper.DEFAULT_LEVEL)
        public static final String COLUMN_SYMBOL_AUX = "Auxiliary";
    }

    /**
     * Positive IDs correspond to words in Dictionary.
     * Spells with ID = -1 correspond to Self names of Entities (Languages and Levels)
     */
    public class Spells implements BaseColumns {
        @DbTable
        public static final String TABLE_NAME = "Spells";

        @DbField (primary = true)
        public static final String COLUMN_TARGET_LANG_ID = "LangId";
        @DbField (primary = true)
        public static final String COLUMN_SPELL_LANG_ID = "SpellLangId";
        @DbField (type = "TEXT")
        public static final String COLUMN_SPELL = "Spell";
    }

    public class Dictionary implements BaseColumns {
        @DbTable
        public static final String TABLE_NAME = "Dict";

        @DbField (primary = true)
        public static final String COLUMN_LANG_ID = "LangId";
        @DbField (type = "TEXT")
        public static final String COLUMN_WORD = "Word";
        @DbField
        public static final String COLUMN_LEVEL = "LevelId";
    }

    /**
     * Big Red Gown should be in "Tea" and in "Clothes"
     * so we may create a pseudo-category for it
     * that is included in these both levels.
     * Node ID _id = 0 is reserved for Parent_Id of root levels
     */
    public class Levels implements BaseColumns {
                @DbTable
        public static final String TABLE_NAME = "Levels";

        /*@DbField (primary = true)
        public static final String COLUMN_NODE_ID = "NodeId";*/ //_id is the node ID
        @DbField (primary = true)
        public static final String COLUMN_PARENT_ID = "Parent_id";
        @DbField (primary = true)
        public static final String COLUMN_LANG_ID = "LangId";
        @DbField
        public static final String COLUMN_LEVEL = "LevelId";
        @DbField (default_value = "1")
        public static final String COLUMN_ENABLED = "Enabled";
    }

    public class Hackers {
        @DbTable
        public static final String TABLE_NAME = "ForThoseWhoHack";

        @DbField (type = "TEXT")
        public static final String COLUMN_INFO = "Info";
    }
}
