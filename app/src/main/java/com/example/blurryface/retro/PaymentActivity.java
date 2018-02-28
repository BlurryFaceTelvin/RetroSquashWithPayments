package com.example.blurryface.retro;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TimeUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.africastalking.AfricasTalking;
import com.africastalking.models.payment.checkout.CheckoutResponse;
import com.africastalking.models.payment.checkout.MobileCheckoutRequest;
import com.africastalking.services.PaymentService;
import com.africastalking.utils.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import dmax.dialog.SpotsDialog;

/**
 * Created by BlurryFace on 2/26/2018.
 */

public class PaymentActivity extends AppCompatActivity {
    PaymentService paymentService;
    TextView life1,life2,life3;
    OkHttpClient client;
    Request request;
    int status;
    boolean onFirstResume;
    SpotsDialog dialog;
    int initialscore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_layout);
        //get the score user had before game over
        String score = getIntent().getStringExtra("score");
        initialscore = Integer.parseInt(score);
        //initialise UI
        life1 = findViewById(R.id.oneLifeTxt);
        life2 = findViewById(R.id.twolivesText);
        life3 = findViewById(R.id.threeLives);
        try {
            AfricasTalking.initialize("192.168.1.130",35897, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //initialise the dialog
        dialog = new SpotsDialog(this,"Processing");
        onFirstResume = true;
        status = 0;
    }
    public void onBuyOne(View view){
        //user buys one life
        final String amount = life1.getText().toString();
        dialog.show();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                payment(amount);

            }
        });
        status = 5;

    }
    public void onBuyTwo(View view){
        //user buys two lives
        final String amount = life2.getText().toString();
        dialog.show();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                payment(amount);

            }
        });
        status = 5;
    }
    public void onBuyThree(View view){
        //user buys three lives
        final String amount = life3.getText().toString();
        dialog.show();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                payment(amount);

            }
        });
        status = 5;
    }
    public void onCancel(View view){
        Intent intent = new Intent(PaymentActivity.this,GameOverActivity.class);
        intent.putExtra("score",String.valueOf(initialscore));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    public void payment(String amount){
        try {
            paymentService = AfricasTalking.getPaymentService();
            MobileCheckoutRequest checkoutRequest = new MobileCheckoutRequest("MusicApp",amount,"0703280748");
            paymentService.checkout(checkoutRequest, new Callback<CheckoutResponse>() {
                @Override
                public void onSuccess(CheckoutResponse data) {
                    Toast.makeText(PaymentActivity.this,data.status,Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Throwable throwable) {
                    dialog.dismiss();
                }

            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void confirmPayment(){
        client = new OkHttpClient();
        request = new Request.Builder().url("http://192.168.1.130:30001/transaction/status").build();
        client.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                dialog.dismiss();
                Log.e("failure",e.getMessage());
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                dialog.dismiss();
                String status = response.body().string();
                if(status.equals("Failed")){
                    //if it fails to pay sends you to game over page
                    Intent intent = new Intent(PaymentActivity.this,GameOverActivity.class);
                    intent.putExtra("score",String.valueOf(initialscore));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                }else if(status.equals("Success")){
                    //if successful resume the game with your previous score and an extra life
                    Intent intent = new Intent(PaymentActivity.this,MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("extralives",String.valueOf(1));
                    intent.putExtra("initialscore",String.valueOf(initialscore));
                    startActivity(intent);
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //when user first gets to the activity
        if(onFirstResume){
            onFirstResume = false;
            Log.e("resume",String.valueOf(status));
        }else if(!onFirstResume&&status==5) {
            //after mpesa pop up
            status = 3;
            Log.e("resume",String.valueOf(status));
            /*
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */
            dialog.show();
            //wait for ten seconds to confirm
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    confirmPayment();
                }
            }, 10000);
        }else{
            Log.e("resume","normal");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(status==5){
            status = 5;
        }
        else {
            status=3;
        }

    }
}
