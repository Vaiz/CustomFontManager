package com.tetsu31415.customfont;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SelectFontActivity extends Activity implements DialogInterface.OnClickListener ,OnClickListener{
	
	EditText editText;
	Button button;
	ListView listView;
	ArrayAdapter<String> adapter;
	File thisDirectory;
	
	String fontfile;
	
	public static List<String> sortIgnoreCase(List<String> list) {
		List<String> r = new ArrayList<String>(list);
		Collections.sort(r, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		});
		return r;
	}
	
	public void onCreate(Bundle savedInstanceState){		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selectlist);
		if (Build.VERSION.SDK_INT>=8) {
			getWindow().setLayout(
					android.view.WindowManager.LayoutParams.MATCH_PARENT, 
					android.view.WindowManager.LayoutParams.MATCH_PARENT);
		}else {
			getWindow().setLayout(
					android.view.WindowManager.LayoutParams.FILL_PARENT, 
					android.view.WindowManager.LayoutParams.FILL_PARENT);
		}
		
		findViews();
		setListeners();
		thisDirectory = Environment.getExternalStorageDirectory();
		viewFileList(thisDirectory);		
	}
	
	protected void findViews() {
		listView = (ListView)findViewById(R.id.listView1);
		editText = (EditText)findViewById(R.id.editText1);
		button = (Button)findViewById(R.id.button1);
	}
	
	protected void setListeners() {
		button.setOnClickListener(this);
	}
	
	private void viewFileList(final File file){
		List<String> fList = new ArrayList<String>();
		List<String> dList = new ArrayList<String>();

		File[] Files = file.listFiles();
		try	{
			for(File f : Files){
				if (f.isHidden()) {
					//ignore
				}else if(f.isDirectory()){
					dList.add(f.getName());
				}else if(f.isFile()){
					fList.add(f.getName());
				}
			}
		}catch (Exception e){
			showToast(R.string.cant_open);
			return;
		}
		
		final List<String> fileList = sortIgnoreCase(fList);
		final List<String> dirList = sortIgnoreCase(dList);
		if (fileList.size()+dirList.size()==0) {
			showToast(R.string.empty_dir);
			return;
		}
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		thisDirectory = file;
		editText.setText(file.getAbsolutePath());
		
		for(String f : dirList){
			adapter.add("["+f+"]");
		}
		for(String f : fileList){
	        adapter.add(f);
	    }
		  
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent , View view , int position , long id) {
					File f;
					String filePath;
					if (position<dirList.size()) {
						filePath = file.getAbsolutePath()+"/"+dirList.get(position);
						f = new File(filePath);
						viewFileList(f);
					}else {
						fontfile =	file.getAbsolutePath()+"/"+fileList.get(position-dirList.size());
						viewFontFile();
					}
			}			
		});
	}

	private void toParent() {
		try {
			File file = new File(thisDirectory.getParent());
			viewFileList(file);
		} catch (Exception e) {
			finish();
		}
	}
	
	private void viewFontFile(){
		LayoutInflater inflater = LayoutInflater.from(SelectFontActivity.this);
		View view = inflater.inflate(R.layout.viewfont, null);
		TextView sampleTextView = (TextView)view.findViewById(R.id.textView1);
		sampleTextView.setText(getString(R.string.sample));
		try {
			Typeface typeface = Typeface.createFromFile(fontfile);
			sampleTextView.setTypeface(typeface);
			
		} catch (Exception e) {
			showToast(R.string.not_font);
			return;
		}
		
		new AlertDialog.Builder(this)
			.setTitle(R.string.font_preview)
			.setView(view)
			.setPositiveButton(android.R.string.ok, this)
			.setNegativeButton(android.R.string.cancel, this)
			.show();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_BACK) {
			toParent();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			Intent result = new Intent();
			result.putExtra("path", fontfile);
			setResult(RESULT_OK, result);
			finish();
		}
	}

	public void onClick(View v) {
		toParent();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("path", thisDirectory.getPath());
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		String path = savedInstanceState.getString("path");
		if (path!=null) {
			thisDirectory = new File(path);
			viewFileList(thisDirectory);
		}
	}
	
	private void showToast(int resId){
		Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
	}
}