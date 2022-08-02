package com.example.autocall.activities;


import static android.telecom.TelecomManager.ACTION_CHANGE_DEFAULT_DIALER;
import static android.telecom.TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.PreciseCallState;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.autocall.R;
import com.example.autocall.adapters.AudioAdapter;
import com.example.autocall.adapters.PhoneAdapter;
import com.example.autocall.models.AudioModel;
import com.example.autocall.models.PhoneModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Activity_CallWithMp3 extends AppCompatActivity {
    private static int WRITE_EXTERNAL_PERMISSION_CODE=210;
    private static int MY_PERMISSION_REQUEST_CODE_CALL_PHONE = 555;
    private static int PICK_CONTACTS_REQUEST = 502;
    private MediaRecorder myAudioRecorder;
    private MediaPlayer mediaPlayer;
    //RecyclerView list_Phone;
    ListView listView_Phone, listView_Audio;
    TextView txt_Add_Phone, txt_Add_Mp3, txt_Run;
    Spinner spinner_Sim;
    List<String> list_Sim;
    EditText edit_Mp3;
    ArrayList<PhoneModel> phoneList = new ArrayList<>();
    ArrayList<PhoneModel> phoneList_Temp = new ArrayList<>();
    ArrayList<PhoneModel> select_List = new ArrayList<>();
    PhoneAdapter adapter;
    PhoneModel phoneModel;
    AudioAdapter adapterAudio;
    AudioModel selected_Audio;
    //ArrayAdapter arrayAdapterAudio;
    ArrayList<AudioModel> audioModelList = new ArrayList<>();
    Toolbar toolbar;
    boolean wentIntoCall = false;
    int selected_Sim;
    // ...
    //private CallStateListener mCallStateListener = new CallStateListener();
    private TelephonyManager mTelephonyManager;
    private int mCallState;
    public static boolean wasRinging;
    // ...

    private final int PICK_CSV_REQUEST = 205;

    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference storageReference;
    @Override
    protected void onResume() {
        super.onResume();
        mTelephonyManager.listen(mnPhoneStateListener, PhoneStateListener.LISTEN_PRECISE_CALL_STATE);
        //mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        /*if(wentIntoCall)
            callNow("0364647812");*/
    }
    @Override
    protected void onStop() {
        super.onStop();
        mTelephonyManager.listen(mnPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        //mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
    }
    @Override
    public void onStart() {
        super.onStart();
        offerReplacingDefaultDialer();

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_with_mp3);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.ic_icon_test);
        setSupportActionBar(toolbar);
        ViewBinding();
        phoneList.add(new PhoneModel("duc","123"));
        TelecomManager systemService = this.getSystemService(TelecomManager.class);
        if (systemService != null && !systemService.getDefaultDialerPackage().equals(this.getPackageName())) {
            startActivity((new Intent(ACTION_CHANGE_DEFAULT_DIALER)).putExtra(EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, this.getPackageName()));
        }
        getPermission();
//        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PRECISE_PHONE_STATE)
//            != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.READ_PRECISE_PHONE_STATE}, 1);
//        }
        list_Sim = new ArrayList<>();
        list_Sim.add("Sim 1");
        list_Sim.add("Sim 2");
        spinner_Sim = (Spinner) findViewById(R.id.spinner_Sim);
        ArrayAdapter spinnerAdapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, list_Sim);

        spinner_Sim.setAdapter(spinnerAdapter);
        spinner_Sim.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                //đối số postion là vị trí phần tử trong list Data
                selected_Sim = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                //Toast.makeText(Activity_CallWithMp3.this, "onNothingSelected", Toast.LENGTH_SHORT).show();
            }
        });

        try{
            select_List = (ArrayList<PhoneModel>) getIntent().getSerializableExtra("select_List");
        }catch (Exception e){}
        mTelephonyManager = (TelephonyManager) getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        mCallState = mTelephonyManager.getCallState();
        //mTelephonyManager.listen((PhoneStateListener) mCallStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        loadPhone();
        txt_Add_Phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogAdd();
            }
        });

        txt_Run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    phoneList_Temp.clear();
                }
                catch (Exception e){}
                for (PhoneModel item:phoneList) {
                    phoneList_Temp.add(item);
                }
                if(phoneList_Temp.size()==0){
                    Toast.makeText(getApplicationContext(), "Vui lòng thêm số điện thoại", Toast.LENGTH_LONG).show();
                }
                else if(edit_Mp3.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "Vui lòng thêm tệp Mp3", Toast.LENGTH_LONG).show();
                }
                else {
                    callNow(phoneList_Temp.get(0).getPhone_Number());
                }
            }
        });
        txt_Add_Mp3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogAddMp3();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_import_csv, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_import_csv:
                loadCsvFromStorage();
                //readCsvFile();
                break;
            case R.id.menu_delete_csv:
                phoneList.clear();
                loadPhone();
                break;
            case R.id.menu_contacts:
