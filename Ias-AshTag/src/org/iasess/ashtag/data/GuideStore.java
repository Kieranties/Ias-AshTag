package org.iasess.ashtag.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.api.TaxonItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import au.com.bytecode.opencsv.CSVReader;

public class GuideStore extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "ASHTAG.db";
	private static final int DATABASE_VERSION = 12;
	
	private static final String TABLE_NAME = "guide";

	public static final String COL_PK = "_id"; // NEEDS to be called _id
												// otherwise cursors don't work
	public static final String COL_SOURCE = "source";
	public static final String COL_DETAIL = "detail";
	public static final String COL_TITLE = "title";
	public static final String COL_SMALL_IMAGE = "small_image";
	public static final String COL_LARGE_IMAGE = "large_image";

	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " + COL_PK + " integer primary key,"
												+ COL_DETAIL + " text, " + COL_SOURCE + " text, " + COL_TITLE
												+ " text, " + COL_SMALL_IMAGE + " text, " + COL_LARGE_IMAGE + " text);";

	
	public GuideStore(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public Cursor getAll() {

		return executeStringQuery("SELECT * FROM " + TABLE_NAME);
	}

	public Cursor getByPk(long pk) {
		return executeStringQuery("Select * FROM " + TABLE_NAME + " WHERE " + COL_PK + " = " + pk);

	}
	
	private Cursor executeStringQuery(String q) {
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(q, null);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		// create
		db.execSQL(TABLE_CREATE);
		
		// populate from file system		
		InputStream stream;
		try {
			stream = AshTagApp.getContext().getAssets().open("guide.csv");
		
			CSVReader reader = new CSVReader(new InputStreamReader(stream));
		    String [] nextLine = reader.readNext(); // read header
		    while ((nextLine = reader.readNext()) != null) {
		    	ContentValues values = new ContentValues();
		    	values.put(COL_SOURCE, nextLine[0]);
				values.put(COL_TITLE, nextLine[1]);
				values.put(COL_DETAIL, nextLine[2]);
				values.put(COL_SMALL_IMAGE, nextLine[3]);
				values.put(COL_LARGE_IMAGE, nextLine[4]);
				db.insert(TABLE_NAME, null, values);
		    }
		    reader.close();
		    stream.close();	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// drop and recreate db
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);		
	}
}
