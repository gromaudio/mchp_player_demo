package com.gromaudio.simplifiedmediaplayer.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gromaudio.simplifiedmediaplayer.R;
import com.gromaudio.simplifiedmediaplayer.models.AppDetail;
import com.gromaudio.simplifiedmediaplayer.players.IDemoPlayer;
import com.gromaudio.simplifiedmediaplayer.players.PlayerMgr;
import com.gromaudio.simplifiedmediaplayer.ui.activity.CarPlayActivity;
import com.gromaudio.simplifiedmediaplayer.ui.activity.MainActivity;
import com.gromaudio.utils.recyclerview.GridSpacingItemDecoration;
import com.gromaudio.utils.recyclerview.WrappableGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import static com.gromaudio.simplifiedmediaplayer.models.AppDetail.STATE.DISABLED;
import static com.gromaudio.simplifiedmediaplayer.models.AppDetail.STATE.NORMAL;
import static com.gromaudio.simplifiedmediaplayer.players.PlayerMgr.PlayerType.CARPLAY_PLAYER;


public class LauncherFragment extends BaseFragment implements PlayerMgr.IPlayerMgrCallback {

    private static final String TAG = "LauncherFragment";

    private PlayerMgr mPlayerMgr = null;

    public static LauncherFragment newInstance() {
        Log.d(TAG, "newInstance()");
        final LauncherFragment playerFragment = new LauncherFragment();
        final Bundle bundle = new Bundle();
        playerFragment.setArguments(bundle);
        return playerFragment;
    }

    private static List<AppDetail> createApplicationList(Context ctx) {
        final List<AppDetail> result = new ArrayList<>();

        AppDetail appDetail = new AppDetail(
            ctx.getString(R.string.mass_storage_device), R.drawable.ic_usb_storage_75dp
        );
        appDetail.setState(DISABLED);
        appDetail.setSelected(false);
        appDetail.setPlayerType(PlayerMgr.PlayerType.FILE_PLAYER);
        result.add(appDetail);

        appDetail = new AppDetail(
            ctx.getString(R.string.iTunes), R.drawable.ic_itunes_75dp
        );
        appDetail.setState(DISABLED);
        appDetail.setPlayerType(PlayerMgr.PlayerType.ITUNES_PLAYER);
        result.add(appDetail);

        appDetail = new AppDetail(
            ctx.getString(R.string.android_usb_streaming), R.drawable.ic_aoap_streaming_75dp
        );
        appDetail.setState(DISABLED);
        appDetail.setPlayerType(PlayerMgr.PlayerType.AOAP_PLAYER);
        result.add(appDetail);

        appDetail = new AppDetail(
            ctx.getString(R.string.car_play), R.drawable.ic_carplay_75dp
        );
        appDetail.setState(DISABLED);
        appDetail.setPlayerType(CARPLAY_PLAYER);
        result.add(appDetail);

        appDetail = new AppDetail(
            ctx.getString(R.string.android_auto), R.drawable.ic_android_auto_75dp
        );
        appDetail.setState(DISABLED);
        appDetail.setPlayerType(PlayerMgr.PlayerType.AAUTO_PLAYER);
        result.add(appDetail);
        return result;
    }

    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_launcher, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated()");
        setTitle(getString(R.string.launcher_activity_title));

        final int spanCount = 3; // 3 columns
        final int spacing = getResources().getDimensionPixelSize(R.dimen.spacing);
        final boolean includeEdge = true;

