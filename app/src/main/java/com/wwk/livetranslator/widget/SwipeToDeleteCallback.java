package com.wwk.livetranslator.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.wwk.livetranslator.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import kotlin.jvm.internal.Intrinsics;

/**
 * Created by Pei on 1/6/18.
 * Copyright © 2017 Unoceros, Inc. All rights reserved
 */

public abstract class SwipeToDeleteCallback
        extends ItemTouchHelper.SimpleCallback {

    private final Drawable deleteIcon;
    private final int intrinsicWidth;
    private final int intrinsicHeight;
    private final Drawable background;
//    private final int backgroundColor;

    private final int deleteIconRightMargin;
    private final int cardElevation;

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    public SwipeToDeleteCallback(@NotNull Context context) {
        super(0, ItemTouchHelper.LEFT);
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        this.intrinsicWidth = this.deleteIcon.getIntrinsicWidth();
        this.intrinsicHeight = this.deleteIcon.getIntrinsicHeight();
//        this.background = new ColorDrawable();
        this.background = ContextCompat.getDrawable(context, R.drawable.card_delete_background);
//        this.backgroundColor = ContextCompat.getColor(context, R.color.redLight);

        this.deleteIconRightMargin = (int) context.getResources().getDimension(R.dimen.item_delete_icon_margin_right);
        this.cardElevation = (int) (context.getResources().getDimension(R.dimen.card_elevation_small) + 4);
    }

    public boolean onMove(@Nullable RecyclerView recyclerView, @Nullable RecyclerView.ViewHolder viewHolder, @Nullable RecyclerView.ViewHolder target) {
        return false;
    }

    public void onChildDraw(@Nullable Canvas c, @Nullable RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        Intrinsics.checkParameterIsNotNull(viewHolder, "viewHolder");
        if (Math.abs(dX) > 5) {
            View itemView = viewHolder.itemView;
            int itemHeight = itemView.getBottom() - itemView.getTop();
            this.background.setBounds(itemView.getLeft() + cardElevation, itemView.getTop() + cardElevation, itemView.getRight() - cardElevation, itemView.getBottom() - cardElevation);
            this.background.draw(c);
            int deleteIconVerticalMargin = (itemHeight - this.intrinsicHeight) / 2;
            int deleteIconTop = itemView.getTop() + deleteIconVerticalMargin;
            int deleteIconLeft = itemView.getRight() - deleteIconRightMargin - this.intrinsicWidth;
            int deleteIconRight = itemView.getRight() - deleteIconRightMargin;
            int deleteIconBottom = deleteIconTop + this.intrinsicHeight;
            this.deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
            this.deleteIcon.draw(c);

            int itemWidth = itemView.getRight() - itemView.getLeft();
            itemView.setAlpha(1 - Math.abs(dX) / (float) itemWidth);
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
