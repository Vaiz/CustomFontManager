package com.tetsu31415.customfont;

import java.io.File;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class FontInstallService extends Service{
	private BroadcastReceiver receiver;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		receiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				Commands commands = new Commands(context);
				
				File inputFile = commands.getFilePath(Strings.CUSTOM_FONT_FILE);
				File outputFile;
				if (commands.getManufacturer()==0) {
					outputFile = new File(Strings.FONT_PATH_SHARP);
				}else {
					outputFile = new File(Strings.FONT_PATH_FUJITSU);
				}
				commands.copyFile(inputFile, outputFile);
				commands.execCommand("rm "+inputFile.getPath());
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SHUTDOWN);
		registerReceiver(receiver, filter);		
	}
	
}
