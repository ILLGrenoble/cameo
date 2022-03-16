package fr.ill.ics.cameo.base;

public class FilteredEventListener {

	private EventListener listener;
	private boolean filtered;
	
	public FilteredEventListener(EventListener listener, boolean filtered) {
		this.listener = listener;
		this.filtered = filtered;
	}

	public EventListener getListener() {
		return listener;
	}

	public boolean isFiltered() {
		return filtered;
	}
	
	
}
