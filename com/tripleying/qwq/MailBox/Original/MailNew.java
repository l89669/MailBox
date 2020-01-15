package com.tripleying.qwq.MailBox.Original;

import com.tripleying.qwq.MailBox.Mail.*;
import com.tripleying.qwq.MailBox.API.MailBoxAPI;
import com.tripleying.qwq.MailBox.GlobalConfig;
import com.tripleying.qwq.MailBox.MailBox;
import static com.tripleying.qwq.MailBox.Original.MailNew.color;
import static com.tripleying.qwq.MailBox.Original.MailNew.sendable;
import com.tripleying.qwq.MailBox.Utils.DateTime;
import com.tripleying.qwq.MailBox.VexView.MailContentGui;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MailNew {
    public static void New(CommandSender sender){
        if(sender.hasPermission("mailbox.admin.send.player")
            || sender.hasPermission("mailbox.admin.send.system")
            || sender.hasPermission("mailbox.admin.send.permission")
            || sender.hasPermission("mailbox.admin.send.date")
            || sender.hasPermission("mailbox.admin.send.times")
            || sender.hasPermission("mailbox.admin.send.keytimes")
            || sender.hasPermission("mailbox.admin.send.cdkey")
            || sender.hasPermission("mailbox.admin.send.online")
            || sender.hasPermission("mailbox.admin.send.template")){
            create(new TypeSelect(sender), sender);
        }else if(MailBoxAPI.hasPlayerPermission(sender, "mailbox.send.player") || MailBoxAPI.hasPlayerPermission(sender, "mailbox.send.times") || MailBoxAPI.hasPlayerPermission(sender, "mailbox.send.keytimes")){
            int c = 0;
            boolean pl = sendable(sender,"player",null);
            boolean ti = sendable(sender,"times",null);
            boolean kti = sendable(sender,"keytimes",null);
            if(pl) c++;
            if(ti) c++;
            if(kti) c++;
            if(c>1){
                create(new TypeSelect(sender), sender);
            }else if(pl){
                sender.sendMessage(GlobalConfig.normal+GlobalConfig.pluginPrefix+"正在创建"+GlobalConfig.getTypeName("player")+GlobalConfig.normal+"邮件");
                create(new Topic(new PlayerMail(0,sender.getName(),null,null,null,null), sender, false), sender);
            }else if(ti){
                sender.sendMessage(GlobalConfig.normal+GlobalConfig.pluginPrefix+"正在创建"+GlobalConfig.getTypeName("times")+GlobalConfig.normal+"邮件");
                create(new Topic(new TimesMail(0,sender.getName(),null,null,null,0), sender, false), sender);
            }else if(kti){
                sender.sendMessage(GlobalConfig.normal+GlobalConfig.pluginPrefix+"正在创建"+GlobalConfig.getTypeName("keytimes")+GlobalConfig.normal+"邮件");
                create(new Topic(new KeyTimesMail(0,sender.getName(),null,null,null,0,null), sender, false), sender);
            }else{
                sender.sendMessage(GlobalConfig.warning+GlobalConfig.pluginPrefix+" 你没有权限发送邮件");
            }
        }else{
            sender.sendMessage(GlobalConfig.warning+GlobalConfig.pluginPrefix+" 你没有权限发送邮件");
        }
    }
    public static void New(CommandSender sender, BaseMail bm){
        if(bm==null) New(sender);
        else if(bm instanceof MailTemplate){
            create(new TypeSelect(sender, bm), sender);
        }else{
            sender.sendMessage(GlobalConfig.normal+GlobalConfig.pluginPrefix+"正在创建"+GlobalConfig.getTypeName(bm.getType())+GlobalConfig.normal+"邮件");
            if(bm.getSender()==null){
                create(new Sender(bm, sender, true), sender);
            }else{
                if(bm instanceof MailPermission && ((MailPermission)bm).getPermission()==null) create(new Permission(bm, sender, true),sender);
                if(bm instanceof MailPlayer && (((MailPlayer)bm).getRecipient()==null || ((MailPlayer)bm).getRecipient().isEmpty())) create(new Recipient(bm, sender, true),sender);
                if(bm instanceof MailDate && (bm.getDate().equals("0") && ((MailDate)bm).getDeadline().equals("0"))) create(new StartDate(bm, sender, true),sender);
                if(bm instanceof MailTimes && ((MailTimes)bm).getTimes()==0) create(new Times(bm, sender, true),sender);
                else if(bm instanceof MailKeyTimes && ((MailKeyTimes)bm).getKey()==null) create(new TimesKey(bm, sender, true),sender);
                if(bm instanceof MailCdkey) create(new Cdkey(bm, sender, true),sender);
                create(new Preview(bm, sender), sender);
            }
        }
    }
    public static void Preview(CommandSender sender, BaseMail bm){
        sender.sendMessage(GlobalConfig.normal+GlobalConfig.pluginPrefix+"正在创建"+bm.getTypeName()+GlobalConfig.normal+"邮件");
        if(bm.getSender()==null){
            if(sender instanceof Player){
                bm.setSender(((Player)sender).getName());
            }else{
                create(new Sender(bm, sender, true), sender);
                return;
            }
        }
        create(new Preview(bm, sender), sender);
    }
    public static void create(ValidatingPrompt p, CommandSender s){
        ((Conversable)s).acceptConversationInput(OriginalConfig.stopStr);
        Conversation conversation = new ConversationFactory(MailBox.getInstance())
        .withFirstPrompt(p)
        .addConversationAbandonedListener((ConversationAbandonedEvent abandonedEvent) -> {
            if (abandonedEvent.gracefulExit()) {
                abandonedEvent.getContext().getForWhom().sendRawMessage(OriginalConfig.msgStop);
            }
        }).buildConversation((Conversable)s);
        conversation.begin();
    }
    public static String color(String target){
        return target.replace('&', '§');
    }
    public static boolean sendable(CommandSender sender, String type, ConversationContext cc){
        if(sender instanceof Player){
            switch (type) {
                case "player":
                    if(sender.hasPermission("mailbox.admin.send."+type) || MailBoxAPI.hasPlayerPermission(sender, "mailbox.send.player")){
                        Player p = (Player)sender;
                        int out = MailBoxAPI.playerAsSenderAllow(p);
                        int outed = MailBoxAPI.playerAsSender(p);
                        if(outed>=out){
                            if(cc==null){
                                sender.sendMessage(GlobalConfig.warning+GlobalConfig.pluginPrefix+" 你的"+GlobalConfig.getTypeName(type)+"邮件发送数量达到上限");
                            }else{
                                cc.getForWhom().sendRawMessage(GlobalConfig.warning+GlobalConfig.pluginPrefix+" 你的"+GlobalConfig.getTypeName(type)+"邮件发送数量达到上限");
                            }
                            return false;
                        }
                        return true;
                    }
                    return false;
                case "times":
                    return MailBoxAPI.hasPlayerPermission(sender, "mailbox.send.times");
                case "keytimes":
                    return MailBoxAPI.hasPlayerPermission(sender, "mailbox.send.keytimes");
                default:
                    return sender.hasPermission("mailbox.admin.send."+type);
            }
        }else return sender instanceof ConsoleCommandSender;
    }
    public static boolean filable(CommandSender sender){
        if(sender instanceof Player){
            return (MailBoxAPI.hasPlayerPermission(sender, "mailbox.send.money.coin") || 
                    MailBoxAPI.hasPlayerPermission(sender, "mailbox.send.money.point") || 
                    sender.hasPermission("mailbox.admin.send.command") || 
                    itemable(sender)>0);
        }else return sender instanceof ConsoleCommandSender;
    }
    public static int itemable(CommandSender sender){
        if(sender instanceof Player){
            return MailBoxAPI.playerSendItemAllow((Player)sender);
        }else if(sender instanceof ConsoleCommandSender){
            return GlobalConfig.maxItem;
        }else{
            return 0;
        }
    }
}

