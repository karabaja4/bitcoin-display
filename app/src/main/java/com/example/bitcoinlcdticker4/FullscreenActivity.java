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

    private TextView mTv = null;
    private TextView GetTextView()
    {
        if (mTv == null)
        {
            mTv = findViewById(R.id.fullscreen_content);
        }
        return mTv;
    }

    private Typeface mDigitalTypeface = null;
    private Typeface GetDigitalTypeface()
    {
        if (mDigitalTypeface == null)
        {
            mDigitalTypeface = Typeface.createFromAsset(getAssets(), "fonts/7segment.ttf");
        }
        return mDigitalTypeface;
    }

    private boolean mTypefaceDigital = false;
    private void SetText(String color, int size, String text, boolean digital)
    {
        try
        {
            TextView tx = GetTextView();
            if (digital && !mTypefaceDigital)
            {
                tx.setTypeface(GetDigitalTypeface());
                mTypefaceDigital = true;
            }
            else if (!digital && mTypefaceDigital)
            {
                tx.setTypeface(Typeface.DEFAULT);
                mTypefaceDigital = false;
            }
            tx.setTextColor(Color.parseColor(color));
            tx.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
            tx.setText(text);
        }
        catch (Exception ex)
        {
        }
    }

    private void SetError(String message)
    {
        try
        {
            TextView tx = GetTextView();

            // errors only in default font
            tx.setTypeface(Typeface.DEFAULT);
            mTypefaceDigital = false;

            tx.setTextColor(Color.parseColor("#ff0000"));
            tx.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
            tx.setText(message);
        }
        catch (Exception ex)
        {
        }
    }

    Runnable mStatusChecker = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                if (!mLock)
                {
                    updateStatus();
                }
            }
            catch (Exception e)
            {
            }
            finally
            {
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    private boolean mLock = false;
    private void updateStatus()
    {
        mLock = true;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, mUrl, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                try
                {
                    String text = response.getString("text");
                    String color = response.getString("color");
                    int size = response.getInt("size");
                    boolean digital = response.getBoolean("digital");

                    int interval = response.getInt("interval");
                    if (interval <= 0)
                    {
                        interval = 5000;
                    }
                    mInterval = interval;

                    SetText(color, size, text, digital);
                }
                catch (Exception e)
                {
                    SetError(e.getMessage());
                }
                finally
                {
                    mLock = false;
                }
            }
        },
        new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                try
                {
                    String message = mAndroidId;
                    try
                    {
                        if (error != null && error.networkResponse != null)
                        {
                            message += " (" + error.networkResponse.statusCode + ")";
                        }
                    }
                    catch (Exception ex)
                    {
                    }
                    SetError(message);
                }
                catch (Exception ex)
                {
                }
                finally
                {
                    mLock = false;
                }
            }
        });

        mQueue.add(request);
    }

    void startRepeatingTask()
    {
        mStatusChecker.run();
    }
}
