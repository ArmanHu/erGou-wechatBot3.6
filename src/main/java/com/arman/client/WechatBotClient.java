package com.arman.client;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;

import cn.hutool.http.HttpUtil;

import com.arman.common.AjaxResult;
import com.arman.common.WechatBotCommon;
import com.arman.common.WechatBotConfig;
import com.arman.domain.WechatMsg;
import com.arman.domain.WechatReceiveMsg;
import com.arman.idiom.GameStatus;
import com.arman.service.DivinationService;
import com.arman.service.IdiomSolitaireService;
import com.arman.service.PersonService;
import com.arman.service.WebServiceService;

/**
 * websocket机器人客户端
 *
 * @author: [青衫] 'QSSSYH@QQ.com'
 * @Date: 2021-03-16 18:20
 * @Description: < 描述 >
 */
public class WechatBotClient extends WebSocketClient implements WechatBotCommon {

	/**
	 * 注入二狗成语接龙service
	 */
	@Autowired
    private IdiomSolitaireService idiomSolitaireService;
	/**
	 * 注入二狗卜卦service
	 */
	@Autowired
	private  DivinationService divinationService;
	
	@Autowired
	private PersonService personService;

	@Autowired
	private WebServiceService webServiceService;
    /**
     * 描述: 构造方法创建 WechatBotClient对象
     *
     * @param url WebSocket链接地址
     * @return
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-26
     */
    public WechatBotClient(String url) throws URISyntaxException {
        super(new URI(url));
    }

    /**
     * 描述: 在websocket连接开启时调用
     *
     * @param serverHandshake
     * @return void
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-16
     */
    @Override
    public void onOpen(ServerHandshake serverHandshake) {
    	//发送 5010类型请求，获取当前用户所有群组信息
    	personService.getNickNameAndWxidByChatroom();
        System.err.println("已发送尝试连接到微信客户端请求");
    }

    /**
     * 描述: 方法在接收到消息时调用
     *
     * @param msg
     * @return void
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-16
     */
    @Override
    public void onMessage(String s) {
        // 由于我的机器人是放在某个小服务器上的, 就将接收数据后的处理交给了另外一个服务器(看群里好多群友也这么干的)所以我这里就加了这几行代码,这根据自己的想法进行自定义

        // 这里也可以不进行转换 直接将微信中接收到的消息交给服务端, 提高效率,但是浪费在网络通信上的资源相对来说就会变多(根据自己需求自信来写没什么特别的)
        System.out.println("微信中收到了消息:" + s);

        personService.saveNickNameAndWxid(s);
        //判断是否为接龙状态如果是，直接走程序
        if (GameStatus.isRunning(AjaxResult.CHATROOM)) {
            idiomSolitaireService.idiomSolitaire(s);
            return;
        }
        
        //成语接龙
        if(s.contains("二狗")&&s.contains("接龙"))
        	//出题，并把状态置为接龙状态
        	idiomSolitaireService.idiomSolitaireFirst(s);
        //成语提示
        else if(s.contains("二狗成语提示"))
        	idiomSolitaireService.idiomSolitaireRemind(s);
        //成语接龙：接别人的龙
        else if(s.contains("👉")&&s.contains("👈"))
        	idiomSolitaireService.proceedYourGame(s);
        //成语接龙：接别人的龙
        else if(s.contains("条：")&&s.contains("("))
        	idiomSolitaireService.proceedYourGame(s);
        //卜卦
        else  if(s.contains("二狗卜卦"))
        	divinationService.practiseDivination(s);
        //解卦
        else if(s.contains("二狗解卦"))
            divinationService.explainDivination(s);
        //天气
        else if(s.contains("二狗")&&s.contains("天气"))
        	webServiceService.getWeatherByCityName(s);
        else if(s.contains("二狗")&&s.contains("游戏次数"))
        	idiomSolitaireService.updateGameTime(s);
        else if(s.contains("二狗二狗"))
        	personService.getNickNameAndWxidByChatroom();
        else if(s.contains("二狗"))
        	personService.introduceFunctions(s);
        
        // 是否开启远程处理消息功能
        if (WechatBotConfig.wechatMsgServerIsOpen) {
            // 不等于心跳包
            WechatReceiveMsg wechatReceiveMsg = JSONObject.parseObject(s, WechatReceiveMsg.class);
            if (!WechatBotCommon.HEART_BEAT.equals(wechatReceiveMsg.getType())) {
                HttpUtil.post(WechatBotConfig.wechatMsgServerUrl, s);
            }
        }
    }

    /**
     * 描述: 方法在连接断开时调用
     *
     * @param i
     * @param s
     * @param b
     * @return void
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-16
     */
    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("已断开连接... ");
    }

    /**
     * 描述: 方法在连接出错时调用
     *
     * @param e
     * @return void
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-16
     */
    @Override
    public void onError(Exception e) {
        System.err.println("通信连接出现异常:" + e.getMessage());
    }

    /**
     * 描述: 发送消息工具 (其实就是把几行常用代码提取出来 )
     *
     * @param wechatMsg 消息体
     * @return void
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-18
     */
    public void sendMsgUtil(WechatMsg wechatMsg) {
        if (!StringUtils.hasText(wechatMsg.getExt())) {
            wechatMsg.setExt(NULL_MSG);
        }
        if (!StringUtils.hasText(wechatMsg.getNickname())) {
            wechatMsg.setNickname(NULL_MSG);
        }
        if (!StringUtils.hasText(wechatMsg.getRoomid())) {
            wechatMsg.setRoomid(NULL_MSG);
        }
        if (!StringUtils.hasText(wechatMsg.getContent())) {
            wechatMsg.setContent(NULL_MSG);
        }
        if (!StringUtils.hasText(wechatMsg.getWxid())) {
            wechatMsg.setWxid(NULL_MSG);
        }
        // 消息Id
        wechatMsg.setId(String.valueOf(System.currentTimeMillis()));
        // 发送消息
        String string = JSONObject.toJSONString(wechatMsg);
        System.err.println(":" + string);
        send(JSONObject.toJSONString(wechatMsg));
    }
}