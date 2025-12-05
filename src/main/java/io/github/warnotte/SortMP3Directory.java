package io.github.warnotte;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;


public class SortMP3Directory {
	
	/**
	 * CONFIGURATION PART:
	 * En théorie tu touche a rien d'autres qu'a ça... et tu backup et tu testes avant de lancer tout sinon...
	 * 
	 */
	
	//static String inputDirectory = "D:\\mp3\\Electro\\Aphex Twin";
	static String inputDirectory = "e:\\manson";
	static String outputDirectory = "e:\\manson_sorted";
	static boolean debugMode = false;
	
	
	protected static Logger Logger;
	
	private static int DirectoryProcessed = 0;
	private static int file_copy_failed = 0;
	private static int file_copy_success = 0;
	
	static List<File> listCOPYERROR = new ArrayList<>();
	static List<String> listNOTAG = new ArrayList<>();
	static List<DirectoryReport> directoryReports = new ArrayList<>();
	

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		deleteDir(new File("logs"));
		Logger = LogManager.getLogger("SortMP3Directory");
		directoryReports.clear();
		listCOPYERROR.clear();
		listNOTAG.clear();
		DirectoryProcessed = 0;
		file_copy_failed = 0;
		file_copy_success = 0;
		if (debugMode==false)
			new File(outputDirectory).mkdir();
		// Mettre le dernier param�tre a true pour ne pas effectuer les opeération et debug.
		doRecursive(new File(inputDirectory), false, debugMode); // Ne deplace rien, affiche juste la console (dernier param�tre) // 22/02/2017
		
		
		
		
		logFinalReport();
		
