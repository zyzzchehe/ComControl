package com.example.comlib;

/**
 * @author zhangyazhou
 * @date 2020/1/17
 */
public interface OnDataReceivedListener {
    void onDataReceived(final byte[] buffer, final int size);
}
