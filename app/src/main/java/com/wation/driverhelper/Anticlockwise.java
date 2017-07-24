package com.wation.driverhelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

//自定义倒计时器
@SuppressLint({ "ViewConstructor", "SimpleDateFormat" })
public class Anticlockwise extends TextView
{
	public Anticlockwise(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		mTimeFormat = new SimpleDateFormat("HH:mm:ss");
		mTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));//**TimeZone时区，加上这句
	}

	public void setText(long currentTimeMs) {
		super.setText(mTimeFormat.format(new Date(currentTimeMs)));
	}

	private SimpleDateFormat mTimeFormat;
	public static final int TYPE_UNKNOWN = 0;
	public static final int TYPE_PLAY = 1;
	public static final int TYPE_PAUSE = 2;

	private long time = 0; // 时间增量
	private boolean running = false; // 是否正在运行
	private boolean alarming = false; // 是否正在报警

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	private int type;
	
	public Anticlockwise(Context context)
	{
		super(context);
	}

	public void reset() {
		this.setText(mTimeFormat.format(new Date(0)));
	}

	public void setTime(long baseTime) {
		time = baseTime;
	}

	public void updateText() {
		if (running) {
			this.setText(mTimeFormat.format(new Date(time)));
		} else {
			this.setText(mTimeFormat.format(new Date(0)));
		}
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public long getTime() {
		return time;
	}

	public void timeAutoIncrement() {
		time += 1000;
	}

	public boolean isAlarming() {
		return alarming;
	}

	public void setAlarming(boolean alarming) {
		this.alarming = alarming;
	}
}