/*
 * Copyright 2014-2017 ieclipse.cn.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ieclipse.smartqq;

import com.scienjus.smartqq.client.SmartQQClient;
import com.scienjus.smartqq.model.DiscussFrom;
import com.scienjus.smartqq.model.FriendFrom;
import com.scienjus.smartqq.model.GroupFrom;
import com.scienjus.smartqq.model.QQContact;
import com.scienjus.smartqq.model.QQMessage;

import cn.ieclipse.smartim.IMClientFactory;
import cn.ieclipse.smartim.IMHistoryManager;
import cn.ieclipse.smartim.IMPlugin;
import cn.ieclipse.smartim.callback.ReceiveCallback;
import cn.ieclipse.smartim.common.IMUtils;
import cn.ieclipse.smartim.common.Notifications;
import cn.ieclipse.smartim.model.impl.AbstractFrom;
import cn.ieclipse.smartim.model.impl.AbstractMessage;
import cn.ieclipse.smartim.preferences.SettingsPerferencePage;
import cn.ieclipse.smartqq.console.QQChatConsole;
import cn.ieclipse.smartqq.views.QQContactView;

/**
 * 类/接口描述
 * 
 * @author Jamling
 * @date 2017年10月16日
 *       
 */
public class QQReceiveCallback implements ReceiveCallback {
    private QQChatConsole lastConsole;
    private QQContactView fContactView;
    
    public QQReceiveCallback(QQContactView fContactView) {
        this.fContactView = fContactView;
    }
    
    @Override
    public void onReceiveMessage(AbstractMessage message, AbstractFrom from) {
        if (from != null && from.getContact() != null) {
            boolean unknown = false;
            boolean notify = IMPlugin.getDefault().getPreferenceStore()
                    .getBoolean(SettingsPerferencePage.NOTIFY_FRIEND);
            String uin = from.getContact().getUin();
            QQContact qqContact = null;
            if (from instanceof GroupFrom) {
                GroupFrom gf = (GroupFrom) from;
                unknown = (gf.getGroupUser() == null
                        || gf.getGroupUser().isUnknown());
                uin = gf.getGroup().getUin();
                notify = IMPlugin.getDefault().getPreferenceStore()
                        .getBoolean(SettingsPerferencePage.NOTIFY_GROUP);
                qqContact = fContactView.getClient()
                        .getGroup(gf.getGroup().getId());
            }
            else if (from instanceof DiscussFrom) {
                DiscussFrom gf = (DiscussFrom) from;
                unknown = (gf.getDiscussUser() == null
                        || gf.getDiscussUser().isUnknown());
                uin = gf.getDiscuss().getUin();
                notify = IMPlugin.getDefault().getPreferenceStore()
                        .getBoolean(SettingsPerferencePage.NOTIFY_GROUP);
                qqContact = fContactView.getClient()
                        .getGroup(gf.getDiscuss().getId());
            }
            if (!unknown) {
                SmartQQClient client = IMClientFactory.getInstance()
                        .getQQClient();
                IMHistoryManager.getInstance().save(client, uin,
                        message.getRaw());
            }
            if (notify) {
                boolean hide = unknown && !IMPlugin.getDefault()
                        .getPreferenceStore()
                        .getBoolean(SettingsPerferencePage.NOTIFY_UNKNOWN);
                try {
                    hide = hide || from.getMember().getUin().equals(
                            fContactView.getClient().getAccount().getUin());
                } catch (Exception e) {
                    IMPlugin.getDefault().log("", e);
                }
                if (hide) {
                    // don't notify
                }
                else {
                    CharSequence content = (from instanceof FriendFrom)
                            ? message.getText()
                            : from.getName() + ":" + message.getText();
                    Notifications.notify(QQChatConsole.class, from.getContact(),
                            from.getContact().getName(), content);
                }
            }
            if (qqContact != null) {
                qqContact.setLastMessage(message);
            }
            
            QQChatConsole console = IMPlugin.getDefault()
                    .findConsole(QQChatConsole.class, from.getContact(), false);
            if (console != null) {
                lastConsole = console;
                String name = from.getName();
                String msg = null;
                if (message instanceof QQMessage) {
                    QQMessage m = (QQMessage) message;
                    msg = IMUtils.formatMsg(m.getTime(), name, m.getContent());
                }
                console.write(msg);
            }
            else {
                if (qqContact != null) {
                    qqContact.increaceUnRead();
                }
            }
            fContactView.notifyUpdateContacts(0, false);
        }
    }
    
    @Override
    public void onReceiveError(Throwable e) {
        if (e == null) {
            return;
        }
        if (lastConsole != null) {
            lastConsole.error(e);
        }
        else {
            IMPlugin.getDefault().log("SmartQQ 接收异常", e);
        }
    }
    
}
