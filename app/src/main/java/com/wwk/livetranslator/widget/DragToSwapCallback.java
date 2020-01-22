package com.wwk.livetranslator.widget;

import android.graphics.Canvas;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.wwk.livetranslator.Constants;

/**
 * Created by Pei on 2/12/18.
 * Copyright © 2017 Unoceros, Inc. All rights reserved
 */

public abstract class DragToSwapCallback extends ItemTouchHelper.SimpleCallback {

    public DragToSwapCallback() {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return viewHolder.getItemViewType() == Constants.LANGUAGE_ITEM_FAVORITE ? makeMovementFlags(dragFlags, 0) : 0;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        View itemView = viewHolder.itemView;
        if (Math.abs(dY) > 1.0f) {
            itemView.setElevation(4.0f);
        } else {
            itemView.setElevation(0.0f);
        }
    }

    @Override
    public boolean onMove(RecyclerView recyclerView,
                          RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        if (target.getItemViewType() != Constants.LANGUAGE_ITEM_FAVORITE) {
            return false;
        }

        onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    protected abstract void onItemMove(int oldPosition, int newPosition);
}