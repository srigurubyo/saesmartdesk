package com.sae.smartdesk.notify.service;

import com.sae.smartdesk.notify.entity.Notification;

public interface NotificationChannelAdapter {

    boolean supports(String channel);

    void send(Notification notification) throws Exception;
}
