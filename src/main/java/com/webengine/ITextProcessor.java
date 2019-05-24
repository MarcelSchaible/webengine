package com.webengine;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.webengine.textprocessing.TextProcessingResult;

public interface ITextProcessor {

	public TextProcessingResult process(String content) throws FileNotFoundException, IOException; //throws FileNotFoundException, IOException
	
	
}
