package com.swjtu.gcmformojo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.swjtu.gcmformojo.MyApplication.QQ;
import static com.swjtu.gcmformojo.MyApplication.WEIXIN;

/**
 * 消息列表适配器
 * Created by HeiPi on 2017/2/14.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> implements View.OnClickListener {


    private List<User> objects;
    private OnItemClickListener mOnItemClickListener = null;

    /*
    定义构造器，在Activity创建对象Adapter的时候将数据data和Inflater传入自定义的Adapter中进行处理。
    */
    public UserAdapter(List<User> objects) {
        this.objects = objects;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.current_userlist_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = objects.get(position);

        //获得自定义布局中每一个控件的对象。
        assert user != null;
        switch (user.getUserType()) {
            case QQ:
                holder.itemType.setImageResource(R.mipmap.qq_ico);
                break;
            case WEIXIN:
                holder.itemType.setImageResource(R.mipmap.weixin_ico);
                break;
            default:
                switch (user.getUserId()) {
                    case "0":
                        holder.itemType.setImageResource(R.mipmap.message);
                        break;
                    case "1":
                        holder.itemType.setImageResource(R.mipmap.qq_ico);
                        break;
                    case "2":
                        holder.itemType.setImageResource(R.mipmap.weixin_ico);
                        break;
                    default:
                        holder.itemType.setImageResource(R.mipmap.message);
                }
        }

        holder.itemName.setText(user.getUserName());
        holder.itemTime.setText(user.getUserTime().substring(5));
        holder.itemMessage.setText(user.getUserMessage());

        if(user.getMsgCount().equals("0")){
            holder.itemMsgCount.setVisibility(INVISIBLE);
        }else {
            holder.itemMsgCount.setVisibility(VISIBLE);
            holder.itemMsgCount.setText(user.getMsgCount());
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public int getItemCount() {
        return objects.size();
    }
    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取position
            mOnItemClickListener.onItemClick(v,(int)v.getTag());
        }
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView itemType;
        TextView itemName;
        TextView itemMessage;
        TextView itemTime;
        TextView itemMsgCount;


        public ViewHolder(View itemView) {
            super(itemView);
            itemType = itemView.findViewById(R.id.current_user_item_type);
            itemName = itemView.findViewById(R.id.current_user_item_name);
            itemMessage = itemView.findViewById(R.id.current_user_item_message);
            itemTime = itemView.findViewById(R.id.current_user_item_time);
            itemMsgCount = itemView.findViewById(R.id.current_user_item_msgcount);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view , int position);
        boolean onItemLongClick(View view, int position);
    }

}