class TypeSelect extends ValidatingPrompt{
    CommandSender sender;
    List<String> select = new ArrayList();
    String type = null;
    BaseMail bm = null;
    TypeSelect(CommandSender sender){
        this.sender = sender;
    }
    TypeSelect(CommandSender sender, BaseMail bm){
        this.sender = sender;
        this.bm = bm;
    }
    @Override
    public String getPromptText(ConversationContext cc) {
        int i = 1;
        for(String t:MailBoxAPI.getAllType()){
            if(sender.hasPermission("mailbox.admin.send."+t)){
                cc.getForWhom().sendRawMessage("§b[邮件预览]: 输入"+(i++)+"发送"+GlobalConfig.getTypeName(t)+"§b邮件");
                select.add(t);
            }
        }
        if(i==1){
            if(sendable(sender,"player",null)){
                cc.getForWhom().sendRawMessage("§b[邮件预览]: 输入"+(i++)+"发送"+GlobalConfig.getTypeName("player")+"§b邮件");
                select.add("player");
            }
            if(sendable(sender,"times",null)){
                cc.getForWhom().sendRawMessage("§b[邮件预览]: 输入"+(i++)+"发送"+GlobalConfig.getTypeName("times")+"§b邮件");
                select.add("times");
            }
            if(sendable(sender,"keytimes",null)){
                cc.getForWhom().sendRawMessage("§b[邮件预览]: 输入"+(i++)+"发送"+GlobalConfig.getTypeName("keytimes")+"§b邮件");
                select.add("keytimes");
            }
        }
        return OriginalConfig.msgCancel;
    }
    @Override
    protected boolean isInputValid(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)){
            return true;
        }
        try{
            switch(Integer.parseInt(str)){
                case 9:
                    if(select.size()>=9){
                        type = select.get(7);
                        return true;
                    }else break;
                case 8:
                    if(select.size()>=8){
                        type = select.get(7);
                        return true;
                    }else break;
                case 7:
                    if(select.size()>=7){
                        type = select.get(6);
                        return true;
                    }else break;
                case 6:
                    if(select.size()>=6){
                        type = select.get(5);
                        return true;
                    }else break;
                case 5:
                    if(select.size()>=5){
                        type = select.get(4);
                        return true;
                    }else break;
                case 4:
                    if(select.size()>=4){
                        type = select.get(3);
                        return true;
                    }else break;
                case 3:
                    if(select.size()>=3){
                        type = select.get(2);
                        return true;
                    }else break;
                case 2:
                    if(select.size()>=2){
                        type = select.get(1);
                        return true;
                    }else break;
                case 1:
                    if(select.size()>=1){
                        type = select.get(0);
                        return true;
                    } break;
                default:
                    break;
            }
            cc.getForWhom().sendRawMessage("§a[邮件预览]：目标选项不存在");
            return false;
        }catch(NumberFormatException e){
            cc.getForWhom().sendRawMessage(GlobalConfig.warning+GlobalConfig.pluginPrefix+"输入格式错误，请输入数字");
            return false;
        }
    }
    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr) || type==null) return Prompt.END_OF_CONVERSATION;
        cc.getForWhom().sendRawMessage(GlobalConfig.normal+GlobalConfig.pluginPrefix+"正在创建"+GlobalConfig.getTypeName(type)+GlobalConfig.normal+"邮件");
        if(bm!=null){
            bm = bm.setType(type);
            if(bm.getSender()==null) return new Sender(bm, sender, true);
            if(bm instanceof MailPermission && ((MailPermission)bm).getPermission()==null) return new Permission(bm, sender, true);
            if(bm instanceof MailPlayer && (((MailPlayer)bm).getRecipient()==null || ((MailPlayer)bm).getRecipient().isEmpty())) return new Recipient(bm, sender, true);
            if(bm instanceof MailDate && (bm.getDate().equals("0") && ((MailDate)bm).getDeadline().equals("0"))) return new StartDate(bm, sender, true);
            if(bm instanceof MailTimes && ((MailTimes)bm).getTimes()==0) return new Times(bm, sender, true);
            if(bm instanceof MailKeyTimes && ((MailKeyTimes)bm).getKey()==null) return new TimesKey(bm, sender, true);
            if(bm instanceof MailCdkey) return new Cdkey(bm, sender, true);
            if(bm instanceof MailTemplate && ((MailTemplate)bm).getTemplate()==null) return new Template(bm, sender, true);
            return new Preview(bm, sender);
        }
        if(sender instanceof Player){
            return new Topic(MailBoxAPI.createBaseMail(type,0,sender.getName(),null,null,null,null,null,null,0,null,false,null), sender, false);
        }else{
            return new Topic(MailBoxAPI.createBaseMail(type,0,null,null,null,null,null,null,null,0,null,false,null), sender, false);
        }
    }
}

