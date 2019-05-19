package com.hector.carduino;

import android.content.Context;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.io.IOException;

class FeedbackTracker extends Thread {
    public static final int MAIN_ACTIVITY = 1;
    public static final int CONTROLS_ACTIVITY = 2;
    public static final int DRIVE_ACTIVITY = 3;

    public com.hector.carduino.Status currentStatus;
    public Messenger messageSender;
    public Context context;
    public int type;

    public FeedbackTracker(Messenger messageSender, Context context, int type) {
        super();
        this.currentStatus = new com.hector.carduino.Status();
        this.messageSender = messageSender;
        this.context = context;
        this.type = type;
        this.start();
    }

    /**
     * Intenta actualizar el status cada 500ms
     */
    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {};

            publishProgress();
        }
    }

    /**
     * Intenta leer el status y realizar las comprobaciones oportunas
     */
    public void publishProgress() {
        try {
            currentStatus = currentStatus.readFromFile(context);
            Boolean connected = false;
            if(currentStatus == null || System.currentTimeMillis() - currentStatus.getLastStatus() > 4000) {
                Thread.sleep(1000);
                refresh();
                Thread.sleep(2000);
            } else {
                connected = true;
            }
            callbackHandler(connected);
        } catch (NullPointerException e) {
        } catch (InterruptedException e) {
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {}
    }

    public void callbackHandler(boolean connected) {
        switch (type) {
            case MAIN_ACTIVITY:
                ((MainActivity) context).dashboardHandler(currentStatus, connected);
                break;
            case DRIVE_ACTIVITY:
                ((DriveActivity) context).dashBoardHandler(currentStatus, connected);
                break;
            case CONTROLS_ACTIVITY:
                ((ControlsActivity) context).dashBoardHandler(currentStatus);
                break;
            default:
                break;
        }
    }

    public void refresh() {
        Message msg = Message.obtain(null, BluetoothService.RESTART);
        try {
            messageSender.send(msg);
        } catch (RemoteException e) { }
    }
}
