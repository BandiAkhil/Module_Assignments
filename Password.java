package ss.week2.hotel;

/** 
 * Password and its methods
 * @author Akhil Bandi
 */

public class Password {
	
	//-----instance variable------
	
	public static final String INITIAL = "Hi";
	private String password;
	
	//-----constructor------
	
	public Password() {
		password = INITIAL;
	}
		
	//-----queries------
	
	/**
	 * returns boolean if password is acceptable or not
	 * @param suggestion if the new password is acceptable
	 * @return false if password is not acceptable
	 *         true if password is acceptable
	 */
	
	public boolean acceptable (String suggestion) {
		if (suggestion.length() <= 5 || suggestion.contains(" ")) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * returns a boolean if the password typed matches the set password
	 * @param test testWord of the new password
	 * @return true if the password match the typed string
	 *         false if the password does not match the string
	 */
	
	public boolean testWord (String test) {
		if (test.equals(password)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * returns a boolean if the password is changed from old to new
	 * @param oldpass, newpass setWord to new password change
	 * @return true if new password is acceptable and changed
	 *         false if the password is not changed
	 */
	
	public boolean setWord(String oldpass, String newpass) {
		if (testWord(oldpass) && acceptable(newpass)) {
			password = newpass;
			return true;
		}
		else {
			return false;
		}
	}
}
