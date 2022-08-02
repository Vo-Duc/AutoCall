package com.example.autocall.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.autocall.R;
import com.example.autocall.adapters.Select_Contact_Adapter;
import com.example.autocall.models.PhoneModel;

import java.util.ArrayList;

public class SelectContacts_Activity extends AppCompatActivity {
    private static int CONTACTS_PERMISSION_CODE=205;
    Select_Contact_Adapter adapter;
    Toolbar toolbar;
    ListView listView_Contacts;
    //Button btn_Ok = new Button("", "", "", "");
    ArrayList<PhoneModel> phoneList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_contacts);

        toolbar = findViewById(R.id.toolbar_Select_Contacts);
        toolbar.setLogo(R.drawable.ic_icon_test);
        setSupportActionBar(toolbar);
        ViewBinding();
        getContactList();


    }

    private void ViewBinding(){
        listView_Contacts=findViewById(R.id.listView_Contacts);
        //btn_Ok = findViewById(R.id.btn_select);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select_contact, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_select_all:
                select_All();
                break;
            case R.id.menu_deselect_all:
                deselect_All();
                break;
            case R.id.menu_finish:
                finish_Select();
                break;
            default:break;
        }

        return super.onOptionsItemSelected(item);
    }
    private void loadContact(){
        adapter = new Select_Contact_Adapter(this, R.layout.item_contact, phoneList);
        listView_Contacts.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
    @SuppressLint({"Range","MissingPermission"})
    private void getContactList() {
        if(getContactsPermission()) {
            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null);

            if ((cur != null ? cur.getCount() : 0) > 0) {
                phoneList.clear();
                while (cur != null && cur.moveToNext()) {

                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        Cursor pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            PhoneModel phoneModel = new PhoneModel(name, phoneNo);
                            phoneList.add(phoneModel);
                        }
                        pCur.close();
                    }
                }
                loadContact();
            }
            if (cur != null) {
                cur.close();
            }
        }
    }
    private boolean getContactsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.READ_CONTACTS}, CONTACTS_PERMISSION_CODE);
        }
        return true;
    }
    public void select_All(){
        if(adapter != null)
            adapter.selectedAll();
    }
    public void deselect_All(){
        if(adapter != null)
            adapter.deselectedAll();
    }
    public void finish_Select(){
        if(adapter != null) {
            ArrayList<PhoneModel> select_List = new ArrayList<>();
            select_List = adapter.finish_Select();
            Intent intent = new Intent(SelectContacts_Activity.this, Activity_CallWithMp3.class);
            intent.putExtra("select_List", select_List);
            startActivity(intent);
        }
    }
}