package com.us.hotr.ui.fragment.massage;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.us.hotr.Constants;
import com.us.hotr.R;
import com.us.hotr.customview.MyBaseAdapter;
import com.us.hotr.webservice.response.GetMassageDetailResponse;
import com.us.hotr.storage.bean.Masseur;
import com.us.hotr.ui.activity.beauty.ListActivity;
import com.us.hotr.ui.activity.massage.MasseurActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mloong on 2017/9/30.
 */

public class MassageSpaFragment extends Fragment {

    private RecyclerView recyclerView;
    private MyAdapter mAdapter;
    private GetMassageDetailResponse massageDetail;

    public static MassageSpaFragment newInstance(GetMassageDetailResponse massageDetail) {
        MassageSpaFragment massageSpaFragment = new MassageSpaFragment();
        Bundle b = new Bundle();
        b.putSerializable(Constants.PARAM_DATA, massageDetail);
        massageSpaFragment.setArguments(b);
        return massageSpaFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        massageDetail = (GetMassageDetailResponse) getArguments().getSerializable(Constants.PARAM_DATA);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);

        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2, GridLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new MyAdapter();
        MyBaseAdapter myBaseAdapter = new MyBaseAdapter(mAdapter);
        myBaseAdapter.setFooterView();
        recyclerView.setAdapter(myBaseAdapter);
    }

    public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        public static final int VIEW_TYPE_IMAGE =100;
        public static final int VIEW_TYPE_MASSEUR = 101;
        public static final int VIEW_TYPE_FOOTER = 102;
        public static final int VIEW_TYPE_HEADER = 103;
        private List<Item> itemList = new ArrayList<>();

        public MyAdapter() {
//            if(massageDetail.get().getProductDetail()!=null)
//                itemList.add(new Item(VIEW_TYPE_WEBVIEW, massageDetail.getProduct().getProductDetail()));
            if(massageDetail.getMassageistList()!=null && massageDetail.getMassageistList().size()>0) {
                itemList.add(new Item(VIEW_TYPE_HEADER, null));
                for(Masseur m:massageDetail.getMassageistList())
                    itemList.add(new Item(VIEW_TYPE_MASSEUR, m));
                if(massageDetail.getTotalMasseurCount()>4)
                    itemList.add(new Item(VIEW_TYPE_FOOTER, null));
            }
        }

        public class MasseurHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvAddress, tvAppointment;
            ImageView ivAvatar, ivLike;

            public MasseurHolder(View view) {
                super(view);
                tvName = (TextView) view.findViewById(R.id.tv_name);
                tvAddress = (TextView) view.findViewById(R.id.tv_address);
                tvAppointment = (TextView) view.findViewById(R.id.tv_appointment);
                ivAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
                ivLike = (ImageView) view.findViewById(R.id.iv_like);
            }
        }

        public class ImageHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            public ImageHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.imageview);
            }
        }

        public class FooterHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public FooterHolder(View view) {
                super(view);
                textView = (TextView) view.findViewById(R.id.tv_title);
            }
        }

        public class HeaderHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public HeaderHolder(View view) {
                super(view);
                textView = (TextView) view.findViewById(R.id.tv_title);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case VIEW_TYPE_MASSEUR:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_masseur, parent, false);
                    return new MasseurHolder(view);
                case VIEW_TYPE_IMAGE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_hospital_image, parent, false);
                    return new ImageHolder(view);
                case VIEW_TYPE_FOOTER:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_see_more, parent, false);
                    return new FooterHolder(view);
                case VIEW_TYPE_HEADER:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_title_text, parent, false);
                    return new HeaderHolder(view);
                default:
                    return null;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_MASSEUR:
                    final Masseur masseur = (Masseur) itemList.get(position).getContent();
                    MasseurHolder masseurHolder = (MasseurHolder) holder;
                    if(position%2==1) {
                        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) masseurHolder.ivAvatar.getLayoutParams();
                        lp.setMargins(12, 0, 6, 0);
                        masseurHolder.ivAvatar.setLayoutParams(lp);
                    }
                    else {
                        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) masseurHolder.ivAvatar.getLayoutParams();
                        lp.setMargins(6, 0, 12, 0);
                        masseurHolder.ivAvatar.setLayoutParams(lp);
                    }
                    Glide.with(getContext()).load(masseur.getMassagist_main_img()).placeholder(R.drawable.holder_masseur).error(R.drawable.holder_masseur).into(masseurHolder.ivAvatar);
                    masseurHolder.tvAddress.setText(masseur.getAddress());
                    masseurHolder.tvAppointment.setText(String.format(getString(R.string.masseur_appointment), masseur.getOrder_num()));
                    masseurHolder.tvName.setText(masseur.getMassagist_name());
                    masseurHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(getActivity(), MasseurActivity.class);
                            Bundle b = new Bundle();
                            b.putInt(Constants.PARAM_ID, masseur.getId());
                            i.putExtras(b);
                            startActivity(i);
                        }
                    });
                    break;
                case VIEW_TYPE_IMAGE:
                    ImageHolder imageHolder = (ImageHolder) holder;
                    imageHolder.imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    });
                    break;
                case VIEW_TYPE_FOOTER:
                    FooterHolder footerHolder = (FooterHolder) holder;
                    footerHolder.textView.setText(getString(R.string.all_masseur));
                    footerHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getActivity(), ListActivity.class);
                            Bundle b = new Bundle();
                            b.putInt(Constants.PARAM_SPA_ID, massageDetail.getMassage().getId());
                            b.putInt(Constants.PARAM_TYPE, Constants.TYPE_MASSEUR);
                            b.putString(Constants.PARAM_TITLE, getString(R.string.masseur_list));
                            i.putExtras(b);
                            startActivity(i);
                        }
                    });
                    break;
                case VIEW_TYPE_HEADER:
                    ((HeaderHolder) holder).textView.setText(getString(R.string.masseur));
                    break;
            }
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) manager);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        int type = getItemViewType(position);
                        switch (type) {
                            case VIEW_TYPE_MASSEUR:
                                return 1;
                            case VIEW_TYPE_FOOTER:
                            case VIEW_TYPE_IMAGE:
                                return 2;
                            default:
                                return 2;
                        }
                    }
                });
            }
        }

        @Override
        public int getItemViewType(int position) {
            return itemList.get(position).getId();
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