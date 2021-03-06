package com.us.hotr.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.us.hotr.Constants;
import com.us.hotr.storage.bean.User;
import com.us.hotr.ui.activity.info.ChatActivity;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.NotificationClickEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;

/**
 * Created by liaobo on 2018/3/9.
 */

public class NotificationClickEventReceiver {
    private Context mContext;

    public NotificationClickEventReceiver(Context context) {
        mContext = context;
        //注册接收消息事件
        JMessageClient.registerEventReceiver(this);
    }

    /**
     * 收到消息处理
     * @param notificationClickEvent 通知点击事件
     */
    public void onEvent(NotificationClickEvent notificationClickEvent) {
        if (null == notificationClickEvent) {
            return;
        }
        Message msg = notificationClickEvent.getMessage();
        if (msg != null) {
            Conversation conv = JMessageClient.getSingleConversation(msg.getFromUser().getUserName(), "");
            conv.resetUnreadCount();
            Intent notificationIntent = new Intent(mContext, ChatActivity.class);
            Bundle b = new Bundle();
            User user = new User();
            user.setUserId(Long.parseLong(msg.getFromUser().getUserName().replace("user", "")));
            user.setNickname(msg.getFromUser().getNickname());
            user.setHead_portrait(msg.getFromUser().getAddress());
            b.putSerializable(Constants.PARAM_DATA, user);
            notificationIntent.putExtras(b);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(notificationIntent);
        }
    }
}
