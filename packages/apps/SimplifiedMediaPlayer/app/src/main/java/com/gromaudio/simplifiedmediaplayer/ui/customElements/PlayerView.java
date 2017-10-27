package com.gromaudio.simplifiedmediaplayer.ui.customElements;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.gromaudio.simplifiedmediaplayer.R;


public class PlayerView extends FrameLayout {

    private TextView mArtistTextView;
    private TextView mAlbumTextView;
    private TextView mTrackTextView;

    private CustomImageButton mNextButton;
    private CustomImageButton mPrevButton;
    private CustomImageButton mPlayButton;
    private SeekBar mProgressBar;

    private TextView mTotalTime;
    private TextView mPositionTime;
    private TextView mTrackCountTextView;

    private LinearLayout mProgressBarRootLayout;

    private CustomImageButton mLeftControlButton;
    private CustomImageButton mRightControlButton;

    private ImageView mCoverImageView;
    private CustomGLSurfaceView mCoverSurface;

    public static abstract class Listener {
        public void onClick(View v) {
        }

        public boolean onLongClick(View v) {
            return false;
        }

        public boolean onTouch(View v, MotionEvent event) {
            return false;
        }
    }

    public PlayerView(Context context) {
        super(context);
        if(!isInEditMode()) {
            init(context/*, null, 0*/);
        }
    }

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(!isInEditMode()) {
            init(context/*, attrs, 0*/);
        }
    }

    public PlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if(!isInEditMode()) {
            init(context/*, attrs, defStyleAttr*/);
        }
    }

    private void init(Context context/*, AttributeSet attrs, int defStyleAttr*/) {
        inflate(context, R.layout.fragment_player_player_view, this);

        mArtistTextView = (TextView) findViewById(R.id.additional_artist);
        mAlbumTextView = (TextView) findViewById(R.id.additional_album);
        mTrackTextView = (TextView) findViewById(R.id.additional_title);

        mProgressBar = (SeekBar) findViewById(R.id.playerControlViewProgressBar);
        mPlayButton = (CustomImageButton) findViewById(R.id.play_button_additional);
        mNextButton = (CustomImageButton) findViewById(R.id.next_button);
        mPrevButton = (CustomImageButton) findViewById(R.id.prev_button);
        mLeftControlButton = (CustomImageButton) findViewById(R.id.left_button);
        mRightControlButton = (CustomImageButton) findViewById(R.id.right_button);
        mTotalTime = (TextView) findViewById(R.id.additional_time);
        mPositionTime = (TextView) findViewById(R.id.position_time);
        mTrackCountTextView = (TextView) findViewById(R.id.trackCountTextView);

        mProgressBarRootLayout = (LinearLayout) findViewById(R.id.progressBarRootLayout);
        mCoverSurface = (CustomGLSurfaceView) findViewById(R.id.cover_surface);
        mCoverImageView = (ImageView) findViewById(R.id.additional_cover);

        mLeftControlButton.setChangeColorFilter(true);
        mRightControlButton.setChangeColorFilter(true);
    }

    public CustomImageButton getLeftControlButton() {
        return mLeftControlButton;
    }

    public CustomImageButton getRightControlButton() {
        return mRightControlButton;
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        mProgressBar.setOnSeekBarChangeListener(listener);
    }

    public void setMaxValueBySeekBar(int value) {
        mProgressBar.setMax(value);
    }

    public void setProgress(int value) {
        mProgressBar.setProgress(value);
    }

    public SeekBar getProgressBar() {
        return mProgressBar;
    }

    public void setProgressBarVisibility(int visibility) {
        mProgressBar.setVisibility(visibility);
        mTrackCountTextView.setVisibility(visibility);
        mTotalTime.setVisibility(visibility);
        mPositionTime.setVisibility(visibility);
    }

    private void setButtonListener(View view, final Listener listener) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v);
            }
        });
    }



    public void setNextButtonListener(Listener listener) {
        setButtonListener(mNextButton, listener);
    }

    public void setPrevButtonListener(Listener listener) {
        setButtonListener(mPrevButton, listener);
    }

    public void setPlayButtonListener(Listener listener) {
        setButtonListener(mPlayButton, listener);
    }

    public void setLeftControlButton(Listener listener) {
        setButtonListener(mLeftControlButton, listener);
    }

    public void setRightControlButtonListener(Listener listener) {
        setButtonListener(mRightControlButton, listener);
    }


    public void setPlayPauseButtonPlaying(boolean state) {
        mPlayButton.setImageResource(
            state ? R.drawable.ic_pause_47dp : R.drawable.ic_play_47dp
        );
    }

    public void setArtistName(@Nullable String artist) {
        mArtistTextView.setText(artist);
    }

    public void setAlbumName(@Nullable String album) {
        mAlbumTextView.setText(album);
    }

    public void setTrackName(@Nullable String title) {
        mTrackTextView.setText(title);
    }

    public void setTotalTime(@Nullable String totalTime) {
        if (mTotalTime != null && totalTime != null && !totalTime.equals(mTotalTime.getText())) {
            mTotalTime.setText(totalTime);
        }
    }

    public void setPositionTime(@Nullable String positionTime) {
        if (mPositionTime != null && positionTime != null && !positionTime.equals(mPositionTime.getText())) {
            mPositionTime.setText(positionTime);
        }
    }

    public void setTrackCount(@Nullable String trackCount) {
        if (mTrackCountTextView != null) {
            if (trackCount == null) {
                mTrackCountTextView.setText("");
            } else if (!trackCount.equals(mTrackCountTextView.getText())) {
                mTrackCountTextView.setText(trackCount);
            }
            if (mTrackCountTextView.getVisibility() != VISIBLE) {
                mTrackCountTextView.setVisibility(VISIBLE);
            }
        }
    }

    public TextView getAlbumTextView() {
        return mAlbumTextView;
    }

    public void setTextLayoutOnClickListener(final OnClickListener listener) {
        final LinearLayout textLayout = (LinearLayout) findViewById(R.id.textLayout);
        textLayout.setOnClickListener(new TextLayoutClickListener(listener));
    }

    @Nullable
    public LinearLayout getProgressBarRootLayout() {
        return mProgressBarRootLayout;
    }

    private static final class ButtonLongClickListener implements OnLongClickListener {
        private final Listener mListener;

        private ButtonLongClickListener(Listener listener) {
            mListener = listener;
        }

        @Override
        public boolean onLongClick(View v) {
            return mListener.onLongClick(v);
        }
    }

    private static final class ButtonTouchListener implements OnTouchListener {
        private final Listener mListener;

        private ButtonTouchListener(Listener listener) {
            mListener = listener;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return mListener.onTouch(v, event);
        }
    }

    private static final class TextLayoutClickListener implements OnClickListener {
        private final OnClickListener mListener;

        private TextLayoutClickListener(OnClickListener listener) {
            mListener = listener;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onClick(v);
            }
        }
    }

    // TODO: if need use 3d cover
    /*public void setCover(@Nullable Bitmap bitmap,
                         int bitmapIdentifier,
                         @NonNull CustomGLSurfaceView.ANIMATION animation) {
        mCoverSurface.setCover(bitmap, bitmapIdentifier, animation);
    }*/
    // TODO: if need use 3d cover
    /*@NonNull
    public CustomGLSurfaceView getCoverSurface() {
        return mCoverSurface;
    }*/


    public void setCover(@DrawableRes int resImage) {
        if (mCoverImageView.getVisibility() != VISIBLE) {
            mCoverImageView.setVisibility(VISIBLE);
            mCoverSurface.setVisibility(GONE);
        }
        mCoverImageView.setImageResource(resImage);
    }

    public void setCover(@Nullable Bitmap bitmap) {
        if (mCoverImageView.getVisibility() != VISIBLE) {
            mCoverImageView.setVisibility(VISIBLE);
            mCoverSurface.setVisibility(GONE);
        }
        mCoverImageView.setImageBitmap(bitmap);
    }


}
