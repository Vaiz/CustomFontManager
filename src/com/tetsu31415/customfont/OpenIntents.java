package com.tetsu31415.customfont;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

public class OpenIntents {
	
	private Activity activity;
	
	public OpenIntents(Activity activity) {
		this.activity = activity;		
	}
	
	public boolean openSharpFontSetting(boolean ifShowToast){
		int resId = R.string.select_custom;
		Intent intent = new Intent(Intent.ACTION_MAIN);
		/* First intent */
		intent.setClassName("jp.co.sharp.android.downloadfont",
					"jp.co.sharp.android.downloadfont.DLFontManager.DLFontManagerActivity");
		boolean success = startActivity(intent, resId, ifShowToast);
		if (success) return true;
		/* Second intent */
		intent.setClassName("com.android.settings",
					"com.android.settings.DisplayFontTypeSettings");
		success = startActivity(intent, resId, ifShowToast);
		if (success) return true;
		/* Third intent */
		intent.setClassName("com.android.settings",
					"com.android.settings.display.DisplaySettings");
		success = startActivity(intent, resId, ifShowToast);
		if (success) return true;
		
		openFailed();
		return false;
	}
	
	public boolean openFujitsuFontSetting(){
		int resId = R.string.select_handmade_font;
		Intent intent = new Intent(Intent.ACTION_MAIN);
		/* First intent */
		intent.setClassName("com.android.settings",
				"com.android.settings.fujitsu.fontsetting.Font_Setting_ProductCom");
		boolean success = startActivity(intent, resId);		
		if(success) return true;
		/* Second intent */
		intent.setClassName("com.android.settings",
				"com.android.settings.toshiba.fontsetting.Font_Setting_ProductCom");
		success = startActivity(intent, resId);
		if(success) return true;
		
		openFailed();
		return false;
	}
	
	public boolean openFujitsuHandmadeFontSetting(){
		Intent intent = new Intent();
		intent.setClassName("com.fujitsu.mobile_phone.myfont", 
				"com.fujitsu.mobile_phone.myfont.MyFontStartCheckActivity");
		if (startActivity(intent, R.string.delete_handwriting_font)) {
			return true;
		}
		openFailed();
		return false;
	}
	
	private boolean startActivity(Intent intent, int resId){
		return startActivity(intent, resId, true);
	}
	
	/**
	 * 
	 * @param intent
	 * @param resId
	 * @param ifShowToast
	 * @return true if it succeeded
	 */
	
	private boolean startActivity(Intent intent, int resId, boolean ifShowToast){
		try {
			activity.startActivityForResult(intent, 2);
			if (ifShowToast) showToast(resId);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void showToast(int resId) {
		Toast.makeText(activity, activity.getString(resId), Toast.LENGTH_LONG).show();
	}
	
	private void openFailed() {
		showToast(R.string.cant_open);
	}
	
}
