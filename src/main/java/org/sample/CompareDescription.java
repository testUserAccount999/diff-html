package org.sample;

public class CompareDescription {
	private String oldElement;
	private String newElement;
	private String description;

	public CompareDescription(String oldElement, String newElement, String description) {
		this.oldElement = oldElement;
		this.newElement = newElement;
		this.description = description;
	}

	public String getOldElement() {
		return oldElement;
	}

	public String getNewElement() {
		return newElement;
	}

	public String getDescription() {
		return description;
	}
}
