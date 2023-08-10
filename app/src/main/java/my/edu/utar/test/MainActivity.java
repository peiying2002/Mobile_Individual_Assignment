package my.edu.utar.test;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private my.edu.utar.test.SQLiteAdapter mySQLiteAdapter;
    private EditText click_friends;
    private static final int PICK_CONTACT_REQUEST = 1;
    private ArrayList<String> friend_added = new ArrayList<String>();
    private ArrayList<String> formatedPNum_add = new ArrayList<String>();
    private ArrayList<String> descriptionlist = new ArrayList<String>();
    private ArrayList<Double> percentage_added = new ArrayList<>();
    private ArrayList<String> amount_added = new ArrayList<>();
    private RadioButton equal_split;
    private RadioButton custom_spilt;
    private EditText total_amount;
    private EditText description;
    private double equaltotal;
    private Button click_btnCalculate;
    private EditText percentage;
    private ImageView goback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        click_friends = findViewById(R.id.etfriend);
        custom_spilt = findViewById(R.id.rbCustomSplit);
        click_btnCalculate = findViewById(R.id.btnCalculate);
        equal_split = findViewById(R.id.rbEqualSplit);
        total_amount = findViewById(R.id.etTotalAmount);
        description = findViewById(R.id.description);
        mySQLiteAdapter = new my.edu.utar.test.SQLiteAdapter(this);
        goback = findViewById(R.id.left_back);

        LinearLayout loginscreen = findViewById(R.id.linearLayout);

        loginscreen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });

        click_friends.setOnClickListener(new View.OnClickListener(){
            //after clicking the etfriend EditView
            @Override
            public void onClick(View view) {
                //Create and open the contact picker to allow the user to choose for their friends to split the bill
                Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
            }
        });

        custom_spilt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                custom_info();
            }
        });

        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, main_screen.class);
                startActivity(intent);
            }
        });

        click_btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String totalAmount = total_amount.getText().toString().trim();
                String numPeople = click_friends.getText().toString().trim();
                String desc = description.getText().toString().trim();

                if (numPeople.isEmpty() || friend_added.size()==1) {
                    showToast("Please add at least two friends.");
                    return;
                }
                if (totalAmount.isEmpty()) {
                    showToast("Please enter the total amount.");
                    return;
                }
                if(desc.isEmpty()){
                    showToast("Please enter description.");
                }

                double total_bill = Double.parseDouble(totalAmount);

                if (!(numPeople.isEmpty() || friend_added.size()==1) && !totalAmount.isEmpty() && !desc.isEmpty() ){
                    descriptionlist.add(desc);
                    if (equal_split.isChecked() ) {
                        equaltotal = calculateEqualSplit(total_bill);
                        equal_result(equaltotal);
                    } else if(custom_spilt.isChecked()){
                        calculateCustomSplitResult(total_bill);
                    }
                    else{
                        showToast("Please choose one bill break-down method.");
                    }
                    hideKeyboard(view);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int request_Code, int result_Code, Intent input_Data) {
        super.onActivityResult(request_Code, result_Code, input_Data);
        if (request_Code == PICK_CONTACT_REQUEST) {
            if (result_Code == RESULT_OK) {
                // Handle the result from the contact picker
                Uri contactUri = input_Data.getData();

                // Get the contact ID from the URI
                String contactID = contactUri.getLastPathSegment();

                // Query for contact data
                Cursor cursor = getContentResolver().query(
                        ContactsContract.Data.CONTENT_URI,
                        null,
                        ContactsContract.Data.CONTACT_ID + "=?",
                        new String[]{contactID},
                        null
                );

                if (cursor != null && cursor.moveToFirst()) {
                    try {
                        // Get the contact's name
                        int nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        String person_selected = cursor.getString(nameColumnIndex);

                        int phonemnumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        String original_phonenum = cursor.getString(phonemnumber);

                        // Add the person's name and phone num to the respective ArrayList
                        //do not have duplicated friends to be added
                        if (!friend_added.contains(person_selected)) {
                            friend_added.add(person_selected);
                            String format_PN = original_phonenum.replaceAll("[^\\d]", "");
                            formatedPNum_add.add(format_PN);
                        }
                        else{
                            showToast("Your friend has been selected :)"+"\n"+"Please choose other person.");
                        }

                        // display all the item in friend_added array list into the textview.
                        String display_friend = TextUtils.join(", ", friend_added);
                        click_friends.setText(display_friend);
                        for (String personName : friend_added) {
                            Log.d("FriendAdded", personName); // This will print each friend's name to Logcat
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        cursor.close();
                    }
                }

            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private double calculateEqualSplit(double total_bill) {
        int total_ppl = friend_added.size();
        double equal_split = total_bill / total_ppl;

        return equal_split;
    }

    //calculate and show the result by percentage
    private void calculateCustomSplitResult(double total_bill){

        String desc = description.getText().toString().trim();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_result, null);

        // Set custom layout to the dialog
        dialogBuilder.setView(dialogView);

        TextView friend = dialogView.findViewById(R.id.friendlist);
        TextView totalamount = dialogView.findViewById(R.id.customamount);
        ImageView save_customDatabase = dialogView.findViewById(R.id.save_icon);

        // to prevent the previous amount is in the list and ready to the current calculation
        amount_added.clear();

        for (Double amount : percentage_added) {
            double calculated_amount = (amount/100)*total_bill;
            String formatted_calculated_amount = String.format("%.2f", calculated_amount);
            amount_added.add(formatted_calculated_amount);
        }

        StringBuilder contentTextPeople = new StringBuilder();
        StringBuilder contentTextTotal = new StringBuilder();


        for (int i = 0; i < friend_added.size(); i++) {
            contentTextPeople.append(friend_added.get(i)).append("\n\n"); // append the name of the people
            contentTextTotal.append("RM ").append(amount_added.get(i)).append("\n\n"); // append the total amount
        }

        friend.setText(contentTextPeople.toString());
        totalamount.setText(contentTextTotal.toString());

        // Create and show the dialog
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        save_customDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // save the result to the database
                dialog.dismiss(); // Close the AlertDialog after saving
                SharedPreferences prefQ1 =getSharedPreferences("MySharedPreferences",0); // if know the name of the file, then can access it at other activity file
                String phonenum = prefQ1.getString("PhoneNumber","0");
                mySQLiteAdapter.openToWrite();
                //insert the friend name, decription and their total amount into the database
                for (int i = 0; i < friend_added.size(); i++) {
                    //only insert friends' info to the database except own information
                    if (!formatedPNum_add.get(i).equals(phonenum)) { //to check if there is own phone number that entered in the getStarted page, no need insert to the database
                        double amount = Double.parseDouble(amount_added.get(i));
                        mySQLiteAdapter.insert(friend_added.get(i), desc, amount);
                    } 
                }
                mySQLiteAdapter.close(); // Close the database after insertion
                Intent intent = new Intent(MainActivity.this, main_screen.class);
                startActivity(intent);

            }
        });

    }


    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void custom_info() {
        percentage_added.clear();
        final ArrayList<Double> format_input = new ArrayList<>();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_info, null);

        // Set custom layout to the dialog
        dialogBuilder.setView(dialogView);

        // Get references to the container layouts
        LinearLayout containerLayout = dialogView.findViewById(R.id.mainlayout);


        for (String personName : friend_added) {
            // Create a new horizontal LinearLayout to hold friendTextView and underline_percent
            LinearLayout mainLayout = new LinearLayout(this);
            //to set the padding of the mainLayout
            int padding = 20;
            int paddingInPx = (int) (padding * getResources().getDisplayMetrics().density);
            mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            mainLayout.setOrientation(LinearLayout.HORIZONTAL);
            mainLayout.setGravity(Gravity.CENTER_VERTICAL);
            mainLayout.setPadding(paddingInPx,0,paddingInPx,0);

            // add the friends
            TextView friendTextView = new TextView(this);
            friendTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            friendTextView.setText(personName);
            friendTextView.setTextColor(Color.BLACK);
            mainLayout.addView(friendTextView);

            // create a linear layout to put rhe edittext and textview.
            LinearLayout underline_percent = new LinearLayout(this);
            underline_percent.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            underline_percent.setOrientation(LinearLayout.HORIZONTAL);
            underline_percent.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);

            // Create a new EditText view for each person
            EditText underline = new EditText(this);
            int underlineWidth = 50; // Set the width in dp
            int pixel_underlineWidth = (int) (underlineWidth * getResources().getDisplayMetrics().density);
            underline.setLayoutParams(new LinearLayout.LayoutParams(
                    pixel_underlineWidth, // Set the width in pixels
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            underline.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            underline_percent.addView(underline);

            // add percentage symbol next to the underline
            TextView percentSymbol = new TextView(this);
            percentSymbol.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            percentSymbol.setText("%");
            underline_percent.addView(percentSymbol);

            // Add underline_percent to the rowLayout
            mainLayout.addView(underline_percent);

            // Add the new rowLayout to the containerLayout
            containerLayout.addView(mainLayout);
        }

        final AlertDialog dialog = dialogBuilder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //after click the tick_icon
                ImageView click_tick = ((AlertDialog) dialog).findViewById(R.id.tick_icon);
                click_tick.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        double totalPercentage = 0;
                        //to get the right view to check the user's input
                        for (int i = 0; i < containerLayout.getChildCount(); i++) {
                            View childView = containerLayout.getChildAt(i); // each row
                            if (childView instanceof LinearLayout) {
                                LinearLayout childLayout = (LinearLayout) childView;
                                // String friend_name = friend_added.get(i);
                                for (int j = 0; j < childLayout.getChildCount(); j++) {
                                    View grandChildView = childLayout.getChildAt(j); //child views of each row
                                    if (grandChildView instanceof LinearLayout) {
                                        // It's the layout containing EditText and %
                                        LinearLayout innerLayout = (LinearLayout) grandChildView;
                                        for (int k = 0; k < innerLayout.getChildCount(); k++) {
                                            View innerChildView = innerLayout.getChildAt(k); //child view of the underline_percent
                                            if (innerChildView instanceof EditText) {
                                                String input_percent = ((EditText) innerChildView).getText().toString();
                                                if (input_percent.isEmpty()) {
                                                    showToast("Percentage cannot be empty.");
                                                    return; // Exit the loop early if an empty input is found.
                                                }
                                                else {
                                                    double percent = Double.parseDouble(input_percent);
                                                    percentage_added.add(percent);
                                                    if (percent < 1 || percent > 100) {
                                                        showToast("Percentage must be in the range of 1-100.");
                                                        ((EditText) innerChildView).setText(""); //clear the edit text if it is out of range
                                                        return; // Exit the loop early if an invalid input is found.
                                                    }
                                                    totalPercentage += percent; //calculate the total percentage to check the percentage validation
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (totalPercentage > 100) { //check the total percentage is valid or not
                            showToast("Total percentage cannot exceed 100.");
                        } else if (totalPercentage < 100) {
                            showToast("Total percentage must be 100.");
                        } else {
                            hideKeyboard(view);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        dialog.show();
    }

    //put the result into the database! later can show the result at the another screen
    private void equal_result(double equaltotal){

        String desc = description.getText().toString().trim();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.equal_result, null);

        // Set custom layout to the dialog
        dialogBuilder.setView(dialogView);

        TextView friend = dialogView.findViewById(R.id.friendlist);
        TextView totalamount = dialogView.findViewById(R.id.equalamount);
        ImageView save_equalDatabase = dialogView.findViewById(R.id.save_icon);

        StringBuilder contentTextPeople = new StringBuilder();
        StringBuilder contentTextTotal = new StringBuilder();
        String formattedEqualTotal = String.format("%.2f", equaltotal);

        ArrayList<String> sortedFriends = new ArrayList<>(friend_added);
        Collections.sort(sortedFriends);

        for (String people : sortedFriends) {
            contentTextPeople.append(people).append("\n\n"); // append the name of the people
            contentTextTotal.append("RM ").append(formattedEqualTotal).append("\n\n"); // append the total amount
        }

        friend.setText(contentTextPeople.toString());
        totalamount.setText(contentTextTotal.toString());

        // Create and show the dialog
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        save_equalDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // save the result to the database
                SharedPreferences prefQ1 =getSharedPreferences("MySharedPreferences",0); // if know the name of the file, then can access it at other activity file
                String phonenum = prefQ1.getString("PhoneNumber","0");
                mySQLiteAdapter.openToWrite();
                for (int i = 0; i < friend_added.size(); i++) {
                    if(formatedPNum_add.get(i).equals(phonenum)){
                        continue;
                    }
                    else{
                        double amount = Double.parseDouble(formattedEqualTotal);
                        mySQLiteAdapter.insert(friend_added.get(i), desc, amount);
                    }
                }
                mySQLiteAdapter.close(); // Close the database after insertion

                Intent intent = new Intent(MainActivity.this, main_screen.class);
                startActivity(intent);
            }
        });
    }

    private void hideKeyboard(){
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            input.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
