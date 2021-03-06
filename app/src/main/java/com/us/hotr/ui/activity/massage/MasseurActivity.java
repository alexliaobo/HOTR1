package com.us.hotr.ui.activity.massage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import com.tencent.stat.StatService;
import com.us.hotr.Constants;
import com.us.hotr.R;
import com.us.hotr.customview.FlowLayout;
import com.us.hotr.customview.HorizontalImageAdapter;
import com.us.hotr.customview.ImageBannerCrop;
import com.us.hotr.customview.MyBaseAdapter;
import com.us.hotr.receiver.Share;
import com.us.hotr.storage.HOTRSharePreference;
import com.us.hotr.storage.bean.Massage;
import com.us.hotr.storage.bean.MasseurTag;
import com.us.hotr.storage.bean.Post;
import com.us.hotr.storage.bean.Spa;
import com.us.hotr.storage.bean.Type;
import com.us.hotr.ui.activity.BaseLoadingActivity;
import com.us.hotr.ui.activity.MainActivity;
import com.us.hotr.ui.activity.PayNumberActivity;
import com.us.hotr.ui.activity.beauty.ListActivity;
import com.us.hotr.ui.activity.info.LoginActivity;
import com.us.hotr.ui.dialog.ShareDialogFragment;
import com.us.hotr.ui.view.CommentView;
import com.us.hotr.ui.view.MassageView;
import com.us.hotr.ui.view.PostView;
import com.us.hotr.ui.view.SpaBigView;
import com.us.hotr.util.Tools;
import com.us.hotr.webservice.ServiceClient;
import com.us.hotr.webservice.response.GetMasseurDetailResponse;
import com.us.hotr.webservice.rxjava.LoadingSubscriber;
import com.us.hotr.webservice.rxjava.ProgressSubscriber;
import com.us.hotr.webservice.rxjava.SilentSubscriber;
import com.us.hotr.webservice.rxjava.SubscriberListener;
import com.us.hotr.webservice.rxjava.SubscriberWithReloadListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by Mloong on 2017/9/29.
 */

public class MasseurActivity extends BaseLoadingActivity {

    private RecyclerView mRecyclerView;
    private MasseurAdapter mAdapter;
    private ImageView ivFav, ivBackHome;
    private TextView tvPurchase;
    private float offset = 0;

