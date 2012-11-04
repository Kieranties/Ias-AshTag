package org.iasess.ashtag.handlers;

import org.iasess.ashtag.AshTagApp;

public class EmailHandler {

	public static boolean isValid() {
		return isValid(AshTagApp.getUsernamePreferenceString());
	}

	private static boolean isValid(String email) {
		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}

	public static boolean ValidateAndSet(String email) {
		if (isValid(email)) {
			AshTagApp.setUsernamePreferenceString(email);
			return true;
		}

		return false;
	}
}
