package com.us.hotr.ui.fragment.info;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.us.hotr.Constants;
import com.us.hotr.R;
import com.us.hotr.customview.MyBaseAdapter;
import com.us.hotr.storage.HOTRSharePreference;
import com.us.hotr.storage.bean.MassageOrder;
import com.us.hotr.ui.activity.PayOrderActivity;
import com.us.hotr.ui.activity.info.MassageOrderDetailActivity;
import com.us.hotr.ui.activity.massage.MassageActivity;
import com.us.hotr.ui.dialog.CancelOrderDialogFragment;
import com.us.hotr.ui.dialog.TwoButtonDialog;
import com.us.hotr.ui.fragment.BaseLoadingFragment;
import com.us.hotr.util.Tools;
import com.us.hotr.webservice.ServiceClient;
import com.us.hotr.webservice.request.CancelOrderRequest;
import com.us.hotr.webservice.response.BaseListResponse;
import com.us.hotr.webservice.response.GetMassageDetailResponse;
import com.us.hotr.webservice.rxjava.LoadingSubscriber;
import com.us.hotr.webservice.rxjava.ProgressSubscriber;
import com.us.hotr.webservice.rxjava.SilentSubscriber;
import com.us.hotr.webservice.rxjava.SubscriberListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liaobo on 2018/1/26.
 */

public class MassageOrderListFragment extends BaseLoadingFragment {
    public static final int TYPE_ALL =100;
    public static final int TYPE_PAID =101;
    public static final int TYPE_NOT_PAID =102;

    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private MyBaseAdapter myBaseAdapter;
    private ConstraintLayout clEmpty;
    private int totalSize = 0;
    private int currentPage = 1;
    private int type;
    private boolean isLoaded = false;
    private List<MassageOrder> orderList = new ArrayList<>();


    public static MassageOrderListFragment newInstance(int type) {
        MassageOrderListFragment massageOrderListFragment = new MassageOrderListFragment();
        Bundle b = new Bundle();
        b.putInt(Constants.PARAM_TYPE, type);
        massageOrderListFragment.setArguments(b);
        return massageOrderListFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_beauty, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        type = getArguments().getInt(Constants.PARAM_TYPE);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        clEmpty = (ConstraintLayout) view.findViewById(R.id.cl_empty);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        if(getUserVisibleHint() && !isLoaded){
            loadData(Constants.LOAD_PAGE);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed() && !isLoaded) {
            loadData(Constants.LOAD_PAGE);
        }
    }

    @Override
    protected void loadData(final int loadType) {
        Integer c = null;
        if (type == TYPE_ALL)
            c = null;
        if (type == TYPE_PAID)
            c = 1;
        if (type == TYPE_NOT_PAID)
            c = 0;

        SubscriberListener mListener = new SubscriberListener<BaseListResponse<List<MassageOrder>>>() {
            @Override
            public void onNext(BaseListResponse<List<MassageOrder>> result) {
                isLoaded = true;
                updateList(loadType, result);
            }
        };
        if (loadType == Constants.LOAD_MORE) {
            ServiceClient.getInstance().getMassageOrderList(new SilentSubscriber(mListener, getActivity(), refreshLayout),
                    HOTRSharePreference.getInstance(getActivity().getApplicationContext()).getUserID(), c, currentPage, Constants.MAX_PAGE_ITEM);
        } else {
            currentPage = 1;
            if (loadType == Constants.LOAD_PAGE)
                ServiceClient.getInstance().getMassageOrderList(new LoadingSubscriber(mListener, this),
                        HOTRSharePreference.getInstance(getActivity().getApplicationContext()).getUserID(), c, currentPage, Constants.MAX_PAGE_ITEM);
            else if (loadType == Constants.LOAD_DIALOG)
                ServiceClient.getInstance().getMassageOrderList(new ProgressSubscriber(mListener, getContext()),
                        HOTRSharePreference.getInstance(getActivity().getApplicationContext()).getUserID(), c, currentPage, Constants.MAX_PAGE_ITEM);
            else if (loadType == Constants.LOAD_PULL_REFRESH)
                ServiceClient.getInstance().getMassageOrderList(new SilentSubscriber(mListener, getActivity(), refreshLayout),
                        HOTRSharePreference.getInstance(getActivity().getApplicationContext()).getUserID(), c, currentPage, Constants.MAX_PAGE_ITEM);
        }
    }

