package com.arman.idiom;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;

import com.arman.common.AjaxResult;
import com.arman.common.ThreadPoolConfig;
import com.arman.common.util.ChengyuUtil;
import com.arman.common.util.GetBeanUtil;
import com.arman.common.util.MapSortUtil;
import com.arman.common.util.MyFileUtil;
import com.arman.common.util.RandomTextUtil;
import com.arman.service.IdiomSolitaireService;

public class IdiomGame implements Runnable {

	@Autowired
	private IdiomSolitaireService idiomSolitaireService = GetBeanUtil.getBean(IdiomSolitaireService.class);
    /**
     * 保存当前游戏进度
     */
	public final List<GameNode> GAME_LIST;
	
    public static int gameTime = 0; // 今日已玩次数
    public static int maxGameTime = 5; // 今日已玩次数
	

	private String currentIdiom() {
        return GAME_LIST.get(GAME_LIST.size() - 1).getIdiom();
    }

    /**
     * 群ID
     */
    private final String roomid;

    /**
     * 游戏超时时间
     */
    private final long timeout;

    /**
     * 后台计时任务
     */
    private  Timer taskHalf, taskFull;
    /**
     * 后台计时任务是否跑完了
     */
    private  boolean taskHalfRun, taskFullRun;
    /**
     * 消息来源wxid
     */
    private volatile String wxid;

    /**
     * 消息内容
     */
    private volatile String message;

   
    /**
     * 是否为有限接龙 1-是  0-否
     */
    private  boolean isLimit;

    public static void start(String roomid, boolean isLimit) {
    	
    	new IdiomGame(roomid,isLimit);
    }

    public IdiomGame(String roomid, boolean isLimit) {
        this.GAME_LIST = new ArrayList<GameNode>();
        this.roomid = roomid;
        this.timeout = 60000L;
        this.isLimit = isLimit;
        /*this.taskHalf = new Timer();
        this.taskFull = new Timer();*/
        ThreadPoolConfig.gamePool.submit(this);
    }


	public synchronized void setMessage(String wxid, String message) {
        this.wxid = wxid;
        this.message = message.trim();
        notify();
        System.out.println(Thread.currentThread().getName()+"=======解除等待状态1");
    }
	
    
    private String getFirstIdiom(){
    	String filePath = MyFileUtil.getFilePathByFileName(AjaxResult.CHENGYU);
		String str = RandomTextUtil.getRandomText(filePath);
		String[] split = str.split("=");
    	String firstIdiom = split[0];
    	return firstIdiom;
    }
    
    public void sendGroupMessage(String content) {
    	
    	/*//初始化往群里发消息
		WechatMsg wechatMsg = new WechatMsg();
		wechatMsg.setWxid(roomid);
		wechatMsg.setContent(content);
		wechatBotService.sendTextMsg(wechatMsg);*/
    	SendTask.sendQueue.offer(content);
	}
    
