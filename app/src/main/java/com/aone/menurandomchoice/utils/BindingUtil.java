package com.aone.menurandomchoice.utils;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aone.menurandomchoice.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import androidx.databinding.BindingAdapter;
import retrofit2.http.Body;

public class BindingUtil {

    @BindingAdapter({"bind:photoUrl", "bind:error"})
    public static void loadImage(ImageView imageView, String url, Drawable errorDrawable) {
        Glide.with(imageView.getContext())
                .load(url)
                .apply(new RequestOptions().placeholder(errorDrawable).error(errorDrawable).centerCrop())
                .into(imageView);
    }

    @BindingAdapter({"bind:url", "bind:defaultDrawable"})
    public static void loadRegisterImage(ImageView imageView, String url, Drawable defaultDrawable) {
        GlideUtil.loadRegisterImageWithSkipCache(imageView, url, defaultDrawable);
    }

    @BindingAdapter({"bind:photoUrl"})
    public static void loadImage(ImageView imageView, String url) {
        GlideUtil.loadImageWithSkipCache(imageView, url);
    }

    @BindingAdapter({"bind:opentime", "bind:closetime"})
    public static void setStoreTime(TextView textView, String opentime, String closetime) {
        if(opentime == null) {
            opentime = " ";
        }
        if(closetime == null) {
            closetime = " ";
        }

        textView.setText(String.format("%s-%s", opentime, closetime));
    }
}
