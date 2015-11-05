package com.javapapers.androidreceivesms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";

    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";
            for (int i = 0; i < sms.length; ++i) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);

                String smsBody = smsMessage.getMessageBody().toString();
                String address = smsMessage.getOriginatingAddress();

                smsMessageStr += "SMS From: " + address + "\n";
                smsMessageStr += smsBody + "\n";

                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("https://hooks.slack.com/services/");
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
                nameValuePair.add(new BasicNameValuePair("payload", "{\"text\": \""+smsMessageStr+"\"}"));

                //Encoding POST data
                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));

                } catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }

                try {
                    HttpResponse response = httpClient.execute(httpPost);
                    // write response to log
                    Log.d("Http Post Response:", response.toString());
                } catch (ClientProtocolException e) {
                    // Log exception
                    e.printStackTrace();
                } catch (IOException e) {
                    // Log exception
                    e.printStackTrace();
                }
            }
//            Toast.makeText(context, smsMessageStr, Toast.LENGTH_SHORT).show();

            //this will update the UI with message
            SmsActivity inst = SmsActivity.instance();
            inst.updateList(smsMessageStr);
        }
    }
}