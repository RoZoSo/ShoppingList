package de.codeyourapp.shoppinglist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ShoppingMemoDbHelper extends SQLiteOpenHelper{

    private static final String LOG_TAG = ShoppingMemoDbHelper.class.getSimpleName();

    public static final String DB_NAME = "shopping_list.db"; // Name der Datenbank
    public static final int DB_VERSION = 2; // Version der Datenbank, upgrade-relevant

    public static final String TABLE_SHOPPING_LIST = "shopping_list"; // Name der Tabelle

    // Name der Spalten unserer Tabelle
    public static final String COLUMN_ID = "_id"; // primär-schlüssel ( eindeutig für jeden Eintrag)
    public static final String COLUMN_PRODUCT = "product";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_CHECKED = "checked";

    // definition des SQL-Strings mit den definierten Variablen
    public static final String SQL_CREATE =
            "CREATE TABLE " + TABLE_SHOPPING_LIST +
                    "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + // _id wird automatisch festgelegt
                    COLUMN_PRODUCT + " TEXT NOT NULL, " + // product muss gesetzt werden , darf nicht NULL sein
                    COLUMN_QUANTITY + " INTEGER NOT NULL, " + // quantity muss gesetzt werden, darf nicht NULL sein
                    COLUMN_CHECKED + " BOOLEAN NOT NULL DEFAULT 0);"; // set initial to false

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_SHOPPING_LIST;


    // Konstruktor
    public ShoppingMemoDbHelper(Context context) {
        //super(context, "PLATZHALTER_DATENBANKNAME", null, 1);
        super(context, DB_NAME, null, DB_VERSION); // Super-Konstruktor mit Konstanten
        Log.d(LOG_TAG, "DbHelper hat die Datenbank: " + getDatabaseName() + " erzeugt.");
    }

    // Die onCreate-Methode wird nur aufgerufen, falls die Datenbank noch nicht existiert
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.d(LOG_TAG, "Die Tabelle wird mit SQL-Befehl: " + SQL_CREATE + " angelegt.");
            db.execSQL(SQL_CREATE); // Tabelle wird erzeugt
        }
        // fehler abfangen, wenn try nicht funktioniert
        catch (Exception ex) {
            Log.e(LOG_TAG, "Fehler beim Anlegen der Tabelle: " + ex.getMessage());
        }
    }

    // this method will be called if the version number is greater than the current one
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "Die Tabelle mit Versionsnummer " + oldVersion + " wird entfernt.");
        db.execSQL(SQL_DROP);
        onCreate(db);

    }
}
