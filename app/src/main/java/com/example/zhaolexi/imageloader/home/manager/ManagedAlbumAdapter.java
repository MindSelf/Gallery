package com.example.zhaolexi.imageloader.home.manager;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.zhaolexi.imageloader.R;

import java.util.Collections;
import java.util.List;

/**
 * Created by ZHAOLEXI on 2018/2/3.
 */

public class ManagedAlbumAdapter extends RecyclerView.Adapter implements AlbumItemTouchHelperCallback.ItemTouchHelperAdapter {

    public static final int TYPE_HINT = 0;
    public static final int TYPE_ADD = 1;
    public static final int TYPE_LOCAL_ALBUM = 2;
    public static final int TYPE_RANDOM_ALBUM = 3;
    public static final int DRAG_SLOP = 100;  //超过100ms即为拖动

    private List<Album> mLocalAlbum;
    private List<Album> mRandomAlbum;
    private ItemTouchHelper mItemTouchHelper;
    private AlbumManageViewInterface mAlbumManager;
    private int mHintIndex;
    private boolean mIsEditable;
    private OnItemClickListener mClickListener;
    private OnItemAddListener mAddListener;

    public ManagedAlbumAdapter(AlbumManageViewInterface albumManager, List<Album> local, List<Album> random) {
        mAlbumManager = albumManager;
        mLocalAlbum = local;
        mRandomAlbum = random;
        mHintIndex = mLocalAlbum.size() + 1;
    }


    public int getIndexOfLocalAlbum(Album album) {
        return mLocalAlbum.indexOf(album);
    }

    public int getLocalAlbumCount() {
        return mLocalAlbum.size();
    }

    public Album getLocalAlbum(int position) {
        return mLocalAlbum.get(position);
    }

    public void addAlbumToLocal(Album album) {
        boolean isEmptyBefore = mLocalAlbum.isEmpty();
        mLocalAlbum.add(album);
        notifyItemInserted(mLocalAlbum.size());
        mHintIndex++;
        if (isEmptyBefore) mAlbumManager.onAlbumListStateChanged(false, mIsEditable);
    }

    public Album getAlbum(int position) {
        int viewType = getItemViewType(position);
        int realPos = getPositionInData(position);
        Album album = null;
        if (viewType == TYPE_LOCAL_ALBUM) {
            album = mLocalAlbum.get(realPos);
        } else if (viewType == TYPE_RANDOM_ALBUM) {
            album = mRandomAlbum.get(realPos);
        }
        return album;
    }

    public Album removeAlbum(int position) {
        if (position < 0) return null;
        Album removed = null;
        int viewType = getItemViewType(position);
        int realPos = getPositionInData(position);
        if (viewType == TYPE_LOCAL_ALBUM) {
            removed = mLocalAlbum.remove(realPos);
            mHintIndex--;
            if (mLocalAlbum.isEmpty())
                mAlbumManager.onAlbumListStateChanged(true, mIsEditable);
        } else if (viewType == TYPE_RANDOM_ALBUM) {
            removed = mRandomAlbum.remove(realPos);
        }
        notifyItemRemoved(position);
        return removed;
    }


    public boolean isEditable() {
        return mIsEditable;
    }

    public void setEditable(boolean editable, boolean performChangeState) {
        mIsEditable = editable;
        notifyDataSetChanged();
        if (performChangeState) {
            mAlbumManager.onAlbumListStateChanged(mLocalAlbum.isEmpty(), editable);
        }
    }

