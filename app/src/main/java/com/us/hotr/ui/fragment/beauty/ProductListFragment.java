package com.us.hotr.ui.fragment.beauty;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.us.hotr.Constants;
import com.us.hotr.R;
import com.us.hotr.customview.ItemSelectedListener;
import com.us.hotr.customview.MyBaseAdapter;
import com.us.hotr.eventbus.Events;
import com.us.hotr.eventbus.GlobalBus;
import com.us.hotr.storage.HOTRSharePreference;
import com.us.hotr.storage.bean.Product;
import com.us.hotr.ui.activity.BaseLoadingActivity;
import com.us.hotr.ui.fragment.BaseLoadingFragment;
import com.us.hotr.ui.view.ProductView;
import com.us.hotr.util.Tools;
import com.us.hotr.webservice.ServiceClient;
import com.us.hotr.webservice.response.BaseListResponse;
import com.us.hotr.webservice.rxjava.LoadingSubscriber;
import com.us.hotr.webservice.rxjava.ProgressSubscriber;
import com.us.hotr.webservice.rxjava.SilentSubscriber;
import com.us.hotr.webservice.rxjava.SubscriberListener;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mloong on 2017/9/20.
 */

public class ProductListFragment extends BaseLoadingFragment {
    private static final String PARAM_CITY = "PARAM_CITY";
    private static final String PARAM_HOSPITAL = "PARAM_HOSPITAL";
    private static final String PARAM_SUBJECT = "PARAM_SUBJECT";
    private static final String PARAM_TYPE_ID = "PARAM_TYPE_ID";
    private static final String PARAM_DOCTOR = "PARAM_DOCTOR";
    private static final String PARAM_PRODUCT_ID = "PARAM_PRODUCT_ID";

    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private MyBaseAdapter myBaseAdapter;
    private int totalSize = 0;
    private int currentPage = 1;
    private int type;
    private boolean enableRefresh;
    private Long productTypeId = null;
    private Long hospitalId = null;
    private Long doctorId = null;

    public static ProductListFragment newInstance(String keyword, boolean enableRefresh, int type, long typeId, long productTypeId, long subjectId, long cityId, long hospitalId, long doctorId) {
        ProductListFragment productListFragment = new ProductListFragment();
        Bundle b = new Bundle();
        b.putBoolean(Constants.PARAM_ENABLE_REFRESH, enableRefresh);
        b.putString(Constants.PARAM_KEYWORD, keyword);
        b.putLong(PARAM_TYPE_ID, typeId);
        b.putLong(PARAM_SUBJECT, subjectId);
        b.putLong(PARAM_CITY, cityId);
        b.putLong(PARAM_HOSPITAL, hospitalId);
        b.putLong(PARAM_DOCTOR, doctorId);
        b.putInt(Constants.PARAM_TYPE, type);
        b.putLong(PARAM_PRODUCT_ID, productTypeId);
        productListFragment.setArguments(b);
        return productListFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_refresh_recyclerview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        enableRefresh = getArguments().getBoolean(Constants.PARAM_ENABLE_REFRESH);
        keyword = getArguments().getString(Constants.PARAM_KEYWORD);
        subjectId = getArguments().getLong(PARAM_SUBJECT);
        typeId = getArguments().getLong(PARAM_TYPE_ID);
        type = getArguments().getInt(Constants.PARAM_TYPE);
        productTypeId = getArguments().getLong(PARAM_PRODUCT_ID);

        if(subjectId<=0)
            subjectId = null;
        if(typeId<=0)
            typeId = null;
        if(productTypeId<=0)
            productTypeId = null;
        cityCode = getArguments().getLong(PARAM_CITY);
        if(cityCode<=0 || (keyword != null && !keyword.isEmpty()))
            cityCode = null;
        hospitalId = getArguments().getLong(PARAM_HOSPITAL);
        if(hospitalId<=0)
            hospitalId = null;
        doctorId = getArguments().getLong(PARAM_DOCTOR);
        if(doctorId<=0)
            doctorId = null;
        if(hospitalId!=null || doctorId!=null){
            type = 5;
            cityCode = null;
        }
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        enablePullDownRefresh(enableRefresh);
        loadData(Constants.LOAD_PAGE);
    }

