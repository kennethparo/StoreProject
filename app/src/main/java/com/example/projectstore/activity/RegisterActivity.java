package com.example.projectstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projectstore.R;
import com.example.projectstore.obj.Security;
import com.example.projectstore.obj.UserClass;
import com.google.firebase.database.DataSnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {
    public EditText fullname, location, contactNumber, storeName, email, username, password, rpassword;
    public Button signUp;
    public TextView loginHere, alertTitle;
    private String status = "";
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();

        fullname = (EditText)findViewById(R.id.fullname);
        location = (EditText)findViewById(R.id.location);
        contactNumber = (EditText)findViewById(R.id.contactnumber);
        storeName = (EditText)findViewById(R.id.storename);
        email = (EditText)findViewById(R.id.email);
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        rpassword = (EditText)findViewById(R.id.rpassword);
        loginHere = (TextView)findViewById(R.id.loginHere);
        alertTitle = (TextView)findViewById(R.id.alertTitle);
        signUp = (Button)findViewById(R.id.signUpBtn);
        status = getIntent().getStringExtra("UserType");
        storeName.setVisibility(status.equals("Customer") ? View.GONE : View.VISIBLE);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fn = fullname.getText().toString();
                String loc = location.getText().toString();
                String cn = contactNumber.getText().toString().trim();
                String store = storeName.getText().toString();
                String email_ = email.getText().toString();
                String un = username.getText().toString();
                String pw = password.getText().toString();
                String rpw = rpassword.getText().toString();

                if(fn.equals("") || loc.equals("") || cn.equals("") ||
                        email.equals("") || pw.equals("") || rpw.equals("") || (status.equals("StoreOwner") && store.equals(""))) {
                    alertTitle.setText("All fields are required.");
                }
                else if(!fn.matches("[a-zA-Z\\.\\s]+")) {
                    alertTitle.setText("");
                    fullname.setError("Other characters and digits is not allowed.");
                }
                else if(!un.matches("[a-zA-Z0-9]+")) {
                    username.setError("Other characters/symbols are not allowed.");
                }
                else if(un.length() < 4) {
                    username.setError("Username should be 4 characters or more.");
                }
                else if(loc.length() < 10) {
                    location.setError("Please enter a valid location.");
                }
                else if(!pw.equals(rpw)) {
                    password.setError("Password does not match.");
                    rpassword.setError("Password does not match.");
                    alertTitle.setText("");
                }
                else if(pw.length() < 8 || rpw.length() < 8) {
                    password.setError("Password should be 8 characters or more");
                }
                else if(cn.matches("[\\D]+") && cn.length() != 11
                        || !cn.substring(0, 2).equals("09")) {
                    alertTitle.setText("");
                    contactNumber.setError("Please put a valid contact number.");
                }
                else {
                    String emailHolder = Character.toString(email_.charAt(email_.length()-1));
                    String[] emailSplit = email_.split("@");
                    String[] emailSplit_ = email_.split("\\.");
                    if(!email_.matches("[a-zA-Z0-9\\@\\.]+") || email_.matches("[@.]+")
                            || emailHolder.matches("[\\W]+")) {
                        email.setError("Please enter a valid email.");
                        alertTitle.setText("");
                    }
                    else if(emailSplit[0].length() < 6) {
                        email.setError("Email should contain 6 or more characters.");
                    }
                    else {
                        int count = 0;
                        for(int i=0; i<email_.length(); i++) {
                            if(email_.charAt(i) == '@') count++;
                        }
                        if(count != 1 || Character.toString(emailSplit[1].charAt(0)).matches("[\\W]+")) {
                            email.setError("Please enter a valid email. ");
                        }
                        else {
                            int count_ = 0;
                            for(int i=0; i<emailSplit[1].length(); i++) {
                                if(emailSplit[1].charAt(i) == '.') count_++;
                            }
                            if(count_ != 1) email.setError("Please enter a valid email.");
                            else {
                                int _count = 0;
                                boolean checkIfMultipleDot = false;
                                for(int i=0; i<emailSplit[0].length(); i++) {
                                    if(emailSplit[0].charAt(i) == '.') _count++;
                                }
                                for(int i=0; i<_count; i++) {
                                    if(emailSplit_[i].equals("")) checkIfMultipleDot = true;
                                }
                                if(checkIfMultipleDot) email.setError("Please enter a valid email.");
                                else getData(fn, loc, cn, store, email_, un, new Security().encryptData(pw, un), status);
                            }
                        }
                        alertTitle.setText("");
                    }
                }
            }
        });

        loginHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
    }

    public void insertData(String fn, String loc, String cn, String store, String email, String un, String pw, String userType) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        UserClass userClass = null;
        if(userType.equals("Customer")) userClass = new UserClass(fn, loc, cn, email, un, pw, userType);
        else userClass = new UserClass(fn, loc, cn, store, email, un, pw, userType);
        reference.child(un).setValue(userClass);
    }

    public void emailAndPasswordAuth(final String fn, final String loc, final String cn, final String store,
                                     final String e, final String un, final String pw, final String userType) {
        mAuth.createUserWithEmailAndPassword(e, pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    insertData(fn, loc, cn, store, e, un, pw, userType);
                    fullname.setText("");
                    location.setText("");
                    contactNumber.setText("");
                    if(userType.equals("StoreOwner")) storeName.setText("");
                    email.setText("");
                    username.setText("");
                    password.setText("");
                    rpassword.setText("");
                    startActivity(new Intent(RegisterActivity.this, HomePageActivity.class));
                }
                else alertTitle.setText("Email is Existing.");
            }
        });
    }

    public void getData(final String fn, final String loc, final String cn, final String store,
                        final String email_, final String un, final String pw, final String userType) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query checkUser = reference.orderByChild("username").equalTo(un);

        checkUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) alertTitle.setText("Username is already existing.");
                else emailAndPasswordAuth(fn, loc, cn, store, email_, un, pw, userType);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}