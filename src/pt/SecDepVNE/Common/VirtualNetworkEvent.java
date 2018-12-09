package pt.SecDepVNE.Common;

import pt.SecDepVNE.Main.Event;
import pt.SecDepVNE.Virtual.VirtualNetwork;

public class VirtualNetworkEvent{
	
	private VirtualNetwork virNet;
	private Event event;
	
	public VirtualNetworkEvent(VirtualNetwork virNet, Event event) {
		this.virNet = virNet;
		this.event = event;
	}

	public VirtualNetwork getVirNet() {
		return virNet;
	}
	
	public Event getEvent() {
		return event;
	}
}
