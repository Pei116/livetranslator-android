package com.wwk.livetranslator.widget;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.wwk.livetranslator.R;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by wwk on 10/5/17.
 * Copyright © 2017 WYFI, Inc. All rights reserved
 */

public class ZippyListAdapter<T> extends RecyclerView.Adapter {

    public static final int ITEM_LOADING = -1;
    public static final int ITEM_OBJECT = 0;

    private static final int VISIBLE_THRESHOLD = 2;
    private int totalCount;
    private List<T> objects = new ArrayList<>();
    private boolean hasMore;
    private boolean loading;
    private LoadObjectsCallback loadCallback;

    private SwipeRefreshLayout refreshLayout;
    private View emptyView;
    private RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            updateEmptyView();
        }
    };

    public ZippyListAdapter() {
        super();
        registerAdapterDataObserver(dataObserver);
    }

    private void updateEmptyView() {
        if (emptyView != null) {
            boolean showEmptyView = getItemCount() == 0;
            emptyView.setVisibility(showEmptyView ? VISIBLE : GONE);
        }
    }

    public void setupListView(RecyclerView listView) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(listView.getContext());
        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalCount = layoutManager.getItemCount();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                if (hasMore && !loading && totalCount <= lastVisibleItem + VISIBLE_THRESHOLD) { // Reached end
                    if (loadCallback != null) {
                        loading = true;
                        loadCallback.loadMore(ZippyListAdapter.this, totalCount);
                    }
                }
            }
        });
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(this);
    }

    public void setEmptyView(View view) {
        emptyView = view;
        updateEmptyView();
    }

    public void setRefreshLayout(SwipeRefreshLayout refreshLayout) {
        this.refreshLayout = refreshLayout;
        refreshLayout.setOnRefreshListener(() -> {
            if (loadCallback != null) loadCallback.refreshAll(this);
        });
    }

    public void setLoadCallback(LoadObjectsCallback callback) {
        loadCallback = callback;
    }

    public boolean isLoading() {
        return loading;
    }

    @Override
    public int getItemViewType(int position) {
        if (objects == null || objects.get(position) == null)
            return ITEM_LOADING;
        return ITEM_OBJECT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_LOADING) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_view, parent, false);
            return new LoadingHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ZippyListAdapter.LoadingHolder) {
            ((ZippyListAdapter.LoadingHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return objects == null ? 0 : objects.size();
    }

    public T getItemObject(int position) {
        return objects.get(position);
    }

    public List<T> getObjects() {
        return objects;
    }

    public void didStartLoading(boolean loadMore) {
        loading = true;
        if (loadMore) {
            // Add null object to show loading view in the fragment
            objects.add(null);
            notifyItemInserted(objects.size() - 1);
        } else if (refreshLayout != null && !refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(true);
        }
    }

    public void didFinishLoading(List<T> newObjects, Boolean hasMore, boolean isAll) {
        if (isAll) {
            objects.clear();
        } else {
            objects.remove(objects.size() - 1);
            notifyItemRemoved(objects.size() - 1);
        }
        if (newObjects != null) {
            if (hasMore != null)
                objects.addAll(newObjects);
            else
                objects.addAll(0, newObjects);
        }

        if (hasMore != null)
            this.hasMore = hasMore;

        if (refreshLayout != null) refreshLayout.setRefreshing(false);
        loading = false;

        notifyDataSetChanged();
    }

    public void didFinishLoading() {
        if (refreshLayout != null) refreshLayout.setRefreshing(false);
        loading = false;
    }

    public void insertObject(int position, T object) {
        objects.add(position, object);
        notifyItemInserted(position);
        updateEmptyView();
    }

    public void removeObject(T object) {
        int index = objects.indexOf(object);
        if (index >= 0) {
            objects.remove(index);
            notifyItemRemoved(index);
            updateEmptyView();
        }
    }

    public void removeObject(int position) {
        objects.remove(position);
        notifyItemRemoved(position);
        updateEmptyView();
    }

    public void resetAll() {
        objects.clear();
        loading = false;
        notifyDataSetChanged();
    }

    public interface LoadObjectsCallback {
        void refreshAll(ZippyListAdapter sender);

        void loadMore(ZippyListAdapter sender, int skip);
    }

    private class LoadingHolder extends RecyclerView.ViewHolder {
        private final ProgressBar progressBar;

        private LoadingHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

}
