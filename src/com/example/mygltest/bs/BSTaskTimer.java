package com.example.mygltest.bs;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

public class BSTaskTimer {
	private Timer mTimer;
	private LinkedBlockingQueue<BSTimerTask> mTasks = new LinkedBlockingQueue<BSTimerTask>();
	
	public BSTaskTimer(String timerName) {
		mTimer = new Timer(timerName);
	}
	
	public void scheduleTask(BSTimerTask task, long delay) {
		mTimer.schedule(task, delay);
		mTasks.add(task);
	}
	
	public int cancelAllTasks() {
		BSTimerTask task;
		while ((task = mTasks.poll()) != null) {
			task.cancel();
		}
		
		return mTimer.purge();
	}
	
    public abstract static class BSTimerTask extends TimerTask {

    	private volatile boolean mActive = false;
		private volatile boolean mCancelled = false;
    	
    	@Override
    	public void run() {
    		mActive = true;
    		
    		runTask();
    		
    		mActive = false;
    	}
    	
    	public abstract void runTask();
    	
		public boolean isActive() {
			return mActive;
		}
		
		public boolean cancelled() {
			return mCancelled;
		}
    	
		@Override
		public boolean cancel() {
			mCancelled  = true;
			
			return super.cancel();
		}
    }
}
