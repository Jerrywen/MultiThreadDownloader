package com.example.net.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadThread extends Thread{
	private int block;
	private File saveFile;
	private int threadId;
	private URL downUrl;
	private int downLength;
	private FileDownLoader fileDownLoader;
	private boolean finish=false;
	public DownloadThread(FileDownLoader fileDownLoader,int block, File saveFile, int threadId, URL downUrl,
			int downLength) {
		super();
		this.block = block;
		this.saveFile = saveFile;
		this.threadId = threadId;
		this.downUrl = downUrl;
		this.downLength = downLength;
		this.fileDownLoader = fileDownLoader;
	}
	@Override
	public void run() {
		if(downLength < block){
			try {
				HttpURLConnection connection=(HttpURLConnection) downUrl.openConnection();
				connection.setConnectTimeout(5000);
				connection.setRequestMethod("GET");
				connection.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
				connection.setRequestProperty("Accept-Language", "zh-CN");
				connection.setRequestProperty("Referer", downUrl.toString()); 
				connection.setRequestProperty("Charset", "UTF-8");
				int startPos=block*(threadId-1)+downLength;//线程开始下载位置
				int endPos=block*threadId-1;//线程结束下载位置。
				connection.setRequestProperty("Range", "bytes="+startPos+"-"+endPos);
				connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
				connection.setRequestProperty("Connection", "Keep-Alive");
				InputStream inputStream=connection.getInputStream();
				byte[] buffer=new byte[1024];
				int len=0;
				RandomAccessFile accessFile=new RandomAccessFile(saveFile, "rwd");
				accessFile.seek(startPos);
				while((len=inputStream.read(buffer))!=-1){
					accessFile.write(buffer,0,len);
					downLength+=len;
					fileDownLoader.update(this.threadId, len);
					fileDownLoader.append(len);
				}
				accessFile.close();
				inputStream.close();
				finish=true;
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	/**
	 * 获得当前线程已经下载的大小
	 * <b>方法描述：</b>	<br/>  
	 * @return   
	 * @exception	<br/> 
	 * @since  1.0.0
	 */
	public long getDownloadLength(){
		return downLength;
	}
	public boolean isfinish(){
		return finish;
	}
	
	

}
