package com.gromaudio.utils;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

public final class FontManager {

    private static FontManager instance;

    private AssetManager mMgr;

    private Map<String, Typeface> mFonts;

    private FontManager(AssetManager _mgr) {
        mMgr = _mgr;
        mFonts = new HashMap<>();
    }

    public static void init(AssetManager mgr) {
        instance = new FontManager(mgr);
    }

    public static FontManager getInstance() {
        return instance;
    }

    public Typeface getFont(String asset) {
        if (mFonts.containsKey(asset))
            return mFonts.get(asset);

        Typeface font = null;

        try {
            font = Typeface.createFromAsset(mMgr, asset);
            mFonts.put(asset, font);
        } catch (Exception e) {
            Logger.e("FontManager", e.getMessage());
        }

        if (font == null) {
            try {
                String fixedAsset = fixAssetFilename(asset);
                font = Typeface.createFromAsset(mMgr, fixedAsset);
                mFonts.put(asset, font);
                mFonts.put(fixedAsset, font);
            } catch (Exception e) {
                Logger.e("FontManager", e.getMessage());
            }
        }

        return font;
    }

    private static String fixAssetFilename(String asset) {
        // Empty font filename?
        // Just return it. We can't help.
        if (TextUtils.isEmpty(asset))
            return asset;

        // Make sure that the font ends in '.ttf' or '.ttc'
        if ((!asset.endsWith(".ttf")) && (!asset.endsWith(".ttc")))
            asset = String.format("%s.ttf", asset);

        return asset;
    }
}