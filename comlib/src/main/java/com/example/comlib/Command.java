package com.example.comlib;

import java.io.File;

/**
 * @author zhangyazhou
 * @date 2020/1/17
 */
public class Command {

    private SerialPort serialPort;

    public Command(String node,int baudrate,OnDataReceivedListener listener,int overTime){
        serialPort = new SerialPort(new File(node),baudrate,0,listener,overTime);
    }
    
    public void send(byte[] data){
        serialPort.sendData(data);
        serialPort.start();
    }
}
