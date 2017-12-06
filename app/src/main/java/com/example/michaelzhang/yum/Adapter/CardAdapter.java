package com.example.michaelzhang.yum.Adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.michaelzhang.yum.Model.Model;
import com.example.michaelzhang.yum.R;
import com.huxq17.swipecardsview.BaseCardAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Allan on 11/26/2017.
 */

public class CardAdapter extends BaseCardAdapter {

    private List<Model> modelList;
    private Context context;

    public CardAdapter(List<Model> modelList, Context context) {
        this.modelList = modelList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return modelList.size();
    }

    @Override
    public int getCardLayoutId() {
        return R.layout.card_item;
    }

    @Override
    public void onBindData(int position, View cardview) {
            if(modelList == null || modelList.size() == 0)
            {
                return;
            }
        ImageView imageView = (ImageView)cardview.findViewById(R.id.imageView);
        TextView textView = (TextView)cardview.findViewById(R.id.textView);
        Model model = modelList.get(position);
        textView.setText(model.getTitle());
        Picasso.with(context).load(model.getImage()).into(imageView);
    }
}
