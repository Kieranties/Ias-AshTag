package org.iasess.android.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class IasDatabase extends SQLiteOpenHelper {
	
	protected Context _context;
	
	/**
	 * Database instance name 
	 */
	private static final String DATABASE_NAME = "IAS.db";
	
    /**
     * Current database version
     */
    private static final int DATABASE_VERSION = 5;
    
    /**
     * Constructor method
     * @param context The context to create this instance against
     */
    public IasDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        _context = context;        
    }

    /**
     * Executes the script to create the table for this datastore
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        TaxaStore.create(db);
        ImageStore.create(db);
    }

	/**
	 * Executes the script to update the table for this data store
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		TaxaStore.upgrade(db, oldVersion, newVersion);		
		ImageStore.upgrade(db, oldVersion, newVersion);
	}
			
	/**
	 * Executes the given query against the database
	 * 
	 * @param q the query to execute
	 * @return a {@link Cursor} of the results
	 */
	protected Cursor executeStringQuery(String q){
		SQLiteDatabase db = this.getReadableDatabase();
	    return db.rawQuery(q, null);
	}
	
}