    private long mMasseurId;
    private boolean isCollected = false;
    private Spa spa;
    private Massage selectedMassage;
    private long selectedMassageId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMasseurId = getIntent().getExtras().getLong(Constants.PARAM_ID);
        selectedMassageId = getIntent().getExtras().getLong(Constants.PARAM_MASSAGE_ID, -1);
        initStaticView();
        setMyTitle(R.string.masseur_detail);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Properties prop = new Properties();
        prop.setProperty("id", mMasseurId+"");
        StatService.trackCustomKVEvent(this, Constants.MTA_ID_MASSEUR_DETAIL_SCREEN, prop);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_masseur;
    }

    private void initStaticView(){
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        ivFav = (ImageView) findViewById(R.id.iv_fav);
        ivBackHome = (ImageView) findViewById(R.id.iv_homepage);
        tvPurchase = (TextView) findViewById(R.id.tv_purchase);

        ivBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MasseurActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        enableLoadMore(false);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                offset = offset + dy;
                if(offset > 300){
                    ivBack.setImageResource(R.mipmap.ic_back);
                    ivShare.setImageResource(R.mipmap.ic_share);
                    findViewById(R.id.v_divider).setAlpha(1);
                    findViewById(R.id.tb_title).setAlpha(1);
                }else{
                    findViewById(R.id.tb_title).setAlpha(offset / 300);
                    ivBack.setImageResource(R.mipmap.ic_back_dark);
                    ivShare.setImageResource(R.mipmap.ic_share_dark);
                    findViewById(R.id.v_divider).setAlpha(offset / 300);
                }
            }
        });

        loadData(Constants.LOAD_PAGE);
    }


    @Override
    protected void loadData(int type) {
        SubscriberListener mListener = new SubscriberListener<GetMasseurDetailResponse>() {
            @Override
            public void onNext(final GetMasseurDetailResponse result) {
                if(result == null || result.getMassagist() == null){
                    showErrorPage();
                    return;
                }
                isCollected = result.getIs_collected()==1?true:false;
                spa = result.getMassage();
                if(result.getProductList()!=null && result.getProductList().size()>0) {
                    if(selectedMassageId < 0)
                        selectedMassage = result.getProductList().get(0);
                    else{
                        for(Massage massage:result.getProductList()){
                            if(massage.getKey() == selectedMassageId) {
                                selectedMassage = massage;
                                break;
                            }
                        }
                    }
                }
                if(isCollected)
                    ivFav.setImageResource(R.mipmap.ic_fav_text_ed);
                else
                    ivFav.setImageResource(R.mipmap.ic_fav_text);
                ivFav.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isCollected) {
                            SubscriberWithReloadListener mListener = new SubscriberWithReloadListener<String>() {
                                @Override
                                public void onNext(String result) {
                                    isCollected = false;
                                    Tools.Toast(MasseurActivity.this, getString(R.string.remove_fav_item_success));
                                    ivFav.setImageResource(R.mipmap.ic_fav_text);
                                    mAdapter.notifyItemChanged(0);
                                }
                                @Override
                                public void reload() {
                                    loadData(Constants.LOAD_DIALOG);
                                }
                            };
                            ServiceClient.getInstance().removeFavoriteItem(new ProgressSubscriber(mListener, MasseurActivity.this),
                                    HOTRSharePreference.getInstance(MasseurActivity.this.getApplicationContext()).getUserID(),  Arrays.asList(result.getMassagist().getId()), 5);
                        }else{
                            SubscriberListener mListener = new SubscriberListener<String>() {
                                @Override
                                public void onNext(String result) {
                                    Tools.Toast(MasseurActivity.this, getString(R.string.fav_item_success));
                                    isCollected = true;
                                    ivFav.setImageResource(R.mipmap.ic_fav_text_ed);
                                    mAdapter.notifyItemChanged(0);
                                }
                            };
                            Properties prop = new Properties();
                            prop.setProperty("id", result.getMassagist().getId()+"");
                            StatService.trackCustomKVEvent(MasseurActivity.this, Constants.MTA_ID_FAV_MASSEUR, prop);
                            ServiceClient.getInstance().favoriteItem(new ProgressSubscriber(mListener, MasseurActivity.this),
                                    HOTRSharePreference.getInstance(MasseurActivity.this.getApplicationContext()).getUserID(), result.getMassagist().getId(), 5);
                        }
                    }
                });

                ivShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Share share = new Share();
                        share.setDescription(getString(R.string.share_masseur));
                        share.setImageUrl(Tools.getMainPhoto(result.getMassagist().getMassagistPhotos()));
                        share.setTitle(result.getMassagist().getMassagist_name());
                        share.setUrl(Constants.SHARE_URL + "#/massagist?id="+result.getMassagist().getId());
                        share.setSinaContent(getString(R.string.share_masseur));
                        share.setType(Share.TYPE_NORMAL);
                        ShareDialogFragment.newInstance(share).show(getSupportFragmentManager(), "dialog");
                    }
                });

                mAdapter = new MasseurAdapter(MasseurActivity.this, result);
                MyBaseAdapter myBaseAdapter = new MyBaseAdapter(mAdapter);
                myBaseAdapter.setFooterView();
                mRecyclerView.setAdapter(myBaseAdapter);
            }
        };
        if(type == Constants.LOAD_PAGE)
            ServiceClient.getInstance().getMasseurDetail(new LoadingSubscriber(mListener, this),
                    mMasseurId, HOTRSharePreference.getInstance(getApplicationContext()).getUserID());
        else if (type == Constants.LOAD_PULL_REFRESH)
            ServiceClient.getInstance().getMasseurDetail(new SilentSubscriber(mListener, this, refreshLayout),
                    mMasseurId, HOTRSharePreference.getInstance(getApplicationContext()).getUserID());
    }

    public void purchase(){
        if(Tools.isUserLogin(getApplicationContext())){
            purchaseCheckCount();
        }else{
            LoginActivity.setLoginListener(new LoginActivity.LoginListener() {
                @Override
                public void onLoginSuccess() {
                    purchaseCheckCount();
                }

                @Override
                public void onLoginCancel() {

                }
            });
            startActivityForResult(new Intent(MasseurActivity.this, LoginActivity.class), 0);
        }

    }

    public void purchaseCheckCount(){
        if(selectedMassage.getProductType() == Constants.PROMOTION_PRODUCT){
            SubscriberListener mListener = new SubscriberListener<Integer>() {
                @Override
                public void onNext(Integer result) {
                    if(result == 0){
                        Tools.Toast(MasseurActivity.this, getString(R.string.product_sold_out));
                        tvPurchase.setText(R.string.sold_out);
                        tvPurchase.setBackgroundResource(R.color.bg_button_grey);
                        tvPurchase.setOnClickListener(null);
                    }else {
                        Intent i = new Intent(MasseurActivity.this, PayNumberActivity.class);
                        Bundle b = new Bundle();
                        b.putInt(Constants.PARAM_TYPE, Constants.TYPE_MASSAGE);
                        b.putLong(Constants.PARAM_ID, mMasseurId);
                        b.putSerializable(Constants.PARAM_DATA, selectedMassage);
                        b.putSerializable(Constants.PARAM_SPA_ID, spa);
                        i.putExtras(b);
                        startActivity(i);
                    }
                }
            };
            ServiceClient.getInstance().checkOrderCount(new ProgressSubscriber(mListener, this),
                    selectedMassage.getKey(), 1);
        }else{
            Intent i = new Intent(MasseurActivity.this, PayNumberActivity.class);
            Bundle b = new Bundle();
            b.putInt(Constants.PARAM_TYPE, Constants.TYPE_MASSAGE);
            b.putSerializable(Constants.PARAM_DATA, selectedMassage);
            b.putLong(Constants.PARAM_ID, mMasseurId);
            b.putSerializable(Constants.PARAM_SPA_ID, spa);
            i.putExtras(b);
            startActivity(i);
        }
    }

    public class MasseurAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private GetMasseurDetailResponse masseurDetail;
        private List<Item> itemList = new ArrayList<>();
        private Context mContext;
        private int selectedPosition = 2;
        private long userId;

        private final int TYPE_HEADER = 101;
        private final int TYPE_MASSAGE_HEADER = 102;
        private final int TYPE_MASSAGE = 103;
        private final int TYPE_SPA = 104;
        private final int TYPE_INTERVIEW= 105;
        private final int TYPE_POST_HEADER = 106;
        private final int TYPE_POST = 107;
        private final int TYPE_POST_FOOTER = 108;

        public MasseurAdapter(Context mContext, GetMasseurDetailResponse masseurDetail) {
            this.masseurDetail = masseurDetail;
            this.mContext = mContext;
            itemList.add(new Item(TYPE_HEADER));
            if(masseurDetail.getProductList()!=null && masseurDetail.getProductList().size()>0){
                itemList.add(new Item(TYPE_MASSAGE_HEADER));
                for(int i=0;i<masseurDetail.getProductList().size();i++) {
                    itemList.add(new Item(TYPE_MASSAGE, masseurDetail.getProductList().get(i)));
                    if(masseurDetail.getProductList().get(i).getKey() == selectedMassageId)
                        selectedPosition = 2 + i;
                }
            }
            if(masseurDetail.getMassage()!=null)
                itemList.add(new Item(TYPE_SPA, masseurDetail.getMassage()));
            if(masseurDetail.getHotTopicList()!=null && masseurDetail.getHotTopicList().getTotal()>0){
                itemList.add(new Item(TYPE_POST_HEADER));
                for(int i=0;i<(masseurDetail.getHotTopicList().getTotal()>3?3:masseurDetail.getHotTopicList().getTotal());i++)
                    itemList.add(new Item(TYPE_POST, masseurDetail.getHotTopicList().getRows().get(i)));
                if(masseurDetail.getHotTopicList().getTotal()>3) {
                    itemList.add(new Item(TYPE_POST_FOOTER));
                    userId = masseurDetail.getHotTopicList().getRows().get(0).getUser_id();
                }
            }
        }

        public long getSelectedProductId(){
            return masseurDetail.getProductList().get(selectedPosition-2).getKey();
        }


        public class HeaderHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public HeaderHolder(View view) {
                super(view);
                textView = (TextView) view.findViewById(R.id.tv_title);
            }
        }

        public class FooterHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public FooterHolder(View view) {
                super(view);
                textView = (TextView) view.findViewById(R.id.tv_title);
            }
        }

        public class MasseurHeaderHolder extends RecyclerView.ViewHolder {
            ImageView ivAdd, ivMsg;
            TextView tvName, tvHeight, tvExperience, tvAppointment, tvMark1, tvMark2, tvTagTitle, tvIndroduction, tvShowAll, TvIndroductionTitle;
            FlowLayout flSubject, flComment;
            RecyclerView rvPhoto;
            ImageBannerCrop banner;
            ConstraintLayout clSubject;
            CommentView commentView;

            public MasseurHeaderHolder(View view) {
                super(view);
                banner = (ImageBannerCrop) view.findViewById(R.id.banner);
                ivAdd = (ImageView) view.findViewById(R.id.iv_add);
                ivMsg = (ImageView) view.findViewById(R.id.iv_msg);
                tvName = (TextView) view.findViewById(R.id.tv_name);
                tvHeight = (TextView) view.findViewById(R.id.tv_height);
                tvMark1 = (TextView) view.findViewById(R.id.tv_mark_1);
                tvMark2 = (TextView) view.findViewById(R.id.tv_mark_2);
                tvTagTitle = (TextView) view.findViewById(R.id.tv_tag_title);
                tvExperience = (TextView) view.findViewById(R.id.tv_time);
                tvAppointment = (TextView) view.findViewById(R.id.tv_appointment);
                flSubject = (FlowLayout) view.findViewById(R.id.fl_subject);
                rvPhoto = (RecyclerView) view.findViewById(R.id.rv_photo);
                clSubject = (ConstraintLayout) view.findViewById(R.id.cl_subject);
                flComment = (FlowLayout) view.findViewById(R.id.fl_tag);
                commentView = (CommentView) view.findViewById(R.id.commentView);
                tvIndroduction = (TextView) view.findViewById(R.id.tv_introduction);
                TvIndroductionTitle = (TextView) view.findViewById(R.id.tv_intro_title);
                tvShowAll = (TextView) view.findViewById(R.id.tv_expend);
            }
        }

        public class MassageHolder extends RecyclerView.ViewHolder {
            MassageView massageView;
            public MassageHolder(View view) {
                super(view);
                massageView = (MassageView) view;
            }
        }

        public class SpaHolder extends RecyclerView.ViewHolder {
            SpaBigView spaBigView;
            public SpaHolder(View view) {
                super(view);
                spaBigView = (SpaBigView) view;
            }
        }

        public class PostHolder extends RecyclerView.ViewHolder {
            PostView postView;

            public PostHolder(View view) {
                super(view);
                postView = (PostView) view;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case TYPE_HEADER:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_masseur_header, parent, false);
                    return new MasseurHeaderHolder(view);
                case TYPE_MASSAGE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_massage, parent, false);
                    return new MassageHolder(view);
                case TYPE_MASSAGE_HEADER:
                case TYPE_POST_HEADER:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_title_text, parent, false);
                    return new HeaderHolder(view);
                case TYPE_POST_FOOTER:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_see_more, parent, false);
                    return new FooterHolder(view);
                case TYPE_SPA:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spa_big, parent, false);
                    return new SpaHolder(view);
                case TYPE_POST:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
                    return new PostHolder(view);
                default:
                    return null;
            }
        }

        @Override
        public int getItemViewType(int position) {
            return itemList.get(position).getId();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            switch (holder.getItemViewType()) {
                case TYPE_MASSAGE_HEADER:
                    ((HeaderHolder) holder).textView.setText(getString(R.string.popular_massage));
                    break;
                case TYPE_POST_HEADER:
                    ((HeaderHolder) holder).textView.setText(getString(R.string.post_title));
                    break;
                case TYPE_POST_FOOTER:
                    ((FooterHolder) holder).textView.setText(String.format(getString(R.string.check_post), masseurDetail.getHotTopicList().getTotal()));
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(MasseurActivity.this, ListActivity.class);
                            Bundle b = new Bundle();
                            b.putLong(Constants.PARAM_USER_ID, userId);
                            b.putInt(Constants.PARAM_TYPE, Constants.TYPE_POST);
                            b.putString(Constants.PARAM_TITLE, getString(R.string.post_title));
                            i.putExtras(b);
                            startActivity(i);
                        }
                    });
                    break;
                case TYPE_MASSAGE:
                    final MassageHolder massageHolder = (MassageHolder) holder;
                    final Massage massage = (Massage)itemList.get(position).getContent();
                    massageHolder.massageView.setData(massage, mMasseurId);
                    if(position == selectedPosition) {
                        massageHolder.massageView.setGoButtonResource(R.mipmap.ic_massage_clicked);
                        selectedMassage = massage;
                        if(selectedMassage.getProductType()==Constants.PROMOTION_PRODUCT){
                            if(selectedMassage.getActivityCount()<=0) {
                                tvPurchase.setText(R.string.sold_out);
                                tvPurchase.setBackgroundResource(R.color.bg_button_grey);
                                tvPurchase.setOnClickListener(null);
                            }else{
                                tvPurchase.setText(R.string.buy_now);
                                tvPurchase.setBackgroundResource(R.color.text_black);
                                tvPurchase.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        purchase();
                                    }
                                });
                            }
                        }else {
                            tvPurchase.setText(R.string.buy_now);
                            tvPurchase.setBackgroundResource(R.color.text_black);
                            tvPurchase.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    purchase();
                                }
                            });
                        }
                    }
                    else
                        massageHolder.massageView.setGoButtonResource(R.mipmap.ic_massage_click);
                    massageHolder.massageView.setGoButtonLisenter(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectedPosition = position;
                            notifyDataSetChanged();
                        }
                    });
                    if(position < getItemCount()-1 && !(itemList.get(position+1).getContent() instanceof Massage))
                        massageHolder.massageView.showDivider(false);
                    else
                        massageHolder.massageView.showDivider(true);
                    break;

                case TYPE_HEADER:
                    final MasseurHeaderHolder masseurHeaderHolder = (MasseurHeaderHolder) holder;
                    masseurHeaderHolder.tvName.setText(masseurDetail.getMassagist().getMassagist_name());
                    masseurHeaderHolder.tvHeight.setText(String.format(getString(R.string.height), masseurDetail.getMassagist().getMassagist_height()));
                    masseurHeaderHolder.tvAppointment.setText(String.format(getString(R.string.masseur_appointment), masseurDetail.getSumOrderNum()));
                    masseurHeaderHolder.tvExperience.setText(String.format(getString(R.string.experience), masseurDetail.getJob_time()));
                    if(masseurDetail.getComment_score() == 0)
                        masseurDetail.setComment_score(5);
                    String mark = String.format("%.1f", masseurDetail.getComment_score());
                    masseurHeaderHolder.tvMark1.setText(mark.substring(0,2));
                    masseurHeaderHolder.tvMark2.setText(mark.substring(2,3));
                    masseurHeaderHolder.commentView.setMark(masseurDetail.getComment_score());

                    if(masseurDetail.getTabInfoList()!=null && masseurDetail.getTabInfoList().size()>0) {
                        List<String> subjects = new ArrayList<>();
                        for(MasseurTag t:masseurDetail.getTabInfoList())
                            subjects.add(t.getTab_content() + "(" + t.getTab_num()+")");
                        masseurHeaderHolder.flComment.setFlowLayout(subjects, new FlowLayout.OnItemClickListener() {
                            @Override
                            public void onItemClick(String content, int position) {

                            }
                        });
                    }else {
                        masseurHeaderHolder.flComment.setVisibility(View.GONE);
                        masseurHeaderHolder.tvTagTitle.setVisibility(View.GONE);
                    }

                    if(masseurDetail.getMassagist().getMassagistInfo() !=null && !masseurDetail.getMassagist().getMassagistInfo().isEmpty()) {
                        masseurHeaderHolder.tvIndroduction.setVisibility(View.VISIBLE);
                        final String mData = masseurDetail.getMassagist().getMassagistInfo();
                        if (mData.length() > 50) {
                            final String displayData = mData.substring(0, 50) + "...";
                            masseurHeaderHolder.tvIndroduction.setText(displayData);
                            masseurHeaderHolder.tvShowAll.setTag(false);
                            masseurHeaderHolder.tvShowAll.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (!(boolean) view.getTag()) {
                                        masseurHeaderHolder.tvIndroduction.setText(mData);
                                        view.setTag(true);
                                        masseurHeaderHolder.tvShowAll.setText(R.string.see_part);
                                    } else {
                                        masseurHeaderHolder.tvIndroduction.setText(displayData);
                                        view.setTag(false);
                                        masseurHeaderHolder.tvShowAll.setText(R.string.see_all);
                                    }

                                }
                            });
                        } else {
                            masseurHeaderHolder.tvShowAll.setVisibility(View.GONE);
                            masseurHeaderHolder.tvIndroduction.setText(mData);
                        }
                    }else {
                        masseurHeaderHolder.tvIndroduction.setVisibility(View.GONE);
                        masseurHeaderHolder.tvShowAll.setVisibility(View.GONE);
                        masseurHeaderHolder.TvIndroductionTitle.setVisibility(View.GONE);
                    }

                    if(masseurDetail.getTypeList()!=null && masseurDetail.getTypeList().size()>0) {
                        List<String> subjects = new ArrayList<>();
                        for(Type t:masseurDetail.getTypeList())
                            subjects.add(t.getTypeName() + " " + t.getProduct_num()+getString(R.string.appointment));
                        masseurHeaderHolder.flSubject.setFlowLayout(subjects, new FlowLayout.OnItemClickListener() {
                            @Override
                            public void onItemClick(String content, int position) {

                            }
                        });
                    }else
                        masseurHeaderHolder.clSubject.setVisibility(View.GONE);
                    final List<String> photoes = Tools.mapToList(Tools.gsonStringToMap(masseurDetail.getMassagist().getMassagistPhotos()));
                    List<String> bannerUrls = new ArrayList<>();
                    if(photoes.size()<4)
                        bannerUrls = photoes;
                    else{
                        for(int i=0;i<3;i++)
                            bannerUrls.add(photoes.get(i));
                    }
                    masseurHeaderHolder.rvPhoto.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
                    HorizontalImageAdapter mSAdapter = new HorizontalImageAdapter(mContext, photoes);
                    masseurHeaderHolder.rvPhoto.setAdapter(mSAdapter);

                    masseurHeaderHolder.banner.setRatio(1);
                    masseurHeaderHolder.banner.setPlacehoderResource(R.drawable.placeholder_post_2);
                    if(bannerUrls!=null && bannerUrls.size()>0) {
                        masseurHeaderHolder.banner.setSource(bannerUrls);
//                    masseurHeaderHolder.banner.setBannerItemClickListener(new ImageBanner.BannerClickListener() {
//                        @Override
//                        public void onBannerItemClicked(int position) {
//                            Intent i = new Intent(MasseurActivity.this, ImageViewerActivity.class);
//                            Bundle b = new Bundle();
//                            b.putSerializable(Constants.PARAM_DATA, (Serializable)photoes);
//                            b.putInt(Constants.PARAM_ID, position);
//                            b.putInt(Constants.PARAM_TYPE, Constants.TYPE_MASSAGE);
//                            i.putExtras(b);
//                            startActivity(i);
//                        }
//                    });
                        masseurHeaderHolder.banner.startScroll();
                        if(bannerUrls.size()==1)
                            masseurHeaderHolder.banner.pauseScroll();
                    }
                    if(isCollected)
                        masseurHeaderHolder.ivAdd.setImageResource(R.mipmap.ic_click);
                    else
                        masseurHeaderHolder.ivAdd.setImageResource(R.mipmap.ic_add);
                    masseurHeaderHolder.ivAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(isCollected) {
                                SubscriberWithReloadListener mListener = new SubscriberWithReloadListener<String>() {
                                    @Override
                                    public void onNext(String result) {
                                        isCollected = false;
                                        Tools.Toast(MasseurActivity.this, getString(R.string.remove_fav_item_success));
                                        ivFav.setImageResource(R.mipmap.ic_fav_text);
                                        notifyItemChanged(position);
                                    }
                                    @Override
                                    public void reload() {
                                        loadData(Constants.LOAD_DIALOG);
                                    }
                                };
                                ServiceClient.getInstance().removeFavoriteItem(new ProgressSubscriber(mListener, MasseurActivity.this),
                                        HOTRSharePreference.getInstance(MasseurActivity.this.getApplicationContext()).getUserID(),  Arrays.asList(masseurDetail.getMassagist().getId()), 5);
                            }else{
                                SubscriberListener mListener = new SubscriberListener<String>() {
                                    @Override
                                    public void onNext(String result) {
                                        Tools.Toast(MasseurActivity.this, getString(R.string.fav_item_success));
                                        isCollected = true;
                                        ivFav.setImageResource(R.mipmap.ic_fav_text_ed);
                                        notifyItemChanged(position);
                                    }
                                };
                                ServiceClient.getInstance().favoriteItem(new ProgressSubscriber(mListener, MasseurActivity.this),
                                        HOTRSharePreference.getInstance(MasseurActivity.this.getApplicationContext()).getUserID(), masseurDetail.getMassagist().getId(), 5);
                            }
                        }
                    });
                    break;
                case TYPE_SPA:
                    SpaHolder spaHolder = (SpaHolder) holder;
                    spaHolder.spaBigView.setData(masseurDetail.getMassage());
                    break;
                case TYPE_POST:
                    PostHolder postHolder = (PostHolder) holder;
                    postHolder.postView.setData((Post)itemList.get(position).getContent());
                    postHolder.postView.enableEdit(false);
                    if(itemList.size()>position+1 && itemList.get(position+1).getId()!=TYPE_POST)
                        postHolder.postView.showDivider(false);
                    break;

            }

        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        public class Item{
            private int id;
            private Object content;

            public Item(int id){
                this.id = id;
            }

            public Item(int id, Object content){
                this.id = id;
                this.content = content;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public Object getContent() {
                return content;
            }

            public void setContent(Object content) {
                this.content = content;
            }
        }
    }
}
