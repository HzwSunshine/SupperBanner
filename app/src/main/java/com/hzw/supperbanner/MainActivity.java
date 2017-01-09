package com.hzw.supperbanner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.hzw.supperbanner.SupperBanner.SupperBannerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SupperBannerView bannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bannerView = (SupperBannerView) findViewById(R.id.supperBanner);
        List<String> strings = new ArrayList<>();
        strings.add("https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=3894062614,1839770030&fm=116&gp=0.jpg");
        strings.add("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=214931719,1608091472&fm=116&gp=0.jpg");
        strings.add("https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=1794894692,1423685501&fm=116&gp=0.jpg");
        strings.add("https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=4165520467,4140249363&fm=116&gp=0.jpg");
        bannerView.start(strings);

        bannerView.setItemClickListener(new SupperBannerView.ItemClickListener() {
            @Override
            public void click(View view, int position) {
                Toast.makeText(getApplicationContext(), "点你妹：" + position, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        bannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bannerView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bannerView.cancel();
    }
}
