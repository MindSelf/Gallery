package com.example.zhaolexi.imageloader.ui;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.example.zhaolexi.imageloader.adapter.ManagedAlbumAdapter;

/**
 * Created by ZHAOLEXI on 2018/2/3.
 */

public class AlbumItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private ManagedAlbumAdapter mAdapter;

    public AlbumItemTouchHelperCallback(ManagedAlbumAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = 0;
        if (mAdapter.isEditable() && viewHolder.getItemViewType() == ManagedAlbumAdapter.TYPE_LOCAL_ALBUM) {
            dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        }
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if (target.getItemViewType()== ManagedAlbumAdapter.TYPE_LOCAL_ALBUM) {
            mAdapter.onItemMove(viewHolder.getLayoutPosition(), target.getLayoutPosition());
        }
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {}

    @Override
    public boolean isLongPressDragEnabled() {
        //使用ItemTouchHelper.startDrag()
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    //    @Override
//    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
//        super.onSelectedChanged(viewHolder, actionState);
//        if(actionState!=ItemTouchHelper.ACTION_STATE_IDLE) {
//            viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
//        }
//        //当actionState为IDLE时，传进来的viewHolder为null
//    }
//
//    @Override
//    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//        super.clearView(recyclerView, viewHolder);
//        viewHolder.itemView.setBackgroundColor(recyclerView.getContext().getResources().getColor(R.color.selected_album));
//    }

    public interface ItemTouchHelperAdapter{
        void onItemMove(int fromPosition, int toPosition);
    }
}
