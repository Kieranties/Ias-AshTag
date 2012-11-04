package org.iasess.ashtag.handlers;

import org.iasess.ashtag.AshTagApp;
import org.iasess.ashtag.ImageHandler;
import org.iasess.ashtag.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import com.actionbarsherlock.view.MenuItem;

public class ClickHandler {

	private Activity _activity;
	
	public ClickHandler(Activity activity){
		_activity = activity;
	}
	
	public void onAddSightingClick(MenuItem mi) {
    	if(EmailHandler.isValid()){
        	new ImageHandler(_activity).showChooser();
    	} else {
    		if(GetValidEmail()){
    			new ImageHandler(_activity).showChooser();
    		}
    	}
    } 
	
	public boolean onUsernameClick(){
		return GetValidEmail();
	}
	
	
	private boolean GetValidEmail(){
		new EmailInputDialog(_activity).show();
		return EmailHandler.isValid();
	}
	
	private class EmailInputDialog extends AlertDialog{		
		
		private EditText _input;
		private String _toastText;
		
		protected EmailInputDialog(Context context) {
			super(context);
			
			_input = new EditText(context);
			_input.setTextColor(context.getResources().getColor(android.R.color.black));
			_input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
			
			setTitle(context.getResources().getString(R.string.username_dialog_title));
			setMessage(context.getResources().getString(R.string.username_dialog_message));
			setView(_input);
			
			DialogInterface.OnClickListener stubListener = new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                // this will never be called
	            }
	        };
	        
	        setButton(DialogInterface.BUTTON_POSITIVE, context.getResources().getString(android.R.string.ok), stubListener);	        
	        setButton(DialogInterface.BUTTON_NEGATIVE, context.getResources().getString(android.R.string.cancel), stubListener);
	        _toastText = context.getResources().getString(R.string.username_invalid);
	        
		}
		
		@Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
	            	String value = _input.getText().toString();
    	            if(!EmailHandler.ValidateAndSet(value)){
    	            	AshTagApp.makeToast(_toastText);
    	            } else {
    	            	dismiss();
    	            }
	            }
	        });
	    }		
	}
}
