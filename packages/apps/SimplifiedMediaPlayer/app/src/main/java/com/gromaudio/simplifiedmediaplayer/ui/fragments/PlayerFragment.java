package com.gromaudio.simplifiedmediaplayer.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.gromaudio.simplifiedmediaplayer.App;
import com.gromaudio.simplifiedmediaplayer.R;
import com.gromaudio.simplifiedmediaplayer.models.AppDetail;
import com.gromaudio.simplifiedmediaplayer.players.IDemoPlayer;
import com.gromaudio.simplifiedmediaplayer.ui.customElements.PlayerView;
import com.gromaudio.utils.TimeUtils;


public class PlayerFragment extends BaseFragment implements IDemoPlayer.IDemoPlayerCallback {

    private static final String TAG = "PlayerFragment";

    private IDemoPlayer mPlayer = null;

    private AppDetail mAppDetail = null;

    private PlayerView mPlayerView = null;


    public static PlayerFragment newInstance(AppDetail appDetail, IDemoPlayer player) {
        final PlayerFragment playerFragment = new PlayerFragment(appDetail, player);
        final Bundle bundle = new Bundle();
        playerFragment.setArguments(bundle);
        return playerFragment;
    }

    public PlayerFragment(AppDetail appDetail, IDemoPlayer player) {
        super();
        mAppDetail = appDetail;
        mPlayer = player;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPlayerView = (PlayerView) view.findViewById(R.id.playerControlView);
        initPlayerView();
        if (mPlayer!=null) {
            mPlayer.setCallback(this);
        }
        updateCurrentState();
    }

    @Override
    public void onDestroyView() {
        // TODO: if need use 3d cover
        //mPlayerView.getCoverSurface().disposeTextures();
        //mPlayerView.getCoverSurface().destroyDrawingCache();
        if (mPlayer!=null) {
            mPlayer.setCallback(null);
            mPlayer = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
        //setTestData();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        // TODO: if need use 3d cover
        //mPlayerView.getCoverSurface().onResume();
    }

    @Override
    public void onPause() {
        // TODO: if need use 3d cover
        //mPlayerView.getCoverSurface().onPause();
        super.onPause();
    }

    private void initPlayerView() {

        mPlayerView.setPlayButtonListener(new PlayerView.Listener() {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                if (mPlayer != null) {
                    if(mPlayer.getState() == IDemoPlayer.DemoPlayerState.ST_PLAYED) {
                        if (mPlayer.pause()) {
                            mPlayerView.setPlayPauseButtonPlaying(false);
                        }
                    }
                    else {
                        if (mPlayer.play()) {
                            mPlayerView.setPlayPauseButtonPlaying(true);
                        }
                    }
                }
            }
        });

        mPlayerView.setNextButtonListener(new PlayerView.Listener() {
            @Override
            public void onClick(View v) {
                if (mPlayer!=null) {
                    Log.d(TAG, "onClick(next);");
                    mPlayer.next();
                }
            }
        });

        mPlayerView.setPrevButtonListener(new PlayerView.Listener() {
            @Override
            public void onClick(View v) {
                if (mPlayer!=null) {
                    Log.d(TAG, "onClick(prev);");
                    mPlayer.prev();
                }
            }
        });

        mPlayerView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private boolean mFromTouch = false;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Logger.e("progress= " + progress + " fromUser= " + fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mFromTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mFromTouch) {
                    if (mPlayer!=null) {
                        mPlayer.seekTo( seekBar.getProgress() );
                    }
                }
                mFromTouch = false;
            }
        });

        mPlayerView.setLeftControlButton(new PlayerView.Listener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null) {
                    boolean res = mPlayer.shuffleSwitch();
                    mPlayerView.getLeftControlButton().setChecked(res);
                }
            }
        });

        mPlayerView.setRightControlButtonListener(new PlayerView.Listener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null) {
                    boolean res = mPlayer.repeatSwitch();
                    mPlayerView.getRightControlButton().setChecked(res);
                }
            }
        });
    }

    @Override
    public void onStateChanged() {
        if (mPlayer != null && mPlayerView != null) {
            updateCurrentState();
        }
    }

    private void updateCurrentState() {
        if (mPlayer!=null && mPlayerView!=null) {
            mPlayerView.setAlbumName( mPlayer.getAlbumName() );
            mPlayerView.setTrackName( mPlayer.getTrackName() );
            mPlayerView.setArtistName( mPlayer.getArtistName() );

            int duration = mPlayer.getDuration();
            mPlayerView.setMaxValueBySeekBar( duration );
            String totalTimeString = TimeUtils.makeTimeString(getContext(), duration).toString();
            mPlayerView.setTotalTime(totalTimeString);

            int position = mPlayer.getPosition();
            mPlayerView.setProgress(position);
            StringBuffer time = TimeUtils.makeTimeString(App.get(), position);
            mPlayerView.setPositionTime(time.toString());

            boolean playing = (mPlayer.getState() == IDemoPlayer.DemoPlayerState.ST_PLAYED);
            mPlayerView.setPlayPauseButtonPlaying(playing);
            if (playing) {
                mHandler.postDelayed(mUpdateTimeTask, 1000);
            }
            else {
                mHandler.removeCallbacks(mUpdateTimeTask);
            }

            mPlayerView.setCover(R.drawable.albumart_mp_unknown);
        }
    }


    protected Handler mHandler = new Handler();
    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            if (mPlayer!=null && mPlayerView!=null) {
                int duration = mPlayer.getDuration();
                mPlayerView.setMaxValueBySeekBar( duration );
                String totalTimeString = TimeUtils.makeTimeString(getContext(), duration).toString();
                mPlayerView.setTotalTime(totalTimeString);

                int position = mPlayer.getPosition();
                mPlayerView.setProgress(position);
                StringBuffer time = TimeUtils.makeTimeString(App.get(), position);
                mPlayerView.setPositionTime(time.toString());
            }
            mHandler.postDelayed(mUpdateTimeTask, 1000);
        }
    };
}
