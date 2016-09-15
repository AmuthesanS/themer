package com.infy.brandbuilder.core.models;

import java.io.File;

public class JpegFile extends ImportResource {

	public JpegFile(String string, File tempFile) {
		super(string, tempFile);
	}

	@Override
	public String getMimeType() {
		return "image/jpg";
	}

}
