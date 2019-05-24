package com.webengine.textprocessing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
//import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;


import com.webengine.ITextProcessor;
import opennlp.tools.stemmer.PorterStemmer;

import opennlp.tools.langdetect.Language;
import opennlp.tools.langdetect.LanguageDetector;
import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.model.BaseModel;


import de.uni_leipzig.asv.toolbox.baseforms.Zerleger2;
import de.uni_leipzig.asv.toolbox.viterbitagger.Tagger;
import de.uni_leipzig.asv.utils.Pretree;



@Service
public class TextProcessor  implements ITextProcessor{
	

	private final static Logger LOGGER = Logger.getLogger(TextProcessor.class.getName());
	
	/** the application context. */
	@Autowired
	private ApplicationContext appContext;

	/** constant for german language */
	private final static String de_lang = "deu";
	/** constant for english language */
	private final static String en_lang = "eng";
	/** constant for tokenizer model */
	private final static String token = "token";
	
	/** constant for pos maxent model */
	private final static String posMaxEnt = "pos-maxent";
	
	/** constant for sentence extraction model */
	private final static String sentence = "sent";
	
	/** constant for lemmatization model */
	private final static String lemma = "lemma";
	
	
	
	/** the models used. */
	Map<String, Map<String, BaseModel>> models = new HashMap<String, Map<String, BaseModel>>();
	
	/** the models used. */
	Map<String, Collection<String>> stopwords = new HashMap<String, Collection<String>>();
	/** the categorizer to detect language */
	private LanguageDetector myCategorizer;

	
	
	
	 //reduce file for baseform

    String redbase_en = "./resources/trees/en-nouns.tree";
    String redbase_de = "./resources/trees/de-nouns.tree";
    
    //de-nouns.tree   en-nouns.tree
    
    //reduce file for splitting

    String red = "./resources/trees/grfExt.tree";

    //forward file

    String forw = "./resources/trees/kompVVic.tree";

    //backward file

    String back = "./resources/trees/kompVHic.tree";
	
    String tmFile = "./resources/taggermodels/deTaggerModel.model"; /* deTaggerModel.model*/
    String tmFile2 = "./resources/taggermodels/english.model"; /* enTaggerModel.model*/
	
    Pretree pretree;
	
	
	
	
	
	
	
	
	
	
	/** the constructor */
	public TextProcessor() {
		super();

	}

