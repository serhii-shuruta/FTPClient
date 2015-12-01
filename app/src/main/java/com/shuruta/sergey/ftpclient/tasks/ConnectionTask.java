package com.shuruta.sergey.ftpclient.tasks;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.shuruta.sergey.ftpclient.Constants;
import com.shuruta.sergey.ftpclient.EventBusMessenger;
import com.shuruta.sergey.ftpclient.services.FtpService;
import com.shuruta.sergey.ftpclient.entity.Connection;

import java.io.File;
import java.io.IOException;

import de.greenrobot.event.EventBus;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

/**
 * Author: Sergey Shuruta
 * Date: 08/15/15
 * Time: 22:11
 */
public class ConnectionTask extends Task {

    private FTPClient ftpClient;
    private Connection connection;

    public static final String TAG = FtpService.TAG + "." + ConnectionTask.class.getSimpleName();

    public ConnectionTask(Context context, FTPClient ftpClient, Connection connection) {
        super(context);
        this.ftpClient = ftpClient;
        this.connection = connection;
    }

    @Override
    public void run() {
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.PARAM_CONNECTION_ID, connection.getId());
        EventBusMessenger.sendConnectionMessage(EventBusMessenger.Event.START, bundle);
        try {
            ftpClient.connect(connection.getHost(), connection.getPort());
            ftpClient.setPassive(true);
            ftpClient.setAutoNoopTimeout(connection.getNoop() * 1000);
            ftpClient.login(connection.getLogin(), connection.getPassw());
            ftpClient.setType(FTPClient.TYPE_BINARY);
            ftpClient.changeDirectory(File.separator);
            ftpClient.setCompressionEnabled(ftpClient.isCompressionSupported());
        } catch (IOException e) {
            bundle.putString(EventBusMessenger.MSG, e.getMessage());
            e.printStackTrace();
        } catch (FTPIllegalReplyException e) {
            bundle.putString(EventBusMessenger.MSG, e.getMessage());
            e.printStackTrace();
        } catch (FTPException e) {
            bundle.putString(EventBusMessenger.MSG, e.getMessage());
            e.printStackTrace();
        }
        if(bundle.containsKey(EventBusMessenger.MSG))
            EventBusMessenger.sendConnectionMessage(EventBusMessenger.Event.ERROR, bundle);
        if(ftpClient.isConnected()) {
            if(ftpClient.isAuthenticated()) {
                EventBusMessenger.sendConnectionMessage(EventBusMessenger.Event.OK, bundle);
            } else {
                EventBusMessenger.sendConnectionMessage(EventBusMessenger.Event.ERROR, bundle);
            }
        } else {
            EventBusMessenger.sendConnectionMessage(EventBusMessenger.Event.ERROR, bundle);
        }
        EventBusMessenger.sendConnectionMessage(EventBusMessenger.Event.FINISH, bundle);
    }
}
