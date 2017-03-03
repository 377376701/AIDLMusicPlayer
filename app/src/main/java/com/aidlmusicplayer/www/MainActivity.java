package com.aidlmusicplayer.www;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.ImageView;

import com.aidlmusicplayer.www.activity.MusicActivity;
import com.aidlmusicplayer.www.base.BaseRecyclerViewAdapter;
import com.aidlmusicplayer.www.bean.SongBillListBean;
import com.aidlmusicplayer.www.bean.SongListBean;
import com.aidlmusicplayer.www.config.Constant;
import com.aidlmusicplayer.www.image.ImageLoaderProxy;
import com.aidlmusicplayer.www.net.NetCallBack;
import com.aidlmusicplayer.www.net.NetManager;
import com.aidlmusicplayer.www.util.ToastUtil;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;


/**
 * author：agxxxx on 2017/3/3 10:49
 * email：agxxxx@126.com
 * blog: http://blog.csdn.net/zuiaisha1
 * github: https://github.com/agxxxx
 * Created by Administrator on 2017/3/3.
 */
public class MainActivity extends AppCompatActivity implements XRecyclerView.LoadingListener {
    private XRecyclerView mRvContainer;
    private ArrayList<SongListBean> mSong_list = new ArrayList<>();
    private SongListAdapter mSongListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRvContainer = ButterKnife.findById(this, R.id.rv_container);


        initVie();
        onRefresh();

    }




    int mType = 1;
    int mSize = 10;
    int mOffset = 0;
    int mPager = 0;
    private void loadData() {
        NetManager.
                getInstance().getSongBillListData(mType,
                mSize,
                mOffset,
                new NetCallBack<SongBillListBean>() {
                    @Override
                    public void onSuccess(SongBillListBean songBillListBean) {
                        mSongListAdapter.addAll(songBillListBean.song_list);

                        mRvContainer.refreshComplete();
                        mRvContainer.loadMoreComplete();
                    }
                    @Override
                   public void onFailure(String msg) {
                        ToastUtil.showShortToast(MainActivity.this,msg);
                        mRvContainer.refreshComplete();
                        mRvContainer.loadMoreComplete();
                    }
                });
    }




    private void initVie() {
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL);
        mRvContainer.setLayoutManager(staggeredGridLayoutManager);
        mRvContainer.setLoadingListener(this);
        mSongListAdapter = new SongListAdapter(mSong_list);
        mSongListAdapter.setOnItemClickListener(mSongListAdapter);
        mRvContainer.setAdapter(mSongListAdapter);
    }

    @Override
    public void onRefresh() {
        mPager = 0;
        mOffset = 0;
        mSongListAdapter.removeAll();
        loadData();
    }

    @Override
    public void onLoadMore() {
        mPager++;
        mOffset = mPager * mSize;
        loadData();
    }

    class SongListAdapter extends BaseRecyclerViewAdapter<SongListBean>
            implements BaseRecyclerViewAdapter.OnItemClickListener<SongListBean>{
        public SongListAdapter(List<SongListBean> mDatum) {
            super(mDatum);
        }

        @Override
        protected int getItemLayoutId() {
            return R.layout.item_music;
        }
        @Override
        protected void onBind(ViewHolder holder, int position, SongListBean data) {
            ImageView imageView = holder.getImageView(R.id.iv_icon);
            ImageLoaderProxy.getInstance().
                    transform(MainActivity.this, data.pic_big, imageView);
            holder.setTextView(R.id.tv_name,data.title);
        }


        @Override
        public void onItemClick(View view, int position, SongListBean info) {


            Intent intent = new Intent(MainActivity.this, MusicActivity.class);
            intent.putExtra(Constant.TAG_VAL_1,  mSong_list);

            startActivity(intent);
        }
    }


}
