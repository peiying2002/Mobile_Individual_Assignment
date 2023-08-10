package my.edu.utar.test;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.content.Intent;
import android.net.Uri;

public class main_screen extends AppCompatActivity {

    private Button addExpenses;
    private TextView placephonenum;
    private ListView listViewDatabaseContent; // Reference to the ListView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

            addExpenses = findViewById(R.id.buttonAddExpenses);
            placephonenum = findViewById(R.id.writephonenum);
            listViewDatabaseContent = findViewById(R.id.listViewDatabaseContent); // Initialize ListView
            TextView noRecord_TextView = findViewById(R.id.noRecordTextView);

            //retrieve data from sharedPreferences
            SharedPreferences prefQ1 = getSharedPreferences("MySharedPreferences", 0); // if know the name of the file, then can access it at other activity file
            String username = prefQ1.getString("Name", "Own");
            String phonenum = prefQ1.getString("PhoneNumber", "0");
            String format_phonenum = phonenum.substring(0, 3) + "-" + phonenum.substring(3);

            SQLiteAdapter mySQLiteAdapter = new SQLiteAdapter(this);
            mySQLiteAdapter.openToRead();
            Double amount = mySQLiteAdapter.summation_All(); // calculate the total value of the amount and display it
            String format_amount = String.format("%.2f", amount);
            mySQLiteAdapter.close();
            placephonenum.setText("Welcome, " + username + " ! " + format_phonenum + "\n" + "You are owned RM" + format_amount + ".");

            //retrieve the data from database
            mySQLiteAdapter.openToRead();

            String databaseContent = mySQLiteAdapter.queueAll();
            mySQLiteAdapter.close();

            if (databaseContent.isEmpty()) {
                //show no records when there is no data in database
                listViewDatabaseContent.setVisibility(View.GONE);
                noRecord_TextView.setVisibility(View.VISIBLE);
            } else {
                //show all the records when there is no data in database
                listViewDatabaseContent.setVisibility(View.VISIBLE);
                noRecord_TextView.setVisibility(View.GONE);
                // Split the database content string into separate items
                String[] databaseArray = databaseContent.split("\n");
                ArrayList<String> databaseContentList = new ArrayList<>(Arrays.asList(databaseArray));

                // sort the ListView content
                Comparator<String> compare = new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        String[] parts1 = s1.split(";");
                        String[] parts2 = s2.split(";");
                        String firstElement1 = parts1[0].trim();
                        String firstElement2 = parts2[0].trim();
                        return firstElement1.compareTo(firstElement2);
                    }
                };

                // Sort the databaseContentList using the custom comparator
                Collections.sort(databaseContentList, compare);

                // Create the custom adapter and set data to the ListView
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, databaseContentList) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        if (convertView == null) {
                            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
                        }

                        String element = getItem(position);
                        String[] split = element.split(";");

                        TextView textView = convertView.findViewById(R.id.Text);

                        // Set the text for the TextView
                        String format_amount = split[2].trim();
                        float amount = Float.parseFloat(format_amount);
                        String formattedAmount = String.format("%.2f", amount);
                        String sentence = split[0].trim() + " owes you RM" + formattedAmount + " for " + split[1].trim();
                        textView.setText(sentence);
                        //textView.setWidth();

                        return convertView;
                    }
                };
                listViewDatabaseContent.setAdapter(adapter);
                registerForContextMenu(listViewDatabaseContent);

            }

            addExpenses.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //navigate user to the next page (MainActivity.java)
                    Intent intent = new Intent(main_screen.this, MainActivity.class);
                    startActivity(intent);
                }
            });

    }

    //create the context menu for delete and share the bills
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();

        // Inflate the custom layout for the context menu header
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater1 = getLayoutInflater();
        View dialogView = inflater1.inflate(R.layout.share_menu, null);
        menu.setHeaderView(dialogView);

        // Inflate the menu items
        inflater.inflate(R.menu.context_menu, menu);
    }

    // Handle context menu item selection
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                // Handle the share option
                // Extract necessary data from the selected list item and call the sharing method
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                String selectedItem = listViewDatabaseContent.getItemAtPosition(info.position).toString();
                String[] split = selectedItem.split(";");
                String sentence = "You owe me RM" + split[2].trim() + " for " + split[1].trim();
                showShareDialogBox(sentence, split[0].trim());
                return true;

            case R.id.delete:
                // Handle the delete option
                // Implement the logic to delete the selected item from the database and the list view
                // Refresh the list view after deletion if needed
                /*AdapterView.AdapterContextMenuInfo info1 = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                String selected_Item = listViewDatabaseContent.getItemAtPosition(info1.position).toString();
                String[] deleted = selected_Item.split(";");
                SQLiteAdapter mySQLiteAdapter = new SQLiteAdapter(this);
                mySQLiteAdapter.openToRead();
                //pass the right information of the databasecontent to the function to delete the data in database.
                mySQLiteAdapter.deleteAll_Condition(deleted[0].trim(),deleted[1].trim());
                mySQLiteAdapter.close();
                recreate(); //refresh the page after deleting the history*/
                showDeleteDialogBox(item);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showShareDialogBox(String message,String name) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.share_result, null);

        // Set custom layout to the dialog
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //pass the phone num
                shareresult(message,name);
            }
        });

        // when the user click for "Cancel" button, do nothing and the dialog dismiss
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

    }

    private void showDeleteDialogBox(MenuItem item) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.delete_result, null);

        // Set custom layout to the dialog
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AdapterView.AdapterContextMenuInfo info1 = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                String selected_Item = listViewDatabaseContent.getItemAtPosition(info1.position).toString();
                String[] deleted = selected_Item.split(";");
                SQLiteAdapter mySQLiteAdapter = new SQLiteAdapter(main_screen.this);
                mySQLiteAdapter.openToRead();
                //pass the right information of the databasecontent to the function to delete the data in database.
                mySQLiteAdapter.deleteAll_Condition(deleted[0].trim(),deleted[1].trim());
                mySQLiteAdapter.close();
                recreate(); //refresh the page after deleting the history
            }
        });

        // when the user click for "Cancel" button, do nothing and the dialog dismiss
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

    }


    private void shareresult(String message,String name) {
        // Create a string to hold the message content
        // Create an Intent to send an SMS
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:"+ name));  // This ensures only SMS apps respond
        smsIntent.putExtra("sms_body", message.toString()); //set as the msg

        if (smsIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(smsIntent);
        } else {
            showToast("No SMS app installed.");
        }
    }
}
