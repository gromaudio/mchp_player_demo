package com.gromaudio.simplifiedmediaplayer.ui.customElements;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.gromaudio.simplifiedmediaplayer.R;
import com.gromaudio.utils.FontManager;
import com.gromaudio.utils.Logger;

public class FontTextView extends TextView {

    public FontTextView(Context context) {
        super(context);

        if (isInEditMode())
            return;

        initView(context, null, 0);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode())
            return;

        initView(context, attrs, 0);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (isInEditMode())
            return;

        initView(context, attrs, defStyle);
    }

    private void initView(Context context, AttributeSet attrs, int defStyle) {
        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FontTextView);
        if (ta != null) {
            String fontAsset = ta.getString(R.styleable.FontTextView_typefaceAsset);

            if (!TextUtils.isEmpty(fontAsset)) {
                Typeface tf = FontManager.getInstance().getFont(fontAsset);
                int style = Typeface.NORMAL;
                //float size = getTextSize();

                if (getTypeface() != null)
                    style = getTypeface().getStyle();

                if (tf != null)
                    setTypeface(tf, style);
                else
                    Logger.d("FontText", String.format("Could not create a font from asset: %s", fontAsset));
            }
            ta.recycle();
        }
    }
}