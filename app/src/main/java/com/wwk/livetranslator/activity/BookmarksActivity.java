package com.wwk.livetranslator.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wwk.livetranslator.Constants;
import com.wwk.livetranslator.R;
import com.wwk.livetranslator.manager.BookmarkManager;
import com.wwk.livetranslator.model.Translation;
import com.wwk.livetranslator.widget.SwipeToDeleteCallback;
import com.wwk.livetranslator.widget.ZippyListAdapter;

public class BookmarksActivity extends AppCompatActivity
    implements ZippyListAdapter.LoadObjectsCallback {

    private RecyclerView listView;
    private View emptyView;
    private SwipeRefreshLayout refreshLayout;
    private BookmarkListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        listView = findViewById(R.id.listView);
        emptyView = findViewById(R.id.emptyView);
        refreshLayout = findViewById(R.id.swipeRefreshLayout);

        setupToolbar();
        initRecyclerView();
        refreshAll(listAdapter);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
    }

    private void initRecyclerView() {
        listView.setHasFixedSize(true);
        listAdapter = new BookmarkListAdapter();
        listAdapter.setupListView(listView);
        listAdapter.setEmptyView(emptyView);
        listAdapter.setRefreshLayout(refreshLayout);
        listAdapter.setLoadCallback(this);

        // Swipe to delete
        SwipeToDeleteCallback swipeHandler = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                BookmarkManager.getInstance().remove(position);
                listAdapter.removeObject(position);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeHandler);
        itemTouchHelper.attachToRecyclerView(listView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void refreshAll(ZippyListAdapter sender) {
        BookmarkManager.getInstance().reloadBookmarks(Constants.BOOKMARK_PAGE_SIZE,
                (translations, hasMore) -> listAdapter.didFinishLoading(translations, hasMore, true));
    }

    @Override
    public void loadMore(ZippyListAdapter sender, int skip) {
        BookmarkManager.getInstance().loadMoreBookmarks(Constants.BOOKMARK_PAGE_SIZE, skip,
                (translations, hasMore) -> listAdapter.didFinishLoading(translations, hasMore, false));
    }

    public class BookmarkListAdapter extends ZippyListAdapter<Translation> {

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
            if (viewHolder == null) {
                if (viewType == ITEM_OBJECT) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_translation, parent, false);
                    viewHolder = new ViewHolder(v);
                }
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            if (holder instanceof ViewHolder) {
                Translation translation = getItemObject(position);
                ViewHolder viewHolder = (ViewHolder) holder;
                viewHolder.sourceTextView.setText(translation.sourceText);
                viewHolder.targetTextView.setText(translation.targetText);
            }
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            private final CardView cardView;
            private final TextView sourceTextView;
            private final TextView targetTextView;

            private ViewHolder(View v) {
                super(v);
                cardView = v.findViewById(R.id.cardView);
                sourceTextView = v.findViewById(R.id.sourceTextView);
                targetTextView = v.findViewById(R.id.targetTextView);
            }
        }

    }
}