class Topic extends ValidatingPrompt{
    BaseMail bm;
    CommandSender sender;
    boolean change;
    Topic(BaseMail bm, CommandSender sender, boolean change){
        this.bm = bm;
        this.sender = sender;
        this.change = change;
    }
    @Override
    public String getPromptText(ConversationContext cc) {
        return OriginalConfig.msgTopic+'\n'+OriginalConfig.msgCancel;
    }
    @Override
    protected boolean isInputValid(ConversationContext cc, String str) {
        if(str.trim().equals("")){
            cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]: 标题不能为空");
            return false;
        }else{
            if(str.length()>OriginalConfig.maxTopic){
                cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]: 标题长度超出限制 "+OriginalConfig.maxTopic);
                return false;
            }else{
                return true;
            }
        }
    }
    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return Prompt.END_OF_CONVERSATION;
        str = MailNew.color(str);
        cc.getForWhom().sendRawMessage("§a[邮件预览]: 设置标题: "+str);
        bm.setTopic(str);
        if(change) return new Preview(bm, sender);
        return new Content(bm, sender, false);
    }
}

class Content extends ValidatingPrompt{
    BaseMail bm;
    CommandSender sender;
    boolean change;
    Content(BaseMail bm, CommandSender sender, boolean change){
        this.bm = bm;
        this.sender = sender;
        this.change = change;
    }
    @Override
    public String getPromptText(ConversationContext cc) {
        return OriginalConfig.msgContent+'\n'+OriginalConfig.msgCancel;
    }
    @Override
    protected boolean isInputValid(ConversationContext cc, String str) {
        if(str.trim().equals("")){
            cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]: 内容不能为空");
            return false;
        }else{
            if(str.length()>OriginalConfig.maxContent){
                cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]: 内容长度超出限制 "+OriginalConfig.maxContent);
                return false;
            }else{
                return true;
            }
        }
    }
    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return Prompt.END_OF_CONVERSATION;
        str = MailNew.color(str);
        cc.getForWhom().sendRawMessage("§a[邮件预览]: 设置内容: "+str);
        bm.setContent(str);
        if(change) return new Preview(bm, sender);
        if(bm.getSender()==null){
            return new Sender(bm, sender, false);
        }else{
            switch (bm.getType()){
                case "permission":
                    return new Permission(bm, sender, false);
                case "player":
                    return new Recipient(bm, sender, false);
                case "date":
                    return new StartDate(bm, sender, false);
                case "keytimes":
                case "times":
                    return new Times(bm, sender, false);
                case "template":
                    return new Template(bm, sender, false);
                default:
                    if(MailNew.filable(sender)){
                        return new File(bm, sender);
                    }else{
                        return new Preview(bm, sender);
                    }
            }
        }
    }
}

class Sender extends ValidatingPrompt{
    BaseMail bm;
    CommandSender sender;
    boolean change;
    Sender(BaseMail bm, CommandSender sender, boolean change){
        this.bm = bm;
        this.sender = sender;
        this.change = change;
    }
    @Override
    public String getPromptText(ConversationContext cc) {
        return OriginalConfig.msgSender+'\n'+OriginalConfig.msgCancel;
    }
    @Override
    protected boolean isInputValid(ConversationContext cc, String str) {
        if(str.trim().equals("")){
            cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]: 发件人不能为空");
            return false;
        }else{
            return true;
        }
    }
    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return Prompt.END_OF_CONVERSATION;
        str = MailNew.color(str);
        cc.getForWhom().sendRawMessage("§a[邮件预览]: 设置发件人: "+str);
        bm.setSender(str);
        if(change){
            if(bm instanceof MailPermission && ((MailPermission)bm).getPermission()==null) return new Permission(bm, sender, true);
            if(bm instanceof MailPlayer && (((MailPlayer)bm).getRecipient()==null || ((MailPlayer)bm).getRecipient().isEmpty())) return new Recipient(bm, sender, true);
            if(bm instanceof MailDate && (bm.getDate().equals("0") && ((MailDate)bm).getDeadline().equals("0"))) return new StartDate(bm, sender, true);
            if(bm instanceof MailTimes && (((MailTimes)bm).getTimes()==0)) return new Times(bm, sender, true);
            if(bm instanceof MailKeyTimes && (((MailKeyTimes)bm).getKey()==null)) return new TimesKey(bm, sender, true);
            if(bm instanceof MailTemplate && ((MailTemplate)bm).getTemplate()==null) return new Template(bm, sender, true);
            return new Preview(bm, sender);
        }
        switch (bm.getType()){
            case "permission":
                return new Permission(bm, sender, false);
            case "player":
                return new Recipient(bm, sender, false);
            case "date":
                return new StartDate(bm, sender, false);
            case "keytimes":
            case "times":
                return new Times(bm, sender, false);
            case "template":
                return new Template(bm, sender, false);
            default:
                if(MailNew.filable(sender)){
                    return new File(bm, sender);
                }else{
                    return new Preview(bm, sender);
                }
        }
    }
}

class Permission extends ValidatingPrompt{
    BaseMail bm;
    CommandSender sender;
    boolean change;
    Permission(BaseMail bm, CommandSender sender, boolean change){
        this.bm = bm;
        this.sender = sender;
        this.change = change;
    }
    @Override
    public String getPromptText(ConversationContext cc) {
        return OriginalConfig.msgPermission+'\n'+OriginalConfig.msgCancel;
    }
    @Override
    protected boolean isInputValid(ConversationContext cc, String str) {
        if(str.trim().equals("")){
            cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]: 权限不能为空");
            return false;
        }
        return true;
    }
    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return Prompt.END_OF_CONVERSATION;
        cc.getForWhom().sendRawMessage("§a[邮件预览]: 设置领取所需权限: "+str);
        ((MailPermission)bm).setPermission(str);
        if(change) return new Preview(bm, sender);
        if(MailNew.filable(sender)){
            return new File(bm, sender);
        }else{
            return new Preview(bm, sender);
        }
    }
}

