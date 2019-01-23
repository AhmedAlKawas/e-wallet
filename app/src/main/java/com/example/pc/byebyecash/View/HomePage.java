package com.example.pc.byebyecash.View;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pc.byebyecash.MainActivity;
import com.example.pc.byebyecash.Model.Admin;
import com.example.pc.byebyecash.Model.Costumer;
import com.example.pc.byebyecash.Model.Vendor;
import com.example.pc.byebyecash.R;
import com.example.pc.byebyecash.utils.Encrypytion;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class HomePage extends AppCompatActivity {

    String mobile, role , totalCredit , roleClue ,recieverMobile;
    TextView userNameTv, tokensTv , logOutTv;
    Button sendBtn, recieveBtn , comfirmVendorBtn , getLocationBtn;
    FloatingActionButton addVendorBtn;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    DocumentReference documentReference, totalCreditRefrence , reciverRefrence;
    EditText vendorNameET , vendorMobileET , vendorPasswordET , vendorLatitudeET , vendorLongitudeET;
    Encrypytion encrypytion;
    Dialog addVendorDialog , sendindDialog;;
    SharedPreferences settings;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        userNameTv = findViewById(R.id.user_name);
        tokensTv = findViewById(R.id.tokens_TV);
        sendBtn = findViewById(R.id.send_btn);
        recieveBtn = findViewById(R.id.recieve_btn);
        addVendorBtn = findViewById(R.id.add_vendor);
        logOutTv = findViewById(R.id.log_out_tv);

        Intent i = getIntent();
        mobile = i.getStringExtra("mobile");
        role = i.getStringExtra("role");

        documentReference = firebaseFirestore.collection(role).document(mobile);
        totalCreditRefrence = firebaseFirestore.collection(getString(R.string.admin)).
                document(getString(R.string.total_credit));
        encrypytion = new Encrypytion();

        updateUI();

        addVendorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAVendor();
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator i = new IntentIntegrator(HomePage.this);
                startActivityForResult(i.createScanIntent(),2);
            }
        });

        recieveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(HomePage.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.dialog_qr_code_view);
                ImageView qrCodeView = dialog.findViewById(R.id.qr_code_img);
                try {
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.encodeBitmap(roleClue+mobile,
                            BarcodeFormat.QR_CODE, 400, 400);
                    qrCodeView.setImageBitmap(bitmap);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                dialog.show();
            }
        });

        logOutTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settings = getSharedPreferences(getString(R.string.pref_name), Context.MODE_PRIVATE);
                editor = settings.edit();
                editor.clear();
                editor.commit();
                startActivity(new Intent(HomePage.this,MainActivity.class));
            }
        });

    }

    private void updateUI() {
        if (role.equals(getString(R.string.customer))) {
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if (documentSnapshot.exists()) {
                        roleClue = getString(R.string.costumer_clue);
                        Costumer costumer = documentSnapshot.toObject(Costumer.class);
                        String userNameString = costumer.getName();
                        if (userNameString.equals("")) {
                            userNameTv.setText(mobile);
                        } else
                            userNameTv.setText(userNameString);
                        tokensTv.setText(String.valueOf(costumer.getCredit()));
                    }
                }
            });
        } else if (role.equals(getString(R.string.vendor))) {
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    roleClue = getString(R.string.vendor_clue);
                    Vendor vendors = documentSnapshot.toObject(Vendor.class);
                    String userNameString = vendors.getName();
                    if (userNameString.equals("")) {
                        userNameTv.setText(mobile);
                    } else
                        userNameTv.setText(userNameString);
                    tokensTv.setText(String.valueOf(vendors.getCredit()));
                }
            });
        } else if (role.equals(getString(R.string.admin))) {
            addVendorBtn.setVisibility(View.VISIBLE);
            recieveBtn.setVisibility(View.GONE);
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    roleClue = getString(R.string.admin_clue);
                    Admin admins = documentSnapshot.toObject(Admin.class);
                    String userNameString = admins.getName();
                    if (userNameString.equals("")) {
                        userNameTv.setText(mobile);
                    } else
                        userNameTv.setText(userNameString);

                    totalCreditRefrence.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            totalCredit = documentSnapshot.get(getString(R.string.total_credit)).toString();
                            tokensTv.setText(totalCredit);
                        }
                    });
                }
            });
        }
    }

    private void addAVendor(){
        addVendorDialog = new Dialog(HomePage.this);
        addVendorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        addVendorDialog.setCancelable(true);
        addVendorDialog.setContentView(R.layout.dialog_add_vendor);
        vendorNameET = addVendorDialog.findViewById(R.id.vendorName);
        vendorMobileET = addVendorDialog.findViewById(R.id.vendorMobile);
        vendorPasswordET = addVendorDialog.findViewById(R.id.vendorPassword);
        comfirmVendorBtn = addVendorDialog.findViewById(R.id.add_vendor_btn);
        getLocationBtn = addVendorDialog.findViewById(R.id.get_location_btn);
        vendorLatitudeET = addVendorDialog.findViewById(R.id.vendorlatitude);
        vendorLongitudeET = addVendorDialog.findViewById(R.id.vendorlongitude);
        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });
        vendorLongitudeET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });
        vendorLatitudeET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });
        comfirmVendorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String vendorName = vendorNameET.getText().toString().trim();
                String vendorMobile = vendorMobileET.getText().toString().trim();
                String vendorLongitudeStr = vendorLongitudeET.getText().toString().trim();
                String vendorLatitudeStr = vendorLatitudeET.getText().toString().trim();
                String vendorPasswordStr = vendorPasswordET.getText().toString().trim();
                if (!vendorMobile.isEmpty()) {
                    if (!vendorPasswordStr.isEmpty()) {
                        if (!vendorLatitudeStr.isEmpty()) {
                            Vendor vendors = new Vendor(Double.parseDouble(vendorLongitudeStr)
                                    , Double.parseDouble(vendorLatitudeStr), vendorName,
                                    0, encrypytion.encrypt(vendorPasswordStr) );
                            DocumentReference documentReference1 = firebaseFirestore
                                    .collection("Vendor").document(vendorMobile);
                            documentReference1.set(vendors);
                            addVendorDialog.dismiss();
                        } else
                            vendorLongitudeET.setError("Please click on the get vebor button and" +
                                    " select the vendors location so that the longitide and " +
                                    "latitude be self generated");
                    }else
                        vendorPasswordET.setError("Please enter the vendors Password");
                }else
                    vendorMobileET.setError("Please enter the vendors mobile number");
            }
        });
        addVendorDialog.show();
    }

    private void getLocation(){
        Intent i = new Intent(HomePage.this,MapsActivity.class);
        startActivityForResult(i,1);
    }

    private void detectRecieverRole(String recieverRole){
        roleClue = recieverRole.substring(0,2);
        recieverMobile = recieverRole.substring(2);
        if (roleClue.equals(getString(R.string.costumer_clue))){

            reciverRefrence = firebaseFirestore.collection(getString(R.string.customer))
                    .document(recieverMobile).collection("Requests").document(mobile);
            createSendingDialog();

        }else if (roleClue.equals(getString(R.string.vendor_clue))){

        }else if (roleClue.equals(getString(R.string.admin_clue))){

        }else
            Toast.makeText(HomePage.this, R.string.user_not_available,Toast.LENGTH_LONG).show();
    }

    private void createSendingDialog(){
        sendindDialog = new Dialog(HomePage.this);
        sendindDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        sendindDialog.setCancelable(true);
        sendindDialog.setContentView(R.layout.dialog_send_credit);
        final EditText ammountET = sendindDialog.findViewById(R.id.ammount_et);
        Button sendCreditBTN = sendindDialog.findViewById(R.id.send_credit_btn);
        sendCreditBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String creditAmmount = ammountET.getText().toString();
                if (!creditAmmount.isEmpty()){
                    
                }else
                    ammountET.setError("Please specify the ammount of credit you want to send");
            }
        });
        sendindDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1){
            if (resultCode == RESULT_OK){
                vendorLatitudeET.setText(data.getStringExtra("latitude"));
                vendorLongitudeET.setText(data.getStringExtra("longitude"));
            }
        }else if (requestCode == 2){

            IntentResult intentResult = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data);
            if (intentResult != null){
                if (intentResult.getContents() == null){
                    Log.e("contents","Canceled");
                }else{
                    detectRecieverRole(intentResult.getContents());
                }
            }

        }else
            super.onActivityResult(requestCode, resultCode, data);
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
