package org.iasess.android.data;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.iasess.android.api.TaxaItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ImageStore  extends IasDatabase {
    
    /**
     * Database table for the image store
     */
    private static final String TABLE_NAME = "images";
    
    /**
     * The column name for the primary key
     */
    public static final String COL_PK  = "_id"; // NEEDS to be called _id otherwise cursors don't work
    
    /**
     * The column name for the taxa id 
     */
    public static final String COL_TAXA_ID = "taxa_id";
    
    /**
     * The column name for the common name property of a taxa
     */
    public static final String COL_SIZE = "size";
    
    /**
     * The column name for the scientific name of a taxa
     */
    public static final String COL_URI = "uri";
            
    
    /**
     * The table create scripts 
     */
    private static final String TABLE_CREATE =
    		"CREATE TABLE " + TABLE_NAME 
    		+ " ( " 
    		+ COL_PK + " integer primary key,"
            + COL_TAXA_ID + " text, " 
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
			
	/**
	  * Updates the store with details of the given collection of {@link TaxaItem}.
	 * <p>
	 * Updates are performed based on an items PK field.
	 * 
	 * @param collection The {@link TaxaItem} collection to update.
	 */
	public void update(Map<String, String[]> collection, int taxaId, SQLiteDatabase db){
		for(Entry<String, String[]> entry : collection.entrySet()){
			String size = entry.getKey();
			db.delete(TABLE_NAME, COL_TAXA_ID + "=? AND " + COL_SIZE + "=?", 
					new String[] { String.valueOf(taxaId), size });
			for(String uri : entry.getValue()){
				ContentValues values = new ContentValues();
				values.put(COL_SIZE, size);
				values.put(COL_TAXA_ID, taxaId);
				values.put(COL_URI, uri);
				
				db.insert(TABLE_NAME, null, values);
			}
		}
	}
	
	/**
	 * Fetches a {@link Cursor} referencing all images in the store.
	 * 
	 * @return a {@link Cursor} of the items from the store
	 */
	public Cursor getAll() {	 
	    return executeStringQuery("SELECT * FROM " + TABLE_NAME);
	}
	
	/**
	 * Fetches a {@link Cursor} referencing the images matched by pk
	 * 
	 * @param pk the primary key identifier of the item to return
	 * @return The {@link Cursor} containing the references item
	 */
	public Cursor getByPk(long pk){
		return executeStringQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_PK + " = " + pk);
	}
	
	public Cursor getByTaxaId(long taxaId){
		return executeStringQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_TAXA_ID + " = " + taxaId);
	}
	
	public Cursor getByTaxaId(long taxaId, String size){
		return executeStringQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_TAXA_ID + " = " + taxaId +
				" AND " + COL_SIZE + " = " + size);
	}
	
	public String getListingImage(long taxaId){
		Cursor results = getByTaxaId(taxaId, "100");
		String uri = null;
		if(results.getCount() > 0){
			results.moveToFirst();
			uri = results.getString(results.getColumnIndex(COL_URI));
		} 
		results.close();
		return uri;
	}
	
	public ArrayList<String> getLargeImages(long taxaId){
		Cursor results = getByTaxaId(taxaId, "800");
		ArrayList<String> uris = new ArrayList<String>();
		while(results.moveToNext()){
			uris.add(results.getString(results.getColumnIndex(COL_URI)));
		}
		results.close();
		return uris;
	}
}
