package com.example.amieruljapri.myapplication31;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private Button buttonSave;
    private EditText editTextName;
    private EditText editTextAddress;
    private TextView textViewPersons;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private Button buttonRegister;
    private Button buttonLogin;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        Firebase.setAndroidContext(this);

        buttonSave = (Button) findViewById(R.id.buttonSave);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextAddress = (EditText) findViewById(R.id.editTextAddress);
        textViewPersons = (TextView) findViewById(R.id.textViewPersons);
        buttonRegister = (Button)findViewById(R.id.buttonRegister);
        progressDialog = new ProgressDialog(this);
        buttonLogin = (Button)findViewById(R.id.buttonLogin);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    Log.d("Auth Listener", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d("Auth Listener", "onAuthStateChanged:signed_out");
                }
            }
        };




        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    login();
            }
        });
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });


    }

    private void saveData() {
        //Creating firebase object
        Firebase ref = new Firebase(Config.FIREBASE_URL);

        //Getting values to store
        String name = editTextName.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();

        //Creating Person object
        Person person = new Person();

        //Adding values
        person.setName(name);
        person.setAddress(address);

        //Storing values to firebase
        ref.child("Person").setValue(person);

        //Value event listener for realtime data update
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    //Getting the data from snapshot
                    Person person = postSnapshot.getValue(Person.class);

                    //Adding it to a string
                    String string = "Name: "+person.getName()+"\nAddress: "+person.getAddress();

                    //Displaying it on textview
                    textViewPersons.setText(string);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(getApplicationContext(),"The read failed: "+firebaseError,Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            firebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void login() {

        String name = editTextName.getText().toString().trim();
        String password = editTextAddress.getText().toString().trim();

        if(!name.isEmpty() && !password.isEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(name, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Login success", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Login failed :" + task.getException(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private void register() {

        //getting email and password from edit texts
        String email = editTextName.getText().toString().trim();
        String password = editTextAddress.getText().toString().trim();

        //checking if email and passwords are empty
        if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password)){
            Toast.makeText(getApplicationContext(),"Please enter email and password",Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("Registering please wait..");
        progressDialog.show();

        try {
            //creating a new user
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //checking if success
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Fail", Toast.LENGTH_LONG).show();
                            }
                            progressDialog.dismiss();
                        }
                    });
        } catch (Exception e){
            Toast.makeText(MainActivity.this,e.toString(),Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }

    }
}
