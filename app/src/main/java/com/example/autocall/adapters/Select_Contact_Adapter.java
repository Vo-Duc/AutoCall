package com.example.autocall.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.autocall.R;
import com.example.autocall.activities.SelectContacts_Activity;
import com.example.autocall.models.PhoneModel;

import java.util.ArrayList;

public class Select_Contact_Adapter extends BaseAdapter {
    SelectContacts_Activity context;
    ArrayList<PhoneModel> phoneList;
    private int layout;
    boolean[] checkBoxState;

    public Select_Contact_Adapter(SelectContacts_Activity context, int layout, ArrayList<PhoneModel> phoneList){
        this.context = context;
        this.phoneList = phoneList;
        this.layout = layout;

        checkBoxState=new boolean[phoneList.size()];
    }

    @Override
    public int getCount() {
        return phoneList.size();
    }

    @Override
    public Object getItem(int i) {
        return phoneList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    private class ViewHolder{
        TextView txt_Name_Contact,txt_Number_Contact;
        CheckBox checkBox_Contact;
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder holder;

        if(view == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getLayoutInflater();
            view = inflater.inflate(layout, null);

            holder.txt_Name_Contact = view.findViewById(R.id.txt_Name_Contact);
            holder.txt_Number_Contact = view.findViewById(R.id.txt_Number_Contact);
            holder.checkBox_Contact = view.findViewById(R.id.checkBox_Contact);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }

        PhoneModel phone = phoneList.get(i);
        holder.txt_Name_Contact.setText(phone.getName());
        holder.txt_Number_Contact.setText(phone.getPhone_Number());
        holder.checkBox_Contact.setChecked(checkBoxState[i]);
        holder.checkBox_Contact.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if(((CheckBox)v).isChecked())
                    checkBoxState[i]=true;
                else
                    checkBoxState[i]=false;

            }
        });

        if(holder.checkBox_Contact.isChecked()){

        }

        return view;
    }
    public void selectedAll() {
        for(int i = 0; i< checkBoxState.length; i++){
            checkBoxState[i] = true;
        }
        notifyDataSetChanged();
    }
    public void deselectedAll() {
        for(int i = 0; i< checkBoxState.length; i++){
            checkBoxState[i] = false;
        }
        notifyDataSetChanged();
    }
    public ArrayList finish_Select(){
        ArrayList<PhoneModel> select_List = new ArrayList<>();
        for(int i = 0;i<phoneList.size();i++){
            if(checkBoxState[i]==true){
                select_List.add(phoneList.get(i));
            }
        }
        return select_List;
    }
}