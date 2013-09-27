package com.example.service;

import java.util.HashMap;
import java.util.Map;

import android.R.integer;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract.Contacts.Data;

public class FileService {
	private DBOpenHelper openHelper;

	public FileService(Context context) {
		super();
		this.openHelper =new DBOpenHelper(context);//创建数据库对象
	}
	/**
	 * 保存每条线程已经下载的长度
	 * <b>方法描述：</b>	<br/>  
	 * @param path
	 * @param map   
	 * @exception	<br/> 
	 * @since  1.0.0
	 */
	public void save(String path,Map<Integer, Integer> map){
		SQLiteDatabase db=openHelper.getWritableDatabase();
		db.beginTransaction();
		try{
			for(Map.Entry<Integer, Integer> entry:map.entrySet()){
				db.execSQL("insert into filedownloadlog(downloadpath,threadid,downlength) values(?,?,?)",new Object[]{
						path,entry.getKey(),entry.getValue()});
			}
			db.setTransactionSuccessful();
		}finally{
			db.endTransaction();
		}
		db.close();
	}
	/**
	 * 获取每条线程已经下载的数据长度
	 * <b>方法描述：</b>	<br/>  
	 * @param path
	 * @return   
	 * @exception	<br/> 
	 * @since  1.0.0
	 */
	public Map<Integer, Integer> getData(String path){
		SQLiteDatabase db=openHelper.getReadableDatabase();
		Map<Integer, Integer> data=new HashMap<Integer, Integer>();
		Cursor cursor=db.rawQuery("select threadid,downlength from filedownloadlog where downloadpath=?", new String[]{path});
		while(cursor.moveToNext()){
			data.put(cursor.getInt(0), cursor.getInt(1));
		}
		cursor.close();
		db.close();
		return data;
	}
	/**
	 * 实时更新每条线程已经下载的文件长度
	 * <b>方法描述：</b>	<br/>  
	 * @param path
	 * @param threadid
	 * @param pos   
	 * @exception	<br/> 
	 * @since  1.0.0
	 */
	public void update(String path,int threadid,int pos){
		SQLiteDatabase db=openHelper.getWritableDatabase();
		db.execSQL("update filedownloadlog set downlength=? where downloadpath=? and threadid=?", new Object[]{pos,path,threadid});
		db.close();
	}
	/**
	 * 删除下载记录，当文件下载完成后
	 * <b>方法描述：</b>	<br/>  
	 * @param path   
	 * @exception	<br/> 
	 * @since  1.0.0
	 */
	public void delete(String path){
		SQLiteDatabase db=openHelper.getWritableDatabase();
		db.execSQL("delete from filedownloadlog where downloadpath=?",new Object[]{path});
		db.close();
		
	}
	

}
