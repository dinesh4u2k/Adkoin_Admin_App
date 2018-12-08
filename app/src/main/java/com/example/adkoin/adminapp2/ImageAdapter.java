package com.example.adkoin.adminapp2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by ramya on 29/9/18.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
        private Context mContext;
        private List<Upload> mUploads;
        private OnItemClickListener mListener;

        public ImageAdapter(Context context, List<Upload> uploads){
            mContext = context;
            mUploads = uploads;

        }
    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.image_item,parent,false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        Upload uploadCurrent  = mUploads.get(position);
        holder.textViewcost.setText(uploadCurrent.getCost());
        holder.textViewtag.setText(uploadCurrent.getTag());
        holder.textViewName.setText(uploadCurrent.getName());
        Picasso.with(mContext)
                .load(uploadCurrent.getImageUrl())
                .fit()
                .centerCrop()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener,MenuItem.OnMenuItemClickListener{
        public TextView textViewtag;
        public TextView textViewcost;
        public TextView textViewName;
        public ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            textViewtag = itemView.findViewById(R.id.text_view_tag);
            textViewcost = itemView.findViewById(R.id.text_view_cost);

            textViewName = itemView.findViewById(R.id.text_view_name);
            imageView = itemView.findViewById(R.id.image_view_upload);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener!=null){
                int position = getAdapterPosition();
                if (position!= RecyclerView.NO_POSITION){
                    mListener.onItemClick(position);
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem delete = menu.add(Menu.NONE,1,1,"Delete");

            delete.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (mListener!=null){
                int position = getAdapterPosition();
                if (position!= RecyclerView.NO_POSITION){
                    switch (item.getItemId()){
                        case 1:
                            mListener.onDeleteClick(position);
                            return true;
                    }


                }
            }
            return false;
        }
    }
    public interface OnItemClickListener{
            void onItemClick(int position);

            void onWhatEverClick(int position);

            void onDeleteClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener){
        mListener= listener;

    }
}
