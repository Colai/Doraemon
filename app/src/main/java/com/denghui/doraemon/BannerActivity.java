package com.denghui.doraemon;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.denghui.doraemon.ui.widget.banner.Banner;
import com.denghui.doraemon.ui.widget.banner.BannerImageLoader;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BannerActivity extends AppCompatActivity {

    @BindView(R.id.banner)
    Banner banner;
    @BindView(R.id.recycle)
    RecyclerView recycle;

    private List<String> imgList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);
        ButterKnife.bind(this);

        imgList = new ArrayList<>();
        imgList.add("http://ww4.sinaimg.cn/large/006uZZy8jw1faic1xjab4j30ci08cjrv.jpg");
        imgList.add("http://ww4.sinaimg.cn/large/006uZZy8jw1faic21363tj30ci08ct96.jpg");
        imgList.add("http://ww4.sinaimg.cn/large/006uZZy8jw1faic259ohaj30ci08c74r.jpg");
        imgList.add("http://ww4.sinaimg.cn/large/006uZZy8jw1faic2b16zuj30ci08cwf4.jpg");
        imgList.add("http://ww4.sinaimg.cn/large/006uZZy8jw1faic2e7vsaj30ci08cglz.jpg");

        banner.setImageLoader(new BannerImageLoader()).setImages(imgList).start();

        recycle.setAdapter(new RecycleAdapter());
        recycle.setLayoutManager(new LinearLayoutManager(this));
    }

    class RecycleAdapter extends RecyclerView.Adapter<Holder>{

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(BannerActivity.this).inflate(R.layout.banner_item,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            holder.bindData(position);
        }

        @Override
        public int getItemCount() {
            return 30;
        }
    }

    class Holder extends RecyclerView.ViewHolder{

        @BindView(R.id.item_banner)
        Banner banner;
        @BindView(R.id.position)
        TextView textView;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        public void bindData(int pos){
            if (pos%2 == 0) {
                banner.setImageLoader(new BannerImageLoader()).setImages(imgList).isAutoPlay(true).start();
            } else {
                banner.setImageLoader(new BannerImageLoader()).setImages(imgList).isAutoPlay(false).start();
            }
            textView.setText(""+pos);
        }

    }

}
