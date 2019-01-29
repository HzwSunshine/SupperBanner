package com.hzw.supperbanner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hzw.supperbanner.SupperBanner.LoopData;
import com.hzw.supperbanner.SupperBanner.SupperBannerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SupperBannerView bannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bannerView = findViewById(R.id.supperBanner);

        final List<String> strings = new ArrayList<>();
        strings.add("http://www.pptok.com/wp-content/uploads/2012/08/xunguang-4.jpg");
        strings.add("http://www.pptbz.com/pptpic/UploadFiles_6909/201203/2012031220134655.jpg");
        strings.add("http://img4q.duitang.com/uploads/item/201303/15/20130315223944_EvRW3.thumb.700_0.jpeg");
        strings.add("http://img5.duitang.com/uploads/item/201206/15/20120615031447_R5EcS.jpeg");
        strings.add("http://www.pptbz.com/pptpic/UploadFiles_6909/201309/2013093019370302.jpg");
        strings.add("http://pic15.nipic.com/20110813/1993003_205156492136_2.jpg");
        strings.add("http://www.51pptmoban.com/d/file/2012/09/05/33d927ff2a511eb23af542f85a19d123.jpg");
        final List<LoopData> datas = new ArrayList<>();
        for (int i = 0; i < strings.size(); i++) {
            LoopData data = new LoopData();
            data.url = strings.get(i);
            datas.add(data);
        }
        bannerView.setData(datas);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
