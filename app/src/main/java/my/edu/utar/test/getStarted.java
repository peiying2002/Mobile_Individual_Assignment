package my.edu.utar.test;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class getStarted extends AppCompatActivity {

    private Button click_start;
    private EditText username_info;
    private EditText phone_number_info;
    private String formattedPhoneNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        click_start = findViewById(R.id.button);
        username_info = findViewById(R.id.username);
        phone_number_info = findViewById(R.id.phonenumber);

        LinearLayout loginscreen = findViewById(R.id.linearLayout);
        //hide the keyboard by touching the screen
        loginscreen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });

        //save the personal information by using shared preferences to show the details in the next page.
        click_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = username_info.getText().toString().trim();
                String phonenumber = phone_number_info.getText().toString().trim();
                if (!username.isEmpty() && !phonenumber.isEmpty()) {
                    if (phonenumber.length() == 10 ||phonenumber.length() == 11 ) {
                        SharedPreferences prefQ1 = getSharedPreferences("MySharedPreferences", MODE_PRIVATE);
                        //use editor to use method to put data in (key,value)
                        SharedPreferences.Editor prefEditor = prefQ1.edit();
                        prefEditor.putString("Name",username);
                        prefEditor.putString("PhoneNumber",phonenumber);
                        prefEditor.commit();
                        Intent intent = new Intent(getStarted.this, main_screen.class);
                        startActivity(intent);
                    } else {
                        showToast("Invalid Phone Number.");
                    }
                }
                else {
                    showToast("Please complete the input. ");
                }
            }
        });

    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard(){
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            input.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}