    public void setItemTouchHelper(ItemTouchHelper helper) {
        mItemTouchHelper = helper;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    public void setOnItemAddListener(OnItemAddListener addListener) {
        this.mAddListener = addListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        View view;
        switch (viewType) {
            case TYPE_LOCAL_ALBUM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_managed_album, parent, false);
                viewHolder = new AlbumViewHolder(view);
                break;
            case TYPE_RANDOM_ALBUM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_managed_album, parent, false);
                viewHolder = new AlbumViewHolder(view);
                break;
            case TYPE_HINT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_managed_random_album_hint, parent, false);
                viewHolder = new HintViewHolder(view);
                break;
            case TYPE_ADD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_managed_album_add, parent, false);
                viewHolder = new AddViewHolder(view);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() != TYPE_ADD && holder.getItemViewType() != TYPE_HINT) {
            List<Album> data;
            AlbumViewHolder viewHolder = (AlbumViewHolder) holder;
            if (viewHolder.getItemViewType() == TYPE_LOCAL_ALBUM) {
                data = mLocalAlbum;
                viewHolder.iv_close.setVisibility(mIsEditable ? View.VISIBLE : View.GONE);
                viewHolder.rl_album.setSelected(getPositionInData(position) == mAlbumManager.getPresenter().getCurrentPage());
            } else {
                data = mRandomAlbum;
            }
            viewHolder.tv_aname.setText(data.get(getPositionInData(position)).getTitle());
        }
    }

    @Override
    public int getItemCount() {
        return mLocalAlbum.size() + mRandomAlbum.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mHintIndex - 1) {
            return TYPE_LOCAL_ALBUM;
        } else if (position == mHintIndex - 1) {
            return TYPE_ADD;
        } else if (position == mHintIndex) {
            return TYPE_HINT;
        } else {
            return TYPE_RANDOM_ALBUM;
        }
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mLocalAlbum, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        mAlbumManager.getPresenter().onAlbumMove(fromPosition, toPosition);
    }


    private int getPositionInData(int position) {
        switch (getItemViewType(position)) {
            case TYPE_LOCAL_ALBUM:
                return position;
            case TYPE_RANDOM_ALBUM:
                return position - mHintIndex - 1;
            default:
                return -1;
        }
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnTouchListener, View.OnClickListener {
        long startTime;
        RelativeLayout rl_album;
        TextView tv_aname;
        ImageView iv_close;

        AlbumViewHolder(View itemView) {
            super(itemView);
            rl_album = (RelativeLayout) itemView.findViewById(R.id.rl_managed_album);
            tv_aname = (TextView) itemView.findViewById(R.id.tv_name);
            iv_close = (ImageView) itemView.findViewById(R.id.iv_close);
            itemView.setOnLongClickListener(this);
            itemView.setOnTouchListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            if (!mIsEditable && getItemViewType() == TYPE_LOCAL_ALBUM) {
                setEditable(true, true);
                return true;
            }
            return false;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startTime = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_MOVE:
                    //触摸时间超过100ms则进行拖动
                    if (mIsEditable && System.currentTimeMillis() - startTime > DRAG_SLOP && mItemTouchHelper != null) {
                        mItemTouchHelper.startDrag(this);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            //这里遇到过一个问题，当时用final position作为其position参数，position值的更新依赖于onBindViewHolder的调用
            //当调用notifyItemRemoved时由于不会调用onBindViewHolder方法，position值没有及时更新，导致出现IndexOutOfBoundsException
            //使用holder.getLayoutPosition由于是动态获取position，所以不会出现这种问题
            mClickListener.onItemClick(getItemViewType() == TYPE_RANDOM_ALBUM, getAdapterPosition(), mIsEditable);
        }
    }

    class HintViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        LinearLayout ll_update;

        HintViewHolder(View itemView) {
            super(itemView);
            ll_update = (LinearLayout) itemView.findViewById(R.id.ll_update);
            ll_update.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mAlbumManager.getPresenter().getRandom();
        }
    }

    class AddViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView add;

        AddViewHolder(View itemView) {
            super(itemView);
            add = (ImageView) itemView.findViewById(R.id.iv_add);
            add.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mAddListener.onClickItemAdd();
        }
    }

    public interface OnItemClickListener {
        void onItemClick(boolean isFromRandom, int position, boolean editable);
    }

    public interface OnItemAddListener {
        void onClickItemAdd();
    }
}
