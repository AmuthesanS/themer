package com.infy.brandbuilder.core.models;

import java.io.File;

public class PngFile extends ImportResource {

	public PngFile(String string, File tempFile) {
		super(string, tempFile);
	}

	@Override
	public String getMimeType() {
		return "image/png";
	}

}
