package com.tetsu31415.customfont;

import java.io.File;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity 
	implements DialogInterface.OnClickListener, OnClickListener{

	private Commands commands;
	private OpenIntents openIntents;
	
	private Handler handler;
	
	private Button selectButton, settingButton, deleteButton;
	private TextView infoTextView, descriptionTextView;
	
	int condition;
	
	boolean isDeleteMode = false;
	
	String path;

	/*
	 * condition
	 * 
	 * -1:not supported
	 *  0:no custom font
	 *  1:exist custom font
	 *  2:waiting for activity result    
	 */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		commands = new Commands(this);
		openIntents = new OpenIntents(this);
		handler = new Handler();
		if (Build.VERSION.SDK_INT>10) {
			new NewApiMethod().setHomeButton(this);
		}
		findViews();
		setListeners();
		checkDevice();
		commands.setHtmlToTextView(descriptionTextView);
	}

	private void findViews(){
		selectButton = (Button)findViewById(R.id.button1);
		settingButton = (Button)findViewById(R.id.button2);
		deleteButton = (Button)findViewById(R.id.button3);
		
		infoTextView = (TextView)findViewById(R.id.textView1);
		descriptionTextView = (TextView)findViewById(R.id.textView3);
	}	
	
	private void setListeners(){
		selectButton.setOnClickListener(this);
		settingButton.setOnClickListener(this);
		deleteButton.setOnClickListener(this);
	}
	
	private void checkDevice(){	
		StringBuilder builder = new StringBuilder();
		builder.append("Device : ").append(Build.MODEL)
			.append(" (").append(Build.VERSION.RELEASE).append(")");
		infoTextView.setText(builder);
		
		condition = commands.getCondition();
		
		if(condition!=-1){ //device is OK
			selectButton.setEnabled(true);
			settingButton.setEnabled(true);
			if (condition == 1){ // customfont is OK
				deleteButton.setEnabled(true);
			}
			
			if (!commands.getFilePath(Strings.FAKE_FONT_FILE).exists()) {
				new MyTask(MyTask.MODE_LOAD_FAKEFONT).execute();
			}
			
		}else{
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
			dialogBuilder.setMessage(R.string.not_support);
			dialogBuilder.setNegativeButton(android.R.string.ok, this);
			dialogBuilder.show();
		}	
	}
		
	public void onClick(DialogInterface dialog, int which) {
		if (which==DialogInterface.BUTTON_POSITIVE) {
			if (isDeleteMode) {
				isDeleteMode = false;
				if (commands.getManufacturer()==0) {
					MyTask hogeTask = new MyTask(MyTask.MODE_DELETE_CUSTOMFONT);
					hogeTask.execute();
				}else {
					condition = 2;
					if (!openIntents.openFujitsuHandmadeFontSetting()) {
						condition = 1;
					}
				}				
			}else {
				MyTask hogeTask = new MyTask(MyTask.MODE_LOAD_CUSTOMFONT);
				hogeTask.setFilePath(path);
				hogeTask.execute();
			}
		}else {
			isDeleteMode = false;
		}
	}

	public void onClick(View v) {
		if (v==selectButton) {
			Intent intent = new Intent(this, SelectFontActivity.class);
			startActivityForResult(intent, 1);
		} else if (v==settingButton) {
			if (commands.getManufacturer()==0) {
				openIntents.openSharpFontSetting(true);
			}else {
				openIntents.openFujitsuFontSetting();
			}
		} else { // v==deleteButton
			isDeleteMode = true;
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
			dialogBuilder.setMessage(R.string.do_you_delete);
			dialogBuilder.setPositiveButton(android.R.string.ok, this);
			dialogBuilder.setNegativeButton(android.R.string.cancel, this);
			dialogBuilder.setCancelable(false);
			dialogBuilder.show();
		}
		
	}
	

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==1 && resultCode == RESULT_OK){
			path = data.getStringExtra("path");
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
			dialogBuilder.setMessage(R.string.do_you_install);
			dialogBuilder.setPositiveButton(android.R.string.ok, this);
			dialogBuilder.setNegativeButton(android.R.string.cancel, this);
			dialogBuilder.setCancelable(false);
			dialogBuilder.show();
		}
		if (requestCode==2 && condition==2) {
			if (commands.getManufacturer()==0) {
				new MyTask(MyTask.MODE_REBOOT).execute();			
			}else{
				new MyTask(MyTask.MODE_DELETE_CUSTOMFONT).execute();
			}
		}
	}
	
	private class MyTask extends AsyncTask<Void, Void, Void>{
		
		public final static int MODE_LOAD_FAKEFONT = 0;
		public final static int MODE_LOAD_CUSTOMFONT = 1;
		public final static int MODE_REBOOT = 2;
		public final static int MODE_DELETE_CUSTOMFONT = 3;
		ProgressDialog dialog;
		
		int mode = -1;
		
		String filePath;
		
		public MyTask(int mode) {
			this.mode = mode; 
		}
		
		public void setFilePath(String path) {
			filePath = path;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(MainActivity.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setCancelable(false);
			dialog.setCanceledOnTouchOutside(false);
			dialog.setTitle(R.string.please_wait);
			dialog.setMessage("");
			dialog.show();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			if (mode==MODE_LOAD_FAKEFONT) { //loadFake
				setDialogMessage(R.string.loading);
				commands.loadFakefont();
			}else if (mode==MODE_LOAD_CUSTOMFONT) { //setFakeFont
				setDialogMessage(R.string.loading);
				commands.copyFile(new File(filePath), commands.getFilePath(Strings.CUSTOM_FONT_FILE));
				/* copy fake font for sharp devices */
				if (commands.getManufacturer()==0) {
					commands.deleteCustomFont();
					commands.copyFile(commands.getFilePath(Strings.FAKE_FONT_FILE), new File(Strings.FONT_PATH_SHARP));
					commands.execCommand("chmod 666 "+Strings.FONT_PATH_SHARP);
				}				
			}else if (mode==MODE_REBOOT) { //Reboot
				setDialogTitle(R.string.reboot);
				setDialogMessage(R.string.please_reboot);
			}else if (mode==MODE_DELETE_CUSTOMFONT) { //Delete
				setDialogMessage(R.string.deleting);
				commands.deleteCustomFont();
			}
			
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (mode==MODE_LOAD_CUSTOMFONT){
				condition = 2;
				Intent service = new Intent(MainActivity.this, FontInstallService.class);
				startService(service);
				if (commands.getManufacturer()==1) {
					setDialogTitle(R.string.reboot);
					setDialogMessage(R.string.please_reboot);
					return;
				}
			}else if(mode==MODE_REBOOT){
				Log.v("CUSTOMFONT", "END");
				return;
			}else if (mode==MODE_DELETE_CUSTOMFONT) {
				condition = 0;
				deleteButton.setEnabled(false);
				Intent service = new Intent(MainActivity.this, FontInstallService.class);
				stopService(service);
				openIntents.openSharpFontSetting(false);	
			}
			dialog.dismiss();
		}
		
		private void setDialogTitle(final int resId){
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					dialog.setTitle(resId);
				}
			});
		}
		
		private void setDialogMessage(final int resId){
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					dialog.setMessage(MainActivity.this.getString(resId));
				}
			});
			
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("cond", condition);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		condition = savedInstanceState.getInt("cond",-1);
	}
		
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);        
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			showAbout();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
				showAbout();
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}
	
	private void showAbout() {
		StringBuilder sb = new StringBuilder();
		sb.append(getString(R.string.developer)).append("\n");
		sb.append(getString(R.string.version_str)).append(commands.getAppVersionName());
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle(R.string.app_name);
		builder.setCancelable(true);
		builder.setMessage(sb);
		builder.show();
	}

}





