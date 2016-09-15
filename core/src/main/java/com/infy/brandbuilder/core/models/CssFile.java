package com.infy.brandbuilder.core.models;

import java.io.File;

public class CssFile extends ImportResource {

	public CssFile(String string, File tempFile) {
		super(string, tempFile);
	}

	@Override
	public String getMimeType() {
		return "text/css";
	}

}
