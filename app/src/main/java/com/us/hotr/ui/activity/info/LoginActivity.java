package com.us.hotr.ui.activity.info;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.tencent.stat.StatService;
import com.us.hotr.Constants;
import com.us.hotr.R;
import com.us.hotr.customview.DeactivatedViewPager;
import com.us.hotr.eventbus.Events;
import com.us.hotr.eventbus.GlobalBus;
import com.us.hotr.storage.HOTRSharePreference;
import com.us.hotr.ui.dialog.VoucherDialog;
import com.us.hotr.ui.fragment.info.LoginPasswordFragment;
import com.us.hotr.ui.fragment.info.LoginPhoneFragment;
import com.us.hotr.util.Tools;
import com.us.hotr.webservice.ServiceClient;
import com.us.hotr.webservice.rxjava.ProgressSubscriber;
import com.us.hotr.webservice.rxjava.SubscriberListener;

import org.greenrobot.eventbus.Subscribe;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

//import com.tencent.stat.StatMultiAccount;

/**
 * Created by Mloong on 2017/10/13.
 */

public class LoginActivity extends AppCompatActivity {
    private ArrayList<String> titleList;
    private ArrayList<Fragment> fragmentList;

    private TabLayout tabLayout;
    private DeactivatedViewPager viewPager;
    private PagerAdapter adapter;
    private ImageView ivBack, ivTencent, ivBackground;
    private static LoginListener loginListener;

    public static void setLoginListener(LoginListener l){
        loginListener = l;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        GlobalBus.getBus().register(this);
        initStaticView();
    }

    private void initStaticView(){
        titleList = new ArrayList<String>() {{
            add(getString(R.string.phone_login));
            add(getString(R.string.password_login));
        }};

        fragmentList = new ArrayList<Fragment>() {{
            add(LoginPhoneFragment.newInstance());
            add(LoginPasswordFragment.newInstance());
        }};

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (DeactivatedViewPager) findViewById(R.id.pager);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivTencent = (ImageView) findViewById(R.id.iv_tencent);
        ivBackground = (ImageView) findViewById(R.id.iv_background);

        ivBackground.setImageBitmap(readBitMap(this, R.drawable.bg_login));
        adapter = new PagerAdapter(getSupportFragmentManager(), titleList, fragmentList);
        viewPager.setSwipeLocked(true);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager, true);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        ivTencent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Tools.loginWechat(LoginActivity.this);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(loginListener!=null)
            loginListener.onLoginCancel();
        loginListener = null;
        super.onBackPressed();
    }

    public static Bitmap readBitMap(Context context, int resId){
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is,null,opt);
    }

    public void loginSuccess(){
        if(loginListener!=null)
            loginListener.onLoginSuccess();
        loginListener = null;
    }

    public class PagerAdapter extends FragmentStatePagerAdapter {

        private ArrayList<String> titleList;
        private ArrayList<Fragment> fragmentList;

        public PagerAdapter(FragmentManager fm, ArrayList<String> titleList, ArrayList<Fragment> fragmentList) {
            super(fm);
            this.titleList = titleList;
            this.fragmentList = fragmentList;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleList.get(position);
        }
    }

    public interface LoginListener{
        void onLoginSuccess();
        void onLoginCancel();
    }

    @Subscribe
    public void getMessage(final Events.Login login) {
        if (login.getGetLoginResponse().getUser() != null
                && login.getGetLoginResponse().getUser().getMobile() != null
                && !login.getGetLoginResponse().getUser().getMobile().isEmpty()) {
            JMessageClient.register("user" + login.getGetLoginResponse().getUser().getUserId(), "123456", new BasicCallback() {
                @Override
                public void gotResult(int i, String s) {
                    if (i == 0 || i == 898001) {
                        JMessageClient.login("user" + login.getGetLoginResponse().getUser().getUserId(), "123456", new BasicCallback() {
                            @Override
                            public void gotResult(int i, String s) {
                                if (i == 0) {
                                    UserInfo userInfo = JMessageClient.getMyInfo();
                                    userInfo.setNickname(login.getGetLoginResponse().getUser().getNickname());
                                    userInfo.setAddress(login.getGetLoginResponse().getUser().getHead_portrait());
                                    JMessageClient.updateMyInfo(UserInfo.Field.nickname, userInfo, new BasicCallback() {
                                        @Override
                                        public void gotResult(int i, String s) {
                                        }
                                    });
                                    JMessageClient.updateMyInfo(UserInfo.Field.address, userInfo, new BasicCallback() {
                                        @Override
                                        public void gotResult(int i, String s) {
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
            int i = Integer.parseInt(login.getGetLoginResponse().getUser().getMobile().substring(2));
            JPushInterface.setAlias(this, i, login.getGetLoginResponse().getUser().getMobile());
            HOTRSharePreference.getInstance(getApplicationContext()).storeUserID(login.getGetLoginResponse().getJsessionid());
            HOTRSharePreference.getInstance(getApplicationContext()).storeUserInfo(login.getGetLoginResponse().getUser());
            HOTRSharePreference.getInstance(getApplicationContext()).storeMasseurInfo(login.getGetLoginResponse().getMasseurData());
//            StatMultiAccount account = new StatMultiAccount(
//                    StatMultiAccount.AccountType.PHONE_NO, login.getGetLoginResponse().getUser().getMobile());
//            long time = System.currentTimeMillis() / 1000;
//            account.setLastTimeSec(time);
//            account.setExpireTimeSec(time + 60*60*24*365*10);
//            StatService.reportMultiAccount(this, account);
            if(login.getGetLoginResponse().isFirst_register()) {
                getNewUserVoucher();
                StatService.trackCustomKVEvent(this, Constants.MTA_ID_NEW_USER, new Properties());
            }else {
                finish();
            }
            loginSuccess();
        } else {
            Intent i = new Intent(this, ChangePhoneNumberActivity.class);
            Bundle b = new Bundle();
            b.putInt(Constants.PARAM_TYPE, ChangePhoneNumberActivity.TYPE_SET_PASSWORD);
            b.putSerializable(Constants.PARAM_DATA, login.getGetLoginResponse().getWechatUser());
            b.putString(Constants.PARAM_ID, login.getGetLoginResponse().getJsessionid());
            i.putExtras(b);
            startActivityForResult(i, 0);
        }
    }

    private void getNewUserVoucher(){
        SubscriberListener mListener = new SubscriberListener<String>() {
            @Override
            public void onNext(final String result) {
                VoucherDialog dialog = new VoucherDialog(LoginActivity.this, R.style.CustomDialog);
                dialog.show();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                });
            }
        };
        hideKeyBoard();
        ServiceClient.getInstance().addAllVoucher(new ProgressSubscriber(mListener, LoginActivity.this),
                HOTRSharePreference.getInstance(getApplicationContext()).getUserID(), Constants.NEW_USER_VOUCHER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode ==RESULT_OK) {
            loginSuccess();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GlobalBus.getBus().unregister(this);
    }

    private void hideKeyBoard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

    }

}
