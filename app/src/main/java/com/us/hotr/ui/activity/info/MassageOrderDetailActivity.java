package com.us.hotr.ui.activity.info;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.bumptech.glide.Glide;
import com.us.hotr.Constants;
import com.us.hotr.R;
import com.us.hotr.storage.HOTRSharePreference;
import com.us.hotr.storage.bean.MassageOrder;
import com.us.hotr.ui.activity.BaseLoadingActivity;
import com.us.hotr.ui.activity.MapViewActivity;
import com.us.hotr.ui.activity.PayOrderActivity;
import com.us.hotr.ui.activity.massage.MassageActivity;
import com.us.hotr.ui.activity.receipt.ReceiptDetailActivity;
import com.us.hotr.ui.dialog.TwoButtonDialog;
import com.us.hotr.util.PermissionUtil;
import com.us.hotr.util.Tools;
import com.us.hotr.webservice.ServiceClient;
import com.us.hotr.webservice.response.GetMassageDetailResponse;
import com.us.hotr.webservice.rxjava.LoadingSubscriber;
import com.us.hotr.webservice.rxjava.ProgressSubscriber;
import com.us.hotr.webservice.rxjava.SilentSubscriber;
import com.us.hotr.webservice.rxjava.SubscriberListener;

import java.text.DecimalFormat;

/**
 * Created by liaobo on 2018/2/1.
 */

/**
 * Created by Mloong on 2017/9/19.
 */

