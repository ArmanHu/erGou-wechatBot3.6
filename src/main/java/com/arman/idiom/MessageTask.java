package com.arman.idiom;

public class MessageTask implements Runnable {

	public static int gameTime = 0; // 今日已玩次数
	/*public static int maxGameTime = 5; // 今日已玩次数
    private int status = 0; // 0: 监听信息; 1: 特殊模式下
    private int mode = 0; // 0: 无; 1: 成语接龙模式
    private int wordsCount = 0; // 接龙长度
    private int maxLength = 12; // 接龙长度

    private String currentWords = ""; // 当前成语
    private Set<String> historyWords = new HashSet<>(); // 历史猜过的成语
    private Map<String, Integer> rankMap = new HashMap<>(); // 排行榜
    private static Map<String, String> usernameMap = new HashMap<>(); // 群内昵称
    public static final LinkedBlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();

    @Override
    public void run() {
        getMemberList();
        while (true) {
            try {
                Thread.sleep(100);
                Message message = messageQueue.poll();
                if (message == null) continue;
                botStatus(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void botStatus(Message message) {
        String msg = message.getMsg();
        if (status == 0) { // 正常状态下
            if (msg.contains("二狗")) {
                if (msg.contains("成语接龙")) {
                    getMemberList();
                    if (gameTime >= maxGameTime) {
                        // 今日游戏次数已用完
                        sendMessage("【成语接龙】今天次数已用完。\n" +
                                "群内大大也可回复如下指令购买游戏次数\n" +
                                "“@王二狗 游戏次数”");
                        return;
                    }
                    if (msg.contains("无限")) maxLength = Integer.MAX_VALUE;
                    status = 1; // 1 进入特殊模式下
                    mode = 1; // 1 为成语接龙模式
                    gameTime++;
                    wordsCount = 1;
                    System.out.println("成语接龙模式");
                    // 随机取一个成语, 发开始接龙的消息
                    currentWords = IdiomsUtil.getRandomIdiom();
                    historyWords.add(currentWords);
                    if (msg.contains("无限")) sendMessage("\u203C\uFE0F成语接龙规则：直到无法再继续则游戏结束。每次接龙时间为\u23F11分钟。可以同音字接龙");
                    else sendMessage("\u203C\uFE0F成语接龙规则：直到接满12条或者无法再继续则游戏结束。每次接龙时间为\u23F11分钟。可以同音字接龙");
                    sendMessage("第1条：\uD83D\uDC49".concat(currentWords).concat("\uD83D\uDC48"));
                    // 启动计时器, 倒计时 1 分钟
                    stopTimestamp.getAndSet(System.currentTimeMillis() + 60 * 1000);
                    new Thread(timeDown).start();
                } else if (msg.contains("游戏次数")) {
                    if (gameTime == 5) {
                        gameTime = 2;
                        sendMessage("成语接龙模式充值成功。");
                    }
                } else if (msg.contains("开启自动挡")) {
                    System.out.println(message.getUserId());
                    if ("L-kaxy".equals(message.getUserId())) {
                        status = 1; // 1 进入特殊模式下
                        mode = 2; // 2 为成语接龙自动接龙模式
                        System.out.println("开启自动挡成功");
                    }
                }
            }
        } else if (status == 1) { // 特殊模式下
            if (mode == 1) {
                // 成语接龙模式, 判断消息
                msg = msg.replaceAll("，", ",");
                Boolean checkResult = IdiomsCheckUtil.check(currentWords, msg);
                if (checkResult == null) {
                    // 忽略
                } else if (checkResult) {
                    if (historyWords.contains(msg)) {
                        // 已经猜过这个成语
                        sendMessage(msg.concat("已经猜过了"));
                        return;
                    }
                    // 成功
                    Integer oldCount = rankMap.getOrDefault(message.getUserId(), 0);
                    rankMap.put(message.getUserId(), oldCount + 1);
                    sendMessage("\uD83C\uDF8A 恭喜 @".concat(usernameMap.get(message.getUserId())).concat(" 接龙成功！"));
                    if (wordsCount >= maxLength) {
                        // 游戏结束
                        gameOver();
                    } else {
                        // 继续接龙
                        stopTimestamp.getAndSet(System.currentTimeMillis() + 60 * 1000);
                        lessThen20s = false;
                        wordsCount++;
                        currentWords = msg;
                        historyWords.add(currentWords);
                        sendMessage("第".concat(String.valueOf(wordsCount))
                                .concat("条：\uD83D\uDC49")
                                .concat(currentWords)
                                .concat("\uD83D\uDC48"));
                    }
                } else {
                    // 失败
                    sendMessage("\u261D 【".concat(msg).concat("】这不是成语哦。"));
                }
            } else if (mode == 2) {
                // 自动接龙模式下
                if (msg.contains("关闭自动挡") && "L-kaxy".equals(message.getUserId())) {
                    status = 0;
                    mode = 0;
                    System.out.println("关闭自动挡成功");
                } else {
                    // 自动接龙
                    // 第1条：一心一意(yì)
                    // wxid_2wradd67vjbp22
                    if ("wxid_2wradd67vjbp22".equals(message.getUserId())) {
                        String lastWordPinyin = null;
                        String noIdiom = null;
                        if (msg.matches("^第\\d+条：.{4}\\(.+\\)$")) {
                            String idioms = msg.split("条：", 2)[1].split("\\(")[0];
                            System.out.println("自动接龙：" + idioms);
                            List<String> pinyinList = IdiomsUtil.getPinyin(idioms);
                            if (pinyinList != null) {
                                lastWordPinyin = pinyinList.get(pinyinList.size() - 1);
                                System.out.println("识别最后一个字的拼音：" + lastWordPinyin);
                            }
                        } else if (msg.matches("\\[打脸\\]\\[.{4}\\]不是成语噢×")) {
                            String idioms = msg.split("\\[打脸\\]\\[", 2)[1].split("\\]不是成语噢")[0];
                            System.out.println("自动接龙：" + idioms);
                            noIdiom = idioms;
                            List<String> pinyinList = IdiomsUtil.getPinyin(idioms);
                            if (pinyinList != null) {
                                lastWordPinyin = pinyinList.get(pinyinList.size() - 1);
                                System.out.println("识别最后一个字的拼音：" + lastWordPinyin);
                            }
                        }
                        if (lastWordPinyin != null) {
                            List<String> nextIdioms = IdiomsUtil.getBadIdiomFromPinyin(lastWordPinyin);
                            if (nextIdioms != null) {
                                if (noIdiom != null) {
                                    String finalNoIdiom = noIdiom;
                                    nextIdioms = nextIdioms.stream().filter(idiom -> idiom.equals(finalNoIdiom)).collect(Collectors.toList());
                                }
                                if (nextIdioms.size() >= 1) {
                                    String send = nextIdioms.get(new Random().nextInt(nextIdioms.size()));
                                    sendMessage(send);
                                }
                            }
                        }
                    } else if ("wxid_g4uasfvu37vc21".equals(message.getUserId())) {
                        String lastWordPinyin = null;
                        String noIdiom = null;
                        if (msg.matches("^第\\d+条：\uD83D\uDC49.{4}\uD83D\uDC48$")) {
                            String idioms = msg.split("\uD83D\uDC49", 2)[1].split("\uD83D\uDC48")[0];
                            System.out.println("自动接龙：" + idioms);
                            List<String> pinyinList = IdiomsUtil.getPinyin(idioms);
                            if (pinyinList != null) {
                                lastWordPinyin = pinyinList.get(pinyinList.size() - 1);
                                System.out.println("识别最后一个字的拼音：" + lastWordPinyin);
                            }
                        } else if (msg.matches("☝ 【.{4}】这不是成语哦。")) {
                            String idioms = msg.split("【", 2)[1].split("】")[0];
                            System.out.println("自动接龙：" + idioms);
                            noIdiom = idioms;
                            List<String> pinyinList = IdiomsUtil.getPinyin(idioms);
                            if (pinyinList != null) {
                                lastWordPinyin = pinyinList.get(pinyinList.size() - 1);
                                System.out.println("识别最后一个字的拼音：" + lastWordPinyin);
                            }
                        }
                        if (lastWordPinyin != null) {
                            List<String> nextIdioms = IdiomsUtil.getBadIdiomFromPinyin(lastWordPinyin);
                            if (nextIdioms != null) {
                                if (noIdiom != null) {
                                    String finalNoIdiom = noIdiom;
                                    nextIdioms = nextIdioms.stream().filter(idiom -> idiom.equals(finalNoIdiom)).collect(Collectors.toList());
                                }
                                String send = nextIdioms.get(new Random().nextInt(nextIdioms.size()));
                                sendMessage(send);
                            }
                        }
                    }
                }
            }
        }
    }

    private volatile AtomicLong stopTimestamp = new AtomicLong(-1L);
    private boolean lessThen20s = false;
    private Runnable timeDown = () -> {
        while (true) {
            try {
                Thread.sleep(1000);
                long currentTimeMillis = System.currentTimeMillis();
                if (stopTimestamp.get() < 0) break;
                if (!lessThen20s && stopTimestamp.get() - currentTimeMillis <= 20000) {
                    // 提示游戏剩余时间
                    lessThen20s = true;
                    sendMessage("\u26A1 还剩20秒！");
                }
                if (stopTimestamp.get() - currentTimeMillis <= 0) {
                    // 游戏结束
                    sendMessage("\uD83D\uDC7B时间到！【".concat(currentWords).concat("】，没有人接龙成功。"));
                    gameOver();
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private volatile AtomicLong stopTimestamp = new AtomicLong(-1L);
    private boolean lessThen20s = false;
    @SuppressWarnings("unused")
	private Runnable timeDown = () -> {
        while (true) {
            try {
                Thread.sleep(1000);
                long currentTimeMillis = System.currentTimeMillis();
                if (stopTimestamp.get() < 0) break;
                if (!lessThen20s && stopTimestamp.get() - currentTimeMillis <= 20000) {
                    // 提示游戏剩余时间
                    lessThen20s = true;
                    sendMessage("\u26A1 还剩20秒！");
                }
                if (stopTimestamp.get() - currentTimeMillis <= 0) {
                    // 游戏结束
                    sendMessage("\uD83D\uDC7B时间到！【".concat(currentWords).concat("】，没有人接龙成功。"));
                    gameOver();
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private void gameOver() {
        stopTimestamp.getAndSet(-1L);
        lessThen20s = false;
        status = 0;
        mode = 0;
        wordsCount = 0;
        maxLength = 12;
        currentWords = "";
        historyWords.clear();
        // 游戏结束, 公布排行消息
        StringBuilder sb = new StringBuilder();
        sb.append("游戏结束，下面公布成绩\n");
        if (!rankMap.isEmpty()) {
            List<Map.Entry<String, Integer>> entries = new ArrayList<>(rankMap.entrySet());
            entries.sort(Map.Entry.comparingByValue());
            Collections.reverse(entries);
            for (int i = 0; i < entries.size(); i++) {
                Map.Entry<String, Integer> kv = entries.get(i);
                if (i == 0) sb.append("\uD83E\uDD47 第");
                else if (i == 1) sb.append("\uD83E\uDD48 第");
                else if (i == 2) sb.append("\uD83E\uDD49 第");
                else sb.append("\uD83C\uDF6D 第");
                sb.append(i + 1).append("名：@")
                        .append(usernameMap.get(kv.getKey()))
                        .append("  （接龙").append(kv.getValue())
                        .append("次，+").append(kv.getValue() > 5 ? 100 : kv.getValue() * 20).append("\uD83D\uDCB0）\n");
            }
            sb.append("\n");
        } else {
            sb.append("没有人接龙成功\uD83D\uDE31\uD83D\uDE31\n");
        }
        rankMap.clear();
        if (maxGameTime - gameTime > 0) {
            sb.append("今天还能玩").append(maxGameTime - gameTime).append("次成语接龙噢");
        } else {
            sb.append("今天成语接龙游戏次数已用完噢");
        }
        sendMessage(sb.toString());

        if (maxGameTime - gameTime <= 0) {
            sendMessage("【成语接龙】今天次数已用完。\n" +
                    "群内大大也可回复如下指令购买游戏次数\n" +
                    "“@王二狗 游戏次数”");
        }
    }

    private void sendMessage(String message) {
        SendTask.sendQueue.offer(message);
    }

    private static void getMemberList() {
        try {
            ResponseEntity<String> response = ApiConfig.getEntity().getForEntity("http://127.0.0.1:5555/api/getmembernick?id=" + System.currentTimeMillis() + "&roomid=" + WechatbotApplication.roomId, String.class);
            JsonObject body = JsonParser.parseString(Objects.requireNonNull(response.getBody())).getAsJsonObject();
            JsonArray content = body.getAsJsonArray("content");
            usernameMap.clear();
            for (JsonElement element : content) {
                JsonObject userObj = element.getAsJsonObject();
                String wxid = userObj.get("wxid").getAsString();
                String nickname = userObj.get("nickname").getAsString();
                usernameMap.put(wxid, nickname);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public static void main(String[] args) {
        
    }

	@Override
	public void run() {
		
	}

}
