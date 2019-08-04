package de.codeyourapp.shoppinglist;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

public class ShoppingMemoDataSource {

    private static final String LOG_TAG = ShoppingMemoDataSource.class.getSimpleName();

    private SQLiteDatabase database;
    private ShoppingMemoDbHelper dbHelper;

    // Array für die Suchanfrage, definition mittels DBHelper
    private String[] columns = {
            ShoppingMemoDbHelper.COLUMN_ID,
            ShoppingMemoDbHelper.COLUMN_PRODUCT,
            ShoppingMemoDbHelper.COLUMN_QUANTITY,
            ShoppingMemoDbHelper.COLUMN_CHECKED
    };

    // Konstruktor
    public ShoppingMemoDataSource(Context context) {
        Log.d(LOG_TAG, "Unsere DataSource erzeugt jetzt den dbHelper.");
        dbHelper = new ShoppingMemoDbHelper(context);
    }

    // Öffnet die Datenbank-Verbindung
    public void open() {
        Log.d(LOG_TAG, "Eine Referenz auf die Datenbank wird jetzt angefragt.");
        database = dbHelper.getWritableDatabase(); // falls keine Datenbank vorhanden, erzeugt neue Datenbank
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten. Pfad zur Datenbank: " + database.getPath());
    }

    // Schließt die Datenbank-Verbindung
    public void close() {
        dbHelper.close(); //
        Log.d(LOG_TAG, "Datenbank mit Hilfe des DbHelpers geschlossen.");
    }

    // add an entry to database
    public ShoppingMemo createShoppingMemo(String product, int quantity) {
        // input vars are added to ContentValue
        ContentValues values = new ContentValues();
        values.put(ShoppingMemoDbHelper.COLUMN_PRODUCT, product);
        values.put(ShoppingMemoDbHelper.COLUMN_QUANTITY, quantity);

        // Datensatz wird in die Datenbank eingefügt ( Name der Tabelle, für leere values, Values)
        long insertId = database.insert(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST, null, values);

        // queries - suchanfragen
        // arg1 - name der Tabelle
        // arg2 - namen der spalten, welche zurück gegeben werden
        // arg3 - suchkriterien ( spalte + operator + Wert )
        // arg4-arg7 nicht genutzt, daher =null
        Cursor cursor = database.query(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,
                columns, ShoppingMemoDbHelper.COLUMN_ID + "=" + insertId,
                null, null, null, null);

        // Rückgabewert ist ein cursor - zeiger auf daten
        cursor.moveToFirst(); // erster Datensatz
        ShoppingMemo shoppingMemo = cursorToShoppingMemo(cursor);
        // cursor schließen
        cursor.close();

        return shoppingMemo;
    }

    // update an entry at database
    public ShoppingMemo updateShoppingMemo(long id, String newProduct, int newQuantity, boolean newChecked) {
        int intValueChecked = (newChecked)? 1 : 0; // boolean to int for SQL
        // input vars are added to ContentValue
        ContentValues values = new ContentValues();
        values.put(ShoppingMemoDbHelper.COLUMN_PRODUCT, newProduct);
        values.put(ShoppingMemoDbHelper.COLUMN_QUANTITY, newQuantity);
        values.put(ShoppingMemoDbHelper.COLUMN_CHECKED, intValueChecked);

        // Contentvalues are updated at position id
        database.update(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,
                values,
                ShoppingMemoDbHelper.COLUMN_ID + "=" + id,
                null);

        // get entered data
        Cursor cursor = database.query(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,
                columns, ShoppingMemoDbHelper.COLUMN_ID + "=" + id,
                null, null, null, null);

        cursor.moveToFirst();
        ShoppingMemo shoppingMemo = cursorToShoppingMemo(cursor);
        cursor.close();

        return shoppingMemo;
    }

    // delete an entry from database
    public void deleteShoppingMemo(ShoppingMemo shoppingMemo) {
        long id = shoppingMemo.getId();

        // delete entry where _ID = id
        database.delete(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,
                ShoppingMemoDbHelper.COLUMN_ID + "=" + id,
                null);

        Log.d(LOG_TAG, "Eintrag gelöscht! ID: " + id + " Inhalt: " + shoppingMemo.toString());
    }

    // Hilfsmethode - Wandelt Cursor in ShoppingMemo um
    private ShoppingMemo cursorToShoppingMemo(Cursor cursor) {
        // getColumnIndex - gibt den Index des Spaltennamens zurück
        int idIndex = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_ID);
        int idProduct = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_PRODUCT);
        int idQuantity = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_QUANTITY);
        int idChecked = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_CHECKED);

        // getString gibt den String des Indexes zurück
        String product = cursor.getString(idProduct);
        // getInt gibt den Integer des Indexes zurück
        int quantity = cursor.getInt(idQuantity);
        // getLong gibt den long int des Indexes zurück
        long id = cursor.getLong(idIndex);
        int intValueChecked = cursor.getInt(idChecked);

        boolean isChecked = (intValueChecked != 0); // transform to boolean back

        // Erzeugt das gewünschte shoppingMemo objekt mit dem cursor Datensatz
        ShoppingMemo shoppingMemo = new ShoppingMemo(product, quantity, id, isChecked);

        // Rückgabe von shoppingMemo
        return shoppingMemo;
        //return new ShoppingMemo(product, quantity, id);
    }

    //  get whole database information
    public List<ShoppingMemo> getAllShoppingMemos() {
        // Liste aus ShoppingMemos
        List<ShoppingMemo> shoppingMemoList = new ArrayList<>();

        // suchanfrage ohne suchkriterium - gibt alles zurück
        Cursor cursor = database.query(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,
                columns, null, null, null, null, null);

        // cursor zum ersten datensatz
        cursor.moveToFirst();
        // tmp shopping memo
        ShoppingMemo shoppingMemo;

        // bis alle abgearbeitet sind
        while(!cursor.isAfterLast()) {
            // convert cursor to shoppingMemo
            shoppingMemo = cursorToShoppingMemo(cursor);
            // add shoppingMemo to List
            shoppingMemoList.add(shoppingMemo);
            // Ausgabe im LogCat
            Log.d(LOG_TAG, "ID: " + shoppingMemo.getId() + ", Inhalt: " + shoppingMemo.toString());
            // move forward
            cursor.moveToNext();
        }

        // schließe den Zeiger
        cursor.close();

        return shoppingMemoList;
    }
}