public class MassageOrderDetailActivity extends BaseLoadingActivity {
    private long orderId;
    private TextView tvTitle, tvSubTitle, tvPrice, tvAddress, tvPlace, tvVoucher, tvAmount, tvPayNow, tvPayNowMoney, tvPayLater,
            tvOrderId, tvPayId, tvPayMethod, tvTime, tvPhone, tvBuyAgain, tvBuyNow;
    private ConstraintLayout clAddress, clPlace;
    private ImageView ivAvatar, ivPhone;
    private String phoneNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyTitle(R.string.order_detail);
        orderId = getIntent().getExtras().getLong(Constants.PARAM_ID);
        initStaticView();
        loadData(Constants.LOAD_PAGE);
    }

    @Override
    protected void loadData(int loadType) {
        SubscriberListener mListener = new SubscriberListener<MassageOrder>() {
            @Override
            public void onNext(MassageOrder result) {
                updateData(result);
            }
        };
        if (loadType == Constants.LOAD_PAGE)
            ServiceClient.getInstance().getMassageOrderDetail(new LoadingSubscriber(mListener, this),
                    HOTRSharePreference.getInstance(getApplicationContext()).getUserID(), orderId);
        else if (loadType == Constants.LOAD_PULL_REFRESH)
            ServiceClient.getInstance().getMassageOrderDetail(new SilentSubscriber(mListener, this, refreshLayout),
                    HOTRSharePreference.getInstance(getApplicationContext()).getUserID(), orderId);
    }

    private void updateData(final MassageOrder result){
        Glide.with(this).load(result.getProduct_img()).placeholder(R.drawable.placeholder_post3).error(R.drawable.placeholder_post3).into(ivAvatar);
        tvTitle.setText(result.getProduct_name_usp());
        tvSubTitle.setText(String.format(getString(R.string.masseur3), result.getMassagist_name()));
        tvPrice.setText(getString(R.string.money) + new DecimalFormat("0.00").format(result.getOnline_price()));
        tvAddress.setText(result.getCity_name()+result.getArea_name()+result.getMassage_address());
        tvPlace.setText(result.getMassage_name());
        tvVoucher.setText(String.format(getString(R.string.deduct_amount), new DecimalFormat("0.00").format(result.getCoupon_price())));
        tvAmount.setText(String.format(getString(R.string.pay_number1), result.getOrder_product_sum()));
        tvPayNowMoney.setText(getString(R.string.money) + new DecimalFormat("0.00").format(result.getSum_price()));
        tvPayNow.setText(R.string.total_price1);
        tvPayLater.setVisibility(View.GONE);
        tvOrderId.setText(String.format(getString(R.string.order_id), result.getOrder_code()));
        if(result.getOrder_payment_state() == 1){
            tvPayId.setVisibility(View.VISIBLE);
            tvPayMethod.setVisibility(View.VISIBLE);
            if(result.getOrder_account_type() == 0){
                tvPayId.setText(String.format(getString(R.string.wechat_pay_id), result.getNumerical_order()));
                tvPayMethod.setText(R.string.pay_by_wechat);
            }
            if(result.getOrder_account_type() == 1){
                tvPayId.setText(String.format(getString(R.string.alipay_pay_id), result.getNumerical_order()));
                tvPayMethod.setText(R.string.pay_by_alipay);
            }
            tvTime.setText(String.format(getString(R.string.order_pay_time), result.getOrder_payment_time()));
        }else{
            tvPayId.setVisibility(View.GONE);
            tvPayMethod.setVisibility(View.GONE);
            tvTime.setText(String.format(getString(R.string.order_create_time), result.getOrder_create_time()));
        }
        tvPhone.setText(Constants.SUPPORT_LINE);
        if(result.getOrder_payment_state() == 1){
            tvBuyAgain.setVisibility(View.VISIBLE);
            tvBuyNow.setVisibility(View.GONE);
            tvBuyNow.setText(R.string.go_to_coupon);
            tvBuyAgain.setText(R.string.buy_again);
            tvBuyAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buyAgain(result);
                }
            });
            tvBuyNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(MassageOrderDetailActivity.this, ReceiptDetailActivity.class);
                    Bundle b = new Bundle();
                    b.putLong(Constants.PARAM_ID, result.getId());
                    b.putInt(Constants.PARAM_TYPE, ReceiptDetailActivity.TYPE_MASSAGE_ORDER);
                    i.putExtras(b);
                    startActivity(i);
                }
            });
            ivPhone.setVisibility(View.VISIBLE);
            ivPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    phoneNumber = result.getContact_mobile();
                    if (PermissionUtil.hasCallPermission(MassageOrderDetailActivity.this)) {
                        callPhoneNumber();
                    } else {
                        PermissionUtil.requestCallPermission(MassageOrderDetailActivity.this);
                    }
                }
            });
        }else{
            ivPhone.setVisibility(View.GONE);
            tvBuyAgain.setVisibility(View.GONE);
            tvBuyNow.setText(R.string.pay);
            tvBuyNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(MassageOrderDetailActivity.this, PayOrderActivity.class);
                    Bundle b = new Bundle();
                    b.putSerializable(Constants.PARAM_DATA, result);
                    b.putInt(Constants.PARAM_TYPE, Constants.TYPE_MASSAGE);
                    b.putBoolean(PayOrderActivity.PARAM_FROM_ORDED_LIST, true);
                    i.putExtras(b);
                    startActivity(i);
                }
            });
        }

        clAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MassageOrderDetailActivity.this, MapViewActivity.class);
                Bundle b= new Bundle();
                b.putParcelable(Constants.PARAM_DATA, new LatLng(result.getLat(),result.getLon()));
                b.putString(Constants.PARAM_TITLE, result.getMassage_name());
                b.putString(Constants.PARAM_HOSPITAL_ID, result.getMassage_address());
                i.putExtras(b);
                startActivity(i);
            }
        });
        tvPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = Constants.SUPPORT_LINE;
                if (PermissionUtil.hasCallPermission(MassageOrderDetailActivity.this)) {
                    callPhoneNumber();
                } else {
                    PermissionUtil.requestCallPermission(MassageOrderDetailActivity.this);
                }
            }
        });


    }

    private void buyAgain(final MassageOrder result){
        final SubscriberListener mListener = new SubscriberListener<GetMassageDetailResponse>() {
            @Override
            public void onNext(final GetMassageDetailResponse response) {
                if(response.getProduct().isMassageVaiable()) {
                    Intent i = new Intent(MassageOrderDetailActivity.this, MassageActivity.class);
                    Bundle b = new Bundle();
                    b.putLong(Constants.PARAM_ID, response.getProduct().getKey());
                    i.putExtras(b);
                    startActivity(i);
                }else
                    Tools.Toast(MassageOrderDetailActivity.this, getString(R.string.product_not_available));
            }
        };
        ServiceClient.getInstance().getMassageDetail(new ProgressSubscriber(mListener, this),
                result.getProduct_id(), HOTRSharePreference.getInstance(getApplicationContext()).getUserID());
    }
    @Override
    protected int getLayout() {
        return R.layout.activity_order_detail;
    }

    private void initStaticView(){
        ivAvatar = (ImageView) findViewById(R.id.iv_user_avatar);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvSubTitle = (TextView) findViewById(R.id.tv_sub_title);
        tvPrice = (TextView) findViewById(R.id.tv_price);
        tvAddress = (TextView) findViewById(R.id.tv_address);
        tvPlace = (TextView) findViewById(R.id.tv_place);
        ivPhone = (ImageView) findViewById(R.id.iv_phone);
        tvVoucher = (TextView) findViewById(R.id.tv_voucher);
        tvAmount = (TextView) findViewById(R.id.tv_amount);
        tvPayNow = (TextView) findViewById(R.id.tv_pay_now);
        tvPayNowMoney = (TextView) findViewById(R.id.tv_pay_now_amount);
        tvPayLater = (TextView) findViewById(R.id.tv_pay_later);
        tvOrderId = (TextView) findViewById(R.id.tv_order_id);
        tvPayId = (TextView) findViewById(R.id.tv_pay_id);
        tvPayMethod = (TextView) findViewById(R.id.tv_pay_method);
        tvTime = (TextView) findViewById(R.id.tv_pay_time);
        tvPhone = (TextView) findViewById(R.id.tv_phone);
        tvBuyAgain = (TextView) findViewById(R.id.tv_buy_again);
        tvBuyNow = (TextView) findViewById(R.id.tv_buy_now);
        clAddress = (ConstraintLayout) findViewById(R.id.cl_address);
        clPlace = (ConstraintLayout) findViewById(R.id.cl_place);

        enableLoadMore(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PermissionUtil.PERMISSIONS_REQUEST_CALL) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callPhoneNumber();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void callPhoneNumber()
    {
        TwoButtonDialog.Builder alertDialogBuilder = new TwoButtonDialog.Builder(this);
        alertDialogBuilder.setMessage(phoneNumber);
        alertDialogBuilder.setPositiveButton(getString(R.string.call),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + phoneNumber));
                        startActivity(callIntent);
                        dialog.dismiss();
                    }
                });
        alertDialogBuilder.setNegativeButton(getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialogBuilder.create().show();
    }
}

