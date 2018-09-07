package ss.week2.hotel;

/** 
 * Guest with possibly a room.
 * @author AkhilBandi
 */

public class Guest {
	
	//---instance variable----
	private String name;
	private Room room;
	
	//----constructor-----
	
	/**
	* creates a guest with the given name
	* @param name name of the new room
	*/
	
	public Guest (String name) {
		this.name = name;
		
	}
	
	//----queries-----
	
	/**
	 * returns the name of the guest
	 */
	
	public String getName() {
		return name;
		
	}
	
	/**
	 * return the room number of guest
	 * @return room rented by the guest;
	 *         null if guest does not rent a room
	 */
	
	public Room getRoom() {
		return room;
	}
	
	/**
	 * return a boolean if guest can check in to the room
	 * @param room the rooms that are in the hotel
	 * @return true if check in succeeds;
	 *         false if guest already had a room or room already had a guest
	 */
	
	public boolean checkin(Room room) {
		if (room.getGuest() == null && this.room == null) {
			room.setGuest(this);
			this.room = room;
			return true;
		}
		else {
			return false;
		}
		
	}
	
	/**
	 * return a boolean if guest can checkout of the room
	 * @return true if checkout succeeded;
	 *         false if guest does not have a room
	 */
	
	public boolean checkout() {
		if (this.room != null) {
			this.room.setGuest(null);
			this.room = null;
			return true;
		}
		else {
			return false;
		}
	}
	
	public String tostring() {
			return "Guest name: " + getName();
			 
	}
		
}
	