    private void updateList(int loadType, BaseListResponse<List<MassageOrder>> result){
        totalSize = result.getTotal();
        if(totalSize == 0){
            clEmpty.setVisibility(View.VISIBLE);
            return;
        }
        if(loadType == Constants.LOAD_MORE){
//            for(Order o:result.getRows()){
//                if(Tools.getOrderTimeInMinSec(o.getOrder_create_time())<3600000)
//                    orderList.add(o);
//            }
            orderList.addAll(result.getRows());
            mAdapter.notifyDataSetChanged();
        }else{
            orderList.clear();
//            for(Order o:result.getRows()){
//                if(Tools.getOrderTimeInMinSec(o.getOrder_create_time())<3600000)
//                    orderList.add(o);
//            }
            orderList = result.getRows();
            if(mAdapter == null)
                mAdapter = new MyAdapter();
            else
                mAdapter.notifyDataSetChanged();
            myBaseAdapter = new MyBaseAdapter(mAdapter);
            mRecyclerView.setAdapter(myBaseAdapter);
        }
        currentPage ++;
        if((mAdapter.getItemCount() >= totalSize && mAdapter.getItemCount() > 0)
                ||totalSize == 0) {
            enableLoadMore(false);
            myBaseAdapter.setFooterView(LayoutInflater.from(getActivity()).inflate(R.layout.footer_general, mRecyclerView, false));
        }
        else
            enableLoadMore(true);
    }

    private void canceltOrder(MassageOrder order, final int position, String reason){
        SubscriberListener mListener = new SubscriberListener<MassageOrder>() {
            @Override
            public void onNext(MassageOrder result) {
                Tools.Toast(getActivity(), getString(R.string.order_canceled));
                orderList.remove(position);
                mAdapter.notifyItemRemoved(position);
                mAdapter.notifyItemRangeChanged(0, mAdapter.getItemCount());
                if(orderList.size() == 0){
                    clEmpty.setVisibility(View.VISIBLE);
                    return;
                }
            }
        };
        CancelOrderRequest request = new CancelOrderRequest();
        request.setOrder_id(order.getId());
        request.setCancle_reason(reason);
        ServiceClient.getInstance().cancelMassageOrder(new ProgressSubscriber(mListener, getContext()),
                HOTRSharePreference.getInstance(getActivity().getApplicationContext()).getUserID(), request);
    }

