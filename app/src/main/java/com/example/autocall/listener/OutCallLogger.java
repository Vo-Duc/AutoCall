package com.example.autocall.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.PreciseCallState;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.widget.Toast;

import com.android.server.am.ServiceRecordProto;

public class OutCallLogger extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        showToast(context, "hello");
//        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        telephonyManager.listen(new PhoneStateListener() {
//            public void onCallStateChanged(int state, String number) {
//
//                if (state == TelephonyManager.CALL_STATE_IDLE) {
//                    showToast(context, "CALL_STATE_IDLE");
//                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
//                    showToast(context, "CALL_STATE_OFFHOOK");
//                }
//            }
//        }, PhoneStateListener.LISTEN_CALL_STATE);
    }
    void showToast(Context context,String message){
        Toast toast=Toast.makeText(context,message,Toast.LENGTH_LONG);
        //toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }
}
