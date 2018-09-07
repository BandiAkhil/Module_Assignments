package ss.week2.hotel;

import ss.week2.hotel.*;

public class Hotel {
	// ---instance variables----
	// @ private invariant name != null;
	// @ private invariant gname != null;
	// @ private invariant password != null;
	private String name;
	private String gname;
	private Room room101;
	private Room room102;
	private Guest guest;
	private Password password;
	private Safe safe;

	// ---constructor---
	public Hotel(String name) {
		assert name != null;
		this.name = name;
		room101 = new Room(101);
		room102 = new Room(102);
		password = new Password();
	}
	
	// ---commands---
	// @ requires password != null;
	// @ requires gname != null;
	// @ ensures \result == this.getFreeRoom() || \result == null;
	public Room checkIn(String password, String gname) {
		assert password != null;
		assert gname != null;
		if (this.password.testWord(password) && this.getRoom(gname) == null && this.getFreeRoom() != null) {
			Room free = this.getFreeRoom();
			guest = new Guest(gname);
			guest.checkin(free);
			return free;
		} else {
			return null;
		}
	}

	// @ requires gname != null;
	// @ requires this.getRoom(gname) != null;
	// @ ensures this.getRoom(gname) == null;
	// @ ensures this.getRoom(gname).getSafe().isActive() == false;
	public void checkOut(String gname) {
		assert gname != null;
		if (this.getRoom(gname) != null) {
			Room room = this.getRoom(gname);
			room.getSafe().deactivate();
			guest.checkout();
			assert this.getRoom(gname) == null;
			assert room.getSafe().isActive() == false;
		}
	}

	// ---queries---
	// @ ensures \result == null || \result.getGuest() == null;
	public Room getFreeRoom() {
		if (this.room101.getGuest() == null) {
			return this.room101;
		} else if (this.room102.getGuest() == null) {
			return this.room102;
		} else {
			return null;
		}
	}

	// @ requires gname != null;
	// @ ensures \result == null || \result.getGuest().getName() == gname;
	// @ pure
	public Room getRoom(String gname) {
		assert gname != null;
		if (this.room101.getGuest() != null && this.room101.getGuest().getName().equals(gname)) {
			return this.room101;
		} else if (this.room102.getGuest() != null && this.room102.getGuest().getName().equals(gname)) {
			return this.room102;
		} else {
			return null;
		}
	}

	// @ ensures \result != null;
	public Password getPassword() {
		return this.password;
	}

	// @ pure;
	public String toString() {
		return String.format("Hotel name: " + this.name + ".%n" + "Rooms: " + this.room101 + ", " + this.room102 + ".%n"
				+ "Guest in 101: " + guest.getName() + ".%n" + "Guest in 102: " + guest.getName() + ".%n"
				+ "Safe in 101: " + room101.getSafe().isActive() + ".%n" + "Safe in 102: "
				+ room102.getSafe().isActive());

	}

}