class Recipient extends ValidatingPrompt{
    BaseMail bm;
    CommandSender sender;
    boolean change;
    Recipient(BaseMail bm, CommandSender sender, boolean change){
        this.bm = bm;
        this.sender = sender;
        this.change = change;
    }
    @Override
    public String getPromptText(ConversationContext cc) {
        return OriginalConfig.msgRecipient+'\n'+OriginalConfig.msgCancel;
    }
    @Override
    protected boolean isInputValid(ConversationContext cc, String str) {
        String[] r = str.split(" ");
        if(r.length<1){
            cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]: 收件人不能为空");
            return false;
        }else{
            if(r.length>1 && !sender.hasPermission("mailbox.admin.send.multiplayer")){
                cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]: 您只能填写一位收件人");
                return false;
            }
            if(sender instanceof Player && !sender.hasPermission("mailbox.admin.send.me")){
                for(String name:r){
                    if(name.equals(sender.getName())){
                        cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]: 收件人不能是自己");
                        return false;
                    }
                }
            }
            return true;
        }
    }
    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return Prompt.END_OF_CONVERSATION;
        ((MailPlayer)bm).setRecipient(Arrays.asList(str.split(" ")));
        if(((MailPlayer)bm).getRecipient().size()==1){
            cc.getForWhom().sendRawMessage("§a[邮件预览]: 设置收件人: "+((MailPlayer)bm).getRecipient().get(0));
        }else{
            cc.getForWhom().sendRawMessage("§a[邮件预览]: 设置以下收件人: ");
            ((MailPlayer)bm).getRecipient().forEach((s) -> {
                cc.getForWhom().sendRawMessage(s);
            });
        }
        if(change) return new Preview(bm, sender);
        if(MailNew.filable(sender)){
            return new File(bm, sender);
        }else{
            return new Preview(bm, sender);
        }
    }
}

class StartDate extends ValidatingPrompt{
    BaseMail bm;
    CommandSender sender;
    String date;
    boolean change;
    StartDate(BaseMail bm, CommandSender sender, boolean change){
        this.bm = bm;
        this.sender = sender;
        this.change = change;
    }
    @Override
    public String getPromptText(ConversationContext cc) {
        return OriginalConfig.msgStartDate+'\n'+OriginalConfig.msgStartDateCancel+'\n'+OriginalConfig.msgCancel;
    }
    @Override
    protected boolean isInputValid(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr) || str.equals("0")) return true;
        List<Integer> t = DateTime.toDate(str, sender, cc);
        switch (t.size()) {
            case 3:
            case 6:
                date = DateTime.toDate(t, sender, cc);
                return date != null;
            default:
                cc.getForWhom().sendRawMessage(GlobalConfig.warning+"输入错误，请输入3或6个数字");
                return false;
        }
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return Prompt.END_OF_CONVERSATION;
        if(str.equals("0")){
            cc.getForWhom().sendRawMessage("§a[邮件预览]: 不设置开始时间");
            bm.setDate(str);
        }else{
            cc.getForWhom().sendRawMessage("§a[邮件预览]: 设置开始时间: "+date);
            bm.setDate(date);
        }
        if(change) return new Preview(bm, sender);
        return new Deadline(bm, sender, false);
    }
}

class Deadline extends ValidatingPrompt{
    BaseMail bm;
    CommandSender sender;
    String date;
    boolean change;
    Deadline(BaseMail bm, CommandSender sender, boolean change){
        this.bm = bm;
        this.sender = sender;
        this.change = change;
    }
    @Override
    public String getPromptText(ConversationContext cc) {
        return OriginalConfig.msgDeadline+'\n'+OriginalConfig.msgDeadlineCancel+'\n'+OriginalConfig.msgCancel;
    }
    @Override
    protected boolean isInputValid(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr) || str.equals("0")) return true;
        List<Integer> t = DateTime.toDate(str, sender, cc);
        switch (t.size()) {
            case 3:
            case 6:
                date = DateTime.toDate(t, sender, cc);
                return date != null;
            default:
                cc.getForWhom().sendRawMessage(GlobalConfig.warning+"输入错误，请输入3或6个数字");
                return false;
        }
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return Prompt.END_OF_CONVERSATION;
        if(str.equals("0")){
            cc.getForWhom().sendRawMessage("§a[邮件预览]: 不设置截止时间");
            ((MailDate)bm).setDeadline(str);
        }else{
            cc.getForWhom().sendRawMessage("§a[邮件预览]: 设置截止时间: "+date);
            ((MailDate)bm).setDeadline(date);
        }
        if(change) return new Preview(bm, sender);
        if(MailNew.filable(sender)){
            return new File(bm, sender);
        }else{
            return new Preview(bm, sender);
        }
    }
}

class Times extends ValidatingPrompt{
    BaseMail bm;
    CommandSender sender;
    boolean change;
    Times(BaseMail bm, CommandSender sender, boolean change){
        this.bm = bm;
        this.sender = sender;
        this.change = change;
    }
    @Override
    public String getPromptText(ConversationContext cc) {
        return OriginalConfig.msgTimes+'\n'+OriginalConfig.msgCancel;
    }
    @Override
    protected boolean isInputValid(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)){
            return true;
        }
        try{
            if(Integer.parseInt(str)<1){
                cc.getForWhom().sendRawMessage("§a[邮件预览]：邮件数量不能小于1");
                return false;
            }
            if(Integer.parseInt(str)>GlobalConfig.times_count && !sender.hasPermission("mailbox.admin.send.check.times")){
                cc.getForWhom().sendRawMessage("§a[邮件预览]：邮件数量不能大于"+GlobalConfig.times_count);
                return false;
            }
            return true;
        }catch(NumberFormatException e){
            cc.getForWhom().sendRawMessage(GlobalConfig.warning+GlobalConfig.pluginPrefix+"输入格式错误，请输入数字");
            return false;
        }
    }
    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return Prompt.END_OF_CONVERSATION;
        int times = Integer.parseInt(str);
        cc.getForWhom().sendRawMessage("§a[邮件预览]: 设置邮件数量: "+times);
        ((MailTimes)bm).setTimes(times);
        if(change) return new Preview(bm, sender);
        if(bm instanceof MailKeyTimes) return new TimesKey(bm, sender, false);
        if(MailNew.filable(sender)){
            return new File(bm, sender);
        }else{
            return new Preview(bm, sender);
        }
    }
}

