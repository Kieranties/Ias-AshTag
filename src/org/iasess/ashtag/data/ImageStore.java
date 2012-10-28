package org.iasess.ashtag.data;

import java.util.Map;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ImageStore  extends IasDatabase {
    
    private static final String TABLE_NAME = "images";
    public static final String COL_PK  = "_id"; // NEEDS to be called _id otherwise cursors don't work
    public static final String COL_TAXON_ITEM_ID = "taxon_id";
    public static final String COL_SIZE = "size";
    public static final String COL_URI = "uri";
            
    
    /**
     * The table create scripts 
     */
    private static final String TABLE_CREATE =
    		"CREATE TABLE " + TABLE_NAME 
    		+ " ( " 
    		+ COL_PK + " int primary key," 
    		+ COL_TAXON_ITEM_ID + " int, "
    		+ COL_SIZE + " text, "
    		+ COL_URI + " text);";

    /**
     * Constructor method
     * @param context The context to create this instance against
     */
    public ImageStore(Context context) {
    	super(context);
    }

    /**
     * Executes the script to create the table for this datastore
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    public static void create(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

	/**
	 * Executes the script to update the table for this data store
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */

	public static void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//drop and recreate db
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		create(db);
		
	}
			

	public void update(Map<String, String> collection, long id, SQLiteDatabase db){
		for(Entry<String, String> entry : collection.entrySet()){
			String size = entry.getKey();
			db.delete(TABLE_NAME, COL_TAXON_ITEM_ID + "=? AND " + COL_SIZE + "=?", new String[] { String.valueOf(id), size });
			
			ContentValues values = new ContentValues();
			values.put(COL_SIZE, size);
			values.put(COL_TAXON_ITEM_ID, id);
			values.put(COL_URI, entry.getValue());
			
			db.insert(TABLE_NAME, null, values);
		}
	}
	
	public Cursor getAll() {	 
	    return executeStringQuery("SELECT * FROM " + TABLE_NAME);
	}
	
	
	public Cursor getByTaxaId(long id){
		return executeStringQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_TAXON_ITEM_ID + " = " + id);
	}
	
	public Cursor getByTaxaId(long id, String size){
		return executeStringQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_TAXON_ITEM_ID + " = " + id +
				" AND " + COL_SIZE + " = " + size);
	}
	
	public String getImage(long id, String size){
		Cursor results = getByTaxaId(id, size);
		String uri = null;
		if(results.getCount() > 0){
			results.moveToFirst();
			uri = results.getString(results.getColumnIndex(COL_URI));
		} 
		results.close();
		return uri;
	}
}
