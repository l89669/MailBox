package com.tripleying.qwq.MailBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GlobalConfig {
    public static boolean lowServer1_12 = false;
    public static boolean lowServer1_11 = false;
    public static boolean lowServer1_9 = false;
    public static boolean lowServer1_8 = false;
    public static boolean enVexView;
    public static boolean lowVexView_2_5;
    public static boolean lowVexView_2_4;
    public static boolean enVault;
    public static boolean enPlayerPoints;
    
    public static boolean fileSQL;
    private static final HashMap<String,String> DISPLAY = new HashMap();
    public static String pluginPrefix;
    public static String normal;
    public static String success;
    public static String warning;
    public static List<String> tips;
    public static String tipsMsg;
    public static String fileCmdPlayer;
    public static int maxItem;
    public static String fileBanLore;
    public static List<String> fileBanId;
    public static String expiredDay;
    public static List<Integer> player_out;
    public static int times_count;
    public static String vaultDisplay;
    public static double vaultMax;
    public static double vaultExpand;
    public static double vaultItem;
    public static String playerPointsDisplay;
    public static int playerPointsMax;
    public static int playerPointsExpand;
    public static int playerPointsItem;
    
    public static void setVexView(boolean vv){
        enVexView = vv;
    }
    
    public static void setLowVexView_2_5(boolean vv){
        lowVexView_2_5 = vv;
    }
    
    public static void setLowVexView_2_4(boolean vv){
        lowVexView_2_4 = vv;
    }
    
    public static boolean setVault(boolean v){
        enVault = v;
        return enVault;
    }
    
    public static boolean setPlayerPoints(boolean pp){
        enPlayerPoints = pp;
        return enPlayerPoints;
    }
    
    public static void setGlobalConfig(
        boolean fileSQL,
        String pluginPrefix,
        String normal,
        String success,
        String warning,
        List<String> tips,
        String tipsMsg,
        String mailDisplay_SYSTEM,
        String mailDisplay_PLAYER,
        String mailDisplay_PERMISSION,
        String mailDisplay_DATE,
        String mailDisplay_TIMES,
        String mailDisplay_CDKEY,
        String mailDisplay_ONLINE,
        String mailDisplay_TEMPLATE,
        String fileCmdPlayer,
        int maxItem,
        String fileBanLore,
        List<String> fileBanId,
        String expiredDay,
        List<Integer> player_out,
        int times_count,
        String vaultDisplay,
        double vaultMax,
        double vaultExpand,
        double vaultItem,
        String playerPointsDisplay,
        int playerPointsMax,
        int playerPointsExpand,
        int playerPointsItem
    ){
        // 是否使用数据库存储附件
        GlobalConfig.fileSQL = fileSQL;
        // 全局
        GlobalConfig.pluginPrefix = pluginPrefix+" : ";// 插件提示信息前缀
        GlobalConfig.normal = normal;// 普通 插件信息颜色
        GlobalConfig.success = success;// 成功 插件信息颜色
        GlobalConfig.warning = warning;// 失败 插件信息颜色
        GlobalConfig.tips = tips;// 新消息提示
        GlobalConfig.tipsMsg = tipsMsg;// 提示消息
        GlobalConfig.DISPLAY.clear();
        GlobalConfig.DISPLAY.put("system", mailDisplay_SYSTEM);
        GlobalConfig.DISPLAY.put("player", mailDisplay_PLAYER);
        GlobalConfig.DISPLAY.put("permission", mailDisplay_PERMISSION);
        GlobalConfig.DISPLAY.put("date", mailDisplay_DATE);
        GlobalConfig.DISPLAY.put("times", mailDisplay_TIMES);
        GlobalConfig.DISPLAY.put("cdkey", mailDisplay_CDKEY);
        GlobalConfig.DISPLAY.put("online", mailDisplay_ONLINE);
        GlobalConfig.DISPLAY.put("template", mailDisplay_TEMPLATE);
        // 附件
        GlobalConfig.fileCmdPlayer = fileCmdPlayer;// 领取邮件的玩家变量
        GlobalConfig.fileBanLore = fileBanLore;// Lore中包含此文字的物品禁止作为附件发送
        GlobalConfig.maxItem = maxItem;// 最大发送物品数量
        GlobalConfig.fileBanId = formatMaterial(fileBanId);// ID在此列表中的物品禁止作为附件发送
        // player邮件
        GlobalConfig.expiredDay = expiredDay;// 过期时间
        GlobalConfig.player_out = player_out;// 玩家发件量
        // times邮件
        GlobalConfig.times_count = times_count;//单封邮件最大数量
        // [Vault]设置
        GlobalConfig.vaultDisplay = vaultDisplay;// 显示名称
        GlobalConfig.vaultMax = vaultMax;// 单次邮件发送最大值
        GlobalConfig.vaultExpand = vaultExpand;// 发送邮件时所消耗的金钱
        GlobalConfig.vaultItem = vaultItem;// 每多一个附件物品增加的金钱消耗
        // [PlayerPoints]设置
        GlobalConfig.playerPointsDisplay = playerPointsDisplay;// 显示名称
        GlobalConfig.playerPointsMax = playerPointsMax;// 单次邮件发送最大值
        GlobalConfig.playerPointsExpand = playerPointsExpand;// 发送邮件时所消耗的点券
        GlobalConfig.playerPointsItem = playerPointsItem;// 每多一个附件物品增加的点券消耗
    }
    
    private static List<String> formatMaterial(List<String> idList){
        List<String> material = new ArrayList();
        idList.stream().map((id) -> {
            if(id.length()>9 && id.substring(0, 10).equalsIgnoreCase("minecraft:")) id = id.substring(10);
            return id;
        }).map((id) -> {
            if(id.contains(":")) id = id.replace(":", "_");
            return id;
        }).map((id) -> id.toUpperCase()).forEach((id) -> {
            material.add(id);
        });
        return material;
    }
    
    public static String getTypeName(String type) {
        return GlobalConfig.DISPLAY.get(type);
    }
}