    @Override
    protected void loadData(final int loadType){
        SubscriberListener mListener= new SubscriberListener<BaseListResponse<List<Product>>>() {
            @Override
            public void onNext(BaseListResponse<List<Product>> result) {
                updateList(loadType, result);
            }
        };
        if(type == Constants.TYPE_FAVORITE){
            if (loadType == Constants.LOAD_PAGE)
                ServiceClient.getInstance().getCollectionProduct(new LoadingSubscriber(mListener, this),
                        HOTRSharePreference.getInstance(getActivity().getApplicationContext()).getUserID());
            else if (loadType == Constants.LOAD_PULL_REFRESH)
                ServiceClient.getInstance().getCollectionProduct(new SilentSubscriber(mListener, getActivity(), refreshLayout),
                        HOTRSharePreference.getInstance(getActivity().getApplicationContext()).getUserID());
        }else if (type == Constants.TYPE_MY_PRODUCT) {
            mListener= new SubscriberListener<List<Product>>() {
                @Override
                public void onNext(List<Product> result) {
                    if(mAdapter == null)
                        mAdapter = new MyAdapter(result);
                    else
                        mAdapter.setItems(result);
                    myBaseAdapter = new MyBaseAdapter(mAdapter);
                    mRecyclerView.setAdapter(myBaseAdapter);
                }
            };
            if (loadType == Constants.LOAD_PAGE)
                ServiceClient.getInstance().getPurchasedProduct(new LoadingSubscriber(mListener, this),
                        HOTRSharePreference.getInstance(getActivity().getApplicationContext()).getUserID());
            else if (loadType == Constants.LOAD_PULL_REFRESH)
                ServiceClient.getInstance().getPurchasedProduct(new SilentSubscriber(mListener, getActivity(), refreshLayout),
                        HOTRSharePreference.getInstance(getActivity().getApplicationContext()).getUserID());
        }else{
            if (loadType == Constants.LOAD_MORE) {
                ServiceClient.getInstance().getProductList(new SilentSubscriber(mListener, getActivity(), refreshLayout),
                        keyword, typeId, productTypeId, hospitalId, doctorId, subjectId, cityCode, null, null, Constants.MAX_PAGE_ITEM, currentPage);
            } else {
                currentPage = 1;
                if (loadType == Constants.LOAD_PAGE) {
                    if (getActivity() instanceof BaseLoadingActivity)
                        ServiceClient.getInstance().getProductList(new LoadingSubscriber(mListener, (BaseLoadingActivity) getActivity()),
                                keyword, typeId, productTypeId, hospitalId, doctorId, subjectId, cityCode, null, null, Constants.MAX_PAGE_ITEM, currentPage);
                    else
                        ServiceClient.getInstance().getProductList(new LoadingSubscriber(mListener, this),
                                keyword, typeId, productTypeId, hospitalId, doctorId, subjectId, cityCode, null, null, Constants.MAX_PAGE_ITEM, currentPage);
                } else if (loadType == Constants.LOAD_PULL_REFRESH)
                    ServiceClient.getInstance().getProductList(new SilentSubscriber(mListener, getActivity(), refreshLayout),
                            keyword, typeId, productTypeId, hospitalId, doctorId, subjectId, cityCode, null, null, Constants.MAX_PAGE_ITEM, currentPage);
                else if (loadType == Constants.LOAD_DIALOG)
                    ServiceClient.getInstance().getProductList(new ProgressSubscriber(mListener, getContext()),
                            keyword, typeId, productTypeId, hospitalId, doctorId, subjectId, cityCode, null, null, Constants.MAX_PAGE_ITEM, currentPage);
            }
        }
    }

    private void updateList(int loadType, BaseListResponse<List<Product>> result){
        totalSize = result.getTotal();
        Events.GetSearchCount event = new Events.GetSearchCount(totalSize);
        GlobalBus.getBus().post(event);
        if(loadType == Constants.LOAD_MORE){
            mAdapter.addItems(result.getRows());
        }else{
            if(mAdapter == null)
                mAdapter = new MyAdapter(result.getRows());
            else
                mAdapter.setItems(result.getRows());
            myBaseAdapter = new MyBaseAdapter(mAdapter);
            mRecyclerView.setAdapter(myBaseAdapter);
        }
        currentPage ++;
        if((mAdapter.getItemCount() >= totalSize && mAdapter.getItemCount() > 0)
                ||totalSize == 0) {
            enableLoadMore(false);
            if(totalSize>0)
                myBaseAdapter.setFooterView(LayoutInflater.from(getActivity()).inflate(R.layout.footer_general, mRecyclerView, false));
            else
                myBaseAdapter.setFooterView(LayoutInflater.from(getActivity()).inflate(R.layout.footer_empty, mRecyclerView, false));
        }
        else
            enableLoadMore(true);
    }


