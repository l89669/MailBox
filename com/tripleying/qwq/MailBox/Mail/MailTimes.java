package com.tripleying.qwq.MailBox.Mail;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.entity.Player;

public interface MailTimes {
    public int getTimes();
    public void setTimes(int times);
    public boolean ExpireValidate();
    public boolean TimesValidate();
    public boolean collectValidate(Player p);
    public boolean sendData();
    public boolean sendValidate(Player p, ConversationContext cc);
}
