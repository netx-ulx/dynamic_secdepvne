package pt.SecDepVNE.Main;

/**
 * Holds the info related to an event
 * @author Luis Ferrolho, fc41914, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class Event {

	private EventType type; // Arrival or depart
	private int time; //Instant of arrival or departure
	private int index; // Index of the event
	
	public Event(EventType type, int time, int index) {
		this.type = type;
		this.time = time;
		this.index = index;
	}
	
	public EventType getEventType() {
		return type;
	}
	
	public int getTime() {
		return time;
	}
	
	public int getIndex() {
		return index;
	}
	
}
