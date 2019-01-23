package com.example.pc.byebyecash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.pc.byebyecash.Model.Admin;
import com.example.pc.byebyecash.Model.Costumer;
import com.example.pc.byebyecash.Model.Vendor;
import com.example.pc.byebyecash.View.HomePage;
import com.example.pc.byebyecash.utils.Encrypytion;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    RadioGroup radioGroup;
    EditText nameEditText, passwordEditText, editText;
    RadioButton customerRadioButton, adminRadioButton, vendorRadioButton;
    String name, password, mobile, defaultMobile, defaultPassword, defultRole;
    private static String PREFRENCEMOBILE = "PREFRENCEMOBILE";
    private static String PREFRENCEPASSWORD = "PREFRENCEPASSWORD";
    private static String PREFRENCEROLE = "PREFRENCEROLE";
    DocumentReference adminRefrence;
    Encrypytion encrypytion;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    ProgressBar progressBar;
    SharedPreferences settings;
    SharedPreferences.Editor editor;

    @Override
    protected void onStart() {
        SharedPreferences settings = getSharedPreferences(getString(R.string.pref_name), Context.MODE_PRIVATE);
        String databaseMobile = settings.getString(PREFRENCEMOBILE, defaultMobile);
        String databasePassword = settings.getString(PREFRENCEPASSWORD, defaultPassword);
        String role = settings.getString(PREFRENCEROLE, defultRole);

        if (!(databaseMobile == null)) {
            progressBar.setVisibility(View.VISIBLE);
            if (role.equals(getString(R.string.customer))) {
                costumerLogin(databaseMobile, databasePassword);
            } else if (role.equals(getString(R.string.vendor))) {
                vendorLogin(databaseMobile, databasePassword);
            } else if (role.equals(getString(R.string.admin))) {
                adminLogin(databaseMobile, databasePassword);
            }
        }

        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        encrypytion = new Encrypytion();
        radioGroup = findViewById(R.id.roleRG);
        editText = findViewById(R.id.mobileET);
        nameEditText = findViewById(R.id.nameET);
        customerRadioButton = findViewById(R.id.customerRB);
        vendorRadioButton = findViewById(R.id.vendorRB);
        adminRadioButton = findViewById(R.id.adminRB);
        passwordEditText = findViewById(R.id.passwordET);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.continueBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = nameEditText.getText().toString().trim();
                mobile = editText.getText().toString().trim();
                password = passwordEditText.getText().toString().trim();
                if (radioGroup.getCheckedRadioButtonId() != -1) {
                    if (!password.isEmpty()) {
                        if (!mobile.isEmpty()) {
                            progressBar.setVisibility(View.VISIBLE);
                            if (customerRadioButton.isChecked()) {
                                costumerLogin(mobile, password);
                            } else if (vendorRadioButton.isChecked()) {
                                vendorLogin(mobile, password);
                            } else if (adminRadioButton.isChecked()) {
                                adminLogin(mobile, password);
                            }
                        } else
                            editText.setError("Please enter your mobile number");
                    } else
                        passwordEditText.setError("Please enter your password");
                } else
                    Toast.makeText(MainActivity.this, "Please choose your role",
                            Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void costumerLogin(final String mobile, final String password) {

        firebaseFirestore.collection("Costumer").document(mobile)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Costumer costumer = documentSnapshot.toObject(Costumer.class);
                            String password1 = encrypytion.decrypt(costumer.getPassword());
                            if (password.equals(password1)) {
                                goTOHomePage(mobile, password, getString(R.string.customer));
                            } else
                                passwordEditText.setError("Wrong Password");
                        } else {
                            Costumer costumer = new Costumer(name, 0,
                                    encrypytion.encrypt(password));
                            firebaseFirestore.collection("Costumer")
                                    .document(mobile).set(costumer);
                            goTOHomePage(mobile, password, getString(R.string.customer));
                        }
                    }
                });
    }

    private void vendorLogin(final String mobile, final String password) {
        firebaseFirestore.collection("Vendor").document(mobile)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Vendor vendors = documentSnapshot.toObject(Vendor.class);
                            String password1 = encrypytion.decrypt(vendors.getPassword());
                            if (password.equals(password1)) {
                                goTOHomePage(mobile, password, getString(R.string.vendor));
                            } else
                                passwordEditText.setError("Wrong Password");
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Sorry this phone number does not belong" +
                                            " to any vendor", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

    private void adminLogin(final String mobile, final String password) {
        adminRefrence = firebaseFirestore.collection("Admin").document(mobile);
        adminRefrence.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Admin admins = documentSnapshot.toObject(Admin.class);
                    String password1 = encrypytion.decrypt(admins.getPassword());
                    if (password.equals(password1)) {
                        goTOHomePage(mobile, password, getString(R.string.admin));
                    } else {
                        passwordEditText.setError("Wrong password");
                    }

                } else {
                    Toast.makeText(MainActivity.this,
                            "Sorry this phone number does not belong" +
                                    " to any admin", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

    private void goTOHomePage(String mobile, String password, String role) {

        settings = getSharedPreferences(getString(R.string.pref_name), Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.putString(PREFRENCEMOBILE, mobile);
        editor.putString(PREFRENCEPASSWORD, password);
        editor.putString(PREFRENCEROLE, role);
        editor.commit();

        Intent i = new Intent(MainActivity.this,
                HomePage.class);
        i.putExtra("role", role);
        i.putExtra("mobile", mobile);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
        super.onBackPressed();
    }
}
