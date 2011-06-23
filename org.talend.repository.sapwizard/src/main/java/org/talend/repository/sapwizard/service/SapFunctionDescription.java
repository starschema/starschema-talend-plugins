package org.talend.repository.sapwizard.service;

/**
 * @author Ammu
 * 
 */
public class SapFunctionDescription {
	private String name;
	private String description;

	public SapFunctionDescription(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String paramString) {
		this.name = paramString;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String paramString) {
		this.description = paramString;
	}
}