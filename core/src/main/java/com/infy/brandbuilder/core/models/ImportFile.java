package com.infy.brandbuilder.core.models;

import java.io.File;

public class ImportFile extends ImportResource {

	public ImportFile(String string, File tempFile) {
		super(string, tempFile);
	}

	@Override
	public String getMimeType() {
		return "text/html";
	}

}
