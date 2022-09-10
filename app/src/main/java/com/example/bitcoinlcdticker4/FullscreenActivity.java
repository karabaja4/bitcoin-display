package com.example.bitcoinlcdticker4;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.util.TypedValue;
import android.widget.TextView;
import android.graphics.Typeface;
import android.os.Handler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FullscreenActivity extends Activity
{
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        mHandler = new Handler();
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
                updateStatus();
            }
            catch (Exception e)
            {
            }
            finally
            {
                mHandler.postDelayed(mStatusChecker, 1000);
            }
        }
    };

    String mColors[] =
    {
        "#0000ff", // blue
        "#00ff00", // green
        "#00ffff", // cyan
        "#ff0000", // red
        "#ff00ff", // pink
        "#ffff00", // yellow
        "#ffffff"  // white
    };

    String morning = "Dobro jutro :)";
    String day     = "Želim ti dobar dan :)";
    String evening = "Želim ti ugodnu večer :)";
    String night   = "Želim ti laku noć :)";

    public String GetGreeting(int hour)
    {
        if (hour == 23 || (hour >= 0 && hour <= 3))
        {
            return night;
        }
        if (hour >= 4 && hour <= 11)
        {
            return morning;
        }
        if (hour >= 12 && hour <= 17)
        {
            return day;
        }
        if (hour >= 18 && hour <= 22)
        {
            return evening;
        }
        return null;
    }

    DateFormat mDateFormat = new SimpleDateFormat("HH:mm:ss");

    private void updateStatus()
    {
        try
        {
            long uts = System.currentTimeMillis();
            //long ts = uts / 1000;
            Date date = new Date(uts);

            int h = date.getHours();
            int s = date.getSeconds();
            int m = date.getMinutes();
            String greeting = GetGreeting(h);

            String[] words = greeting != null ? greeting.split("\\s+") : new String[]{};
            String color = mColors[(int)(m % mColors.length)];

            int index = s - 30;
            if (index >= 0 && index < words.length)
            {
                SetText(color, 215, words[index], false);
            }
            else
            {
                SetText(color, 235, mDateFormat.format(date), true);
            }
        }
        catch (Exception ex)
        {
            SetError(ex.getMessage());
        }
    }

    void startRepeatingTask()
    {
        mStatusChecker.run();
    }
}
