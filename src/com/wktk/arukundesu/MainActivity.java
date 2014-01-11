package com.wktk.arukundesu;

import java.util.Timer;
import java.util.TimerTask;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * 歩数計アクティビティのクラス
 */
public class MainActivity extends Activity {
	private ImageView mainImageView;
	private Timer mTimer;
	private Handler mHandler;
	private int slimeState;
	private float steps;
	private SurfaceView surfaceView;
	private BackGroundSurfaceView backGroundSurfaceView;
	private SensorManager mSensorManager;
	private SoundPool soundPool;
	private SoundPool clearSoundPool;
	private boolean isSoundPoolLoaded;
	private int stepSoundPoolId;
	private int clearSoundPoolId;
	private float mAccel;
	private float mAccelCurrent;
	private float mAccelLast;
	private final int mAccelSensitivity = 3; // 加速度センサーを利用する場合の感度の閾値

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mainImageView = (ImageView) findViewById(R.id.main_imageview);
		mainImageView.setImageResource(R.drawable.slime_1);
		surfaceView = (SurfaceView) findViewById(R.id.background_surfaceview);
		backGroundSurfaceView = new BackGroundSurfaceView(this, surfaceView);
		
		// バックグラウンドでの動作処理の実装を省略するため、画面を常時起動しておく
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		WakeLock lock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, getString(R.string.app_name));
		lock.acquire();
		//ここの間、画面をONのままにできる
		lock.release();
		
		// スライムを定期的に動かす
		int[] slimeResId = {R.drawable.slime_1, R.drawable.slime_2};
		animateSlime(slimeResId);
		
	    // 歩数を初期化
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    steps = 0;
	    
	    // soundPool
	    isSoundPoolLoaded = false;
	    soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
	    clearSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
	    soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
	        @Override
	        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
	            if (0 == status) {
	                isSoundPoolLoaded = true;
	            }else{
	            	isSoundPoolLoaded = false;
	            }
	        }
	    });
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(soundPool != null){
			stepSoundPoolId = soundPool.load(this, R.raw.katya, 1);
			clearSoundPoolId = clearSoundPool.load(this, R.raw.nc41828, 2);
		}
		Sensor countSensor = null;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
		}
        if (countSensor != null) {
            mSensorManager.registerListener(countSensorListener, countSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Toast.makeText(this, "このデバイスではステップカウンターを利用できません", Toast.LENGTH_LONG).show();
            // 歩数を感知するためのセンサー
    	    mSensorManager.registerListener(shakeSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
    	    mAccel = 0.00f;
    	    mAccelCurrent = SensorManager.GRAVITY_EARTH;
    	    mAccelLast = SensorManager.GRAVITY_EARTH;
            mSensorManager.registerListener(shakeSensorListener,
    				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
    				SensorManager.SENSOR_DELAY_GAME);
        }
	}

	@Override
	protected void onPause() {
		mSensorManager.unregisterListener(shakeSensorListener);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(soundPool != null){
			soundPool.release();
		}
		if(clearSoundPool != null){
			clearSoundPool.release();
		}
		if(mTimer != null) mTimer.cancel();
		mTimer = null;
		mHandler = null;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_najimi:
			Intent intent = new Intent(this, NajimiActivity.class);
			intent.putExtra("najimi", (int) steps);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * StepCounter用のリスナー
	 */
	private final SensorEventListener countSensorListener = new SensorEventListener() {
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			steps = event.values[0];
			if(!isFinishing()){
				if(isSoundPoolLoaded){
					// ゲームを進行させるための独自メソッド
					onStepCounted();
				}
			}
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			
		}
	};
	
	/**
	 * StepCounterが使えない場合の擬似歩数計測リスナー
	 * Android 4.3以下
	 * 参考:http://stackoverflow.com/questions/2317428/android-i-want-to-shake-it
	 */
	private final SensorEventListener shakeSensorListener = new SensorEventListener() {

		public void onSensorChanged(SensorEvent se) {
			float x = se.values[0];
			float y = se.values[1];
			float z = se.values[2];
			mAccelLast = mAccelCurrent;
			mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
			float delta = mAccelCurrent - mAccelLast;
			mAccel = mAccel * 0.9f + delta; // perform low-cut filter
			if(mAccel > mAccelSensitivity){
				if(!isFinishing()){
					if(isSoundPoolLoaded){
						// ゲームを進行させるための独自メソッド
						onStepCounted();
						steps++;
					}
				}
			}
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};
	
	/**
	 * スライムがネチョネチョ動くアニメーションを
	 * 一定間隔で行う
	 * @param resId
	 */
	private void animateSlime(final int ...resId){
		slimeState = 1;
		mHandler = new Handler();
		mTimer = new Timer(true);
		mTimer.schedule( new TimerTask(){
	        @Override
	        public void run() {
	            mHandler.post( new Runnable() {
	                public void run() {
	                	if(slimeState == 1){
	                		mainImageView.setImageResource(resId[1]);
	                		slimeState = 2;
	                	}else if(slimeState == 2){
	                		mainImageView.setImageResource(resId[0]);
	                		slimeState = 1;
	                	}else{
	                		mainImageView.setImageResource(resId[0]);
	                		slimeState = 1;
	                	}
	                	mainImageView.invalidate();
	                }
	            });
	        }
	    }, 3000, 3000);
	}
	
	/**
	 * 歩数に応じてイベントを発生させる
	 */
	private void onStepCounted(){
		soundPool.play(stepSoundPoolId, 1.0F, 1.0F, 0, 0, 1.0F);
		backGroundSurfaceView.doDraw();
		if(steps == 50){
			// 敵出現
			new EnemyDialogFragment().show(getFragmentManager(), "enemyDialog");
		}
		if(steps == 100){
			// ステージクリア
			onStageCleared();
		}
	}
	
	/**
	 * なじみ度が一定を超えた場合に進化する
	 */
	private void onStageCleared(){
		clearSoundPool.play(clearSoundPoolId, 1.0F, 1.0F, 0, 0, 1.0F);
		if(mTimer != null) mTimer.cancel();
		mTimer = null;
		mHandler = null;
		// 進化後のスライムのアニメーション
		int[] hoimiSlimeId = {R.drawable.hoimislime_1, R.drawable.hoimislime_2};
		animateSlime(hoimiSlimeId);
	}
}