class TimesKey extends ValidatingPrompt{
    BaseMail bm;
    CommandSender sender;
    boolean change;
    TimesKey(BaseMail bm, CommandSender sender, boolean change){
        this.bm = bm;
        this.sender = sender;
        this.change = change;
    }
    @Override
    public String getPromptText(ConversationContext cc) {
        return OriginalConfig.msgTimesKey+'\n'+OriginalConfig.msgCancel;
    }
    @Override
    protected boolean isInputValid(ConversationContext cc, String str) {
        if(str.trim().equals("")){
            cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]: 口令不能为空");
            return false;
        }
        return true;
    }
    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return Prompt.END_OF_CONVERSATION;
        cc.getForWhom().sendRawMessage("§a[邮件预览]: 设置邮件口令: "+str);
        ((MailKeyTimes)bm).setKey(color(str));
        if(change) return new Preview(bm, sender);
        if(MailNew.filable(sender)){
            return new File(bm, sender);
        }else{
            return new Preview(bm, sender);
        }
    }
}

class Cdkey extends ValidatingPrompt{
    BaseMail bm;
    CommandSender sender;
    boolean change;
    boolean only;
    Cdkey(BaseMail bm, CommandSender sender, boolean change){
        this.bm = bm;
        this.sender = sender;
        this.change = change;
    }
    @Override
    public String getPromptText(ConversationContext cc) {
        return OriginalConfig.msgCdkey+'\n'+OriginalConfig.msgCancel;
    }
    @Override
    protected boolean isInputValid(ConversationContext cc, String str) {
        if(str.equalsIgnoreCase("y")){
            only = true;
        }else if(str.equalsIgnoreCase("n")){
            only = false;
        }else if(str.equals(OriginalConfig.stopStr)){
        }else{
            cc.getForWhom().sendRawMessage(GlobalConfig.warning+GlobalConfig.pluginPrefix+"目标选项不存在");
            return false;
        }
        return true;
    }
    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return Prompt.END_OF_CONVERSATION;
        cc.getForWhom().sendRawMessage("§a[邮件预览]: 设置兑换码唯一性: "+only);
        ((MailCdkey)bm).setOnly(only);
        if(change) return new Preview(bm, sender);
        if(MailNew.filable(sender)){
            return new File(bm, sender);
        }else{
            return new Preview(bm, sender);
        }
    }
}

class Template extends ValidatingPrompt{
    BaseMail bm;
    CommandSender sender;
    boolean change;
    Template(BaseMail bm, CommandSender sender, boolean change){
        this.bm = bm;
        this.sender = sender;
        this.change = change;
    }
    @Override
    public String getPromptText(ConversationContext cc) {
        return OriginalConfig.msgTemplate+'\n'+OriginalConfig.msgCancel;
    }
    @Override
    protected boolean isInputValid(ConversationContext cc, String str) {
        if(str.trim().equals("")){
            cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]: 文件名不能为空");
            return false;
        }
        return true;
    }
    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return Prompt.END_OF_CONVERSATION;
        cc.getForWhom().sendRawMessage("§a[邮件预览]: 设置模板名: "+str);
        ((MailTemplate)bm).setTemplate(str);
        if(change) return new Preview(bm, sender);
        if(MailNew.filable(sender)){
            return new File(bm, sender);
        }else{
            return new Preview(bm, sender);
        }
    }
}

class File extends ValidatingPrompt{
    BaseMail bm;
    CommandSender sender;
    boolean file;
    File(BaseMail bm, CommandSender sender){
        this.bm = bm;
        this.sender = sender;
    }
    @Override
    public String getPromptText(ConversationContext cc) {
        return OriginalConfig.msgFile+'\n'+OriginalConfig.msgCancel;
    }
    @Override
    protected boolean isInputValid(ConversationContext cc, String str) {
        if(str.equalsIgnoreCase("y")){
            file = true;
        }else if(str.equalsIgnoreCase("n")){
            file = false;
        }else if(str.equals(OriginalConfig.stopStr)){
        }else{
            cc.getForWhom().sendRawMessage(GlobalConfig.warning+GlobalConfig.pluginPrefix+"目标选项不存在");
            return false;
        }
        return true;
    }
    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return Prompt.END_OF_CONVERSATION;
        if(file){
            if(GlobalConfig.enVault && MailBoxAPI.hasPlayerPermission(sender, "mailbox.send.money.coin")){
                return new Coin(bm.addFile(), sender, false);
            }else if(GlobalConfig.enPlayerPoints && MailBoxAPI.hasPlayerPermission(sender, "mailbox.send.money.point")){
                return new Point(bm.addFile(), sender, false);
            }else if(MailNew.itemable(sender)>0){
                return new Item(bm.addFile(), sender, false);
            }else if(sender.hasPermission("mailbox.admin.send.command")){
                return new Command(bm.addFile(), sender, false);
            }else{
                return new Preview(bm, sender);
            }
        }else{
            return new Preview(bm, sender);
        }
    }
}