//                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
//                startActivityForResult(intent, PICK_CONTACTS_REQUEST);
                //getContactList();
                startActivity(new Intent(Activity_CallWithMp3.this, SelectContacts_Activity.class));

                break;
            default:break;
        }

        return super.onOptionsItemSelected(item);
    }

    // When results returned
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_PERMISSION_REQUEST_CODE_CALL_PHONE) {
            if (resultCode == RESULT_OK) {
                // Do something with data (Result returned).
                Toast.makeText(this, "Action OK", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Action Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Action Failed", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == PICK_CSV_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            Uri uri = data.getData();
            if(uri.getPath().endsWith(".csv")) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(data.getData());
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream, Charset.forName("UTF-8"))
                    );
                    String line = "";
                    try {
                        phoneList.clear();
                        reader.readLine();
                        while ((line = reader.readLine()) != null) {
                            String[] tokens = line.split(",");

                            //Read data
                            PhoneModel phoneModel = new PhoneModel(tokens[0], tokens[1]);
                            phoneList.add(phoneModel);
                        }
                        loadPhone();
                        Toast.makeText(Activity_CallWithMp3.this, "Đã thêm tệp csv!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                    }

                } catch (FileNotFoundException e) {
                    Toast.makeText(Activity_CallWithMp3.this, "Đã xảy ra lỗi!", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
            else{
                Toast.makeText(Activity_CallWithMp3.this, "Vui lòng chọn file .csv!", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == PICK_CONTACTS_REQUEST && resultCode == RESULT_OK)
        {
            Uri contactData = data.getData();
            Cursor c = getContentResolver().query(contactData, null, null, null, null);
            if (c.moveToFirst()) {
                System.out.println(c.getColumnNames());
                System.out.println(c.getString(1));
            }
        }
    }
    private void ViewBinding(){
        txt_Add_Phone=findViewById(R.id.txt_Add_Phone);
        txt_Add_Mp3=findViewById(R.id.txt_Add_Mp3);
        txt_Run=findViewById(R.id.txt_Run);
        edit_Mp3=findViewById(R.id.edit_Mp3);
        //list_Phone=findViewById(R.id.list_Phone);
        listView_Phone=findViewById(R.id.listview_Phone);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

    }
    private final static String simSlotName[] = {
            "extra_asus_dial_use_dualsim",
            "com.android.phone.extra.slot",
            "slot",
            "simslot",
            "sim_slot",
            "subscription",
            "Subscription",
            "phone",
            "com.android.phone.DialingMode",
            "simSlot",
            "slot_id",
            "simId",
            "simnum",
            "phone_type",
            "slotId",
            "slotIdx"
    };
    @SuppressLint("MissingPermission")
    private void callNow(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        try {
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            callIntent.putExtra("com.android.phone.force.slot", true);
            callIntent.putExtra("Cdma_Supp", true);
            //Add all slots here, according to device.. (different device require different key so put all together)
            for (String s : simSlotName)
                callIntent.putExtra(s, selected_Sim); //0 or 1 according to sim.......

            this.startActivity(callIntent);
            startRecord(phoneNumber);
        } catch (Exception ex) {
            try{
                stopRecord();
            }catch (Exception e){}
            Toast.makeText(getApplicationContext(), "Your call failed... " + ex.getMessage(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    //TelephonyManager telM = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
    PhoneStateListener mnPhoneStateListener = new PhoneStateListener( ) {
        @Override
        public void onPreciseCallStateChanged (PreciseCallState callState){
            //Call back when there is a precise call state
            super.onPreciseCallStateChanged(callState);
            int t = callState.getForegroundCallState();
            t = callState.getBackgroundCallState();
            t = callState.getRingingCallState();
            switch(callState.getForegroundCallState()){
                case PreciseCallState.PRECISE_CALL_STATE_ACTIVE:
                    Toast.makeText(Activity_CallWithMp3.this, "PRECISE_CALL_STATE_ACTIVE", Toast.LENGTH_SHORT).show();
                case PreciseCallState.PRECISE_CALL_STATE_DIALING:
                    Toast.makeText(Activity_CallWithMp3.this, "PRECISE_CALL_STATE_DIALING", Toast.LENGTH_SHORT).show();
                case PreciseCallState.PRECISE_CALL_STATE_IDLE:
                    Toast.makeText(Activity_CallWithMp3.this, "PRECISE_CALL_STATE_IDLE", Toast.LENGTH_SHORT).show();

            }
        }
    };

    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            //mCallState = mTelephonyManager.getCallState();

//            switch (state) {
//
//                case TelephonyManager.CALL_STATE_IDLE:
//                    wasRinging = false;
//                    try {
//                        db.collection("Audio").document(selected_Audio.getId()).update("status", "0");
//                    }
//                    catch (Exception e){}
//                    //Toast.makeText(Activity_CallWithMp3.this, "kết thúc cuộc gọi", Toast.LENGTH_SHORT).show();
//                    if(wentIntoCall==true){
//                        try {
//                            stopRecord();
//                        }
//                        catch (Exception e){}
//                        if(phoneList_Temp.size()==1){
//                            wentIntoCall=false;
//                            //db.collection("User").document(auth.getUid()).update("select_Audio", "0");
//                        }
//                        else if(phoneList_Temp.size()!=0){
//                            phoneList_Temp.remove(0);
//                            wentIntoCall=false;
//                            callNow(phoneList_Temp.get(0).getPhone_Number());
//                        }
//                    }
//                    break;
//                case TelephonyManager.CALL_STATE_RINGING:
//                    wasRinging = true;
//                    Toast.makeText(Activity_CallWithMp3.this, "đổ chuông", Toast.LENGTH_SHORT).show();
//                    break;
//                case TelephonyManager.CALL_STATE_OFFHOOK:
//                    if (!wasRinging) {
//                        try {
//                            //Kiểm tra trạng thái bắt máy
//                            // ???????????????????
//                            db.collection("Audio").document(selected_Audio.getId()).update("status", "1");
//                            //Toast.makeText(Activity_CallWithMp3.this, "bắt máy", Toast.LENGTH_SHORT).show();
//                            wentIntoCall = true;
//                        }catch (Exception e) {}
//                    }
//                    wasRinging = true;
//                    break;
//            }
        }
    };

    private void startRecord(String number) throws IllegalStateException, IOException {
        try {
            myAudioRecorder = new MediaRecorder();
            myAudioRecorder.reset();
            myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            myAudioRecorder.setOutputFile(getRecordingFilePath(number));
            myAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            myAudioRecorder.prepare();

            myAudioRecorder.start();
            //Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
        } catch (IllegalStateException ise) {
            // make something ...
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void stopRecord(){
        myAudioRecorder.stop();
        myAudioRecorder.release();
        myAudioRecorder=null;
        //Toast.makeText(getApplicationContext(), "Audio Recorder stopped", Toast.LENGTH_LONG).show();
    }
    private boolean isMicrophonePresent(){
        if(this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)){
            return true;
        }
        else{
            return false;
        }
    }
    private void getPermission() {
        String[] permissions = { Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PRECISE_PHONE_STATE, Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG, Manifest.permission.PROCESS_OUTGOING_CALLS };
        ActivityCompat.requestPermissions(this, permissions, WRITE_EXTERNAL_PERMISSION_CODE);
    }
    private String getRecordingFilePath(String number) {
//        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
//        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
//        File file = new File(musicDirectory, "testRecordingFile" + ".mp3");
//        return file.getPath();
        String path = Environment.getExternalStorageDirectory().toString()+"/Music/"+System.currentTimeMillis()+"+"+number+".3gp";
        return path;
    }

    //Xử lý listview
    private void loadPhone(){
        if(select_List!=null && select_List.size()!=0){
            try {
                phoneList.clear();
            }
            catch (Exception e){}
            for (PhoneModel item:select_List) {
                phoneList.add(item);
            }
            select_List.clear();
        }
        adapter = new PhoneAdapter(this, R.layout.item_phone, phoneList);
        listView_Phone.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void DialogAdd(){
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_phone);

        EditText add_Name = (EditText) dialog.findViewById(R.id.add_Name);
        EditText add_Number = (EditText) dialog.findViewById(R.id.add_Number);
        Button btn_Add = (Button) dialog.findViewById(R.id.btnSaveAddPhone);
        Button btn_Cancel = (Button) dialog.findViewById(R.id.btnCancelAddPhone);

        btn_Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(add_Name.getText().toString().equals("") || add_Number.getText().toString().equals("")){
                    Toast.makeText(Activity_CallWithMp3.this, "Vui long nhap day du!", Toast.LENGTH_SHORT).show();
                }else{
                    // Add a new data record
                    phoneModel = new PhoneModel(add_Name.getText().toString(),add_Number.getText().toString());

                    phoneList.add(phoneModel);
                    loadPhone();
                    Toast.makeText(Activity_CallWithMp3.this, "Added!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
        btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void DialogDeletePhone(PhoneModel phone){
        AlertDialog.Builder dialogDelete = new AlertDialog.Builder(this);
        dialogDelete.setMessage("Do you want to delete this contact?");
        dialogDelete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                phoneList.remove(phone);
                loadPhone();
                Toast.makeText(Activity_CallWithMp3.this, "Deleted", Toast.LENGTH_SHORT).show();
            }
        });
        dialogDelete.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialogDelete.show();
    }
    public void DialogEditPhone(PhoneModel phone){
        Dialog dialogEdit = new Dialog(this);
        dialogEdit.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogEdit.setContentView(R.layout.dialog_edit_phone);

        EditText edit_Name = (EditText) dialogEdit.findViewById(R.id.edit_Name);
        EditText edit_Phone = (EditText) dialogEdit.findViewById(R.id.edit_Number);
        Button btn_Save = (Button) dialogEdit.findViewById(R.id.btnSaveEditPhone);
        Button btn_Cancel = (Button) dialogEdit.findViewById(R.id.btnCancelEditPhone);

        edit_Name.setText(phone.getName());
        edit_Phone.setText(phone.getPhone_Number());

        btn_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edit_Name.getText().toString().equals("") || edit_Phone.getText().toString().equals("")){
                    Toast.makeText(Activity_CallWithMp3.this, "Vui long nhap day du!", Toast.LENGTH_SHORT).show();
                }else {
                    phone.setName(edit_Name.getText().toString().trim());
                    phone.setPhone_Number(edit_Phone.getText().toString().trim());

                    loadPhone();
                    Toast.makeText(Activity_CallWithMp3.this, "Updated!", Toast.LENGTH_SHORT).show();
                    dialogEdit.dismiss();
                }
            }
        });

        btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogEdit.dismiss();
            }
        });

        dialogEdit.show();
    }
    Dialog dialog_Add_Mp3;
    private void DialogAddMp3(){
        dialog_Add_Mp3 = new Dialog(this);
        dialog_Add_Mp3.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_Add_Mp3.setContentView(R.layout.dialog_add_mp3);

        listView_Audio = dialog_Add_Mp3.findViewById(R.id.list_View_ChooseMp3);
        adapterAudio = new AudioAdapter(Activity_CallWithMp3.this,R.layout.item_audio, audioModelList);
        listView_Audio.setAdapter(adapterAudio);
        loadAudio();

        dialog_Add_Mp3.show();
    }
    private void loadAudio(){
        audioModelList.clear();
        db.collection("Audio").whereEqualTo("user_Id", auth.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    AudioModel audioModel = doc.toObject(AudioModel.class);
                    audioModelList.add(audioModel);
                }
                adapterAudio.notifyDataSetChanged();
            }
        });
    }
    public void Choose_Audio(AudioModel audioModel){
        selected_Audio = audioModel;
        final String[] id = new String[1];
        db.collection("User").whereEqualTo("id", auth.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    id[0] = doc.getId();
                }
                db.collection("User").document(id[0]).update("select_Audio", audioModel.getId());
                edit_Mp3.setText(selected_Audio.getName());
                dialog_Add_Mp3.dismiss();
                Toast.makeText(Activity_CallWithMp3.this, "Đã chọn File " + audioModel.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void readCsvFile(String path){

    }
    private void loadCsvFromStorage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_CSV_REQUEST);
    }
    private void offerReplacingDefaultDialer() {
        Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
        intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, Activity_CallWithMp3.this.getPackageName());
        startActivity(intent);
    }
}