        final WrappableGridLayoutManager lm = new WrappableGridLayoutManager(getContext(), spanCount);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(lm);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));
        mRecyclerView.setAdapter(new RecyclerViewAdapter(createApplicationList(getContext())));

        if (mPlayerMgr != null) {
            mPlayerMgr.close(getContext());
            mPlayerMgr = null;
        }
        mPlayerMgr = new PlayerMgr();
        boolean res = mPlayerMgr.init(getContext(), this);
        if (!res) {
            showToast("Unable to connect to the 'base_daemon' service.");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mPlayerMgr != null) {
            mPlayerMgr.close(getContext());
            mPlayerMgr = null;
        }
    }

    @Override
    public void onResumeFragment() {
        Log.d(TAG, "onResumeFragment()");
        super.onResumeFragment();
        setTitle(getString(R.string.launcher_activity_title));
    }

    /*
     *  IPlayerMgrCallback
     */
    @Override
    public void onPlayerStateChanged(PlayerMgr.PlayerType player) {
        Log.d(TAG, "onPlayerStateChanged( "+player+" )");
        RecyclerViewAdapter adapter = (RecyclerViewAdapter)mRecyclerView.getAdapter();
        if (adapter!=null) {
            int size = adapter.getItemCount();
            for (int a=0; a < size; ++a) {
                PlayerMgr.PlayerType playerType = adapter.getItem(a).getPlayerType();
                if (player == playerType) {
                    if (mPlayerMgr!=null && mPlayerMgr.isPlayerEnabled(playerType)) {
                        adapter.setState(AppDetail.STATE.NORMAL, a);
                    }
                    else {
                        adapter.setState(AppDetail.STATE.DISABLED, a);
                    }
                }
            }
        }
    }


    private void startPlayerFragment(AppDetail appDetail, IDemoPlayer player) {
        final BaseFragment baseFragment = PlayerFragment.newInstance(appDetail, player);
        final Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).showFragment(baseFragment);
        }
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout mItemRootLayout;
        private TextView mAppNameTextView;
        private ImageView mIcon;

        ViewHolder(LinearLayout itemView) {
            super(itemView);
            mItemRootLayout = itemView;
            mIcon = (ImageView) itemView.findViewById(R.id.itemIcon);
            mAppNameTextView = (TextView) itemView.findViewById(R.id.itemTitle);
        }

        void update(AppDetail appDetail) {
            mIcon.setImageResource(appDetail.getResIcon());
            mAppNameTextView.setText(appDetail.getAppName());

            final Context context = mItemRootLayout.getContext();
            if (appDetail.getState() == NORMAL) {
                enable(context);
                if (appDetail.isSelected()) {
                    selected(context);
                }
            } else {
                disable(context);
            }
        }

        private void disable(Context context) {
            mItemRootLayout.setBackgroundResource(android.R.color.transparent);
            mAppNameTextView.setTextColor(context.getResources().getColor(R.color.fragment_launcher_item_text_disable));
            setLocked(mIcon);
        }

        private void enable(Context context) {
            mItemRootLayout.setBackgroundResource(R.drawable.item_selector);
            mAppNameTextView.setTextColor(context.getResources().getColor(android.R.color.white));
            setUnlocked(mIcon);
        }

        private void selected(Context context) {
            mItemRootLayout.setBackgroundColor(
                context.getResources().getColor(R.color.fragment_launcher_item_selected)
            );
        }

        private static void setLocked(ImageView v) {
            final ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);  //0 means grayscale
            final ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
            v.setColorFilter(cf);
            v.setImageAlpha(128);   // 128 = 0.5
        }

        private static void setUnlocked(ImageView v) {
            v.setColorFilter(null);
            v.setImageAlpha(255);
        }
    }

    private final class RecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {
        @NonNull
        private List<AppDetail> mItems;
        private RecyclerViewAdapter(@NonNull List<AppDetail> lit) {
            mItems = lit;
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            final LinearLayout v = (LinearLayout) LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.fragment_launcher_item, viewGroup, false);
            v.setOnClickListener(new MyOnClickListener());
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            final AppDetail item = mItems.get(i);
            viewHolder.update(item);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        AppDetail getItem(int position) {
            return mItems.get(position);
        }

        void setSelection(int position) {
            for (AppDetail appDetail : mItems) {
                appDetail.setSelected(false);
            }
            if (position >= 0 && position < getItemCount()) {
                final AppDetail appDetail = getItem(position);
                appDetail.setSelected(true);
            }
            notifyDataSetChanged();
        }

        public void setState(AppDetail.STATE state, int position) {
            if (position >= 0 && position < getItemCount()) {
                final AppDetail appDetail = getItem(position);
                appDetail.setState(state);
                notifyItemChanged(position);
            }
        }
    }

    private class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final int itemPosition = mRecyclerView.indexOfChild(v);

            final RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
            if (adapter instanceof RecyclerViewAdapter) {
                final RecyclerViewAdapter a = (RecyclerViewAdapter) adapter;
                final AppDetail appDetail = a.getItem(itemPosition);
                if (appDetail.getState() == NORMAL && mPlayerMgr!=null) {
                    //CarPlay is special
                    if (appDetail.getPlayerType() == CARPLAY_PLAYER) {
                        IDemoPlayer player = mPlayerMgr.activatePlayer(appDetail.getPlayerType());
                        if (!appDetail.isSelected()) {
                            a.setSelection(itemPosition);
                        }
                        Intent intent = new Intent(getContext(), CarPlayActivity.class);
                        startActivity(intent);
                    }
                    else {
                        IDemoPlayer player = mPlayerMgr.activatePlayer(appDetail.getPlayerType());
                        if (player != null) {
                            if (!appDetail.isSelected()) {
                                a.setSelection(itemPosition);
                            }
                            startPlayerFragment(appDetail, player);
                        } else {
                            showToast("Player '" + appDetail.getAppName() + "' can't be activated.");
                        }
                    }
                }
            }
        }
    }
}