    private void deleteOrder(MassageOrder order, final int position){
        SubscriberListener mListener = new SubscriberListener<String>() {
            @Override
            public void onNext(String result) {
                Tools.Toast(getActivity(), getString(R.string.order_deleted));
                orderList.remove(position);
                mAdapter.notifyItemRemoved(position);
                mAdapter.notifyItemRangeChanged(0, mAdapter.getItemCount());
                if(orderList.size() == 0){
                    clEmpty.setVisibility(View.VISIBLE);
                    return;
                }
            }
        };
        ServiceClient.getInstance().deleteMassageOrder(new ProgressSubscriber(mListener, getContext()),
                HOTRSharePreference.getInstance(getActivity().getApplicationContext()).getUserID(), order.getId());
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tvPay, tvTime, tvTitle, tvSubTitle, tvPrice,tvCancel, tvBuy, tvMoney;
            ImageView ivAvatar;
            public MyViewHolder(View view){
                super(view);
                tvPay = (TextView) view.findViewById(R.id.tv_pay);
                tvCancel = (TextView) view.findViewById(R.id.tv_cancel);
                tvBuy = (TextView) view.findViewById(R.id.tv_buy);
                tvTime = (TextView) view.findViewById(R.id.tv_time);
                tvTitle = (TextView) view.findViewById(R.id.tv_title);
                tvSubTitle = (TextView) view.findViewById(R.id.tv_sub_title);
                tvPrice = (TextView) view.findViewById(R.id.tv_amount);
                tvMoney = (TextView) view.findViewById(R.id.tv_money);
                ivAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
            }
        }

        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            final MassageOrder order = orderList.get(position);
            holder.tvTitle.setText(order.getProduct_name_usp());
            holder.tvMoney.setText(R.string.total_price1);
            holder.tvSubTitle.setText(String.format(getString(R.string.number), order.getOrder_product_sum()));
            holder.tvPrice.setText(getString(R.string.money) + new DecimalFormat("0.00").format(order.getSum_price()));
            Glide.with(MassageOrderListFragment.this).load(order.getProduct_img()).placeholder(R.drawable.placeholder_post3).error(R.drawable.placeholder_post3).into(holder.ivAvatar);
            switch (order.getOrder_payment_state()){
                case Constants.ORDER_UNPAID:
                    holder.tvPay.setText(R.string.order_wait_pay);
                    holder.tvCancel.setText(R.string.cancel_order);
                    holder.tvBuy.setText(R.string.pay_now);
                    holder.tvTime.setText(Tools.getOrderTime(getContext(), Tools.getOrderTimeInMinSec(order.getOrder_create_time())));
                    holder.tvCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new CancelOrderDialogFragment()
                                    .setOnItemSelectedListener(new CancelOrderDialogFragment.OnItemSelectedListener() {
                                        @Override
                                        public void OnItemSelected(String reason) {
                                            canceltOrder(order, position, reason);
                                        }
                                    }).show(getFragmentManager(), "dialog");
                        }
                    });
                    holder.tvBuy.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getActivity(), PayOrderActivity.class);
                            Bundle b = new Bundle();
                            b.putSerializable(Constants.PARAM_DATA, order);
                            b.putInt(Constants.PARAM_TYPE, Constants.TYPE_MASSAGE);
                            b.putBoolean(PayOrderActivity.PARAM_FROM_ORDED_LIST, true);
                            i.putExtras(b);
                            startActivity(i);
                        }
                    });
                    break;
                case Constants.ORDER_PAID:
                    holder.tvPay.setText(R.string.order_pay_success);
                    holder.tvCancel.setText(R.string.delete_order);
                    holder.tvBuy.setText(R.string.buy_again);
                    if(order.getOrder_payment_time()!=null)
                        holder.tvTime.setText(getString(R.string.pay_time) + order.getOrder_payment_time().replace("-", "/"));
                    else
                        holder.tvTime.setText("");
                    holder.tvCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            TwoButtonDialog.Builder alertDialogBuilder = new TwoButtonDialog.Builder(getContext());
                            alertDialogBuilder.setMessage(getString(R.string.do_you_want_to_delete_order));
                            alertDialogBuilder.setPositiveButton(getString(R.string.yes),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            deleteOrder(order, position);
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
                    });
                    holder.tvBuy.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            buyAgain(order);
                        }
                    });
                    break;
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getActivity(), MassageOrderDetailActivity.class);
                    Bundle b = new Bundle();
                    b.putLong(Constants.PARAM_ID, order.getId());
                    i.putExtras(b);
                    startActivity(i);
                }
            });
        }

        private void buyAgain(final MassageOrder result){
            final SubscriberListener mListener = new SubscriberListener<GetMassageDetailResponse>() {
                @Override
                public void onNext(final GetMassageDetailResponse response) {
                    if(response.getProduct().isMassageVaiable()) {
                        Intent i = new Intent(getActivity(), MassageActivity.class);
                        Bundle b = new Bundle();
                        b.putLong(Constants.PARAM_ID, response.getProduct().getKey());
                        i.putExtras(b);
                        startActivity(i);
                    }else
                        Tools.Toast(getActivity(), getString(R.string.product_not_available));
                }
            };
            ServiceClient.getInstance().getMassageDetail(new ProgressSubscriber(mListener, getActivity()),
                    result.getProduct_id(), HOTRSharePreference.getInstance(getActivity().getApplicationContext()).getUserID());
        }

        @Override
        public int getItemCount() {
            if(orderList == null)
                return 0;
            else
                return orderList.size();
        }
    }
}