	/** loads the models */
	@PostConstruct
	public void init() throws IOException {
		// load the models
		
		//ClassLoader classLoader = this.getClass().getClassLoader();
		//resources folder must be added to build path!!!
		
		/*
		stopwords.put(de_lang, getWords(classLoader.getResource("models/"+de_lang+"/stopp.txt")));
		stopwords.put(en_lang, getWords(classLoader.getResource("models/"+en_lang+"/stopp.txt")));
		
		myCategorizer = new LanguageDetectorME(new LanguageDetectorModel(
				new FileInputStream(classLoader.getResource("models/langdetect-183.bin").getFile())   ));
		*/
		
		
		
		redbase_en = appContext.getResource("classpath:/trees/en-nouns.tree").getFile().getPath();
	    redbase_de = appContext.getResource("classpath:/trees/de-nouns.tree").getFile().getPath();
	    
	    red = appContext.getResource("classpath:/trees/grfExt.tree").getFile().getPath(); 

	    //forward file

	    forw = appContext.getResource("classpath:/trees/kompVVic.tree").getFile().getPath(); 

	    //backward file

	    back = appContext.getResource("classpath:/trees/kompVHic.tree").getFile().getPath(); 
		
		stopwords.put(de_lang, getWords(appContext.getResource("classpath:/models/"+de_lang+"/stopp.txt")));
		stopwords.put(en_lang, getWords(appContext.getResource("classpath:/models/"+en_lang+"/stopp.txt")));
		
		myCategorizer = new LanguageDetectorME(new LanguageDetectorModel(
				appContext.getResource("classpath:/models/langdetect-183.bin").getInputStream()));
		
		
		
		HashMap<String, BaseModel> deModels = new HashMap<String, BaseModel>();
	
		/*deModels.put(token, new TokenizerModel(
				new FileInputStream(classLoader.getResource("models/" + de_lang + "/token.bin").getFile()) ));
		deModels.put(sentence, new SentenceModel(
				new FileInputStream(classLoader.getResource("models/" + de_lang + "/sent.bin").getFile()) ));
		deModels.put(posMaxEnt, new POSModel(
				new FileInputStream(classLoader.getResource("models/" + de_lang + "/"+posMaxEnt+".bin").getFile()) ));
*/
		deModels.put(token, new TokenizerModel(
				appContext.getResource("classpath:/models/" + de_lang + "/token.bin").getInputStream()));
		deModels.put(sentence, new SentenceModel(
				appContext.getResource("classpath:/models/" + de_lang + "/sent.bin").getInputStream()));
		deModels.put(posMaxEnt, new POSModel(
				appContext.getResource("classpath:/models/" + de_lang + "/"+posMaxEnt+".bin").getInputStream()));
		
		
		
		HashMap<String, BaseModel> enModels = new HashMap<String, BaseModel>();
		/*enModels.put(token, new TokenizerModel(
				new FileInputStream(classLoader.getResource("models/" + en_lang + "/token.bin").getFile()) ));
		enModels.put(sentence, new SentenceModel(
				new FileInputStream(classLoader.getResource("models/" + en_lang + "/sent.bin").getFile()) ));
		enModels.put(posMaxEnt, new POSModel(
				new FileInputStream(classLoader.getResource("models/" + en_lang + "/"+posMaxEnt+".bin").getFile()) ));
		*/
		
		enModels.put(token, new TokenizerModel(
				appContext.getResource("classpath:/models/" + en_lang + "/token.bin").getInputStream()));
		enModels.put(sentence, new SentenceModel(
				appContext.getResource("classpath:/models/" + en_lang + "/sent.bin").getInputStream()));
		enModels.put(posMaxEnt, new POSModel(
				appContext.getResource("classpath:/models/" + en_lang + "/"+posMaxEnt+".bin").getInputStream()));
		
		
		models.put(en_lang, enModels);
		models.put(de_lang, deModels);
		// load en models
		
		
		
		pretree = new Pretree();
		
		
	}

