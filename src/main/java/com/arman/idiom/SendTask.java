package com.arman.idiom;


import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;

import com.arman.common.AjaxResult;
import com.arman.common.util.GetBeanUtil;
import com.arman.domain.WechatMsg;
import com.arman.service.WechatBotService;

public class SendTask implements Runnable {

    public static final LinkedBlockingQueue<String> sendQueue = new LinkedBlockingQueue<>();
    @Autowired
    private WechatBotService wechatBotService = GetBeanUtil.getBean(WechatBotService.class);
    @Override
    public void run() {
    	//sendQueue.offer("启动成功~~~~");
        while (true) {
            try {
                Thread.sleep(1000);
                String message = sendQueue.poll();
                if (message == null) continue;
                sendGroupMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendGroupMessage(String message) {
        try {
            WechatMsg wechatMsg = new WechatMsg();
            wechatMsg.setId(String.valueOf(System.currentTimeMillis()));
            wechatMsg.setType(555);
            wechatMsg.setWxid(AjaxResult.CHATROOM);
            wechatMsg.setRoomid("null");
            wechatMsg.setContent(message);
            wechatMsg.setNickname("null");
            wechatMsg.setExt("null");
            wechatBotService.sendTextMsg(wechatMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