class Coin extends ValidatingPrompt{
    BaseFileMail fm;
    CommandSender sender;
    double coin;
    boolean change;
    Coin(BaseFileMail fm, CommandSender sender, boolean change){
        this.fm = fm;
        this.sender = sender;
        this.change = change;
    }
    @Override
    public String getPromptText(ConversationContext cc) {
        return OriginalConfig.msgCoin+'\n'+OriginalConfig.msgCancel;
    }
    @Override
    protected boolean isInputValid(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return true;
        try{
            coin = Double.parseDouble(str);
            if(sender instanceof Player){
                Player p = (Player)sender;
                fm.setCoin(coin);
                double expand = fm.getExpandCoin();
                if(expand>MailBoxAPI.getEconomyBalance(p) && !p.hasPermission("mailbox.admin.send.check.coin")){
                    cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]："+GlobalConfig.vaultDisplay+GlobalConfig.warning+"余额不足, 您有"+MailBoxAPI.getEconomyBalance(p));
                    return false;
                }else if(expand>GlobalConfig.vaultMax && !p.hasPermission("mailbox.admin.send.check.coin")){
                    cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]："+GlobalConfig.vaultDisplay+GlobalConfig.warning+"超出最大发送限制: "+GlobalConfig.vaultMax);
                    return false;
                }else{
                    return true;
                }
            }else return sender instanceof ConsoleCommandSender;
        }catch(NumberFormatException e){
            cc.getForWhom().sendRawMessage(GlobalConfig.warning+GlobalConfig.pluginPrefix+"输入格式错误，请输入数字");
            return false;
        }
    }
    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return Prompt.END_OF_CONVERSATION;
        cc.getForWhom().sendRawMessage("§a[邮件预览]: 设置发送"+GlobalConfig.vaultDisplay+": "+coin);
        fm.setCoin(coin);
        if(change) return new Preview(fm, sender);
        if(GlobalConfig.enPlayerPoints && MailBoxAPI.hasPlayerPermission(sender, "mailbox.send.money.point")){
            return new Point(fm, sender, false);
        }else if(MailNew.itemable(sender)>0){
            return new Item(fm, sender, false);
        }else if(sender.hasPermission("mailbox.admin.send.command")){
            return new Command(fm, sender, false);
        }else{
            return new Preview(fm, sender);
        }
    }
}

class Point extends ValidatingPrompt{
    BaseFileMail fm;
    CommandSender sender;
    int point;
    boolean change;
    Point(BaseFileMail fm, CommandSender sender, boolean change){
        this.fm = fm;
        this.sender = sender;
        this.change = change;
    }
    @Override
    public String getPromptText(ConversationContext cc) {
        return OriginalConfig.msgPoint+'\n'+OriginalConfig.msgCancel;
    }
    @Override
    protected boolean isInputValid(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return true;
        try{
            point = Integer.parseInt(str);
            if(sender instanceof Player){
                Player p = (Player)sender;
                fm.setPoint(point);
                int expand = fm.getExpandPoint();
                if(expand>MailBoxAPI.getPoints(p) && !p.hasPermission("mailbox.admin.send.check.point")){
                    cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]："+GlobalConfig.vaultDisplay+GlobalConfig.warning+"余额不足, 您有"+MailBoxAPI.getPoints(p));
                    return false;
                }else if(expand>GlobalConfig.playerPointsMax && !p.hasPermission("mailbox.admin.send.check.point")){
                    cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]："+GlobalConfig.playerPointsDisplay+GlobalConfig.warning+"超出最大发送限制: "+GlobalConfig.playerPointsMax);
                    return false;
                }else{
                    return true;
                }
            }else return sender instanceof ConsoleCommandSender;
        }catch(NumberFormatException e){
            sender.sendMessage(GlobalConfig.warning+GlobalConfig.pluginPrefix+"输入格式错误，请输入数字");
            return false;
        }
    }
    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return Prompt.END_OF_CONVERSATION;
        cc.getForWhom().sendRawMessage("§a[邮件预览]: 设置发送"+GlobalConfig.playerPointsDisplay+": "+point);
        fm.setPoint(point);
        if(change) return new Preview(fm, sender);
        if(MailNew.itemable(sender)>0){
            return new Item(fm, sender, false);
        }else if(sender.hasPermission("mailbox.admin.send.command")){
            return new Command(fm, sender, false);
        }else{
            return new Preview(fm, sender);
        }
    }
}

class Item extends ValidatingPrompt{
    BaseFileMail fm;
    CommandSender sender;
    int itemable;
    ArrayList<ItemStack> item = new ArrayList();
    boolean change;
    Item(BaseFileMail fm, CommandSender sender, boolean change){
        this.fm = fm;
        this.sender = sender;
        this.itemable = MailNew.itemable(sender);
        this.change = change;
    }
    @Override
    public String getPromptText(ConversationContext cc) {
        if(sender instanceof Player){
            return OriginalConfig.msgItemPlayer+itemable+'\n'+OriginalConfig.msgItemCancel+'\n'+OriginalConfig.msgCancel;
        }else if(sender instanceof ConsoleCommandSender){
            return OriginalConfig.msgItemConsole+itemable+'\n'+OriginalConfig.msgItemCancel+'\n'+OriginalConfig.msgCancel;
        }else{
            return GlobalConfig.warning+GlobalConfig.pluginPrefix+"对话出错"+OriginalConfig.msgItemCancel+'\n'+OriginalConfig.msgCancel;
        }
    }
    @Override
    protected boolean isInputValid(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return true;
        if(str.equals("0")){
            item = new ArrayList();
            return true;
        }
        if(sender instanceof Player){
            ArrayList<Integer> il = new ArrayList();
            for(String s:str.split(" ")){
                try{
                    int i = Integer.parseInt(s);
                    il.add(i);
                }catch(NumberFormatException e){
                    cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]: "+"输入格式错误，请输入数字");
                    return false;
                }
            }
            if(il.size()>itemable){
                cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]: "+"超出最大发送限制: "+itemable);
                return false;
            }
            ArrayList<ItemStack> ial = new ArrayList();
            Player p = (Player)sender;
            boolean skip = p.hasPermission("mailbox.admin.send.check.ban");
            for(int i:il){
                ItemStack is = p.getInventory().getItem((i-1));
                if(is==null){
                    cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]: "+i+"号格子物品不存在");
                    return false;
                }else{
                    if(skip || MailBoxAPI.isAllowSend(is)){
                        ial.add(is);
                    }else{
                        cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]: "+i+"号格子物品无法作为邮件发送");
                        return false;
                    }
                }
            }
            item = ial;
            return true;
        }else if(sender instanceof ConsoleCommandSender){
            List<String> il = Arrays.asList(str.split(" "));
            if(il.size()>itemable){
                cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]: "+"超出最大发送限制: "+itemable);
                return false;
            }
            ArrayList<ItemStack> ial = new ArrayList();
            for(String s:il){
                ItemStack is = MailBoxAPI.readItem(s);
                if(is==null){
                    cc.getForWhom().sendRawMessage(GlobalConfig.warning+"[邮件预览]： "+s+" 物品不存在");
                    return false;
                }else{
                    ial.add(is);
                }
            }
            item = ial;
            return true;
        }else{
            item = new ArrayList();
            return true;
        }
    }
    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return Prompt.END_OF_CONVERSATION;
        if(item.isEmpty()) {
            cc.getForWhom().sendRawMessage("§a[邮件预览]: 设置不发送物品");
        }else{
            fm.setItemList(item);
            cc.getForWhom().sendRawMessage("§a[邮件预览]: 设置发送物品格子数量: "+item.size());
        }
        if(change) return new Preview(fm, sender);
        if(sender.hasPermission("mailbox.admin.send.command")){
            return new Command(fm, sender, false);
        }else{
            return new Preview(fm, sender);
        }
    }
}

