package com.example.pc.byebyecash.View;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pc.byebyecash.Model.Admin;
import com.example.pc.byebyecash.Model.Costumer;
import com.example.pc.byebyecash.Model.Vendor;
import com.example.pc.byebyecash.R;
import com.example.pc.byebyecash.utils.Encrypytion;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.Map;

public class UserHomePage extends AppCompatActivity {

    TextView tokensTV;
    FloatingActionButton floatingActionButton;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    TextView userName;
    EditText vendorLongitude , vendorLatitude;
    Button sendBTN , recieveBTN;
    DocumentReference documentReference;
    String role , mobile , d;
    DocumentReference costRecieverRequestsRefrence , adminsTotalCredit , recieverDocument , vendRecieverRequestsRefrence;
    CollectionReference myRequestsRef;
    String adminTotal;
    Encrypytion encrypytion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home_page);
        tokensTV = findViewById(R.id.tokens_TV);
        floatingActionButton = findViewById(R.id.add_vendor);
        userName = findViewById(R.id.user_name);
        sendBTN = findViewById(R.id.send_btn);
        recieveBTN = findViewById(R.id.recieve_btn);
        encrypytion = new Encrypytion();

        Intent i = getIntent();
        mobile = i.getStringExtra("mobile");
        role = i.getStringExtra("role");

        documentReference = firebaseFirestore.collection(role).document(mobile);
        adminsTotalCredit = firebaseFirestore.collection(role).document("total credit");
        myRequestsRef = firebaseFirestore.collection(role).document(mobile)
                .collection("Requests");

        myRequestsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                for (final DocumentSnapshot documentSnapshot : documentSnapshots.getDocuments()){
                    String response = (String) documentSnapshot.get("Response");
                    Long requestCredit = (Long) documentSnapshot.get("Ammount");
                    if (response.equals("No")){
                        final String mobileNO = (String) documentSnapshot.get("Mobile");
                        final Dialog dialog = new Dialog(UserHomePage.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setCancelable(true);
                        dialog.setContentView(R.layout.dialog_request_sending);
                        TextView ammountTV = dialog.findViewById(R.id.ammount_to_be_sent_tv);
                        TextView acceptTV = dialog.findViewById(R.id.accept_tv);
                        TextView declineTV = dialog.findViewById(R.id.decline_tv);
                        ammountTV.setText("Someone wants to send you "+requestCredit+" tokens");
                        declineTV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                myRequestsRef.document(mobileNO).delete();
                                dialog.dismiss();
                            }
                        });
                        acceptTV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                myRequestsRef.document(mobileNO).update("Response","Yes");
                                dialog.dismiss();
                            }
                        });
