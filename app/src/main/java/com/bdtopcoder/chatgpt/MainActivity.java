package com.bdtopcoder.chatgpt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Base64;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bdtopcoder.chatgpt.chatmodel.Message;
import com.bdtopcoder.chatgpt.chatmodel.MessageAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText message_text_text;
    ImageView send_btn;
    List<Message> messageList = new ArrayList<>();
    MessageAdapter messageAdapter;

    CountDownTimer waitTimer;

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //====================================
        message_text_text = findViewById(R.id.message_text_text);
        send_btn = findViewById(R.id.send_btn);
        recyclerView = findViewById(R.id.recyclerView);

        // Create Layout behaves and set it in recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        //====================================

        //====================================
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        //====================================

        send_btn.setOnClickListener(view -> {
            String question = message_text_text.getText().toString().trim();
            addToChat(question,Message.SEND_BY_ME);
            message_text_text.setText("");
            callAPI(question);
        });

    } // OnCreate Method End Here ================

    void addToChat (String message, String sendBy){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sendBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }
    void addToChat (Bitmap bitmap, String sendBy){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

//        ImageView imageView = findViewById(R.id.image_view);
//        imageView.setImageBitmap(bitmap);
                messageList.add(new Message(bitmap, sendBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    } // addToChat End Here =====================

    void addResponse(String response){
        messageList.remove(messageList.size()-1);
        addToChat(response, Message.SEND_BY_BOT);
    }
    void addResponse(Bitmap bitmap){

        messageList.remove(messageList.size()-1);
        addToChat(bitmap, Message.SEND_BY_BOT);
    } // addResponse End Here =======

    void callAPI(String prompt){
        // okhttp
        messageList.add(new Message("Typing...", Message.SEND_BY_BOT));

        JSONObject jsonBody = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
//            { role: "system", content: "You are a helpful assistant." },
//            JSONObject jsonObject1 = new JSONObject();
//            jsonObject1.put("role", "system");
//            jsonObject1.put("content", "You are a helpful assistant.");
//
////            { role: "user", content: prompt },
//            JSONObject jsonObject2 = new JSONObject();
//            jsonObject2.put("role", "user");
//            jsonObject2.put("content", prompt);
//
//            jsonArray.put(jsonObject1);
//            jsonArray.put(jsonObject2);


            jsonBody.put("prompt", prompt);
            jsonBody.put("nSteps", 20);


//            jsonBody.put("prompt", prompt);


//            jsonBody.put("model","text-davinci-003");
//            jsonBody.put("prompt", question);
//            jsonBody.put("max_tokens",4000);
//            jsonBody.put("temperature",1.5);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(),JSON);
        Request request = new Request.Builder()
                .url(API.API_URL + "/textToImage")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to"+e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()){
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());

                        String result = jsonObject.getString("base64");
                        System.out.println(result);
//                        String result = jsonArray.getJSONObject(0).getJSONObject("message").getString("content");
                        addResponse("hello");

                        // Get the Base64 encoded string
                        String base64String = result;

// Decode the Base64 string into a byte array
                        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

// Create a Bitmap object from the byte array
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

// Display the Bitmap in an ImageView
                        addResponse(bitmap);



                        if(waitTimer != null) {
                            waitTimer.cancel();
                            waitTimer.onFinish();
                            waitTimer = null;
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    addResponse("Failed to load response due to"+response.body().toString());
                }

            }
        });

        waitTimer = new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                //called every 300 milliseconds, which could be used to
                //send messages or some other action

                Request request = new Request.Builder()
                        .url(API.API_URL + "/status")
                        .get()
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        addResponse("Failed to load response due to"+e.getMessage());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()){
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(response.body().string());

                                String result = jsonObject.getString("progressPct");
                                System.out.println(result + "%");
//                        String result = jsonArray.getJSONObject(0).getJSONObject("message").getString("content");
                        addResponse("hello");
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
//                    addResponse("Failed to load response due to"+response.body().toString());
                        }

                    }
                });
            }

            public void onFinish() {
                //After 60000 milliseconds (60 sec) finish current
                //if you would like to execute something when time finishes
                System.out.println("image generation done");
            }
        }.start();



//        final long period = 1000;
//
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                // do your task here
//
//
//
//            }
//        }, 0, period);



    } // callAPI End Here =============


} // Public Class End Here =========================