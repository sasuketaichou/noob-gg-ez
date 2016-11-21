package com.example.mierul.myapplication18;

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

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText editTextName;
    private EditText editTextAddress;
    private TextView textViewPersons;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        Firebase.setAndroidContext(this);

        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextAddress = (EditText) findViewById(R.id.editTextAddress);
        textViewPersons = (TextView) findViewById(R.id.textViewPersons);

        progressDialog = new ProgressDialog(this);


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

        Button buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(this);
        Button buttonRegister = (Button) findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(this);
        Button buttonLogin = (Button) findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.buttonLogin:
                login();
                break;
            case R.id.buttonRegister:
                register();
                break;
            case R.id.buttonSave:
                saveData();
                break;

            default:
                break;
        }

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

    private void saveData() {

        String name = editTextName.getText().toString().trim();
        String pass = editTextAddress.getText().toString().trim();

        Person person = new Person();
        person.setName(name);
        person.setAddress(pass);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Person").setValue(person);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Person person = dataSnapshot.getValue(Person.class);

                //Adding it to a string
                String string = "Name: " + person.getName() + "\nAddress: " + person.getAddress();

                //Displaying it on textview
                textViewPersons.setText(string);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "The read failed: " + databaseError, Toast.LENGTH_LONG).show();
            }
        });

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
                                Toast.makeText(MainActivity.this, "Login failed :" + task, Toast.LENGTH_LONG).show();
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
            //password must be more / equal than 8 characters?
            // testing@hayoo.com
            // hayookkk
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
