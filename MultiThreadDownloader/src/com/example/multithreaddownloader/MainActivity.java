package com.example.multithreaddownloader;

import java.io.File;
import java.net.NetPermission;

import com.example.net.download.DownloadProgressListner;
import com.example.net.download.FileDownLoader;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.R.integer;
import android.app.Activity;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private EditText pathText;
	private TextView percenText;
	private Button downloadButton;
	private ProgressBar progressBar;
	private Button stopButton;
	private Handler handler=new UIHandler();
	private class UIHandler extends Handler{

		@Override
		public void handleMessage(Message msg) {
			int size=msg.getData().getInt("size");
			progressBar.setProgress(size);
			float num=(float)progressBar.getProgress()/(float)progressBar.getMax();
			int result=(int)(num*100);
			percenText.setText(result+"%");
			if(progressBar.getProgress()==progressBar.getMax()){
				Toast.makeText(getApplicationContext(), R.string.success, 1).show();
			}
			
		}
		
		
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		pathText=(EditText) findViewById(R.id.path);
		downloadButton=(Button) findViewById(R.id.button);
		progressBar=(ProgressBar) findViewById(R.id.progressbar);
		stopButton=(Button) findViewById(R.id.stopbutton);
		percenText=(TextView) findViewById(R.id.percent);
		downloadButton.setOnClickListener(new buttonClickListner());
		stopButton.setOnClickListener(new buttonClickListner());
	}

	private class buttonClickListner implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.button:
				String path=pathText.getText().toString();
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){//判断sd卡是否可以写数据
					File savedir=Environment.getExternalStorageDirectory();
					download(path,savedir);
				}else{
					Toast.makeText(getApplicationContext(), R.string.sderror, 1).show();
				}
				break;
			case R.id.stopbutton:
				
				break;

			default:
				break;
			}
			
			
		}
		private DownloadTask task;
		/*
		由于用户的输入事件(点击button, 触摸屏幕....)是由主线程负责处理的，如果主线程处于工作状态，
		此时用户产生的输入事件如果没能在5秒内得到处理，系统就会报“应用无响应”错误。
		所以在主线程里不能执行一件比较耗时的工作，否则会因主线程阻塞而无法处理用户的输入事件，
		导致“应用无响应”错误的出现。耗时的工作应该在子线程里执行。*/
		private void download(String path, File savedir) {
			task=new DownloadTask(path,savedir);
			new Thread(task).start();
		}
		private class DownloadTask implements Runnable{
			private String path;
			private File savedir;
			public DownloadTask(String path, File savedir) {
				super();
				this.path = path;
				this.savedir = savedir;
			}
			@Override
			public void run() {
				FileDownLoader fileDownLoader=new FileDownLoader(getApplicationContext(), path, savedir, 3);
				fileDownLoader.download(new DownloadProgressListner() {
					@Override
					public void onDownloadSize(int size) {
						Message msMessage=new Message();
						msMessage.what=1;
						msMessage.getData().putInt("size", size);
						handler.sendMessage(msMessage);
					}
				});
				
			}
			
		}
		
	}

}
