package com.tetsu31415.customfont;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

public class Commands {

	private final String TAG = "CUSTOMFONT";
	
	private Context context;
	
	private int manufacturer = -1;
	
	/**
	 * @param context 
	 */
	
	public Commands(Context context){
		this.context = context;
		checkManufacturer();
	}
	
	/**
	 * 
	 * Set manufacturer.
	 * -1: other 
	 *  0: sharp
	 *  1: fujitsu 
	 */
		
	private void checkManufacturer() {
		if (getCondition()==-1) {
			return;
		}
		String manu = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (manu.equals("SHARP")) {
			manufacturer = 0;
		}else if (manu.contains("FUJITSU") || model.matches("[FT].+|.+[FT]")) {
			manufacturer = 1;
		}
	}
	
	/**
	 * @return manufacturer
	 * -1: other 
	 *  0: sharp
	 *  1: fujitsu 
	 */
	
	public int getManufacturer() {
		return manufacturer;
	}
	
	/**
	 * @return condition
	 * 
	 * -1: not supported
	 *  0: not exist customfont
	 *  1: exist customfont
	 *    
	 */
	
	public int getCondition(){
		int result = -1;
		if (new File(Strings.FONT_DIR).exists()) {
			result = 0;
		}else {
			return result;
		}		
		if (manufacturer==0 && new File(Strings.FONT_PATH_SHARP).exists()) {
			return 1;
		}
		if (manufacturer==1 && new File(Strings.FONT_PATH_FUJITSU).exists()) {
			return 1;
		}		
		return result;		
	}
	
	/**
	 * Return File object
	 * The path is "/data/data/[package-name]/files/"+name
	 * @param name File name
	 * @return file 
	 * 
	 */
	
	public File getFilePath(String name) {
		File file = new File(context.getFilesDir(),name);
		return file;
	}
	
	/**
	 * Load fakefont from assets to data-directory.
	 */
	
	public void loadFakefont(){				
		try{
			AssetManager am = context.getResources().getAssets();
			InputStream is = 
					am.open(Strings.FAKE_FONT_ZIP,AssetManager.ACCESS_STREAMING);
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry ze = zis.getNextEntry();
			
			if (ze != null) {
				FileOutputStream fos = 
						new FileOutputStream(getFilePath(Strings.FAKE_FONT_FILE), false);
				byte[] buffer = new byte[1024];
				int size = 0;
				
				while((size = zis.read(buffer, 0, buffer.length)) > -1){
					fos.write(buffer, 0, size);
				}
				fos.close();
				zis.closeEntry();
			}
			zis.close();
			is.close();
		}catch(Exception e){
			e.printStackTrace();
		}	
	}
	
	/**
	 * Copy from inputFile to outputFile
	 * @param inputFile input file 
	 * @param outputFile output file
	 * @return true if it succeeded
	 */
	
	public boolean copyFile(File inputFile , File outputFile){
		try {
			Log.v(TAG, inputFile.getPath()+" -> "+outputFile.getPath());
            FileInputStream fis = new FileInputStream(inputFile.getPath());
            FileOutputStream fos = new FileOutputStream(outputFile.getPath());

            byte buffer[] = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
            fos.close();
            fis.close();
            Log.v(TAG, "Copy Success!");
			return true;
		
		} catch (Exception e) {
			e.printStackTrace();
            Log.v(TAG, "Copy Failed");
			return false;
		}		
	}
	
	/**
	 * Delete custom font 
	 * @return true if it succeeded
	 */
	
	public boolean deleteCustomFont() {
		File file;
		if (manufacturer==0) {
			file = new File(Strings.FONT_PATH_SHARP);
		}else {
			file = new File(Strings.FONT_PATH_FUJITSU);
		}
		
		if (file.exists()) {
			return file.delete();
		}
		return true;
	}
	
	public String execCommand(String commd) {
        Runtime runtime = Runtime.getRuntime();
        Process process;
        String output = "", line = "";
        BufferedReader reader;
        try {
            process = runtime.exec(commd);
            reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null) {
                output += line + "\n";
            }
            reader.close();
            process.waitFor();
        } catch (IOException e) {
        	e.printStackTrace();
        } catch (InterruptedException e){ 
        	e.printStackTrace();
        }
        return output;
    }
	
	/**
	 * Set HTML to TextView
	 * @param textView
	 */
	
	public void setHtmlToTextView(TextView textView){		
		boolean isJapanese = Locale.JAPAN.equals(Locale.getDefault());
		StringBuilder sb = new StringBuilder();
		/* For Sharp devices */
		if (manufacturer==0) {
			sb.append(Strings.SHARP);
		} else if(manufacturer==1) { /* For Fujitsu devices */
			sb.append(Strings.FUJITSU);
		} else {
			sb.append("not_supported");
		}
		
		if (isJapanese) {			
			sb.append("-ja");
		}
		
		Spanned spanned = getHtmlFromAssets(sb.append(".html").toString());
		if (spanned!=null) {			
			textView.setText(spanned);
			textView.setMovementMethod(LinkMovementMethod.getInstance());
		}
	}
	
	/**
	 * Load HTML file
	 * @param fileName HTML file path
	 * @return spanned
	 */
	
	public Spanned getHtmlFromAssets(String fileName){
		AssetManager as = context.getResources().getAssets();
		InputStream is = null;
		BufferedReader br = null;
				
		StringBuilder sb = new StringBuilder();
		try {
			try {
				is = as.open(fileName);
				br = new BufferedReader(new InputStreamReader(is));
				String tmp;
				while((tmp = br.readLine()) !=null){
					sb.append(tmp);
				}
				
			} finally{
				if(is!=null){
					is.close();
				}				
				if (br != null) {
					br.close();
				}
			}
		} catch (IOException e) {
			return null;
		}
		
		return Html.fromHtml(sb.toString());
	}
	
	/**
	 * Get application version name
	 * @return version name
	 */
	
	public String getAppVersionName(){
		try {
			PackageInfo info = context.getPackageManager().
					getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			return info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
}
