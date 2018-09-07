package ss.week2.hotel;

import ss.week2.hotel.Password;

public class Safe {
	
	//@ private invariant password != null;
	private boolean active;
	private boolean open;
	private Password password;
	private Safe safe;
	
	// -- constructor--
	//@ requires password != null;
	public Safe(Password password) {
		assert password != null; //precondition
		this.password = password;
		assert this.password == password;
	}
	public Safe() {
		this(new Password());
	}
	
	//-- queries---
	//@ pure
	public boolean isActive() {
		return this.active;
	}
	
	//@ pure
	public boolean isOpen() {
		return this.open;
	}
	
	//@ pure
	public Password getPassword() {
		return this.password;
	}
	
	//-- commands---
		public void activate(String password) {
		if (this.password.testWord(password) == true) {
			this.active = true;
			assert this.active == true;
		}
		else {
			this.active = false;
			assert this.active == false;
		}
	}
	
		public void deactivate() {
			this.active = false;
			this.open = false;
			assert this.active == false;
			assert this.open == false;
	    }
	
		//@ requires isActive() == true;
	public void open(String password) {
		if (getPassword().testWord(password) == true) {
			this.open = true;
			assert this.open == true;
		}
		else {
			this.open = false;
			assert this.open == false;
		}
	}
	
	//@ requires isActive() == true;
	//@ ensures isOpen() == false;
	//@ ensures isActive() == true;
	public boolean close() {
		this.open = false;
		assert this.open == false;
		return this.open;
	}
	
	// --main method--
	public static void main(String[] args) {
	Safe safe = new Safe(null);
	System.out.println("hello");
	}
}
