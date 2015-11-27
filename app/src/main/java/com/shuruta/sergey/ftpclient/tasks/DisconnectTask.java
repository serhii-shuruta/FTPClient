package com.shuruta.sergey.ftpclient.tasks;

import android.content.Context;

import com.shuruta.sergey.ftpclient.EventBusMessenger;
import com.shuruta.sergey.ftpclient.adapters.FTPFileAdapter;
import com.shuruta.sergey.ftpclient.cache.CacheManager;
import com.shuruta.sergey.ftpclient.interfaces.FFile;
import com.shuruta.sergey.ftpclient.services.FtpService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;

/**
 * Created by Sergey on 28.11.2015.
 */
public class DisconnectTask extends Task {

    private final FTPClient ftpClient;

    public static final String TAG = FtpService.TAG + "." + DisconnectTask.class.getSimpleName();

    public DisconnectTask(Context context, FTPClient ftpClient) {
        super(context);
        this.ftpClient = ftpClient;
    }

    @Override
    public void run() {

        try {
            ftpClient.disconnect(true);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FTPIllegalReplyException e) {
            EventBusMessenger.sendFtpMessage(EventBusMessenger.Event.DISCONNECT_ERROR);
            e.printStackTrace();
        } catch (FTPException e) {
            EventBusMessenger.sendFtpMessage(EventBusMessenger.Event.DISCONNECT_ERROR);
            e.printStackTrace();
        }
        EventBusMessenger.sendFtpMessage(EventBusMessenger.Event.DISCONNECT);
    }

    private void prepareAndPutToCache(FTPFile[] list) {
        List<FFile> ftpFiles = new ArrayList<>();
        for(int i = 0; i < list.length; i++) {
            if(list[i].getName().equals("..")) continue;
            ftpFiles.add(FTPFileAdapter.create(list[i]));
        }
        Collections.sort(ftpFiles, new Comparator<FFile>() {
            @Override
            public int compare(final FFile object1, final FFile object2) {
                if (object1.isDir() && object2.isFile())
                    return -1;
                if (object1.isFile() && object2.isDir())
                    return 1;
                return object1.getName().compareTo(object2.getName());
            }
        });
        CacheManager.getInstance().putFtpFiles(ftpFiles);
    }

}