//                        addVendorDialog.show();
                    }
                }
            }
        });

        recieveBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(UserHomePage.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.dialog_qr_code_view);
                ImageView qrCodeView = dialog.findViewById(R.id.qr_code_img);
                try {
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.encodeBitmap(mobile, BarcodeFormat.QR_CODE,
                            400, 400);
                    qrCodeView.setImageBitmap(bitmap);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                dialog.show();
            }
        });

        sendBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator i = new IntentIntegrator(UserHomePage.this);
                startActivityForResult(i.createScanIntent(),2);
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(UserHomePage.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.dialog_add_vendor);
                final EditText vendorNameET = dialog.findViewById(R.id.vendorName);
                final EditText vendorMobileET = dialog.findViewById(R.id.vendorMobile);
                final EditText vendorPassword = dialog.findViewById(R.id.vendorPassword);
                Button addVendorBtn = dialog.findViewById(R.id.add_vendor_btn);
                Button getLocation = dialog.findViewById(R.id.get_location_btn);
                vendorLatitude = dialog.findViewById(R.id.vendorlatitude);
                vendorLongitude = dialog.findViewById(R.id.vendorlongitude);
                getLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(UserHomePage.this,MapsActivity.class);
                        startActivityForResult(i,1);
                    }
                });
                addVendorBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String vendorName = vendorNameET.getText().toString().trim();
                        String vendorMobile = vendorMobileET.getText().toString().trim();
                        String vendorLongitudeStr = vendorLongitude.getText().toString().trim();
                        String vendorLatitudeStr = vendorLatitude.getText().toString().trim();
                        String vendorPasswordStr = vendorPassword.getText().toString().trim();
                        if (!vendorMobile.isEmpty()) {
                            if (!vendorPasswordStr.isEmpty()) {
                                if (!vendorLatitudeStr.isEmpty()) {
                                    Vendor vendors = new Vendor(Double.parseDouble(vendorLongitudeStr)
                                            , Double.parseDouble(vendorLatitudeStr), vendorName,
                                            0, encrypytion.encrypt(vendorPasswordStr) );
                                    DocumentReference documentReference1 = firebaseFirestore
                                            .collection("Vendor").document(vendorMobile);
                                    documentReference1.set(vendors);
                                    dialog.dismiss();
                                } else
                                    vendorLongitude.setError("Please click on the get vebor button and" +
                                            " select the vendors location so that the longitide and " +
                                            "latitude be self generated");
                            }else
                                vendorPassword.setError("Please enter the vendors Password");
                        }else
                            vendorMobileET.setError("Please enter the vendors mobile number");
                    }
                });
                dialog.show();
            }
        });

        switch (role){
            case "Customer":
                documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        Costumer costumer = documentSnapshot.toObject(Costumer.class);
                        String userNameString = costumer.getName();
                        if (userNameString.equals("")){
                            userName.setText(mobile);
                        }else
                            userName.setText(userNameString);
                        tokensTV.setText(String.valueOf(costumer.getCredit()));
                    }
                });
                break;
            case "Vendor":
                documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        Vendor vendors = documentSnapshot.toObject(Vendor.class);
                        String userNameString = vendors.getName();
                        if (userNameString.equals("")){
                            userName.setText(mobile);
                        }else
                            userName.setText(userNameString);
                        tokensTV.setText(String.valueOf(vendors.getCredit()));
                    }
                });
                break;
            case "Admin":
                floatingActionButton.setVisibility(View.VISIBLE);
                recieveBTN.setVisibility(View.GONE);
                documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        Admin admins = documentSnapshot.toObject(Admin.class);
                        String userNameString = admins.getName();
                        if (userNameString.equals("")){
                            userName.setText(mobile);
                        }else
                            userName.setText(userNameString);
                        adminsTotalCredit.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                d = documentSnapshot.get("total credit").toString();
                                tokensTV.setText(d);
                            }
                        });
                    }
                });
                break;
        }
    }

    private void sendCreditTo(final String mobileNo) {
        recieverDocument = firebaseFirestore.collection("Costumer").document(mobileNo);
        recieverDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
              if (documentSnapshot.exists()){
                  createSendToCustomerDialog(mobileNo);
              }else{
                  recieverDocument = firebaseFirestore.collection("Vendor").document(mobileNo);
                  recieverDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                      @Override
                      public void onSuccess(DocumentSnapshot documentSnapshot) {
                          if (documentSnapshot.exists()){
                              createSendToVendorDialog(mobileNo);
                          }else
                              Toast.makeText(UserHomePage.this,"Sorry the QR Code you just scanned" +
                                      " doesnt belong to any of our users",Toast.LENGTH_LONG).show();
                      }
                  });
              }
            }
        });

    }

    private void createSendToCustomerDialog(final String mobileNo){
        costRecieverRequestsRefrence = firebaseFirestore.collection("Costumer").document(mobileNo)
                .collection("Requests").document(mobile);
        final Dialog dialog = new Dialog(UserHomePage.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_send_credit);
        final EditText ammountET = dialog.findViewById(R.id.ammount_et);
        Button sendCreditBTN = dialog.findViewById(R.id.send_credit_btn);
        sendCreditBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String creditAmmount = ammountET.getText().toString();
                if (!creditAmmount.isEmpty()){
                    final int creditAmmountFloat = Integer.parseInt(creditAmmount);
                    documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Log.e("loggy",role);
                            if (role.equals("Costumer")){
                                Costumer costumers = documentSnapshot.toObject(Costumer.class);
                                int myCredit = costumers.getCredit();
                                Log.e("loggy", String.valueOf(myCredit-creditAmmountFloat));
                                if (myCredit >= creditAmmountFloat){
                                    Log.e("loggy","logg");
                                    sendARequest("Costumer",creditAmmountFloat);
//                                    addVendorDialog.dismiss();
                                    waitForCostAccepting("Costumer" , creditAmmountFloat
                                            ,myCredit , mobileNo);
                                }else
                                    Toast.makeText(UserHomePage.this,"Sorry" +
                                            " thats to much",Toast.LENGTH_SHORT).show();
                            }
                            else if (role.equals("Vendor")){
                                Vendor vendors = documentSnapshot.toObject(Vendor.class);
                                int myCredit = vendors.getCredit();
                                if (myCredit >= creditAmmountFloat){
                                    sendARequest("Costumer",creditAmmountFloat);
                                    dialog.dismiss();
                                    waitForCostAccepting("Vendor" , creditAmmountFloat
                                            , myCredit , mobileNo);
                                }else
                                    Toast.makeText(UserHomePage.this,"Sorry" +
                                            " thats to much",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else
                    ammountET.setError("Please specify the ammount of credit you want to send");
            }
        });
        dialog.show();
    }

    private void createSendToVendorDialog(final String mobileNo){
        vendRecieverRequestsRefrence = firebaseFirestore.collection("Vendor").document(mobileNo)
                .collection("Requests").document(mobile);
        final Dialog dialog = new Dialog(UserHomePage.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_send_credit);
        final EditText ammountET = dialog.findViewById(R.id.ammount_et);
        Button sendCreditBTN = dialog.findViewById(R.id.send_credit_btn);
        sendCreditBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String creditAmmount = ammountET.getText().toString();
                if (!creditAmmount.isEmpty()){
                    final int creditAmmountFloat = Integer.parseInt(creditAmmount);
                    documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                           if (role.equals("Admin")){
                                sendARequest("Vendor",creditAmmountFloat);
                                dialog.dismiss();
                                waitForVendorAccepting( mobileNo, creditAmmountFloat);
                            }
                        }
                    });
                }else
                    ammountET.setError("Please specify the ammount of credit you want to send");
            }
        });
        dialog.show();
    }

    private void sendARequest(String role ,int creditAmmountFloat){
        Map<String , Object> request = new HashMap<>();
        request.put("Ammount" , creditAmmountFloat);
        request.put("Response","No");
        request.put("Mobile",mobile);
        if (role.equals("Costumer")){
            costRecieverRequestsRefrence.set(request);
        }else if (role.equals("Vendor")){
            vendRecieverRequestsRefrence.set(request);
        }
    }

    private void waitForCostAccepting(final String role , final int credit , final int mycredit , final String recieverMobile){
        costRecieverRequestsRefrence.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (documentSnapshot.exists()){
                    String response = documentSnapshot.get("Response").toString();
                    if (response.equals("Yes")){
                        if (role.equals("Costumer")){
                            documentReference.update("credit",mycredit-credit);
                            final DocumentReference documentReference7 = firebaseFirestore
                                    .collection("Costumer")
                                    .document(recieverMobile);
                            documentReference7.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Costumer costumer = documentSnapshot.toObject(Costumer.class);
                                    float recieverCredit = costumer.getCredit();
                                    documentReference7.update("credit",recieverCredit+credit);
                                }
                            });
                        }
                        else if (role.equals("Vendor")){
                            documentReference.update("credit",mycredit-credit);
                            final DocumentReference documentReference7 = firebaseFirestore.collection("Costumer")
                                    .document(recieverMobile);
                            documentReference7.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Costumer costumer = documentSnapshot.toObject(Costumer.class);
                                    float recieverCredit = costumer.getCredit();
                                    documentReference7.update("credit",recieverCredit+credit);
                                }
                            });
                        }
                        costRecieverRequestsRefrence.delete();
                    }
                }
            }
        });
    }

    private void waitForVendorAccepting(final String recieverMobile , final int credit){
        vendRecieverRequestsRefrence.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (documentSnapshot.exists()){
                    String response = documentSnapshot.get("Response").toString();
                    if (response.equals("Yes")){
                        adminsTotalCredit.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                int adminTotalInt;
                                adminTotal = documentSnapshot.get("total credit").toString();
                                Log.e("loog", String.valueOf(adminTotal));
                                adminTotalInt = Integer.parseInt(adminTotal);
                                adminsTotalCredit.update("total credit",adminTotalInt+credit);
                            }
                        });
                        final DocumentReference documentReference7 = firebaseFirestore
                                .collection("Vendor")
                                .document(recieverMobile);
                        documentReference7.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Vendor vendors = documentSnapshot.toObject(Vendor.class);
                                float recieverCredit = vendors.getCredit();
                                documentReference7.update("credit",recieverCredit+credit);
                            }
                        });
                        vendRecieverRequestsRefrence.delete();
                    }
                }
            }
        });
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1){
            if (resultCode == RESULT_OK){
                vendorLatitude.setText(data.getStringExtra("latitude"));
                vendorLongitude.setText(data.getStringExtra("longitude"));
            }
        }
        if (requestCode == 2){
            IntentResult intentResult = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data);
            if (intentResult != null){
                if (intentResult.getContents() == null){
                    Log.e("contents","Canceled");
                }else{
                    Log.e("contents",intentResult.getContents());
                    sendCreditTo(intentResult.getContents());
                }
            }
        }else
            super.onActivityResult(requestCode, resultCode, data);
    }
}
