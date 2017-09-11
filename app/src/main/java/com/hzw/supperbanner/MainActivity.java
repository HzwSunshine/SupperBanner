package com.hzw.supperbanner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hzw.supperbanner.SupperBanner.SupperBannerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SupperBannerView bannerView;
    private SupperBannerView bannerView2;
    private List<String> imageUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bannerView = (SupperBannerView) findViewById(R.id.supperBanner);
        bannerView2 = (SupperBannerView) findViewById(R.id.supperBanner2);
        imageUrls = new ArrayList<>();
        imageUrls.add("https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=3894062614,1839770030&fm=116&gp=0.jpg");
        imageUrls.add("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=214931719,1608091472&fm=116&gp=0.jpg");
        imageUrls.add("https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=1794894692,1423685501&fm=116&gp=0.jpg");
        imageUrls.add("https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=4165520467,4140249363&fm=116&gp=0.jpg");

        bannerView.setImageLoadListener(new SupperBannerView.ImageLoadListener() {
            @Override
            public void loadImage(ImageView imageView, int position) {
                int width = imageView.getWidth();
                if (width == 0) {
                    Glide.with(MainActivity.this)
                            .load(imageUrls.get(position))
                            .into(imageView);
                } else {
                    Glide.with(MainActivity.this)
                            .load(imageUrls.get(position))
                            .override(imageView.getWidth(), imageView.getHeight())
                            .into(imageView);
                }
            }

            @Override
            public void currentPage(int position) {
                Log.i("xxx", "onPageSelected:  " + position);
            }
        });
        bannerView.setItemClickListener(new SupperBannerView.ItemClickListener() {
            @Override
            public void click(View view, int position) {
                Toast.makeText(getApplicationContext(), "点你妹：" + position, Toast.LENGTH_SHORT).show();
            }
        });
        bannerView.start(imageUrls);


        /*-------------------------------------------------------*/


        bannerView2.setImageLoadListener(new SupperBannerView.ImageLoadListener() {
            @Override
            public void loadImage(ImageView imageView, int position) {
                int width = imageView.getWidth();
                if (width == 0) {
                    Glide.with(MainActivity.this)
                            .load(imageUrls.get(position))
                            .into(imageView);
                } else {
                    Glide.with(MainActivity.this)
                            .load(imageUrls.get(position))
                            .override(imageView.getWidth(), imageView.getHeight())
                            .into(imageView);
                }
            }

            @Override
            public void currentPage(int position) {

            }
        });
        bannerView2.setItemClickListener(new SupperBannerView.ItemClickListener() {
            @Override
            public void click(View view, int position) {
                Toast.makeText(getApplicationContext(), "点你妹：" + position, Toast.LENGTH_SHORT).show();
            }
        });
        bannerView2.start(imageUrls);

    }

    @Override
    protected void onResume() {
        super.onResume();
        bannerView.resume();
        bannerView2.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bannerView.pause();
        bannerView2.pause();
    }

}