    @Override
    public synchronized void run() {
    	
    	try {
            
    		//本次游戏次数，大于等于当日游戏次数
	        if (gameTime >= maxGameTime) {
                // 今日游戏次数已用完
	        	sendGroupMessage("【成语接龙】今天次数已用完。\n" +
                        "群内大大也可回复如下指令购买游戏次数\n" +
                        "“@王二狗 游戏次数”");
                return;
            }
	        
	        GameStatus.startGame(roomid, this);
	        String firstIdiom = getFirstIdiom();
	        
	        int idiomNo = 1;
	        //定义最大接龙次数 默认12，无限接龙时最大次数9999
	        int maxIdiomNo = 12;
	        String firstMessage="\u203C\uFE0F成语接龙规则：直到接满12条或者无法再继续则游戏结束。每次接龙时间为\u23F11分钟。可以同音字接龙";
	        
	        //如果是无限的
	        if(!isLimit){
	        	maxIdiomNo=9999;
	        	firstMessage="\u203C\uFE0F成语接龙规则：直到无法再继续则游戏结束。每次接龙时间为\u23F11分钟。可以同音字接龙";
	        }
	        
	        gameTime++;
	        
	        sendGroupMessage(firstMessage);
	        GAME_LIST.add(new GameNode( "System", "System", firstIdiom));
	        sendGroupMessage("第"+idiomNo+"条：👉"+firstIdiom+"👈");
	        
	        startCounter();
	        
	        while(idiomNo<=maxIdiomNo) {
	        	
            	System.out.println(Thread.currentThread().getName()+"=======即将进入等待");
                try {
                	wait();
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
	            System.out.println("拿到message了================>"+message);
	            try {
	            	String nickName = MyFileUtil.getNickNameByWxid(wxid);
	            	
		            if(message.contains("退出") && message.contains("接龙")){
		            	sendGroupMessage("成语接龙已被 @"+ nickName +" 退出！");
		            	break;
		            }
		            if(message.contains("二狗成语提示")){
		            	idiomSolitaireService.idiomSolitaireRemind(roomid,wxid,message);
		            	continue;
		            }
		            //获取本次输入的信息第一个字的拼音（可能是多音字）
		            String firstWord = message.substring(0,1);
		    		List<String> pinyinList = ChengyuUtil.getPinyinByWord(firstWord);
		            
		    		//上次成语的最后一个字
		            String lastWord = currentIdiom().substring(currentIdiom().length()-1);
		    		List<String> lastpinyinList = ChengyuUtil.getPinyinByWord(lastWord);
		    		//lastpinyinList 中不在 pinyinList 的元素都会被剔除
		    		lastpinyinList.retainAll(pinyinList);
		    		//讨论的是和成语接龙无关的内容
		    		if(lastpinyinList.size()==0)
		    		continue;
		    		//获取词库中上一个成语，对应的所有接龙词
		    		List<String> chengyuList = ChengyuUtil.getChengyuListByChengyu(currentIdiom());
		    		if(!chengyuList.contains(message)){
		    			//首字拼音对的，但是接龙失败
		    			sendGroupMessage("\u261D【"+message+"】这不是成语哦。");
		    			continue;
		    		}
		    		//判断是不是上次已经接龙过的
		    		if (GAME_LIST.stream().anyMatch(gameNode -> gameNode.getIdiom().equals(message))){
	    				sendGroupMessage("[打脸] 【"+message+"】已经猜过了。");
	    				continue ;
		    		}
		    		idiomNo++;
		    		//接龙成功
		    		sendGroupMessage("\uD83C\uDF8A 恭喜 @"+ nickName +" 接龙成功！");
		    		sendGroupMessage("第"+idiomNo+"条：\uD83D\uDC49"+message+"\uD83D\uDC48");
		    		resetCounter();
		    		GAME_LIST.add(new GameNode(wxid,nickName, message));
		    		
	            }catch (Exception e) {
	    	        e.printStackTrace();
	    	    }
	        }
	        cancelCounter();
	        buildGameResult();
	        GameStatus.endGame(roomid);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
    }

    //结算
	public void buildGameResult(){
    	Map<String,Integer> map = new HashMap<>();
        for(GameNode gameNode : GAME_LIST){
        	String nickName = gameNode.getNickName();
        	//剔除第一次系统出题的次数
        	if("System".equals(nickName))
    		continue;
        	
            //统计重复数的个数
            map.put(nickName, map.get(nickName) == null? 1 : map.get(nickName)+1);
        }
        //对map按照Value大小进行排序。大的在前面
        map=MapSortUtil.sortMapByValues(map);
        StringBuffer sb = map.isEmpty()?
        		new StringBuffer("游戏结束\n"):new StringBuffer("游戏结束，下面公布成绩\n");
        
        int i=0;
        if (!map.isEmpty()) {
	        for (String key : map.keySet()){
	        	Integer value = map.get(key);
	        	//输出格式:key:value
	        	if (i == 0) sb.append("\uD83E\uDD47 第");
	            else if (i == 1) sb.append("\uD83E\uDD48 第");
	            else if (i == 2) sb.append("\uD83E\uDD49 第");
	            else sb.append("\uD83C\uDF6D 第");
	            sb.append(i + 1).append("名：@")
	                    .append(key)
	                    .append("  （接龙").append(value)
	                    .append("次，+").append(value > 5 ? 100 : value * 20).append("\uD83D\uDCB0）\n");
	        	i++;
	        }
	        sb.append("\n");
        } else {
            sb.append("没有人接龙成功\uD83D\uDE31\uD83D\uDE31\n");
        }
       /* List<Map.Entry<String, Integer>> entries = new ArrayList<>(map.entrySet());
        entries.sort(Map.Entry.comparingByValue());
        Collections.reverse(entries);*/
        
        /*sb.append("发送[二狗接龙]可重新开始游戏");*/
        if (maxGameTime - gameTime > 0) {
            sb.append("今天还能玩").append(maxGameTime - gameTime).append("次成语接龙噢");
        } else {
            sb.append("今天成语接龙游戏次数已用完噢");
        }
        sendGroupMessage(sb.toString());
        //new Thread(new SendTask()).start();
    }


    public void startCounter() {
    	try {
    		taskHalfRun=false;
            taskFullRun=false;
    		this.taskHalf = new Timer();
            this.taskFull = new Timer();
    		//调度
    		taskHalf.schedule(new TimerTask(){
				@Override
				public void run() {
					taskHalfRun=true;
					String roomid = AjaxResult.CHATROOM;
					GameStatus.getGame(roomid).sendGroupMessage("\u26A1 还剩30秒！");
				}
    		}, timeout/2);
    		taskFull.schedule(new TimerTask(){
				@Override
				public void run() {
					taskFullRun=true;
			        sendGroupMessage("\uD83D\uDC7B时间到！【"+ currentIdiom() +"】，没有人接龙成功。");
			        cancelCounter();
			        buildGameResult();
			        GameStatus.endGame(roomid);
				}
    		}, timeout);
    	}catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    public void cancelCounter() {
    	try {
    		if(!taskHalfRun)
    		taskHalf.cancel();
    		if(!taskFullRun)
    		taskFull.cancel();
    	}catch (Exception e) {
			e.printStackTrace();
		}
    }

    public void resetCounter() {
    	cancelCounter();
        startCounter();
    }
    
    
}
