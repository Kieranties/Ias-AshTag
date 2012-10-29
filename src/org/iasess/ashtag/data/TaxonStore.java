package org.iasess.ashtag.data;

import java.util.ArrayList;

import org.iasess.ashtag.api.TaxonItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TaxonStore extends IasDatabase {
	
	/**
     * Database table for taxon store
     */
    private static final String TABLE_NAME = "taxon";
    
    /**
     * The column name for the primary key of a taxa
     */
    public static final String COL_PK  = "_id"; // NEEDS to be called _id otherwise cursors don't work

    public static final String COL_SOURCE = "source";

    public static final String COL_DETAIL = "detail";

    public static final String COL_TITLE = "title";
    
    private static final String TABLE_CREATE =
    		"CREATE TABLE " + TABLE_NAME 
    		+ " ( " 
    		+ COL_PK + " integer primary key,"
            + COL_DETAIL + " text, " 
    		+ COL_SOURCE + " text, "
    		+ COL_TITLE + " text);";

    public TaxonStore(Context context) {
		super(context);
	}
    
    public static void create(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

	public static void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//drop and recreate db
		recreate(db);	
	}
		
	public static void recreate(SQLiteDatabase db){
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		create(db);
	}
	
	public void update(ArrayList<TaxonItem> collection){		
		if(collection != null && collection.size() > 0)
		{
			SQLiteDatabase db = this.getWritableDatabase();
			TaxonStore.recreate(db);
			db.beginTransaction();
			ImageStore imgStore = new ImageStore(_context);
			try{
				for(TaxonItem item : collection){
					long id = db.insert(TABLE_NAME, null, getContent(item));
					imgStore.update(item.getSizes(), id, db);
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
			db.close();
		}
	}
	
	public Cursor getAll() { 
		
	    return executeStringQuery("SELECT * FROM " + TABLE_NAME);
	}
	
	
	private ContentValues getContent(TaxonItem item){
		ContentValues values = new ContentValues();
	    values.put(COL_TITLE, item.getTitle());
	    values.put(COL_DETAIL, item.getDetail());
	    values.put(COL_SOURCE, item.getSource());	    
	    return values;
	}
	
	public Cursor getByPk(long pk){
		return executeStringQuery("Select * FROM " + TABLE_NAME + " WHERE " + COL_PK + " = " + pk);
	
	}
}
