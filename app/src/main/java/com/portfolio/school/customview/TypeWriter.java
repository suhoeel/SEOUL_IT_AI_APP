package com.portfolio.school.customview;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TypeWriter extends androidx.appcompat.widget.AppCompatTextView {

    private CharSequence mText;
    private int mIndex;
    private long mDelay = 500;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            if (mText == null) {
                return;
            }
            setText(mText.subSequence(0, mIndex++));

            if (mIndex <= mText.length()) {
                mHandler.postDelayed(this, mDelay);
            }
        }
    };

    public TypeWriter(@NonNull Context context) {
        super(context);
    }

    public TypeWriter(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void animateText(CharSequence text) {
        mText = text;
        mIndex = 0;
        setText("");
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }

    public void setCharacterDelay(long millis) {
        mDelay = millis;
    }
}
