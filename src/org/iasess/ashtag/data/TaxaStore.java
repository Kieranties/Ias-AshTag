package org.iasess.ashtag.data;

import java.util.ArrayList;

import org.iasess.ashtag.api.TaxaItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TaxaStore extends IasDatabase {
	
	/**
     * Database table for taxa store
     */
    private static final String TABLE_NAME = "taxa";
    
    /**
     * The column name for the primary key of a taxa
     */
    public static final String COL_PK  = "_id"; // NEEDS to be called _id otherwise cursors don't work
    
    /**
     * The column name for the common name property of a taxa
     */
    public static final String COL_COMMON_NAME = "common_name";
    
    /**
     * The column name for the scientific name of a taxa
     */
    public static final String COL_SCIENTIFIC_NAME = "scientfic_name";
    
    /**
     * The column name for the rank of a taxa
     */
    public static final String COL_RANK = "rank";
    
    /**
     * The column name for the key text of a taxa
     */
    public static final String COL_KEY_TEXT = "key_text"; 
        
    
    /**
     * The table create scripts 
     */
    private static final String TABLE_CREATE =
    		"CREATE TABLE " + TABLE_NAME 
    		+ " ( " 
    		+ COL_PK + " integer primary key,"
            + COL_COMMON_NAME + " text, " 
    		+ COL_SCIENTIFIC_NAME + " text, "
    		+ COL_RANK + " text, "
    		+ COL_KEY_TEXT + " text );";

    public TaxaStore(Context context) {
		super(context);
	}
    
    public static void create(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

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
	public void update(ArrayList<TaxaItem> collection){
		SQLiteDatabase db = this.getWritableDatabase();
		db.beginTransaction();
		ImageStore imgStore = new ImageStore(_context);
		try{
			for(TaxaItem item : collection){
				db.replace(TABLE_NAME, null, getContent(item));
				imgStore.update(item.getKeyImages(), item.getPk(), db);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		db.close();
	}
	
	/**
	 * Fetches a {@link Cursor} referencing all {@link TaxaItem}s in the store.
	 * <p>
	 * Ordered by their Common Name
	 * 
	 * @return a {@link Cursor} of the items from the store
	 */
	public Cursor getAll() { 
		
	    return executeStringQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_COMMON_NAME + " ASC");
	}
	
	/**
	 * Fetches a {@link Cursor} referencing the {@link TaxaItem} matched by pk
	 * 
	 * @param pk the primary key identifier of the item to return
	 * @return The {@link Cursor} containing the references item
	 */
	public Cursor getByPk(long pk){
		return executeStringQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_PK + " = " + pk);
	}
	
	/**
	 * Gets the {@link ContentValues} to be stored for a given {@link TaxaItem}
	 * 
	 * @param item The {@link TaxaItem} to process
	 * @return a {@link ContentValues} instance containing the values to store
	 */
	private ContentValues getContent(TaxaItem item){
		ContentValues values = new ContentValues();
	    values.put(COL_PK, item.getPk());
	    values.put(COL_COMMON_NAME, item.getCommonName());
	    values.put(COL_SCIENTIFIC_NAME, item.getScientificName());
	    values.put(COL_RANK, item.getRank());	    
	    values.put(COL_KEY_TEXT, item.getKeyText());	    
	    return values;
	}
}
