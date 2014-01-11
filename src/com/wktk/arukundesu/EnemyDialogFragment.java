package com.wktk.arukundesu;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.app.DialogFragment;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class EnemyDialogFragment extends DialogFragment {
	private Dialog dialog;
	private TextView battleDescriptionTextView;
	private ImageView fightImageview;
	private SoundPool soundPool;
	private int battleSoundPoolId;
	private int cheerUpSoundPoolId;
	private int battleWinSoundPoolId;
	private int cheerUpButtonPushed;
	private Timer mTimer;
	private Handler mHandler;
	private int slimeState;
	private static int i;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		battleSoundPoolId = soundPool.load(getActivity(),
				R.raw.se_maoudamashii_retro21, 1);
		cheerUpSoundPoolId = soundPool.load(getActivity(),
				R.raw.se_maoudamashii_onepoint20, 1);
		battleWinSoundPoolId = soundPool.load(getActivity(),
				R.raw.se_maoudamashii_system49, 1);
		cheerUpButtonPushed = 0;
		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				if (0 == status) {
					soundPool.play(battleSoundPoolId, 1.0F, 1.0F, 0, 0, 1.0F);
				}
			}
		});
		dialog = new Dialog(getActivity());
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		// フルスクリーン表示にする
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		dialog.setContentView(R.layout.enemy_dialog);
		dialog.setTitle("敵出現");

		battleDescriptionTextView = (TextView) dialog.findViewById(R.id.battle_description_textview);
		fightImageview = (ImageView) dialog.findViewById(R.id.fight_imageview);
		// 応援するボタンのリスナ
		dialog.findViewById(R.id.cheer_up_button).setOnTouchListener(
				new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN) {
							soundPool.play(cheerUpSoundPoolId, 1.0F, 1.0F, 0,
									0, 1.0F);
							fightImageview.setImageResource(R.drawable.fight_2);
							cheerUpButtonPushed++;
						} else {
							fightImageview.setImageResource(R.drawable.fight_1);
							if (cheerUpButtonPushed >= 5) {
								v.setEnabled(false);
								mHandler = new Handler();
								mTimer = new Timer(true);
								i = 0;
								mTimer.schedule(new TimerTask() {
									@Override
									public void run() {
										mHandler.post(new Runnable() {
											public void run() {
												if (i <= 6) {
													if (slimeState == 1) {
														fightImageview
																.setImageResource(R.drawable.fight_3);
														slimeState = 2;
													} else if (slimeState == 2) {
														fightImageview
																.setImageResource(R.drawable.fight_1);
														slimeState = 1;
													} else {
														fightImageview
																.setImageResource(R.drawable.fight_1);
														slimeState = 1;
													}
													fightImageview.invalidate();
													i = i + 1;
												}else{
													mTimer.cancel();
													mTimer = null;
													mHandler = null;
													slimeState = 1;
													// スライムが敵に勝利して喜ぶアニメーション
													soundPool.play(battleWinSoundPoolId, 1.0F, 1.0F, 0, 0, 1.0F);
													battleDescriptionTextView.setText(getString(R.string.battle_win_description));
													mHandler = new Handler();
													mTimer = new Timer(true);
													i = 0;
													mTimer.schedule(new TimerTask() {
														@Override
														public void run() {
															mHandler.post(new Runnable() {
																public void run() {
																	if (i <= 6) {
																		if (slimeState == 1) {
																			fightImageview
																					.setImageResource(R.drawable.delight_left);
																			slimeState = 2;
																		} else if (slimeState == 2) {
																			fightImageview
																					.setImageResource(R.drawable.delight_right);
																			slimeState = 1;
																		} else {
																			fightImageview
																					.setImageResource(R.drawable.slime_1);
																			slimeState = 1;
																		}
																		fightImageview.invalidate();
																		i = i + 1;
																	}else{
																		mTimer.cancel();
																		mTimer = null;
																		slimeState = 1;	
																		if(dialog != null && !getActivity().isFinishing()){
																			dialog.dismiss();
																		}
																	}
																}
															});
														}
													}, 500, 500);
												}
											}
										});
									}
								}, 500, 500);
							}
						}
						return false;
					}
				});

		return dialog;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (soundPool != null) {
			soundPool.release();
		}
	}
}
