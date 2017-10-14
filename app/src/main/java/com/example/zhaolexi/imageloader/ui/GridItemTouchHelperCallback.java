package com.example.zhaolexi.imageloader.ui;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.example.zhaolexi.imageloader.adapter.ImageAdapter;

/**
 * Created by ZHAOLEXI on 2017/10/15.
 */

public class GridItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private ItemTouchHelperAdapter mAdapter;

    public GridItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
        mAdapter=adapter;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags=0;
        if(viewHolder.getItemViewType()!= ImageAdapter.TYPE_FOOTER) {
            dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        }
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        mAdapter.onItemMove(viewHolder.getLayoutPosition(),target.getLayoutPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {}

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if(actionState!=ItemTouchHelper.ACTION_STATE_IDLE) {
            viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
        }
        //当actionState为IDLE时，传进来的viewHolder为null
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        viewHolder.itemView.setBackgroundColor(0);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    public interface ItemTouchHelperAdapter{
        void onItemMove(int fromPosition, int toPosition);
    }
}