class Command extends ValidatingPrompt{
    BaseFileMail fm;
    CommandSender sender;
    boolean change;
    Command(BaseFileMail fm, CommandSender sender, boolean change){
        this.fm = fm;
        this.sender = sender;
        this.change = change;
    }
    @Override
    public String getPromptText(ConversationContext cc) {
        return OriginalConfig.msgCommand+'\n'+OriginalConfig.msgCommandCancel+'\n'+OriginalConfig.msgCancel;
    }
    @Override
    protected boolean isInputValid(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return true;
        if(str.trim().equals("0")) return true;
        return true;
    }
    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return Prompt.END_OF_CONVERSATION;
        if(str.trim().equals("0")) {
            return new Preview(fm, sender);
        }else{
            if(str.indexOf("/")==0) str = str.substring(1);
            fm.setCommandList(Arrays.asList(str.split("/")));
            if(fm.getCommandList().size()<=1){
                cc.getForWhom().sendRawMessage("§a[邮件预览]: 设置执行指令: /"+fm.getCommandList().get(0));
            }else{
                cc.getForWhom().sendRawMessage("§a[邮件预览]: 设置执行以下指令: ");
                fm.getCommandList().forEach((s) -> {
                    cc.getForWhom().sendRawMessage("/"+s);
                });
            }
            if(change) return new Preview(fm, sender);
            return new CommandDescription(fm, sender, false);
        }
    }
}

class CommandDescription extends ValidatingPrompt{
    BaseFileMail fm;
    CommandSender sender;
    boolean change;
    CommandDescription(BaseFileMail fm, CommandSender sender, boolean change){
        this.fm = fm;
        this.sender = sender;
        this.change = change;
    }
    @Override
    public String getPromptText(ConversationContext cc) {
        return OriginalConfig.msgCommandDescription+'\n'+OriginalConfig.msgCommandDescriptionCancel+'\n'+OriginalConfig.msgCancel;
    }
    @Override
    protected boolean isInputValid(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return true;
        if(str.equals("0")) return true;
        return true;
    }
    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return Prompt.END_OF_CONVERSATION;
        if(str.equals("0")) {
            return new Preview(fm, sender);
        }else{
            ArrayList<String> desc = new ArrayList();
            Arrays.asList(str.split(" ")).forEach((s) -> {
                desc.add(MailNew.color(s));
            });
            fm.setCommandDescription(desc);
            if(fm.getCommandList().size()<=1){
                cc.getForWhom().sendRawMessage("§a[邮件预览]: 设置指令描述: "+fm.getCommandDescription().get(0));
            }else{
                cc.getForWhom().sendRawMessage("§a[邮件预览]: 设置以下指令描述: ");
                fm.getCommandDescription().forEach((s) -> {
                    cc.getForWhom().sendRawMessage(s);
                });
            }
            if(change) return new Preview(fm, sender);
            return new Preview(fm, sender);
        }
    }
}

