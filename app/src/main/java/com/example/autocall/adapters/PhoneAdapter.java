package com.example.autocall.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.autocall.activities.Activity_CallWithMp3;
import com.example.autocall.activities.MainActivity;
import com.example.autocall.R;
import com.example.autocall.models.PhoneModel;

import java.util.ArrayList;

public class PhoneAdapter extends BaseAdapter {
    Activity_CallWithMp3 context;
    ArrayList<PhoneModel> phoneList;
    private int layout;

    public PhoneAdapter(Activity_CallWithMp3 context, int layout, ArrayList<PhoneModel> phoneList){
        this.context = context;
        this.phoneList = phoneList;
        this.layout = layout;
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
        TextView txt_Name,txt_Number;
        ImageView imgDelete,imgEdit;
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder holder;

        if(view == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getLayoutInflater();
            view = inflater.inflate(layout, null);

            holder.txt_Name = view.findViewById(R.id.txt_Name);
            holder.txt_Number = view.findViewById(R.id.txt_Number);
            holder.imgDelete = view.findViewById(R.id.imgDelete_Phone);
            holder.imgEdit = view.findViewById(R.id.imgEdit_Phone);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }

        PhoneModel phone = phoneList.get(i);
        holder.txt_Name.setText(phone.getName());
        holder.txt_Number.setText(phone.getPhone_Number());

        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.DialogDeletePhone(phone);
            }
        });
        holder.imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.DialogEditPhone(phone);
            }
        });

        return view;
    }
}