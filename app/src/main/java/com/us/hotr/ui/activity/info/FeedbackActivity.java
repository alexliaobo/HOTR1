package com.us.hotr.ui.activity.info;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.us.hotr.R;
import com.us.hotr.storage.HOTRSharePreference;
import com.us.hotr.ui.activity.BaseActivity;
import com.us.hotr.util.Tools;
import com.us.hotr.webservice.ServiceClient;
import com.us.hotr.webservice.rxjava.ProgressSubscriber;
import com.us.hotr.webservice.rxjava.SubscriberListener;

/**
 * Created by Mloong on 2017/10/13.
 */

public class FeedbackActivity extends BaseActivity {
    private EditText etContent, etPhone;
    private TextView tvSubmit;

    @Override
    protected int getLayout() {
        return R.layout.activity_feedback;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyTitle(R.string.feedback);
        initStaticView();
    }

    private void initStaticView(){
        etContent = (EditText) findViewById(R.id.et_content);
        etPhone = (EditText) findViewById(R.id.et_phone);
        tvSubmit = (TextView) findViewById(R.id.tv_submit);
        String number = HOTRSharePreference.getInstance(getApplicationContext()).getUserInfo().getMobile();
        if(number!= null && !number.isEmpty())
            etPhone.setText(number);

        tvSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etContent.getText().toString().trim().isEmpty())
                    Tools.Toast(FeedbackActivity.this, getString(R.string.key_in_feedback1));
                else if(etPhone.getText().toString().trim().isEmpty())
                    Tools.Toast(FeedbackActivity.this, getString(R.string.empty_phone_number));
                else if(etPhone.getText().toString().trim().length()!= 11)
                    Tools.Toast(FeedbackActivity.this, getString(R.string.wrong_phone_number_format));
                else{
                    submitFeedback();
                }
            }
        });
    }

    private void submitFeedback(){
        SubscriberListener mListener = new SubscriberListener<String>() {
            @Override
            public void onNext(String result) {
                Tools.Toast(FeedbackActivity.this, getString(R.string.submit_feedback));
                finish();
            }
        };
        ServiceClient.getInstance().userFeedback(new ProgressSubscriber(mListener, FeedbackActivity.this),
                HOTRSharePreference.getInstance(getApplicationContext()).getUserID(),
                etPhone.getText().toString().trim(), etContent.getText().toString().trim());
    }

}
