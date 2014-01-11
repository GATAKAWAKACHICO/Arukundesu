package com.wktk.arukundesu;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class BackGroundSurfaceView extends SurfaceView implements Callback {
	private SurfaceHolder holder;
	private Paint paint;
	private Resources res = this.getContext().getResources();
	private Bitmap grass_left = BitmapFactory.decodeResource(res,
			R.drawable.grass_left);
	private Bitmap grass_right = BitmapFactory.decodeResource(res,
			R.drawable.grass_right);
	private int y1, y2;
	private int surfaceview_width, surfaceview_height;

	public BackGroundSurfaceView(Context context) {
		super(context);
	}

	public BackGroundSurfaceView(Context context, SurfaceView surfaceView) {
		super(context);
		holder = surfaceView.getHolder();
		holder.addCallback(this);
		setFocusable(true);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		surfaceview_width = width;
		surfaceview_height = height;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		paint = new Paint();
		Canvas canvas = holder.lockCanvas();
		canvas.drawColor(Color.WHITE);
		// 芝を表示するy座標
		y1 = 0;
		y2 = grass_left.getHeight();
		holder.unlockCanvasAndPost(canvas);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// thread = null;
	}

	public void doDraw(){
		if (holder != null) {
			if (y2 > 0) {
				y1 = y1 - 5;
				y2 = y2 - 5;
			} else if (y2 <= 0){
				y1 = 0;
				y2 = grass_left.getHeight();
			}
			Canvas canvas = holder.lockCanvas();
			if (canvas != null) {
				canvas.drawColor(Color.WHITE);
				// 左の芝の設定
				canvas.drawBitmap(grass_left, 0, y1, paint);
				canvas.drawBitmap(grass_left, 0, y2, paint);
				// 右の芝の設定
				canvas.drawBitmap(grass_right,
						surfaceview_width - grass_right.getWidth(), y1, paint);
				canvas.drawBitmap(grass_right,
						surfaceview_width - grass_right.getWidth(), y2, paint);
				holder.unlockCanvasAndPost(canvas);
			}
		}
	}
}
