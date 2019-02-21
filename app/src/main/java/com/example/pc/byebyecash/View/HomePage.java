package com.example.pc.byebyecash.View;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.example.pc.byebyecash.Model.Request;
import com.example.pc.byebyecash.Model.Vendor;
import com.example.pc.byebyecash.R;
import com.example.pc.byebyecash.utils.Encrypytion;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class HomePage extends AppCompatActivity {

    String mobile, role, totalCredit, roleClue, recieverMobile, status, respond,
            requestSenserMobile, requestSenderRole, requestStatus , systemTotalCredit;
    TextView userNameTv, tokensTv, logOutTv, requestAmountET, requestAcceptTV, requestDenyTV,
            respondTV, respondOkTV;
    Button sendBtn, recieveBtn, comfirmVendorBtn, getLocationBtn;
    FloatingActionButton addVendorBtn;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    DocumentReference documentReference, totalCreditRefrence, reciverRefrence, senderRefrence,
            requestSenderRefrence , dr;
    EditText vendorNameET, vendorMobileET, vendorPasswordET, vendorLatitudeET, vendorLongitudeET,
            ammountET;
    Encrypytion encrypytion;
    Dialog addVendorDialog, sendindDialog, recievingDialog, requestDialog, respondDialog;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    float currentBalance, creditAmmount, requestAmount , balanceAfterTransformation;
    ImageView respondImgView;

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

        senderRefrence = firebaseFirestore.collection(role).document(mobile)
                .collection(getString(R.string.requests)).document(mobile);

        updateUI();

        senderRefrence.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    if (recievingDialog != null) {
                        recievingDialog.dismiss();
                    }
                    Request request = documentSnapshot.toObject(Request.class);
                    status = request.getStatus();
                    respond = request.getResponse();
                    requestSenserMobile = request.getSenderMobile();
                    requestSenderRole = request.getSenderRole();
                    requestStatus = request.getStatus();
                    requestAmount = request.getCredit();
                    if (status.equals(getString(R.string.reciever))) {
                        if (respond.equals(getString(R.string.no))) {
                            createRequestingDialog(requestSenserMobile, requestSenderRole,
                                    String.valueOf(requestAmount));
                        }
                    } else if (status.equals(getString(R.string.sender))) {

                        createRespondDialog(respond);

                    }
                }
            }
        });

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
                startActivityForResult(i.createScanIntent(), 2);
            }
        });

        recieveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recievingDialog = new Dialog(HomePage.this);
                recievingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                recievingDialog.setCancelable(true);
                recievingDialog.setContentView(R.layout.dialog_qr_code_view);
                ImageView qrCodeView = recievingDialog.findViewById(R.id.qr_code_img);
                try {
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.encodeBitmap(roleClue + mobile,
                            BarcodeFormat.QR_CODE, 400, 400);
                    qrCodeView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                recievingDialog.show();
            }
        });

        logOutTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settings = getSharedPreferences(getString(R.string.pref_name), Context.MODE_PRIVATE);
                editor = settings.edit();
                editor.clear();
                editor.commit();
                startActivity(new Intent(HomePage.this, MainActivity.class));
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

    private void addAVendor() {
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
                                    0, encrypytion.encrypt(vendorPasswordStr));
                            DocumentReference documentReference1 = firebaseFirestore
                                    .collection("Vendor").document(vendorMobile);
                            documentReference1.set(vendors);
                            addVendorDialog.dismiss();
                        } else
                            vendorLongitudeET.setError("Please click on the get vebor button and" +
                                    " select the vendors location so that the longitide and " +
                                    "latitude be self generated");
                    } else
                        vendorPasswordET.setError("Please enter the vendors Password");
                } else
                    vendorMobileET.setError("Please enter the vendors mobile number");
            }
        });
        addVendorDialog.show();
    }

    private void getLocation() {
        Intent i = new Intent(HomePage.this, MapsActivity.class);
        startActivityForResult(i, 1);
    }

    private void detectRecieverRole(String recieverRole) {
        roleClue = recieverRole.substring(0, 2);
        recieverMobile = recieverRole.substring(2);
        if (roleClue.equals(getString(R.string.costumer_clue))) {

            reciverRefrence = firebaseFirestore.collection(getString(R.string.customer))
                    .document(recieverMobile).collection(getString(R.string.requests)).document(recieverMobile);
            createSendingDialog();

        } else if (roleClue.equals(getString(R.string.vendor_clue))) {

            reciverRefrence = firebaseFirestore.collection(getString(R.string.vendor))
                    .document(recieverMobile).collection(getString(R.string.requests)).document(recieverMobile);
            createSendingDialog();

        } else if (roleClue.equals(getString(R.string.admin_clue))) {

            reciverRefrence = firebaseFirestore.collection(getString(R.string.admin))
                    .document(recieverMobile).collection(getString(R.string.requests)).document(recieverMobile);
            createSendingDialog();

        } else
            Toast.makeText(HomePage.this, R.string.user_not_available, Toast.LENGTH_LONG).show();
    }

    private void createSendingDialog() {
        sendindDialog = new Dialog(HomePage.this);
        sendindDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        sendindDialog.setCancelable(true);
        sendindDialog.setContentView(R.layout.dialog_send_credit);
        ammountET = sendindDialog.findViewById(R.id.ammount_et);
        Button sendCreditBTN = sendindDialog.findViewById(R.id.send_credit_btn);
        sendCreditBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                creditAmmount = Float.valueOf(ammountET.getText().toString());
                if (!ammountET.getText().toString().isEmpty()) {
                    if (creditAmmount != 0) {

                        checkUserBalance();

                    } else
                        ammountET.setError(getString(R.string.specify_amount));
                } else
                    ammountET.setError(getString(R.string.specify_amount));
            }
        });
        sendindDialog.show();
    }

    private void checkUserBalance() {

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    if (role.equals(getString(R.string.customer))) {

                        Costumer costumer = documentSnapshot.toObject(Costumer.class);
                        currentBalance = Float.valueOf(costumer.getCredit());
                        if (currentBalance >= creditAmmount) {

                            Request request = new Request(creditAmmount, getString(R.string.no), mobile, role, getString(R.string.reciever));
                            reciverRefrence.set(request);
                            sendindDialog.dismiss();

                        } else
                            Toast.makeText(HomePage.this, getString(R.string.balance_not_enough)
                                    , Toast.LENGTH_SHORT).show();

                    } else if (role.equals(getString(R.string.vendor))) {

                        Vendor vendor = documentSnapshot.toObject(Vendor.class);
                        currentBalance = Float.valueOf(vendor.getCredit());
                        if (currentBalance >= creditAmmount) {

                            Request request = new Request(creditAmmount, getString(R.string.no), mobile, role, getString(R.string.reciever));
                            reciverRefrence.set(request);
                            sendindDialog.dismiss();

                        } else
                            Toast.makeText(HomePage.this, getString(R.string.balance_not_enough)
                                    , Toast.LENGTH_SHORT).show();

                    } else if (role.equals(getString(R.string.admin))) {

                        Request request = new Request(creditAmmount, getString(R.string.no), mobile, role, getString(R.string.reciever));
                        reciverRefrence.set(request);
                        sendindDialog.dismiss();

                    }
                }
            }
        });

    }

    private void createRequestingDialog(final String senderMobile, final String senderRole, final String amount) {

        requestDialog = new Dialog(HomePage.this);
        requestDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestDialog.setCancelable(true);
        requestDialog.setContentView(R.layout.dialog_request_sending);
        requestAmountET = requestDialog.findViewById(R.id.ammount_to_be_sent_tv);
        requestAcceptTV = requestDialog.findViewById(R.id.accept_tv);
        requestDenyTV = requestDialog.findViewById(R.id.decline_tv);
        requestAmountET.setText(getString(R.string.someone_wants_to_send) + String.valueOf(amount)
                + getString(R.string.token));
        requestAcceptTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestSenderRefrence = firebaseFirestore.collection(senderRole).document(senderMobile)
                        .collection(getString(R.string.requests)).document(senderMobile);
                Request request = new Request(getString(R.string.yes), getString(R.string.sender));
                requestSenderRefrence.set(request);
                performTransformation(senderRole,senderMobile,amount);

            }
        });
        requestDenyTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestSenderRefrence = firebaseFirestore.collection(senderRole).document(senderMobile)
                        .collection(getString(R.string.requests)).document(senderMobile);
                Request request = new Request(getString(R.string.no), getString(R.string.sender));
                requestSenderRefrence.set(request);
                senderRefrence.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        requestDialog.dismiss();
                    }
                });
            }
        });

        requestDialog.show();

    }

    private void createRespondDialog(String respond) {

        respondDialog = new Dialog(HomePage.this);
        respondDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        respondDialog.setCancelable(true);
        respondDialog.setContentView(R.layout.dialog_respond);
        respondTV = respondDialog.findViewById(R.id.respond_tv);
        respondOkTV = respondDialog.findViewById(R.id.ok_tv);
        respondImgView = respondDialog.findViewById(R.id.respond_img);

        if (respond.equals(getString(R.string.no))) {

            respondTV.setText(R.string.request_denied);
            respondImgView.setImageResource(R.drawable.ic_sad_colored);
        }

        respondOkTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                senderRefrence.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        updateUI();
                        respondDialog.dismiss();
                    }
                });
            }
        });

        respondDialog.show();

    }

    private void performTransformation(String senderRole , String senderMobile , String ammount){

        creditAmmount = Float.parseFloat(ammount);

        if (senderRole.equals(getString(R.string.admin))){

            totalCreditRefrence.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    systemTotalCredit = documentSnapshot.get(getString(R.string.total_credit)).toString();
                    balanceAfterTransformation = creditAmmount + Float.valueOf(systemTotalCredit);
                    totalCreditRefrence.update("Total credit",String.valueOf(balanceAfterTransformation));
                }
            });

            dr = firebaseFirestore.collection(role).document(mobile);
            dr.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()){
                        Costumer costumer = documentSnapshot.toObject(Costumer.class);
                        currentBalance = Float.valueOf(costumer.getCredit());
                        balanceAfterTransformation = currentBalance + creditAmmount;
                        dr.update("credit",Integer.valueOf((int) balanceAfterTransformation));
                        requestDialog.dismiss();
                        senderRefrence.delete();
                    }
                }
            });

        }else if (senderRole.equals(R.string.vendor)|| senderRole.equals(R.string.customer)){

            if (role.equals(getString(R.string.admin))){

            }else if (role.equals(R.string.vendor)|| role.equals(R.string.customer)){

            }

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                vendorLatitudeET.setText(data.getStringExtra("latitude"));
                vendorLongitudeET.setText(data.getStringExtra("longitude"));
            }
        } else if (requestCode == 2) {

            IntentResult intentResult = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data);
            if (intentResult != null) {
                if (intentResult.getContents() == null) {
                    Log.e("contents", "Canceled");
                } else {
                    detectRecieverRole(intentResult.getContents());
                }
            }

        } else
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
