package com.authentication.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/***
 * 鍗曚緥妯″紡瀹炵幇鏁版嵁搴撹繛鎺�
 * @author bobo
 *
 */
public class Dbhelper extends SQLiteOpenHelper {

	private static Dbhelper dbhelper = null;

	public static Dbhelper getInstens(Context context) {
		if (dbhelper == null) {
			dbhelper = new Dbhelper(context);
		}
		return dbhelper;
	}

	private Dbhelper(Context context) {
		super(context, "finger_fbi.db", null, 1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
		 //杩欏紶琛ㄩ噰鐢ㄤ簩杩涘埗鏂囦欢瀛樺偍瀵硅薄娉ㄦ剰绗簩涓瓧娈垫垜浠皢瀵硅薄瀛樺彇鍦ㄨ繖閲岄潰
		String sql_class_table="create table if not exists finger(_id integer primary key autoincrement,template text)";
		db.execSQL(sql_class_table);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldversion, int newversion) {
		// TODO Auto-generated method stub

	}

}
