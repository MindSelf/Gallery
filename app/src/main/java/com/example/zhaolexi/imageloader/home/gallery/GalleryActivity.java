package com.example.zhaolexi.imageloader.home.gallery;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.home.InteractInterface;
import com.example.zhaolexi.imageloader.home.album.AlbumFragment;
import com.example.zhaolexi.imageloader.home.manager.Album;
import com.example.zhaolexi.imageloader.home.manager.AlbumManageViewInterface;
import com.example.zhaolexi.imageloader.home.manager.AlbumManager;
import com.example.zhaolexi.imageloader.home.navigation.Navigation;
import com.example.zhaolexi.imageloader.home.navigation.NavigationViewInterface;
import com.example.zhaolexi.imageloader.redirect.access.AlbumPasswordDialog;
import com.example.zhaolexi.imageloader.redirect.login.OnLoginStateChangeCallback;
import com.example.zhaolexi.imageloader.redirect.login.TokenManager;
import com.example.zhaolexi.imageloader.redirect.router.Result;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GalleryActivity extends AppCompatActivity implements InteractInterface, View.OnClickListener {

    public static final String ORIGIN_ALBUM = "album";
    public static final String ACTION = "action";
    public static final int ACTION_REACCESS = 0;
    public static final int ACTION_NEW_ALBUM = 1;

    private AlbumManageViewInterface mAlbumManager;
    private NavigationViewInterface mNavigation;
    private MenuItem mMenuItem;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private FloatingActionButton mFab;

    private RecyclerView.RecycledViewPool mRecycledViewPool;
    private AlbumPagerAdapter mPageAdapter;
    private List<Album> mAlbumList;

    private OnLoginStateChangeCallback mCallback;

    private boolean mIsInManagePage, mCanLoadWithoutWifi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initView();
        mAlbumManager.attachPresenter();
        mNavigation.attachPresenter();

        if (mAlbumList.isEmpty()) {
            mAlbumManager.showManagePage(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAlbumManager.detachPresenter();
        mNavigation.detachPresenter();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Album album = (Album) intent.getSerializableExtra(ORIGIN_ALBUM);
        if (album != null) {
            int action = intent.getIntExtra(ACTION, -1);
            switch (action) {
                case ACTION_REACCESS:
                    mAlbumManager.showManagePage(false);
                    mAlbumManager.getPresenter().removeAlbum(album);
                    new AlbumPasswordDialog.Builder(this)
                            .setTitle(getString(R.string.add_album))
                            .setAccountDef(String.valueOf(album.getAccount()))
                            .setCallback(new OnRequestFinishListener<Album>() {
                                @Override
                                public void onSuccess(Album album) {
                                    if (album != null) {
                                        mAlbumManager.getPresenter().addAlbum(album);
                                    }
                                }

                                @Override
                                public void onFail(String reason, Result result) {
                                }
                            })
                            .build().show();
                    break;
                case ACTION_NEW_ALBUM:
                    int currentItem;
                    if (mAlbumList.contains(album)) {
                        int pos = mAlbumList.indexOf(album);
                        mAlbumList.set(pos, album);
                        currentItem = pos;
                    } else {
                        mAlbumManager.getPresenter().addAlbum(album);
                        currentItem = mAlbumList.size() - 1;
                    }
                    mAlbumManager.getPresenter().setCurrentPage(currentItem);
                    mPageAdapter.notifyDataSetChanged();
                    mViewPager.setCurrentItem(currentItem, false);
                    break;
            }
            return;
        }

        if (mAlbumList.isEmpty()) {
            mAlbumManager.showManagePage(false);
        }
    }

    private void initData() {
        mCallback = new DefaultLoginStateCallback(this);
        TokenManager.addLoginStateChangeCallback(mCallback);
        mAlbumList = new ArrayList<>();
        mPageAdapter = new AlbumPagerAdapter(getSupportFragmentManager(), mAlbumList);
        //从数据库中获取历史记录
        Connector.getDatabase();    //数据库不存在则创建数据库
        mAlbumList.addAll(DataSupport.findAll(Album.class));
    }

    private void removeData() {
        mAlbumList.clear();
        DataSupport.deleteAll(Album.class);
    }

    private void initView() {
        setContentView(R.layout.activity_gallery);
        mAlbumManager = new AlbumManager(this, mPageAdapter, mAlbumList);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.open_drawer, R.string.close_drawer);
        ActionBar actionBar = getSupportActionBar();
        //返回箭头
        actionBar.setDisplayHomeAsUpEnabled(true);
        //设置显示三横杠
        toggle.syncState();
        //添加菜单拖动监听事件  根据菜单的拖动距离 将距离折算成旋转角度
        mDrawerLayout.addDrawerListener(toggle);
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                mNavigation.onNavigationShown();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                mNavigation.onNavigationDismiss();
            }
        });

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigation = new Navigation(mDrawerLayout, mNavigationView);

        mViewPager = (ViewPager) findViewById(R.id.vp_album);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            //ViewPager在初始化数据时不会回调onPageSelected方法
            //并且就算调用setCurrentItem(0)，也会因为currentItem位置没有变化而不回调onPageSelected
            //所以也要在初始化时进行设置

            //会在setCurrentItem或者scrollToItem之后回调
            //只有当前位置发生改变才会被调用
            //如果删除当前item然后调用notifyDataSetChanged，也会调用setCurrentItem从而出发onPageSelected
            @Override
            public void onPageSelected(final int position) {
                if (!mIsInManagePage) {
                    //FAB动画
                    FabBehavior behavior = (FabBehavior) ((CoordinatorLayout.LayoutParams) (mFab.getLayoutParams())).getBehavior();
                    if (isCurrentAlbumAccessible(position)) {
                        behavior.startOpening(mFab);
                    } else {
                        behavior.startClosing(mFab);
                    }
                }

                initFab(position);
                initCollectionState(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mViewPager.setAdapter(mPageAdapter);
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        //与ViewPager进行绑定（初始化监听器、同步适配器数据等,最终在populateFromPagerAdapter中addTab）
        mTabLayout.setupWithViewPager(mViewPager);

        mFab = (FloatingActionButton) findViewById(R.id.add_photos);
        mFab.setVisibility(isCurrentAlbumAccessible(0) ? View.VISIBLE : View.GONE);

        initFab(0);
    }

    private void initFab(final int position) {
        //设置FAB回调
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlbumFragment fragment = mPageAdapter.getAlbumFragmentAt(position);
                if (fragment != null) {
                    fragment.getPresenter().openSelectPhotoPage();
                }
            }
        });
    }

    private void initCollectionState(int position) {
        if (position < mAlbumList.size()) {
            mMenuItem.setIcon(mAlbumList.get(position).isFavorite() ? R.mipmap.ic_star : R.mipmap.ic_unstar);
        }
    }

    private void onLogOut() {
        removeData();
        mPageAdapter.notifyDataSetChanged();
        mAlbumManager.onAlbumClear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_gallery, menu);
        mMenuItem = menu.getItem(0);
        if (isInManagePage()) {
            mMenuItem.setVisible(false);
        }
        initCollectionState(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.collect_album:
                int pos = mViewPager.getCurrentItem();
                Album album = mAlbumList.get(pos);
                mPageAdapter.getAlbumFragmentAt(pos).getPresenter().collectAlbum(album);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!mAlbumManager.onBackPressed()) {
            super.onBackPressed();
        }
    }

    private boolean isCurrentAlbumAccessible(int position) {
        if (!mAlbumList.isEmpty()) {
            Album album = mAlbumList.get(position);
            if (album.isAccessible() && !TextUtils.isEmpty(album.getAid())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public RecyclerView.RecycledViewPool getRecycledViewPool() {
        return mRecycledViewPool;
    }

    @Override
    public void setRecycledViewPool(RecyclerView.RecycledViewPool recycledViewPool) {
        mRecycledViewPool = recycledViewPool;
    }

    @Override
    public void setCanLoadWithoutWifi(boolean canLoadWithoutWifi) {
        mCanLoadWithoutWifi = canLoadWithoutWifi;
    }

    @Override
    public boolean canLoadWithoutWifi() {
        return mCanLoadWithoutWifi;
    }

    @Override
    public boolean onShowManagerPage(int currentPos) {
        mTabLayout.setVisibility(View.GONE);
        mFab.clearAnimation();
        mFab.setVisibility(View.GONE);
        if (mMenuItem != null) {
            mMenuItem.setVisible(false);
        }
        if (mViewPager.getCurrentItem() != currentPos) {
            mAlbumManager.getPresenter().setCurrentPage(mViewPager.getCurrentItem());     //记录当前位置
            return true;
        }
        return false;
    }

    @Override
    public void onDismissManagerPage(int currentPos) {
        if (mViewPager.getCurrentItem() != currentPos) {
            mViewPager.setCurrentItem(mAlbumManager.getPresenter().getCurrentPage());   //变更到新的位置
        }
        mFab.setVisibility(isCurrentAlbumAccessible(currentPos) ? View.VISIBLE : View.GONE);
        mMenuItem.setVisible(true);
        initFab(currentPos);
        initCollectionState(currentPos);
        mTabLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void setIsInManagePage(boolean isInManagePage) {
        mIsInManagePage = isInManagePage;
    }

    @Override
    public boolean isInManagePage() {
        return mIsInManagePage;
    }

    @Override
    public void changeCollectState(boolean isCollected) {
        mMenuItem.setIcon(isCollected ? R.mipmap.ic_star : R.mipmap.ic_unstar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                break;
        }
    }

    private static class DefaultLoginStateCallback implements OnLoginStateChangeCallback {

        private WeakReference<GalleryActivity> mRef;

        DefaultLoginStateCallback(GalleryActivity activity) {
            mRef = new WeakReference<>(activity);
        }

        @Override
        public void onSignIn() {

        }

        @Override
        public void onLogOut() {
            if (mRef.get() != null) {
                mRef.get().onLogOut();
            }
        }
    }
}
