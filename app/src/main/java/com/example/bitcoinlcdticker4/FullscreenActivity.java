package com.example.bitcoinlcdticker4;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.util.TypedValue;
import android.widget.TextView;
import android.graphics.Typeface;
import android.os.Handler;
import android.provider.Settings.Secure;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class FullscreenActivity extends Activity
{
    private int mInterval = 5000;
    private Handler mHandler;
    private RequestQueue mQueue;
    private String mUrl = "";
    private String mAndroidId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        mAndroidId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        mUrl = "https://avacyn.aerium.hr/tick/" + mAndroidId;

        mHandler = new Handler();
        mQueue = Volley.newRequestQueue(this);
        startRepeatingTask();
    }

    private boolean mTypefaceDigital = false;
    private void SetText(String color, int size, String text)
    {
        TextView tx = findViewById(R.id.fullscreen_content);
        if (mTypefaceDigital == false)
        {
            tx.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/7segment.ttf"));
            mTypefaceDigital = true;
        }
        tx.setTextColor(Color.parseColor(color));
        tx.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        tx.setText(text);
    }

    private void SetError(String message)
    {
        TextView tx = findViewById(R.id.fullscreen_content);
        tx.setTypeface(Typeface.DEFAULT);
        mTypefaceDigital = false;
        tx.setTextColor(Color.parseColor("#ff0000"));
        tx.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
        tx.setText(message);

        mInterval = 5000; // reset interval on error
    }

    Runnable mStatusChecker = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                updateStatus();
            }
            finally
            {
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    private void updateStatus()
    {
        final TextView tx = findViewById(R.id.fullscreen_content);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, mUrl, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                try
                {
                    String text = response.getString("text");
                    int size = response.getInt("size");
                    String color = response.getString("color");
                    int interval = response.getInt("interval");
                    if (interval <= 0)
                    {
                        interval = 5000;
                    }
                    mInterval = interval;

                    SetText(color, size, text);
                }
                catch (JSONException e)
                {
                    SetError("Parsing error");
                }
            }
        },
        new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                SetError(mAndroidId);
            }
        });

        mQueue.add(request);
    }

    void startRepeatingTask()
    {
        mStatusChecker.run();
    }
}
