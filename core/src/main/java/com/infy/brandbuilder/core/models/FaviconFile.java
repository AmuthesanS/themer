package com.infy.brandbuilder.core.models;

import java.io.File;

public class FaviconFile extends ImportResource {

	public FaviconFile(String string, File tempFile) {
		super(string, tempFile);
	}

	@Override
	public String getMimeType() {
		return "image/ico";
	}

}