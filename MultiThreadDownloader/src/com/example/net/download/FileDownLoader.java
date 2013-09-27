package com.example.net.download;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.service.FileService;

import android.R.integer;
import android.content.Context;
import android.util.Log;

public class FileDownLoader {
	private Context context;
	private File saveFile;//本地保存文件
	private String downloadurl;//下载路径
	private int fileSize=0;//文件大小
	private int downloadSize=0;//已经下载的文件大小
	private FileService fileService; //下载记录操作的业务对象
	private DownloadThread[]  threads;
	private int block;//每条线程下载的数据长度
	private Map<Integer, Integer> data=new ConcurrentHashMap<Integer, Integer>();//缓存各线程下载的长度
	

	/**  
	 * <b>方法描述：</b>	<br/>  
	 * @param args   
	 * @exception	<br/> 
	 * @since  1.0.0  
	 */
	public FileDownLoader(Context context,String downloadurl,File savedirFile,int thredNum){
		try {
			this.downloadurl=downloadurl;
			URL url=new URL(downloadurl);
			threads=new DownloadThread[thredNum];
			this.context=context;
			HttpURLConnection connection=(HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(5000);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
			connection.setRequestProperty("Accept-Language", "zh-CN");
			connection.setRequestProperty("Charset", "UTF-8");
			connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.connect();
			if(connection.getResponseCode()==200){
				fileSize=connection.getContentLength();
				block=(fileSize % threads.length)==0?fileSize / threads.length:fileSize/threads.length+1;//计算每条线程下载的数据长度
				String fileName=downloadurl.substring(downloadurl.lastIndexOf("/")+1);//获得文件名
				this.saveFile=new File(savedirFile, fileName);
				fileService=new FileService(context);
				Map<Integer,Integer> logdata=fileService.getData(downloadurl);
				if(logdata.size()>0){//如果存在下载记录
					for(Map.Entry<Integer, Integer> entry: logdata.entrySet()){
						data.put(entry.getKey(), entry.getValue());//把各条线程已经下载的数据长度放入data中。
					}
				}
				if(data.size()==threads.length){//计算所有线程已经下载的数据总长度
					for(int i=0;i<threads.length;i++){
						downloadSize+=data.get(i+1);
					}
					Log.i("downloadService","已经下载的数据长度"+downloadSize);
				}
				
				
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 开始下载文件
	 * <b>方法描述：</b>	<br/>  
	 * @param listner
	 * @return   
	 * @exception	<br/> 
	 * @since  1.0.0
	 */
	public int download(DownloadProgressListner listner){
		try{
			RandomAccessFile accessFile=new RandomAccessFile(saveFile, "rw");
			if(fileSize>0){
				accessFile.setLength(fileSize);
			}
			accessFile.close();
			URL url=new URL(downloadurl);
			if(data.size()!=threads.length){//如果原先下载的线程数和现在下载的线程数不一致
				this.data.clear();//对原来的map集合清空
				for(int i=0;i<threads.length;i++){
					data.put(i+1, 0);//初始化每天线程已经下载的数据长度为0
				}
				downloadSize=0;
			}
			for(int i=0;i<threads.length;i++){//开启线程进行下载
				int downloadlength=this.data.get(i+1);//给当前的第i个线程赋下载长度。
				if(downloadlength < block && downloadSize < fileSize){//判断线程是否已完成下载，负责继续下载
					this.threads[i]=new DownloadThread(this, block, saveFile, i+1, url, this.data.get(i+1));//线程Id从1开始
					this.threads[i].setPriority(7);
					this.threads[i].start();
				}else{
					this.threads[i]=null;
				}
			}
			fileService.delete(downloadurl);
			fileService.save(downloadurl, data);
			boolean notFinish=true;//下载未完成。
			while(notFinish){
				Thread.sleep(900);
				for(int i=0;i<threads.length;i++){
					if(this.threads[i]!=null && !this.threads[i].isfinish()){
						notFinish=true;
						if(threads[i].getDownloadLength()==-1){//如果下载失败，再重新下载。
							this.threads[i]=new DownloadThread(this, block, saveFile, i+1, url, this.data.get(i+1));
							this.threads[i].setPriority(7);
							this.threads[i].start();
						}
						
					}
				}
				if(listner!=null){
					listner.onDownloadSize(downloadSize);
				}
				
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return downloadSize;
	}
	/**
	 * 更新指定线程最后下载的位置 方法前要加上synchronized
	 * <b>方法描述：</b>	<br/>  
	 * @param threadId
	 * @param downlength   
	 * @exception	<br/> 
	 * @since  1.0.0
	 */
	public synchronized  void update(int threadId,int downlength){
		this.data.put(threadId, downlength);
		fileService.update(this.downloadurl, threadId, downlength);
	}
	/**
	 * 累计已经下载大小
	 * <b>方法描述：</b>	<br/>  
	 * @param size   
	 * @exception	<br/> 
	 * @since  1.0.0
	 */
	public synchronized void  append(int size){
		this.downloadSize+=size;
	}

}
