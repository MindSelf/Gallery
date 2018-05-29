package com.example.zhaolexi.imageloader.me.album.list;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.imageloader.imageloader.ImageLoader;
import com.example.imageloader.imageloader.TaskOption;
import com.example.imageloader.resizer.DecodeOption;
import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.ui.OnItemAddListener;
import com.example.zhaolexi.imageloader.common.ui.OnItemClickListener;
import com.example.zhaolexi.imageloader.common.ui.OnItemLongClickListener;
import com.example.zhaolexi.imageloader.common.utils.AlbumConstructor;
import com.example.zhaolexi.imageloader.common.utils.DisplayUtils;
import com.example.zhaolexi.imageloader.home.manager.Album;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter {

    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_ADD = 1;

    private Context mCtx;
    private ImageLoader mImageLoader;
    private AlbumConstructor constructor;
    private List<Album> mAlbumList;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnItemClickListener mOnItemClickListener;
    private OnItemAddListener mOnItemAddListener;
    private OnItemCloseListener mOnItemCloseListener;

    private boolean mIsIdle = true;
    private boolean mEditable;
    private boolean mAddible;
    private int mEdge;

    public AlbumAdapter(Context context, List<Album> albumList, boolean addible) {
        mCtx = context;
        mAlbumList = albumList;
        mAddible = addible;
        mImageLoader = ImageLoader.getInstance(context);
        constructor = new AlbumConstructor();
        mEdge = DisplayUtils.getScreenMetrics(context).widthPixels - DisplayUtils.dp2px(context, 40);
    }

    public void setIsIdle(boolean isIdle) {
        this.mIsIdle = isIdle;
    }

    public void setEditable(boolean editable) {
        if (editable != mEditable) {
            mEditable = editable;
            notifyDataSetChanged();
        }
    }

    public boolean isEditable() {
        return mEditable;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnItemAddListener(OnItemAddListener listener) {
        this.mOnItemAddListener = listener;
    }

    public void setOnItemCloseListener(OnItemCloseListener listener) {
        this.mOnItemCloseListener = listener;
    }

    public void addAlbums(List<Album> newDatas) {
        int firstPos = mAlbumList.size();
        mAlbumList.addAll(newDatas);
        notifyItemRangeInserted(firstPos, newDatas.size());
    }

    public void replaceAlbum(int pos, Album album) {
        mAlbumList.set(pos, album);
        notifyDataSetChanged();
    }

    public void removeAlbum(int pos) {
        mAlbumList.remove(pos);
        notifyItemRemoved(pos);
    }

    public void clearAlbums() {
        mAlbumList.clear();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        View view;
        if (viewType == TYPE_ADD) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_add, parent, false);
            viewHolder = new AddViewHolder(view);
        }
        if (viewType == TYPE_NORMAL) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_list, parent, false);
            viewHolder = new AlbumViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_NORMAL) {
            AlbumViewHolder viewHolder = (AlbumViewHolder) holder;
            viewHolder.title.setText(mAlbumList.get(position).getTitle());
            viewHolder.total.setText(constructTotal(position));
            viewHolder.close.setVisibility(mEditable ? View.VISIBLE : View.GONE);
            ImageView image = viewHolder.image;

            if (mAlbumList.get(position).getCover() != null) {
                //预加载的图片
                byte[] bytes = mAlbumList.get(position).getCover();
                image.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            }

            //加载和显示图片
            final String tag = (String) image.getTag();
            final String uri = mAlbumList.get(position).getCoverUrl();

            DecodeOption decodeOption = new DecodeOption(mEdge, mEdge);
            decodeOption.shouldResized = true;
            TaskOption option = new TaskOption(decodeOption);

            boolean isNeedLoad = uri != null && !uri.equals(tag);
            if (isNeedLoad) {
                //为了避免View复用导致显示旧的bitmap，这里会先显示内存中缓存的图片，没有再显示占位图
                mImageLoader.bindDefaultImage(uri, image, option);
            }

            //优化列表卡顿，为了避免频繁的加载图片，只在列表停下来的时候才加载图片
            if (isNeedLoad && mIsIdle) {
                image.setTag(uri);
                mImageLoader.bindBitmap(uri, image, option);
            }

        }
    }

    private String constructTotal(int position) {
        String total = String.valueOf(mAlbumList.get(position).getTotal());
        if (constructor.isThird(mAlbumList.get(position))) {
            total = total.concat("+");
        }
        total = total.concat("张");
        return total;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mAlbumList.size()) {
            return TYPE_ADD;
        }
        return TYPE_NORMAL;
    }

    @Override
    public int getItemCount() {
        if (mAddible) {
            return mAlbumList.size() + 1;
        } else {
            return mAlbumList.size();
        }
    }

    class AddViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public AddViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mOnItemAddListener.onClickItemAdd();
        }
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ImageView image, close;
        TextView total, title;

        AlbumViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.iv_image);
            close = (ImageView) itemView.findViewById(R.id.iv_close);
            total = (TextView) itemView.findViewById(R.id.tv_total);
            title = (TextView) itemView.findViewById(R.id.tv_title);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            close.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.iv_close) {
                mOnItemCloseListener.onItemClose(v, getAdapterPosition());
            } else {
                mOnItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            return mOnItemLongClickListener.onItemLongClick(v, getAdapterPosition());
        }
    }

    public interface OnItemCloseListener {
        void onItemClose(View v, int position);
    }
}
