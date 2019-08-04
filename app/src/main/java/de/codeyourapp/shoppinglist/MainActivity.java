package de.codeyourapp.shoppinglist;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

// Output Database
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.List;

// User Input
import android.view.inputmethod.InputMethodManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

// Contextual action bar
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;

// Animation at list
import android.graphics.Color;
import android.graphics.Paint;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // activate the logcat output for better debugging
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    // reference to datasource, connection between main activity and database
    private ShoppingMemoDataSource dataSource;

    // reference to ListView
    private ListView mShoppingMemosListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG_TAG, "Database will be created ");
        dataSource = new ShoppingMemoDataSource(this);

        // generate ListView
        initializeShoppingMemosListView();

        // activate the AddButton
        activateAddButton();
        // initialize the action bar
        initializeContextualActionBar();
    }

    // onResume is entered every time before activity is shown
    @Override
    protected void onResume() {
        super.onResume(); // first call super method before do own stuff

        Log.d(LOG_TAG, "Connection to database will be build up");
        dataSource.open();

        Log.d(LOG_TAG, "Current entrys at database:");
        showAllListEntries();
    }

    // onPause is entered every time before ativity will vanish for any reasons
    @Override
    protected void onPause() {
        super.onPause(); // first call super method before do own stuff

        Log.d(LOG_TAG, "Connection to database got closed");
        dataSource.close();
    }

    //
    private void initializeShoppingMemosListView() {
        List<ShoppingMemo> emptyListForInitialization = new ArrayList<>();

        mShoppingMemosListView = findViewById(R.id.listview_shopping_memos);

        // Erstellen des ArrayAdapters für unseren ListView
        ArrayAdapter<ShoppingMemo> shoppingMemoArrayAdapter = new ArrayAdapter<ShoppingMemo> (
                this,
                android.R.layout.simple_list_item_multiple_choice,
                emptyListForInitialization) {

            // Wird immer dann aufgerufen, wenn der übergeordnete ListView die Zeile neu zeichnen muss
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view =  super.getView(position, convertView, parent);
                TextView textView = (TextView) view;

                ShoppingMemo memo = (ShoppingMemo) mShoppingMemosListView.getItemAtPosition(position);

                // Hier prüfen, ob Eintrag abgehakt ist. Falls ja, Text durchstreichen
                if (memo.isChecked()) {
                    textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    textView.setTextColor(Color.rgb(175,175,175));
                }
                else {
                    textView.setPaintFlags( textView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                    textView.setTextColor(Color.DKGRAY);
                }

                return view;
            }
        };

        mShoppingMemosListView.setAdapter(shoppingMemoArrayAdapter);

        mShoppingMemosListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ShoppingMemo memo = (ShoppingMemo) adapterView.getItemAtPosition(position);

                // Hier den checked-Wert des Memo-Objekts umkehren, bspw. von true auf false
                // Dann ListView neu zeichnen mit showAllListEntries()
                ShoppingMemo updatedShoppingMemo = dataSource.updateShoppingMemo(memo.getId(), memo.getProduct(), memo.getQuantity(), (!memo.isChecked()));
                Log.d(LOG_TAG, "Checked-Status von Eintrag: " + updatedShoppingMemo.toString() + " ist: " + updatedShoppingMemo.isChecked());
                showAllListEntries();
            }
        });

    }

    // update the list that is displayed at the app
    private void showAllListEntries () {
        // Liste mit Einträgen füllen
        List<ShoppingMemo> shoppingMemoList = dataSource.getAllShoppingMemos();

        //array-Adapter update
        ArrayAdapter<ShoppingMemo> adapter = (ArrayAdapter<ShoppingMemo>) mShoppingMemosListView.getAdapter();

        adapter.clear();
        adapter.addAll(shoppingMemoList);
        adapter.notifyDataSetChanged();
    }

    // all AddButton stuff
    private void activateAddButton() {
        Button buttonAddProduct = (Button) findViewById(R.id.button_add_product); // button 1
        final EditText editTextQuantity = (EditText) findViewById(R.id.editText_quantity); // text 1
        final EditText editTextProduct = (EditText) findViewById(R.id.editText_product); // text 2

        // define whats happend if button got clicked
        buttonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String quantityString = editTextQuantity.getText().toString();// get text from text1
                String product = editTextProduct.getText().toString();        // get text from text2

                // check if there is text inside text1
                if(TextUtils.isEmpty(quantityString)) {
                    editTextQuantity.setError(getString(R.string.editText_errorMessage));
                    return;
                }
                // check if there is text inside text2
                if(TextUtils.isEmpty(product)) {
                    editTextProduct.setError(getString(R.string.editText_errorMessage));
                    return;
                }


                int quantity = Integer.parseInt(quantityString); // get int from string
                // reset text 1 and 2
                editTextQuantity.setText("");
                editTextProduct.setText("");

                // save entered data to database
                dataSource.createShoppingMemo(product, quantity);

                // get rid of soft-keyboard ( from StackOverflow )
                InputMethodManager inputMethodManager;
                inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if(getCurrentFocus() != null) {
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }

                // update List at Android App
                showAllListEntries();
            }
        });

    }

    // all action bar stuff
    private void initializeContextualActionBar() {
        final ListView shoppingMemosListView = (ListView) findViewById(R.id.listview_shopping_memos); // reference to main activity
        shoppingMemosListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL); // option to choose multiple list-points

        shoppingMemosListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            int selCount = 0; // counter to change menu points


            // count the number of choosen list entrys
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (checked) {
                    selCount++;
                } else {
                    selCount--;
                }
                String cabTitle = selCount + " " + getString(R.string.cab_checked_string);
                mode.setTitle(cabTitle);
                mode.invalidate();
            }

            // initial menu generation
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getMenuInflater().inflate(R.menu.menu_contextual_action_bar, menu);
                return true;
            }

            // get called via invalidate() to modify menu
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                MenuItem item = menu.findItem(R.id.cab_change);
                if (selCount == 1) {
                    item.setVisible(true);
                } else {
                    item.setVisible(false);
                }

                return true;
            }

            // define the actions of menu options
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                boolean returnValue = true;
                SparseBooleanArray touchedShoppingMemosPositions = shoppingMemosListView.getCheckedItemPositions();

                switch (item.getItemId()) {
                    // delete action
                    case R.id.cab_delete:
                        for (int i = 0; i < touchedShoppingMemosPositions.size(); i++) {
                            boolean isChecked = touchedShoppingMemosPositions.valueAt(i);
                            if (isChecked) {
                                int postitionInListView = touchedShoppingMemosPositions.keyAt(i);
                                ShoppingMemo shoppingMemo = (ShoppingMemo) shoppingMemosListView.getItemAtPosition(postitionInListView);
                                Log.d(LOG_TAG, "Position im ListView: " + postitionInListView + " Inhalt: " + shoppingMemo.toString());
                                dataSource.deleteShoppingMemo(shoppingMemo);
                            }
                        }
                        showAllListEntries();
                        mode.finish();
                        break;

                    // change action
                    case R.id.cab_change:
                        Log.d(LOG_TAG, "Eintrag ändern");
                        for (int i = 0; i < touchedShoppingMemosPositions.size(); i++) {
                            boolean isChecked = touchedShoppingMemosPositions.valueAt(i);
                            if (isChecked) {
                                int postitionInListView = touchedShoppingMemosPositions.keyAt(i);
                                ShoppingMemo shoppingMemo = (ShoppingMemo) shoppingMemosListView.getItemAtPosition(postitionInListView);
                                Log.d(LOG_TAG, "Position im ListView: " + postitionInListView + " Inhalt: " + shoppingMemo.toString());

                                AlertDialog editShoppingMemoDialog = createEditShoppingMemoDialog(shoppingMemo);
                                editShoppingMemoDialog.show();
                            }
                        }

                        mode.finish();
                        break;

                    default:
                        returnValue = false;
                        break;
                }
                return returnValue;
            }

            // reset counter if menu got closed
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selCount = 0;
            }
        });

    }

    // all alertdialog stuff
    private AlertDialog createEditShoppingMemoDialog(final ShoppingMemo shoppingMemo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this); // new alertdialog.builder object
        LayoutInflater inflater = getLayoutInflater(); // generate the layout views

        // View-object from layout
        View dialogsView = inflater.inflate(R.layout.dialog_edit_shopping_memo, null);

        // set text-fields to current data
        final EditText editTextNewQuantity = (EditText) dialogsView.findViewById(R.id.editText_new_quantity);
        editTextNewQuantity.setText(String.valueOf(shoppingMemo.getQuantity()));
        final EditText editTextNewProduct = (EditText) dialogsView.findViewById(R.id.editText_new_product);
        editTextNewProduct.setText(shoppingMemo.getProduct());

        // connect bilder with view
        builder.setView(dialogsView)
                .setTitle(R.string.dialog_title)
                // define enter button
                .setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // read entered data
                        String quantityString = editTextNewQuantity.getText().toString();
                        String product = editTextNewProduct.getText().toString();

                        // check input
                        if ((TextUtils.isEmpty(quantityString)) || (TextUtils.isEmpty(product))) {
                            Log.d(LOG_TAG, "Ein Eintrag enthielt keinen Text. Daher Abbruch der Änderung.");
                            return;
                        }

                        int quantity = Integer.parseInt(quantityString);

                        // update data at database
                        ShoppingMemo updatedShoppingMemo = dataSource.updateShoppingMemo(shoppingMemo.getId(), product, quantity, shoppingMemo.isChecked());

                        // output of change to log
                        Log.d(LOG_TAG, "Alter Eintrag - ID: " + shoppingMemo.getId() + " Inhalt: " + shoppingMemo.toString());
                        Log.d(LOG_TAG, "Neuer Eintrag - ID: " + updatedShoppingMemo.getId() + " Inhalt: " + updatedShoppingMemo.toString());

                        // update list
                        showAllListEntries();
                        // close alertdialog
                        dialog.dismiss();
                    }
                })
                // define cancel button
                .setNegativeButton(R.string.dialog_button_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // cancel action
                        dialog.cancel();
                    }
                });

        // give back builder
        return builder.create();
    }
}