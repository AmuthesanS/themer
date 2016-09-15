package com.infy.brandbuilder.core.models;

import java.io.File;

public class GifFile extends ImportResource {

	public GifFile(String string, File tempFile) {
		super(string, tempFile);
	}

	@Override
	public String getMimeType() {
		return "image/gif";
	}
}
