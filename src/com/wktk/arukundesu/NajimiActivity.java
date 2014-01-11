package com.wktk.arukundesu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

/**
 * なじみ度を表示するAcrivityクラス
 */
public class NajimiActivity extends Activity{
	private int najimi;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_najimi);
		
		Intent intent = getIntent();
		najimi = intent.getIntExtra("najimi", 0);
		ProgressBar najimiBar = (ProgressBar) findViewById(R.id.najimi_progressbar);
		najimiBar.setProgress(najimi);
	}
}
