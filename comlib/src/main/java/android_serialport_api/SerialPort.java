package android_serialport_api;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SerialPort {
	private static final String TAG = "SerialPort";

	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;
	public ReadThread mReadThread;
	private OnDataReceivedListener mListener;
	private byte[] mData = new byte[50];
	private int overTime;



	class ReadThread extends Thread {
		boolean mRunning;
		int cnt,nCount;
		public ReadThread() {
			mRunning = true;
		}
		public void cancel() {
			mRunning = false;
			interrupt();
		}
		@Override
		public void run() {
			super.run();
			try {
				while (mRunning && !isInterrupted()) {
					if (mFileInputStream == null) {
						Log.i(TAG, "run: 串口未打开");
						return;
					}
					if (mFileInputStream.available() <= 0) {
						if (nCount > (overTime / 10)) {
							if (mListener != null) {
								Log.i(TAG, "run: 超时数据未返回");
								mListener.onDataReceived(new byte[] { 0x5 }, 1);
							}
							break;
						} else {
							nCount++;
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							continue;
						}
					}else{
						nCount = 0;
						cnt = mFileInputStream.read(mData);
						if (mListener != null && mData != null) {
							Log.i(TAG, "run: 数据返回，回调");
							mListener.onDataReceived(mData, cnt);
						}
					}
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public SerialPort(File device, int baudrate, int flags, OnDataReceivedListener listener, int overtime){
		this.overTime = overtime;
		for (int i = 0; i < mData.length; i++) {
			mData[i] = 0x00;
		}
		// Check access permission
		if (!device.canRead() || !device.canWrite()) {
			try {
				Process su = Runtime.getRuntime().exec("/system/bin/su");
				String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
				su.getOutputStream().write(cmd.getBytes());
				if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
					Log.i(TAG, "SerialPort: chmod failed");
					throw new SecurityException();
				}
			} catch (Exception e) {
				Log.i(TAG, "SerialPort: 抛出异常");
				e.printStackTrace();
			}
		}

		mFd = open(device.getAbsolutePath(), baudrate, flags);
		if (mFd == null) {
			Log.e(TAG, "device open failed");
			return;
		}

		mListener = listener;
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = null;
		mFileOutputStream = new FileOutputStream(mFd);

	}

	public void start() {
		if (mReadThread != null) {
			mReadThread.cancel();
			mReadThread = null;
		}
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mReadThread = new ReadThread();
		mReadThread.start();
	}

	public void sendData(byte[] data) {
		if (mFileOutputStream != null) {
			try {
				mFileOutputStream.write(data);
				mFileOutputStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private native static FileDescriptor open(String path, int baudrate, int flags);
	public native void close();
	static {
		System.loadLibrary("serial_port");
	}
}