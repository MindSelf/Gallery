package com.example.zhaolexi.imageloader.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.example.zhaolexi.imageloader.bean.Album;
import com.example.zhaolexi.imageloader.view.AlbumFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZHAOLEXI on 2018/1/26.
 */

/*因为FragmentStatePagerAdapter在超出缓存时是调用FragmentManager的remove方法，Fragment会被destroy和attach掉
  如果使用FragmentPagerAdapter,在超出缓存时调用FragmentManager的detach方法，Fragment只会被destroyView，其实例仍然保存在内存中
  由于我们的相册经常需要动态创建，分页较多，所以使用FragmentStatePagerAdapter
*/
public class AlbumPagerAdapter extends FragmentStatePagerAdapter {

    private List<Album> mAlbumList;
    private List<AlbumFragment> mFragmentList;

    public AlbumPagerAdapter(FragmentManager fm, List<Album> list) {
        super(fm);
        mAlbumList = list;
        mFragmentList = new ArrayList<>();
    }

    //getItem在PagerAdapter的instantiateItem中调用，返回的Fragment将作为Page的key
    @Override
    public Fragment getItem(int position) {
        //为了节省内存，在需要时才实例化Fragment
        AlbumFragment fragment=AlbumFragment.newInstance(mAlbumList.get(position));
        if (position >= mFragmentList.size()) {
            mFragmentList.add(fragment);
        }else{
            //之前添加过只是超出缓存后被清空，则添加到旧位置中
            mFragmentList.set(position, fragment);
        }
        return fragment;
    }

    //注意：notifyDataSetChanged会触发destroyItem
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if(position<mFragmentList.size()) mFragmentList.set(position, null);
        super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
        return mAlbumList.size();
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        return mAlbumList.get(position).getTitle();
    }

    //该item的位置是否会改变
    //如果要动态新建和销毁Pager，需要返回POSITION_NONE（否则返回默认值POSITION_UNCHANGED）
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public AlbumFragment getAlbumFragmentAt(int position) {
        return mFragmentList.get(position);
    }
}
