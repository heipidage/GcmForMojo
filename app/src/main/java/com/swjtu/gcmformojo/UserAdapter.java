package com.swjtu.gcmformojo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * 消息列表适配器
 * Created by HeiPi on 2017/2/14.
 */
public class UserAdapter extends ArrayAdapter<User> {

    private final int resourceId;
    private List<User> mUserList = new ArrayList<>();
    private CurrentUserListView mCurrentUserListView;

    /*
    定义构造器，在Activity创建对象Adapter的时候将数据data和Inflater传入自定义的Adapter中进行处理。
            */
    public UserAdapter(Context context,int textViewResourceId, List<User> objects, CurrentUserListView listView){
        super(context,textViewResourceId,objects);
        resourceId=textViewResourceId;
        mUserList = objects;
        mCurrentUserListView = listView;
    }



    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView , ViewGroup parent ) {

        User user = getItem(position);
        View viewUser;
        ViewHolder ViewHolder;
        if(convertView == null) {
            viewUser=LayoutInflater.from(getContext()).inflate(resourceId,null);
            ViewHolder = new ViewHolder();
            ViewHolder.itemType=(ImageView) viewUser.findViewById(R.id.current_user_item_type);
            ViewHolder.itemName=(TextView) viewUser.findViewById(R.id.current_user_item_name);
            ViewHolder.itemTime=(TextView) viewUser.findViewById(R.id.current_user_item_time);
            ViewHolder.itemMessage=(TextView) viewUser.findViewById(R.id.current_user_item_message);
            ViewHolder.itemMsgCount=(TextView) viewUser.findViewById(R.id.current_user_item_msgcount);
            ViewHolder.itemDelete = (TextView) viewUser.findViewById(R.id.list_view_delete);
            viewUser.setTag(ViewHolder);

        }else {
            viewUser=convertView;
            ViewHolder = (ViewHolder)viewUser.getTag();
         }

        ViewHolder.itemDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserList.remove(position);
                notifyDataSetChanged();
                mCurrentUserListView.hideDelete();
            }
        });


        //获得自定义布局中每一个控件的对象。
        if(user.getUserType().equals("Mojo-Webqq")) {
            ViewHolder.itemType.setImageResource(R.mipmap.qq_ico);
        }else if(user.getUserType().equals("Mojo-Weixin")){
            ViewHolder.itemType.setImageResource(R.mipmap.weixin_ico);
        }else {
            ViewHolder.itemType.setImageResource(R.mipmap.message);
        }

        ViewHolder.itemName.setText(user.getUserName());
        ViewHolder.itemTime.setText(user.getUserTime().substring(5));
        ViewHolder.itemMessage.setText(user.getUserMessage());

        if(user.getMsgCount().equals("0")){
            ViewHolder.itemMsgCount.setVisibility(INVISIBLE);
        }else {
            ViewHolder.itemMsgCount.setVisibility(VISIBLE);
            ViewHolder.itemMsgCount.setText(user.getMsgCount());
        }

        return viewUser;
    }

    class ViewHolder {
        ImageView itemType;
        TextView itemName;
        TextView itemMessage;
        TextView itemTime;
        TextView itemMsgCount;
        TextView itemDelete;
    }

}
