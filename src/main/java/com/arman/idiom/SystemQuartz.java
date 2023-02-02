package com.arman.idiom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.arman.common.AjaxResult;
import com.arman.common.util.GetBeanUtil;
import com.arman.domain.WechatMsg;
import com.arman.service.WechatBotService;

@Configuration
@EnableScheduling
public class SystemQuartz {
	@Autowired
	private WechatBotService wechatBotService = GetBeanUtil.getBean(WechatBotService.class);
    
    /**
     * 每日 0 点游戏次数归0
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void clearData() {
        MessageTask.gameTime = 0;
    }
    
    /**
     * 工作日 10 点游戏次数归0
     */
    @Scheduled(cron = "0 0 10 ? * MON-FRI")
    public void dagong1() {
        MessageTask.gameTime = 0;
        String content="做下眼保健操，起身动一动";
    	WechatMsg wechatMsg = new WechatMsg();
		wechatMsg.setWxid(AjaxResult.CHATROOM);
		wechatMsg.setContent(content);
		wechatBotService.sendTextMsg(wechatMsg);
    }
    
    /**
     * 工作日 15点  游戏次数归0
     */
    @Scheduled(cron = "0 0 15 ? * MON-FRI")
    public void yingcha() {
    	String content="三点几嚟 饮茶先啦 做咁多都冇用嘅";
    	WechatMsg wechatMsg = new WechatMsg();
		wechatMsg.setWxid(AjaxResult.CHATROOM);
		wechatMsg.setContent(content);
		wechatBotService.sendTextMsg(wechatMsg);
		
    }
    
    
    
}
