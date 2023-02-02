package com.arman.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.arman.common.AjaxResult;
import com.arman.common.WechatBotCommon;
import com.arman.common.util.MyFileUtil;
import com.arman.domain.Person;
import com.arman.domain.ResponseMsg;
import com.arman.domain.Room;
import com.arman.domain.WechatMsg;
import com.arman.service.PersonService;
import com.arman.service.WechatBotService;

/**
 * @author: ming
 * @Date: 2022年8月3日17点00分
 * @Description: <  >
 */
@Service
public class PersonServiceImpl implements PersonService	 {
	@Autowired
	private WechatBotService wechatBotService;
    
    //public static Map<String, String> usernameMap = new HashMap<>(); // 群内昵称
	@Override
	public void getNickNameAndWxidByChatroom() {
		WechatMsg wechatMsg = new WechatMsg();
		wechatBotService.getWeChatRoomMemberList(wechatMsg);
	}

	@Override
	public void saveNickNameAndWxid(String s) {
		ResponseMsg responseMsg = JSONObject.parseObject(s,ResponseMsg.class);
		
		WechatMsg wechatMsg = new WechatMsg();
		
		if(WechatBotCommon.CHATROOM_MEMBER.equals(responseMsg.getType())){
			System.out.println("======开始发送请求获取个人昵称======");
			
			//如果是收到所有群组联系人消息，就要继续发请求获取昵称
			String content = responseMsg.getContent();
			List<Room> list = new ArrayList<Room>();
			list = JSONObject.parseArray(content, Room.class);
			for (Room room : list) {
				if(room==null||!StringUtils.hasLength(room.getRoom_id())
						||!room.getRoom_id().equals(AjaxResult.CHATROOM)
						||room.getMember().size()==0)
					continue;
				for (String wxid: room.getMember()) {
			        wechatMsg.setRoomid(room.getRoom_id());
			        wechatMsg.setWxid(wxid);
			    	wechatBotService.getChatroomMemberNick(wechatMsg);
				}
			}
		}else if(WechatBotCommon.CHATROOM_MEMBER_NICK.equals(responseMsg.getType())){
			//获取昵称直接存起来
			String content = responseMsg.getContent().replaceAll("\\\\", "");
			content="["+content+"]";
			System.out.println("打印content============>>"+content);
			List<Person> list = new ArrayList<Person>();
			list = JSONObject.parseArray(content, Person.class);
			//通过文件名获取文件路径
			String filePath = MyFileUtil.getFilePathByFileName(AjaxResult.PERSON);
			boolean writeFile = MyFileUtil.writeFile(list, filePath);
			if(writeFile){
				System.out.println("已成功保存群成员昵称到本地");
			}
			/*Person person = JSONObject.parseObject(content, Person.class);
			String wxid = person.getWxid();
            String nickname = person.getNick();
            usernameMap.put(wxid, nickname);*/
		}
	}

	@Override
	public void introduceFunctions(String s) {
		
		ResponseMsg responseMsg = JSONObject.parseObject(s,ResponseMsg.class);
		/*String wxid = responseMsg.getSender();
		String roomid = responseMsg.getReceiver();*/
		String roomid = responseMsg.getWxid();
		String wxid = responseMsg.getId1();
		if(!AjaxResult.CHATROOM.equals(roomid)||AjaxResult.MYWXID.equals(wxid))
		return;
		
		String nickName = MyFileUtil.getNickNameByWxid(wxid);
		StringBuffer sb = new StringBuffer("@"+nickName+" 我还不会闲聊，听不懂您在说什么。。。\n");
        
    	sb.append("想要成语接龙，可以跟我说:二狗成语接龙\n");
    	sb.append("想要无限接龙，可以跟我说:二狗无限接龙\n");
    	sb.append("想要退出接龙，可以跟我说:退出接龙\n");
    	sb.append("想要成语提示，可以跟我说:二狗成语提示 月\n");
    	sb.append("想要诚心卜卦，可以跟我说:二狗卜卦\n");
    	sb.append("想要周易解卦，可以跟我说:二狗解卦\n");
    	sb.append("想要知道天气，可以跟我说:二狗北京天气");
        
        
        WechatMsg wechatMsg = new WechatMsg();
		wechatMsg.setWxid(AjaxResult.CHATROOM);
    	wechatMsg.setContent(sb.toString());
		wechatBotService.sendTextMsg(wechatMsg);
		
		
	}


    
}
