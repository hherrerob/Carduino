package com.hector.carduino;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;

public class BluetoothService extends Service {
    public static final int SEND = 1;
    public static final int SEND_PARAM = 2;
    public static final int RESTART = 3;

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    public ConnectThread connectThread;
    public StatusTracker statusTracker;

    public Messenger messageSender;

    @Override
    public void onCreate() {
        this.connectThread = ConnectThread.connect();
        this.statusTracker = new StatusTracker(connectThread, this);

        HandlerThread thread = new HandlerThread("CarduinoService", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
        messageSender = new Messenger(serviceHandler);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messageSender.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

        return START_STICKY;
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEND:
                    if(msg.obj != null)
                        try {
                            connectThread.send(((String) msg.obj).toCharArray());
                        } catch (ClassCastException e) {
                            connectThread.send((char) msg.obj);
                        }
                    break;
                case SEND_PARAM:
                        connectThread.sendParameter();
                    break;
                case RESTART:
                    connectThread.cancel();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) { }
                    connectThread = ConnectThread.connect();
                    statusTracker = new StatusTracker(connectThread, BluetoothService.this);
                    break;
                default: break;
            }
        }
    }
}
