package com.mao.cleverfish;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mingpeidev on 2018/9/17.
 */

public class Recycleview_Adapter extends RecyclerView.Adapter<Recycleview_Adapter.ViewHolder>{

    private List<String> mystringlist=new ArrayList<String>();

    public Recycleview_Adapter(List<String> fishlist) {
        mystringlist = fishlist;
    }

    private Recycleview_Adapter.OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(Recycleview_Adapter.OnItemClickListener mOnItemClickListener){//按钮点击方法
        this.mOnItemClickListener = mOnItemClickListener;
    }
    public interface OnItemClickListener{//接口
        void onItemClick(View view, int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView item;

        public ViewHolder(View itemView) {
            super(itemView);
            item=(TextView)itemView.findViewById(R.id.popwindow_item);
        }
    }

    @Override
    public Recycleview_Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fishpopwindow_item, parent, false);
        Recycleview_Adapter.ViewHolder viewHolder = new Recycleview_Adapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final Recycleview_Adapter.ViewHolder holder, final int position) {


        holder.item.setText(mystringlist.get(position));

        if(mOnItemClickListener != null){ //为ItemView设置监听器
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.itemView,position);
                }
            });
        }

    }

    public void refreshData(List<String> valueList) {
        this.mystringlist = valueList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mystringlist.size();
    }



}