	/*
	private Collection<String> getWords(URL resource) {
		Collection<String> result = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader( new FileInputStream(resource.getFile()), "UTF-8"))) {

			//br returns as stream and convert it into a List
			result =  br.lines().collect(Collectors.toList());

		} catch (IOException e) {
			LOGGER.error("Error reading stopwqords");
		}
		return result;
		
	}*/ 
	
	
	private Collection<String> getWords(Resource resource) {
		Collection<String> result = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), "UTF-8"))) {

			//br returns as stream and convert it into a List
			result =  br.lines().collect(Collectors.toList());

		} catch (IOException e) {
			LOGGER.error("Error reading stopwqords");
		}
		return result;
		
	}
	

	/**
	 * process the incoming content.
	 * 
	 * @param content
	 *            a textfile
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public TextProcessingResult process(String content) throws FileNotFoundException, IOException {
		TextProcessingResult result = new TextProcessingResult();
		Language lg = detectLanguage(content);
		String language = lg.getLang();
		double conf = lg.getConfidence();
		result.setLanguage(language /*"eng"*/); //language
		
		LOGGER.info("Detected language: " +language + " " + conf);
		
		
		if (language.equals("deu")) {
			
			String[] sentences = extractSentences(language, content);
			
			Stemmer stemmer = new SnowballStemmer(ALGORITHM.GERMAN);
			pretree.load(redbase_de);
			Zerleger2 zer = new Zerleger2();
		       zer.init(forw, back, red);
		       
			
			for (String sentence : sentences) {
				Sentence s = new Sentence();
				Collection<String> words = extractWords(language, sentence);
				//words = words.stream().filter(word -> !stopwords.get(language).contains(word)).collect(Collectors.toList());;
				
				String[] tag = tag(language, words);
				int index = 0;
				for (String word : words) {				
					
					
					if (!stopwords.get(language).contains(word) && !stopwords.get(language).contains(stemmer.stem(word).toString()) ) {
						
						Word w = new Word();				
						w.setTag(tag[index]);
						//w.setStemmedWord(stemmer.stem(word).toString());
						w.setStemmedWord(zer.grundFormReduktion(word));
						
						
						//entry2 = zer.grundFormReduktion(entry2);
						
						s.getWords().add(w);
					}
					
					
					index++;
				}
				result.getSentences().add(s);
			}
		
		} else 
			if (language.equals("eng")) {
				
				String[] sentences = extractSentences(language, content);
				
				ClassLoader classLoader = this.getClass().getClassLoader();
			//	InputStream dictLemmatizer = new FileInputStream(classLoader.getResource("models/" + en_lang + "/"+lemma+".bin").getFile()); 
				
				InputStream dictLemmatizer = new FileInputStream(appContext.getResource("classpath:/models/" + en_lang + "/"+lemma+".bin").getFile());     
				
				
				
				DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(dictLemmatizer);

				
				for (String sentence : sentences) {
					Sentence s = new Sentence();
					Collection<String> words = extractWords(language, sentence);
					//words = words.stream().filter(word -> !stopwords.get(language).contains(word)).collect(Collectors.toList());;
					
					String[] tags = tag(language, words);					
					String[] toks = tok(words);

					String[] lemma = lemmatizer.lemmatize(toks, tags);
					
					int index = 0;
					for (String word : words) {				
						
						if (!stopwords.get(language).contains(word) && !stopwords.get(language).contains(toks[index]) ) {
							Word w = new Word();				
							w.setTag(tags[index]);
							if (lemma[index].equals("O")) {
								w.setStemmedWord(toks[index]);
							//	LOGGER.info("Baseform reduction (is 0): " + toks[index] + "  ->  " + lemma[index]);
							} else {
							w.setStemmedWord(lemma[index]);
							//LOGGER.info("Baseform reduction (not 0): " + toks[index] + "  ->  " + lemma[index]);
							
							}
							s.getWords().add(w);
						}
						

						index++;
					}
					
					
					result.getSentences().add(s);
				}
				
				if (dictLemmatizer!=null)
				dictLemmatizer.close();
				
				
				
				
			}
		
		
		return result;
	}

	public String[] tag(String language, Collection<String> words) {
		POSModel model = (POSModel) models.get(language).get(posMaxEnt);
		POSTaggerME tagger = new POSTaggerME(model);
		String[] wordsArray = words.stream().toArray(String[]::new);
		return tagger.tag(words.toArray(wordsArray));
	}

	
	public String[] tok(Collection<String> words) {
		
		String[] wordsArray = words.stream().toArray(String[]::new);
		return words.toArray(wordsArray);
	}
	
	
	/**
	 * 
	 * @param language
	 *            language of the sentence
	 * @param sentence
	 *            the text of the sentence
	 * @return the words of the sentence
	 * 
	 */
	private Collection<String> extractWords(String language, String sentence) {

		TokenizerModel model = (TokenizerModel) models.get(language).get(token);
		TokenizerME sentenceDetector = new TokenizerME(model);
		return Arrays.asList(sentenceDetector.tokenize(sentence));

	}

	/**
	 * detects tha language of the given string.
	 * 
	 * @param content
	 *            the text
	 * @return the Language of the text
	 * 
	 */
	public Language detectLanguage(String content) {

		// Get the most probable language
		
		Language [] languages = myCategorizer.predictLanguages(content);
		
		LOGGER.info("Probable languages: " );
		
		Language lang = new Language("eng", 0.01);
		
		boolean langset=false;
		
		for (int i=0; i<languages.length; i++) {
			
			Language curlang = languages[i];
			
			LOGGER.info("Probable languages: " + curlang.getLang() + "  " + curlang.getConfidence());
			
			
			if (langset==false) {
				
				if (curlang.getLang().equals("deu") || curlang.getLang().equals("eng")) {
					lang=curlang;
					langset = true;
				}
				
			}
			
			
		}
		
	
		return lang;
	}

	public String[] extractSentences(String language, String content) throws IOException {
		SentenceModel model = (SentenceModel) models.get(language).get(sentence);
		SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
		return sentenceDetector.sentDetect(content);
	}

	

}
