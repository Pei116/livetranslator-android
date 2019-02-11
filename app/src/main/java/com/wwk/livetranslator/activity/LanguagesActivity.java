package com.wwk.livetranslator.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;

import com.wwk.livetranslator.Constants;
import com.wwk.livetranslator.R;
import com.wwk.livetranslator.manager.LanguageManager;
import com.wwk.livetranslator.manager.TranslationManager;
import com.wwk.livetranslator.model.Language;
import com.wwk.livetranslator.widget.DragToSwapCallback;

import java.util.List;

public class LanguagesActivity extends AppCompatActivity {

    private RecyclerView listView;
    private ItemTouchHelper itemTouchHelper;
    private MenuItem checkAllItem;
    private MenuItem checkClearItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_languages);

        listView = findViewById(R.id.listView);

        setupToolbar();
        initRecyclerView();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initRecyclerView() {
        listView.setHasFixedSize(true);
        listView.setAdapter(new LanguageSelectionAdapter());
        listView.setLayoutManager(new LinearLayoutManager(this));

        DragToSwapCallback dragHandler = new DragToSwapCallback() {
            @Override
            protected void onItemMove(int oldPosition, int newPosition) {
                List<Language> languages = LanguageManager.getInstance().getMyLanguages();
                if (languages.size() > oldPosition) {
                    LanguageManager.getInstance().moveLanguage(languages.get(oldPosition), newPosition);
                }
                listView.getAdapter().notifyItemMoved(oldPosition, newPosition);
            }
        };

        itemTouchHelper = new ItemTouchHelper(dragHandler);
        itemTouchHelper.attachToRecyclerView(listView);

        DividerItemDecoration dividerDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        listView.addItemDecoration(dividerDecoration);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.languages, menu);

        checkAllItem = menu.findItem(R.id.action_check_all);
        checkClearItem = menu.findItem(R.id.action_check_clear);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (LanguageManager.getInstance().isAllSelected()) {
            checkAllItem.setVisible(false);
            checkClearItem.setVisible(true);
        }
        else {
            checkAllItem.setVisible(true);
            checkClearItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_check_all:
                LanguageManager.getInstance().selectAll();
                listView.getAdapter().notifyDataSetChanged();
                invalidateOptionsMenu();
                return true;
            case R.id.action_check_clear:
                LanguageManager.getInstance().clearSelection();
                listView.getAdapter().notifyDataSetChanged();
                invalidateOptionsMenu();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class LanguageSelectionAdapter extends RecyclerView.Adapter {

        List<Language> languages = LanguageManager.getInstance().getLanguagesForSelection();
        private boolean onBind;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public int getItemCount() {
            return languages.size();
        }

        @Override
        public int getItemViewType(int position) {
            Language language = languages.get(position);
            return language.isFavorite ? Constants.LANGUAGE_ITEM_FAVORITE : Constants.LANGUAGE_ITEM_NORMAL;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ViewHolder viewHolder = (ViewHolder) holder;
            final Language language = languages.get(position);
            viewHolder.languageView.setText(language.name);
            onBind = true;
            viewHolder.languageView.setChecked(language.isFavorite);
            onBind = false;
            viewHolder.languageView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!onBind) {
                    LanguageManager.getInstance().toggleLanguage(language, isChecked);
                    languages = LanguageManager.getInstance().getLanguagesForSelection();
                    notifyDataSetChanged();
                    LanguagesActivity.this.invalidateOptionsMenu();

                    if (language.equals(TranslationManager.getInstance().getSourceLanguage())) {
                        TranslationManager.getInstance().setSourceLanguage(null);
                    }
                    if (language.equals(TranslationManager.getInstance().getTargetLanguage())) {
                        if (languages.size() > 0)
                            TranslationManager.getInstance().setTargetLanguage(languages.get(0).code);
                    }
                }
            });
            viewHolder.handleButton.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(viewHolder);
                }
                return false;
            });
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            private final View contentView;
            private final CheckBox languageView;
            private final ImageButton handleButton;

            private ViewHolder(View v) {
                super(v);
                contentView = v;
                languageView = v.findViewById(R.id.languageView);
                handleButton = v.findViewById(R.id.handleButton);
            }
        }

    }
}
