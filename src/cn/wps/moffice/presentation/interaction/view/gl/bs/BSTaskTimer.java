package cn.wps.moffice.presentation.interaction.view.gl.bs;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;

public class BSTaskTimer {
	private Timer mTimer;
	private LinkedBlockingQueue<BSTimerTask> mTasks = new LinkedBlockingQueue<BSTimerTask>();
	
	public BSTaskTimer(String timerName) {
		mTimer = new Timer(timerName);
	}
	
	public void scheduleTask(BSTimerTask task, long delay) {
		task.mTimer = this;
		mTimer.schedule(task, delay);
		mTasks.add(task);
	}
	
	public int cancelAllTasks() {
		BSTimerTask task;
		Log.d("tt", "cancelAllTasks count " + mTasks.size());
		while ((task = mTasks.poll()) != null) {
			Log.d("tt", "cancel task " + task.hashCode());
			task.cancel();
		}
		
		return mTimer.purge();
	}
	
    public abstract static class BSTimerTask extends TimerTask {

    	private volatile boolean mActive = false;
		private volatile boolean mCancelled = false;
    	private BSTaskTimer mTimer;
    	
		public BSTimerTask() {
		}
		
    	@Override
    	public void run() {
    		mActive = true;
    		
    		runTask();
    		
    		mActive = false;
    		
    		if (mTimer != null)
    			mTimer.mTasks.remove(this);
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
			if (!mCancelled) {
				mCancelled  = true;
				super.cancel();
				
				return mCancelled;
			}
			
			return mCancelled;
		}
    }
}
