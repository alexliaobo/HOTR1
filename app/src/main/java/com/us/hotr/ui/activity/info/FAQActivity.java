package com.us.hotr.ui.activity.info;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebView;
import android.widget.TextView;

import com.us.hotr.Constants;
import com.us.hotr.R;
import com.us.hotr.storage.bean.Info;
import com.us.hotr.ui.activity.BaseActivity;
import com.us.hotr.util.Tools;

/**
 * Created by Mloong on 2017/10/13.
 */

public class FAQActivity extends BaseActivity {

    private Info info;
    private TextView tvTitle;
    private WebView wvContent;

    @Override
    protected int getLayout() {
        return R.layout.activity_faq;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyTitle(R.string.faq);
        info = (Info)getIntent().getExtras().getSerializable(Constants.PARAM_DATA);
        initStaticView();
    }

    private void initStaticView(){
        tvTitle = (TextView) findViewById(R.id.tv_title);
        wvContent = (WebView) findViewById(R.id.wv_content);

        tvTitle.setText(info.getTitle());
        wvContent.loadData(info.getContent(), "text/html; charset=UTF-8", null);
    }
}
