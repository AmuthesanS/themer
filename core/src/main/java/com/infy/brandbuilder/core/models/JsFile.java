package com.infy.brandbuilder.core.models;

import java.io.File;

public class JsFile extends ImportResource {

	public JsFile(String string, File tempFile) {
		super(string, tempFile);
	}

	@Override
	public String getMimeType() {
		return "application/javascript";
	}

}