		Desktop.getDesktop().open(new File("logs/AudioSort-errors.html"));
		Desktop.getDesktop().open(new File("logs/AudioSort.html"));
		
	}

	private static void deleteDir(File string) throws IOException {
		File[] allContents = string.listFiles();
	    if (allContents != null) {
	        for (File file : allContents) {
	        	deleteDir(file);
	        }
	    }
	    string.delete();
	    
	}

	/**
	 * 
	 * @param f
	 * @param deleteOriginalFile
	 * @param debugMode N'execute aucune modification mais renvoye tout les messages de debbogages de ce qu'il est cens� se passer.
	 */
	private static void doRecursive(File f, boolean deleteOriginalFile, boolean debugMode) {
		DirectoryProcessed = 0;
		doRecursive(f, deleteOriginalFile, 0, debugMode);
	}

	/**
	 * Process recursivement un repertoire pour lui appliquer un process sur
	 * tout les fichiers PDF
	 * 
	 * @param f Le repertoire de base ou se trouvent les PDF
	 * @param processor Le processeur qui doit effectuer un travail sur chacun des fichiers.
	 * @param replaceOriginalFile Efface le fichier original pour le remplacer par le fichier process�
	 * @throws Exception
	 */
	private static void doRecursive(File f, boolean deleteOriginalFile, int level, boolean debugMode) {

		
		DirectoryProcessed++;
		
//		System.err.println("---------------------------");
		String Path = f.getPath();
		
		// Ne retrie pas le repertoire de sortie pr�defini par avant... (inutile dans cette m�thode doit etre deporte ? ou ne pas sortir dans le repertoire de traitement et faire une vrai copie)
		if (Path.contains(outputDirectory))
			return;
		DirectoryReport directoryReport = new DirectoryReport(Path);
		directoryReports.add(directoryReport);

		String listDir[] = f.list(new FilenameFilter_DIR());

		// Process directory recursively if exists
		if (listDir != null)
			for (int i = 0; i < listDir.length; i++) {
				File fils = new File(Path + "\\" + listDir[i]);
				doRecursive(fils, deleteOriginalFile, level + 1, debugMode);
		}
		
		// TODO : le boolean est un inutile puisque l'autre restera null, mais on garde pour savoir si on fait une moyenne ou un cumul au cas ou certains mp3 d'un repertoire aurait l'info manquante. 
		boolean ID3TagPresent = false;
		Tag tag = null;
		AudioHeader audioheader = null;
		String listFiles[] = f.list(new FilenameFilter_FILES());
		int listFilesCount = (listFiles == null) ? 0 : listFiles.length;
		directoryReport.filesCount = listFilesCount;
		
		Logger.info(":) - SCANNING : " + Path + "(" + listFilesCount + ") ");
		if ((listFiles == null) || (listFiles.length==0))
		{
			// TODO : ce test doit etre mis avant le reste du code...
			Logger.info(":) - Empty directory "+Path);
			directoryReport.empty = true;
			return;
		}
		
		
		// Process files in this folder if exists
		if ((listFiles != null) && (listFiles.length!=0)){
			for (int i = 0; i < listFiles.length; i++) {
				File fils = new File(Path + "\\" + listFiles[i]);
				AudioFile fh;

				try {
					fh = AudioFileIO.read(fils);
					audioheader = fh.getAudioHeader();
					
					if (ID3TagPresent == false) {
						tag = fh.getTag();
						if (tag != null)
						{
							ID3TagPresent = true;
							break;
						}
						
					}
				
				}catch (org.jaudiotagger.audio.exceptions.InvalidAudioFrameException e1)
				{
					continue;
				}
				catch ( IOException | TagException | ReadOnlyFileException e1) {
					Logger.fatal(e1);
					
					
				} catch (CannotReadException e1) {
					continue;
				}
				{
					
				}
				
				
				
				
				
			}
		}

		
		
		
		// TODO : Not sure, of the >=0 was >0 but i'm debugging.
		if (level>=0) {
			String YEAR = "UNKNOWN_YEAR";
			String ALBUM = "UNKONWN_ALBUM";
			ALBUM += " ("+f.getName()+")";
			String ARTIST = "UNKONWN_ARTIST";
			
			if ((ID3TagPresent == true) ) {
				
				Logger.info(":) - IDTag Present "+tag.getFieldCount());
				directoryReport.tagFound = true;
				YEAR = tag.getFirst(FieldKey.YEAR);
				if ((YEAR == null) || (YEAR.length() == 0)) {
					YEAR = "UNKNOWN_YEAR";
				}
				ALBUM = tag.getFirst(FieldKey.ALBUM);
				if ((ALBUM == null) || (ALBUM.length() == 0)) {
					ALBUM = "UNKONWN_ALBUM";
					ALBUM += " ("+f.getName()+")";
				}
				ARTIST = tag.getFirst(FieldKey.ARTIST);
				if ((ARTIST == null) || (ARTIST.length() == 0)) {
					// On connais pas l'artist... ni l'année... donc on mets le nom du repertoire original.
					ARTIST = "UNKONWN_ARTIST";
					
				}
			}
			else
			{
				// Si y'a aucun fichier dans ce reperoite c'est vide donc osef.
				
					Logger.info(":( - No Tag present for "+Path);
					listNOTAG.add(Path);
					directoryReport.tagFound = false;
				
				
					
			}
				
				
				if (audioheader!=null)
				{
					
					int bitrate;
					if (audioheader.getBitRate().contains("~")==false)
						bitrate = Integer.parseInt(audioheader.getBitRate());
					else
						bitrate = Integer.parseInt(audioheader.getBitRate().substring(1));
					int sample = Integer.parseInt(audioheader.getSampleRate());
					
					String format = audioheader.getFormat().toUpperCase();
					/*
					System.err.println("Format : "+format);
					System.err.println("Bit : "+bitrate);
					System.err.println("samp: "+sample);
					*/
					if (audioheader.getBitRate().contains("~")==false)
						ALBUM+= String.format(" - [%s %d kBps %d kHz]", format, bitrate, sample);
					else
						ALBUM+= String.format(" - [%s VBR ~%d kBps %d kHz]", format, bitrate, sample);
					
				}
				
				
				
				
				ARTIST = filterInvalidCaracters(ARTIST, Path);
				ALBUM = filterInvalidCaracters(ALBUM, Path);
				//TODO : a tester deja vu un truc genre 2008/2015
				YEAR = filterInvalidCaracters(YEAR, Path);
				
				Logger.info(String.format(":) - SCN RSLT [%s] [%s] [%s] \r\n",YEAR, ARTIST, ALBUM ));
				
		
				// TODO : Attention que parfois certains ARTIST ou ALBUM ont des caractères foireux genre : ou ? ou encore dieu sait quoi ... faut virer tout 
				File destinationDirectory = new File(outputDirectory + "\\" + ARTIST);
				File destinationDirectoryM =  new File(outputDirectory + "\\" + ARTIST + "\\[" + YEAR + "] " + ALBUM);
				
				//System.err.printf(":) - Will copy [%s] to [%s]\r\n", destinationDirectory.getAbsolutePath(), destinationDirectoryM.getAbsolutePath());
				//System.err.printf(":) - Will delete [%s]\r\n",f.getAbsolutePath());
			
				// Copie et deplace tout si pas en mode "preview de ce qu'il va se passer"...
				if (debugMode==false)
				{
					// Creer destination + ARTIST
					destinationDirectory.mkdir();
					// Creer destination + ARTIST + YEAR_ALBUM 
					destinationDirectoryM.mkdir();
					
						int beforeSuccess = file_copy_success;
						int beforeFailed = file_copy_failed;
						CopieRepertoire(f, destinationDirectoryM);
						directoryReport.copiedCount = file_copy_success - beforeSuccess;
						directoryReport.errorCount = file_copy_failed - beforeFailed;
						
						// System.err.println("Will delete :" + f);
					//	boolean ret = f.delete();
						
					//	if (ret==false)
					//		Logger.info(":( - Error DELETING "+f.getAbsolutePath());
						
					

				}
			}
			
		
		
	}

	private static void logFinalReport() {
		RunTotals totals = computeTotals();

		Logger.info("===================== RUN SUMMARY =====================");
		Logger.info("Input=" + inputDirectory + " | Output=" + outputDirectory + " | DebugMode=" + debugMode);
		Logger.info(String.format("Directories: total=%d ok=%d noTag=%d copyError=%d empty=%d",
				totals.directoriesTotal, totals.okDirs, totals.noTagDirs, totals.copyErrorDirs, totals.emptyDirs));
		Logger.info(String.format("Files: scanned=%d copied=%d failed=%d", totals.filesSeen, totals.filesCopied, totals.filesFailed));
		Logger.info("-------------------------------------------------------");

		if (totals.noTagDirs > 0) {
			Logger.warn("Directories missing tags (" + totals.noTagDirs + "):");
			listNOTAG.forEach(path -> Logger.warn("NO TAG : " + path));
			Logger.info("-------------------------------------------------------");
		}
		if (totals.copyErrorDirs > 0 || !listCOPYERROR.isEmpty()) {
			Logger.fatal("Copy errors (" + totals.copyErrorDirs + " dir, " + listCOPYERROR.size() + " files):");
			listCOPYERROR.forEach(file -> Logger.fatal("COPY ERROR : " + file));
			Logger.info("-------------------------------------------------------");
		}

		Logger.info("Per-directory detail (status | files | copied | errors | path)");
		directoryReports.stream()
			.sorted(Comparator.comparing(report -> report.path))
			.forEach(report -> Logger.info(String.format("%-10s | %4d | %4d | %4d | %s",
					report.status(),
					report.filesCount,
					report.copiedCount,
					report.errorCount,
					report.path)));

		writeTextReport(totals);
	}

	private static class DirectoryReport {
		final String path;
		int filesCount;
		int copiedCount;
		int errorCount;
		boolean tagFound;
		boolean empty;

		DirectoryReport(String path) {
			this.path = path;
		}

		String status() {
			if (empty) {
				return "EMPTY";
			}
			if (!tagFound) {
				return "NO_TAG";
			}
			if (errorCount > 0) {
				return "COPY_ERROR";
			}
			return "OK";
		}
	}
	
	private static RunTotals computeTotals() {
		RunTotals totals = new RunTotals();
		totals.directoriesTotal = directoryReports.size();
		totals.filesCopied = file_copy_success;
		totals.filesFailed = file_copy_failed;
		for (DirectoryReport report : directoryReports) {
			totals.filesSeen += report.filesCount;
			String status = report.status();
			if ("OK".equals(status))
				totals.okDirs++;
			else if ("NO_TAG".equals(status))
				totals.noTagDirs++;
			else if ("COPY_ERROR".equals(status))
				totals.copyErrorDirs++;
			else if ("EMPTY".equals(status))
				totals.emptyDirs++;
		}
		return totals;
	}
	
	private static void writeTextReport(RunTotals totals) {
		StringBuilder sb = new StringBuilder();
		sb.append("RUN SUMMARY").append(System.lineSeparator());
		sb.append("Input=").append(inputDirectory).append(" | Output=").append(outputDirectory).append(" | DebugMode=").append(debugMode).append(System.lineSeparator());
		sb.append(String.format("Directories: total=%d ok=%d noTag=%d copyError=%d empty=%d%n",
				totals.directoriesTotal, totals.okDirs, totals.noTagDirs, totals.copyErrorDirs, totals.emptyDirs));
		sb.append(String.format("Files: scanned=%d copied=%d failed=%d%n", totals.filesSeen, totals.filesCopied, totals.filesFailed));
		sb.append(System.lineSeparator());
		sb.append("PER DIRECTORY (status | files | copied | errors | path)").append(System.lineSeparator());
		directoryReports.stream()
			.sorted(Comparator.comparing(report -> report.path))
			.forEach(report -> sb.append(String.format("%-10s | %4d | %4d | %4d | %s%n",
					report.status(),
					report.filesCount,
					report.copiedCount,
					report.errorCount,
					report.path)));
		
		try {
			Path textReport = Path.of("logs", "report.txt");
			Files.createDirectories(textReport.getParent());
			Files.writeString(textReport, sb.toString(), StandardCharsets.UTF_8);
			Logger.info("Saved summary report to " + textReport.toAbsolutePath());
		} catch (IOException e) {
			Logger.error("Failed to write summary report", e);
		}
	}
	
	private static class RunTotals {
		int directoriesTotal;
		int okDirs;
		int noTagDirs;
		int copyErrorDirs;
		int emptyDirs;
		int filesSeen;
		int filesCopied;
		int filesFailed;
	}

	private static String filterInvalidCaracters(String str, String dir) {
		
		if (dir.contains("Expanded"))
		{
			System.err.println("");
		}
		String regexp = "\\/?%*:|\"<>";
//		 regexp = "[?]";
		String filteredstr = str.replaceAll(regexp, "_");
		
		filteredstr = filteredstr.replaceAll("/", "-");
		filteredstr = filteredstr.replaceAll("\\|", "-");
		// https://stackoverflow.com/questions/6222215/regex-for-validating-folder-name-file-name
		filteredstr = filteredstr.replaceAll("\\/?%*:|\"<>", "-");
		filteredstr = filteredstr.replaceAll("\\?", "-");
		filteredstr = filteredstr.replaceAll("\\'", " ");
		filteredstr = filteredstr.replaceAll("\\*", "");
		filteredstr = filteredstr.replaceAll("\\>", ")");
		filteredstr = filteredstr.replaceAll("\\<", "(");
		filteredstr = filteredstr.replaceAll("\\\\", "-");
		
		
		if (str.equals(filteredstr)==false)	{
			Logger.info("This string ["+str+"] has been filtered to ["+filteredstr+"] on path ["+dir+"]");
		}	
				
		return filteredstr;
	}

	private static void CopieRepertoire(File actualDirectory, File destinationDirectory)  {
		String[] filestoCopy = actualDirectory.list(new FilenameFilter_FILES_ALL());
		for (int i = 0; i < filestoCopy.length; i++) {
			File fin = new File(actualDirectory + "\\" + filestoCopy[i]);
			File fout = new File(destinationDirectory + "\\" + filestoCopy[i]);
			//System.err.println(fin+"->"+fout);
			
			
				try {
					Files.copy(fin.toPath(), fout.toPath(), REPLACE_EXISTING);
					file_copy_success++;
				} catch (Exception e) {
					Logger.fatal(String.format(":( - Error COPYING [%s] to [%s] ",fin, fout));
					file_copy_failed++;
					listCOPYERROR.add(fin);
					e.printStackTrace();
				}
				
				
				//boolean ret = fin.delete();
				//	boolean ret = fin.renameTo(fout);
				//if (ret == false)
				//	throw new Exception("Failed to delete : "+fin.getAbsolutePath());
				//
				
			
			
	
		}
	}

}