    @Subscribe
    public void getMessage(Events.EnableEdit enableEdit) {
        if(enableEdit.getEnableEdit() == Events.EnableEdit.ACTION_DONE) {
            mAdapter.setEnableEdit(false);
            enablePullDownRefresh(true);
        }
        if(enableEdit.getEnableEdit() == Events.EnableEdit.ACTION_EDIT) {
            mAdapter.setEnableEdit(enableRefresh);
            enablePullDownRefresh(false);
        }
        if(enableEdit.getEnableEdit() == Events.EnableEdit.ACTION_DELETE) {
            enablePullDownRefresh(true);
            mAdapter.setEnableEdit(false);
            final int length = mAdapter.checkList.size();
            List<Long> removeIds = new ArrayList<>();
            for (int i = length - 1; i >= 0; i--)
                if(mAdapter.checkList.get(i))
                    removeIds.add(mAdapter.productList.get(i).getProductId());
            if(removeIds.size()>0) {
                SubscriberListener mListener = new SubscriberListener<String>() {
                    @Override
                    public void onNext(String result) {
                        Tools.Toast(getActivity(), getString(R.string.remove_fav_item_success));
                        for (int i = length - 1; i >= 0; i--) {
                            if (mAdapter.checkList.get(i)) {
                                mAdapter.productList.remove(i);
                                mAdapter.checkList.remove(i);
                                mAdapter.notifyItemRemoved(i);
                                mAdapter.notifyItemRangeChanged(0, mAdapter.productList.size());
                            }
                        }
                        if (mAdapter.checkList.size() == 0) {
                            Events.GetSearchCount event = new Events.GetSearchCount(0);
                            GlobalBus.getBus().post(event);
                        }
                    }
                };
                ServiceClient.getInstance().removeFavoriteItem(new ProgressSubscriber(mListener, getActivity()),
                        HOTRSharePreference.getInstance(getActivity().getApplicationContext()).getUserID(), removeIds, 3);
            }
        }
    }

    @Subscribe
    public void getMessage(Events.Refresh refresh) {
        loadData(Constants.LOAD_PULL_REFRESH);

    }

    @Subscribe
    public void getMessage(Events.SubjectSelected subjectSelected) {
        if(subjectSelected.getFtId()<0)
            subjectId = null;
        else
            subjectId = subjectSelected.getFtId();
        if(enableRefresh)
            loadData(Constants.LOAD_DIALOG);
        else
            loadData(Constants.LOAD_PAGE);

    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private List<Product> productList;
        private boolean isEdit = false;
        public List<Boolean> checkList = new ArrayList<>();

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ProductView productView;
            public MyViewHolder(View view) {
                super(view);
                productView = (ProductView) view;
            }
        }

        public void setEnableEdit(boolean enable){
            isEdit = enable;
            notifyDataSetChanged();
        }

        public MyAdapter(List<Product> productList) {
            this.productList = productList;
            for(int i=0;i<productList.size();i++)
                checkList.add(false);
        }

        public void addItems(List<Product> productList){
            this.productList.addAll(productList);
            for(int i=0;i<productList.size();i++)
                checkList.add(false);
            notifyDataSetChanged();
        }

        public void setItems(List<Product> productList) {
            this.productList = productList;
            for(int i=0;i<productList.size();i++)
                checkList.add(false);
            notifyDataSetChanged();
        }

        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final Product product = productList.get(position);
            holder.productView.setData(product);
            if(type ==Constants.TYPE_MY_PRODUCT) {
                holder.productView.enableSelect(true);
                holder.productView.setItemSelectedListener(new ItemSelectedListener() {
                    @Override
                    public void onItemSelected(boolean isSelected) {
                        Intent i = new Intent();
                        Bundle b = new Bundle();
                        b.putSerializable(Constants.PARAM_DATA, product);
                        i.putExtras(b);
                        getActivity().setResult(Activity.RESULT_OK, i);
                        getActivity().finish();
                    }
                });
            }else{
                    holder.productView.enableEdit(isEdit);
                    holder.productView.setItemSelectedListener(new ItemSelectedListener() {
                        @Override
                        public void onItemSelected(boolean isSelected) {
                            checkList.set(position, isSelected);
                        }
                    });
                }
        }

        @Override
        public int getItemCount() {
            if(productList == null)
                return 0;
            return productList.size();
        }
    }
}