class Preview extends ValidatingPrompt{
    private final static HashMap<Integer,String> OPTION = new HashMap();
    static {
        OPTION.put(1, "§b[邮件预览]: 输入 0 修改标题");
        OPTION.put(2, "§b[邮件预览]: 输入 0 修改内容");
        OPTION.put(3, "§b[邮件预览]: 输入 0 修改发件人");
        OPTION.put(4, "§b[邮件预览]: 输入 0 修改收件人");
        OPTION.put(5, "§b[邮件预览]: 输入 0 修改权限");
        OPTION.put(6, "§b[邮件预览]: 输入 0 修改"+GlobalConfig.vaultDisplay);
        OPTION.put(7, "§b[邮件预览]: 输入 0 修改"+GlobalConfig.playerPointsDisplay);
        OPTION.put(8, "§b[邮件预览]: 输入 0 修改指令");
        OPTION.put(9, "§b[邮件预览]: 输入 0 修改指令描述");
        OPTION.put(10, "§b[邮件预览]: 输入 0 修改物品");
        OPTION.put(11, "§b[邮件预览]: 输入 0 添加附件");
        OPTION.put(12, "§b[邮件预览]: 输入 0 移除所有附件");
        OPTION.put(13, "§b[邮件预览]: 输入 0 修改发件日期");
        OPTION.put(14, "§b[邮件预览]: 输入 0 修改截止日期");
        OPTION.put(15, "§b[邮件预览]: 输入 0 修改模板文件名");
        OPTION.put(16, "§b[邮件预览]: 输入 0 修改邮件数量");
        OPTION.put(17, "§b[邮件预览]: 输入 0 修改兑换码唯一性");
        OPTION.put(18, "§b[邮件预览]: 输入 0 修改邮件口令");
    }
    public static HashMap<Integer,Integer> optional(BaseMail bm, CommandSender sender){
        HashMap<Integer,Integer> o = new HashMap();
        int i = 1;
        o.put((i++), 1);
        o.put((i++), 2);
        if(sender.hasPermission("mailbox.admin.send.sender")){
            o.put((i++), 3);
        }
        switch (bm.getType()){
            case "player":
                o.put((i++), 4);
                break;
            case "permission":
                o.put((i++), 5);
                break;
            case "date":
                o.put((i++), 13);
                o.put((i++), 14);
                break;
            case "template":
                o.put((i++), 15);
                break;
            case "keytimes":
                o.put((i++), 18);
            case "times":
                o.put((i++), 16);
                break;
            case "cdkey":
                o.put((i++), 17);
                break;
        }
        if(bm instanceof BaseFileMail){
            if(GlobalConfig.enVault && MailBoxAPI.hasPlayerPermission(sender, "mailbox.send.money.coin")) o.put((i++), 6);
            if(GlobalConfig.enPlayerPoints && MailBoxAPI.hasPlayerPermission(sender, "mailbox.send.money.point")) o.put((i++), 7);
            if(sender.hasPermission("mailbox.admin.send.command")){
                o.put((i++), 8);
                o.put((i++), 9);
            }
            if(MailNew.itemable(sender)>0) o.put((i++), 10);
            o.put((i++), 12);
        }else{
            if(MailNew.filable(sender)) o.put((i++), 11);
        }
        return o;
    }
    BaseMail bm;
    CommandSender sender;
    HashMap<Integer,Integer> optional;
    int change = 0;
    Preview(BaseMail bm, CommandSender sender){
        if((bm instanceof BaseFileMail) && !((BaseFileMail)bm).hasFileContent()) bm = ((BaseFileMail)bm).removeFile();
        this.bm = bm;
        this.sender = sender;
    }
    @Override
    public String getPromptText(ConversationContext cc) {
        optional = optional(bm, sender);
        if((sender instanceof Player) && GlobalConfig.enVexView && GlobalConfig.lowVexView_2_5) MailContentGui.openMailContentGui((Player)sender, bm);
        MailView.preview(bm, sender, cc);
        optional.forEach((k,v) -> {
            cc.getForWhom().sendRawMessage(OPTION.get(v).replace("0", Integer.toString(k)));
        });
        if(bm instanceof MailTemplate) return OriginalConfig.msgSavePreview+'\n'+OriginalConfig.msgCancel;
        else return OriginalConfig.msgSendPreview+'\n'+OriginalConfig.msgCancel;
    }
    @Override
    protected boolean isInputValid(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return true;
        try{
            change = Integer.parseInt(str);
            if(change==0){
                return true;
            }else{
                if(optional.containsKey(change)){
                    return true;
                }else{
                    cc.getForWhom().sendRawMessage(GlobalConfig.warning+GlobalConfig.pluginPrefix+"目标选项不存在");
                    return false;
                }
            }
        }catch(NumberFormatException e){
            cc.getForWhom().sendRawMessage(GlobalConfig.warning+GlobalConfig.pluginPrefix+"输入格式错误，请输入数字");
            return false;
        }
    }
    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String str) {
        if(str.equals(OriginalConfig.stopStr)) return Prompt.END_OF_CONVERSATION;
        if(change==0){
            if(!MailNew.sendable(sender, bm.getType(), cc)){
                cc.getForWhom().sendRawMessage(GlobalConfig.warning+"你无权发送邮件");
                return new Preview(bm, sender);
            }
            if(bm.Send(sender, cc)){
                if(bm instanceof MailTemplate) cc.getForWhom().sendRawMessage(OriginalConfig.msgSaveSuccess);
                else cc.getForWhom().sendRawMessage(OriginalConfig.msgSendSuccess);
                return Prompt.END_OF_CONVERSATION;
            }else{
                if(bm instanceof MailTemplate) cc.getForWhom().sendRawMessage(OriginalConfig.msgSaveFailed);
                else cc.getForWhom().sendRawMessage(OriginalConfig.msgSendFailed);
                return new Preview(bm, sender);
            }
        }else{
            switch (optional.get(change)){
                case 1:
                    return new Topic(bm, sender, true);
                case 2:
                    return new Content(bm, sender, true);
                case 3:
                    return new Sender(bm, sender, true);
                case 4:
                    return new Recipient(bm, sender, true);
                case 5:
                    return new Permission(bm, sender, true);
                case 6:
                    return new Coin((BaseFileMail)bm, sender, true);
                case 7:
                    return new Point((BaseFileMail)bm, sender, true);
                case 8:
                    return new Command((BaseFileMail)bm, sender, true);
                case 9:
                    return new CommandDescription((BaseFileMail)bm, sender, true);
                case 10:
                    return new Item((BaseFileMail)bm, sender, true);
                case 11:
                    if(GlobalConfig.enVault && MailBoxAPI.hasPlayerPermission(sender, "mailbox.send.money.coin")){
                        return new Coin(bm.addFile(), sender, false);
                    }else if(GlobalConfig.enPlayerPoints && MailBoxAPI.hasPlayerPermission(sender, "mailbox.send.money.point")){
                        return new Point(bm.addFile(), sender, false);
                    }else if(MailNew.itemable(sender)>0){
                        return new Item(bm.addFile(), sender, false);
                    }else if(sender.hasPermission("mailbox.admin.send.command")){
                        return new Command(bm.addFile(), sender, false);
                    }else{
                        return new Preview(bm, sender);
                    }
                case 12:
                    return new Preview(((BaseFileMail)bm).removeFile(), sender);
                case 13:
                    return new StartDate(bm, sender, true);
                case 14:
                    return new Deadline(bm, sender, true);
                case 15:
                    return new Template(bm, sender, true);
                case 16:
                    return new Times(bm, sender, true);
                case 17:
                    return new Cdkey(bm, sender, true);
                case 18:
                    return new TimesKey(bm, sender, true);
                default:
                    cc.getForWhom().sendRawMessage(GlobalConfig.warning+GlobalConfig.pluginPrefix+"目标选项不存在");
                    return new Preview(bm, sender);
            }
        }
    }
}