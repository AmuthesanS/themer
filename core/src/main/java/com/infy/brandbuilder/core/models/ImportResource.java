package com.infy.brandbuilder.core.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public abstract class ImportResource {
	protected String name;
	protected String mimeType;
	protected File data;
	protected String path;

	public ImportResource(String zipEntryName, File data) {
		initialize(zipEntryName);
		this.data = data;
		this.path = zipEntryName;
	}

	protected void initialize(String zipEntryName) {
		this.name = zipEntryName.substring(zipEntryName.lastIndexOf("/") + 1,
				zipEntryName.length());
	}

	public abstract String getMimeType();

	public String getName() {
		return this.name;
	}

	public String getPath() {
		return path;
	}

	public InputStream getStream() throws FileNotFoundException {
		return new FileInputStream(this.data);
	}

}
