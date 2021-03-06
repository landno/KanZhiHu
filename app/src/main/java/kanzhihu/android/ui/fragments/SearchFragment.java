package kanzhihu.android.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import butterknife.InjectView;
import com.cocosw.undobar.UndoBarController;
import com.cocosw.undobar.UndoBarStyle;
import com.squareup.picasso.Picasso;
import javax.inject.Inject;
import kanzhihu.android.AppConstant;
import kanzhihu.android.R;
import kanzhihu.android.database.ShareActionProvider;
import kanzhihu.android.models.Article;
import kanzhihu.android.modules.IInject;
import kanzhihu.android.ui.adapter.SearchAdapter;
import kanzhihu.android.ui.fragments.base.BaseFragment;
import kanzhihu.android.ui.presenters.QueryPresenter;
import kanzhihu.android.ui.presenters.impl.QueryPresenterImpl;
import kanzhihu.android.ui.views.QueryView;
import kanzhihu.android.utils.Preferences;
import kanzhihu.android.utils.ShareUtils;

/**
 * Created by Jiahui.wen on 2014/11/19.
 */
public class SearchFragment extends BaseFragment implements QueryView {

    public static SearchFragment newInstance(boolean bMarkView) {
        SearchFragment fragment = new SearchFragment();
        Bundle data = new Bundle();
        data.putBoolean(AppConstant.ACTION_MODE_MARK_VIEW, bMarkView);
        fragment.setArguments(data);
        return fragment;
    }

    private SearchView mSearchView;

    private SearchAdapter mAdapter;

    private QueryPresenter mPresenter;

    private boolean bMarkView;

    private UndoBarStyle mUndoStyle;

    private ShareActionProvider mShareActionProvider;
    private Article mShareArticle;
    private MenuItem mShareMenu;

    @Inject Preferences mPreference;

    @Inject Picasso picasso;

    @InjectView(R.id.recyclerView_mark) RecyclerView mRecyclerView;

    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof IInject) {
            IInject injector = (IInject) activity;
            injector.inject(this);
        } else {
            throw new IllegalArgumentException("activity must implements IInject");
        }
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        bMarkView = getArguments().getBoolean(AppConstant.ACTION_MODE_MARK_VIEW, false);
    }

    @Override public int getViewRec() {
        return R.layout.fragment_search;
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(mPresenter.getQueryTextListener());

        mShareMenu = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareMenu);
        mShareActionProvider.setShareHistoryFileName(null);

        if (mShareArticle == null) {
            mShareMenu.setVisible(false);
        }

        if (!bMarkView) {
            MenuItemCompat.setOnActionExpandListener(searchItem, mPresenter.getActionExpandListener());
            MenuItemCompat.expandActionView(searchItem);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new SearchAdapter(getActivity(), null, picasso);
        mAdapter.setImageMode(mPreference.imageMode());
        mRecyclerView.setAdapter(mAdapter);

        mPresenter = new QueryPresenterImpl(this, bMarkView, mPreference);
        mPresenter.loadInitData();
    }

    @Override public void onSearchViewClosed(MenuItem menuItem) {
        if (!bMarkView) {
            getActivity().onBackPressed();
        }
    }

    @Override public Activity getContext() {
        return getActivity();
    }

    @Override public void swapCursor(Cursor cursor) {
        mAdapter.changeCursor(cursor);
    }

    @Override public boolean getVisiable() {
        return isVisible();
    }

    @Override public void switchImageMode(boolean imageVisiable) {
        mAdapter.setImageMode(imageVisiable);
    }

    @Override public void onQueryTextChange(String newText) {
        mAdapter.setCurFilter(newText);
    }

    @Override public Article getArticle(int position) {
        Article article = null;
        Cursor cursor = mAdapter.getCursor();
        if (cursor.moveToPosition(position)) {
            article = Article.fromCursor(cursor);
        }

        return article;
    }

    @Override public void showUndo(Article article) {
        new UndoBarController.UndoBar(getActivity()).message(getString(R.string.delete_article, article.title))
            .listener(mPresenter.getUndoListener())
            .noicon(true)
            .token(article)
            .duration(AppConstant.UNDO_BAR_DURATION)
            .show();
    }

    @Override public void createShareView(Article article) {
        mShareArticle = article;
        mShareMenu.setVisible(true);

        Intent shareIntent = ShareUtils.getShareIntent(mShareArticle);
        mShareActionProvider.setShareIntent(shareIntent);
        mShareActionProvider.showPopup();
    }

    @Override public void closeShareView() {
        mShareArticle = null;
        mShareMenu.setVisible(false);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        mPresenter.onDestory();
    }
}
