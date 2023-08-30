

package io.github.warnotte;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;


// TODO : Add MAVEN
// TODO : Add Log4J2

public class SortMP3Directory {
	
	protected static final Logger Logger = LogManager.getLogger("SortMP3Directory");
	
	/**
	 * 
	 * En th�orie tu touche a rien d'autres qu'a �a... et tu backup et tu testes avant de lancer tout sinon...
	 * 
	 */
	static String inputDirectory = "D:\\mp3\\Electro\\Aphex Twin";
	static String outputDirectory = "e:\\sorted";
	static boolean debugMode = true;
	
	
	
	
	
	private static int DirectoryProcessed = 0;
	private static int file_copy_failed = 0;
	private static int file_copy_success = 0;
	
	static List<File> listCOPYERROR = new ArrayList<>();
	static List<String> listNOTAG = new ArrayList<>();
	

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		
		if (debugMode==false)
			new File(outputDirectory).mkdir();
		// Mettre le dernier param�tre a true pour ne pas effectuer les ope�ration et debug.
		doRecursive(new File(inputDirectory), false, debugMode); // Ne deplace rien, affiche juste la console (dernier param�tre) // 22/02/2017
		
		Logger.error("ERROR");
		
		System.out.println("-------------------------------------------------");
		System.out.println("-------------------------------------------------");
		System.out.println("REPORT ------------------------------------------");
		System.out.println("-------------------------------------------------");
		System.out.println("-------------------------------------------------");
		
		// List all Error or NO Tag
		System.out.println("No TAGGED directory : "+listNOTAG.size());
		System.out.println("COPY ERROR directory : "+listCOPYERROR.size());
		System.out.println("-------------------------------------------------");
		for (int i = 0; i < listNOTAG.size(); i++) {
			System.out.println("NO TAG OF : "+listNOTAG.get(i));
		}System.out.println("-------------------------------------------------");
		for (int i = 0; i < listCOPYERROR.size(); i++) {
			System.out.println("COPY ERROR OF : "+listCOPYERROR.get(i));
		}
		System.out.println("-------------------------------------------------");
		
		System.out.println(":) - Directory processed : " + (DirectoryProcessed-1));
		
		if (file_copy_failed>0) {
			System.out.println("FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! ");
			System.out.println("FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! ");
			System.out.printf(":((- File copied [%s/%s]\r\n", file_copy_success, file_copy_failed+file_copy_success);
			System.out.println("FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! ");
			System.out.println("FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! FAIL !!!! ");
		}
			
		else
			System.out.printf(":) - File copied [%s/%s]\r\n", file_copy_success, file_copy_failed+file_copy_success);
		
		
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

		String listFiles[] = f.list(new FilenameFilter_FILES());
		// Process files in this folder if exists
		if ((listFiles != null) && (listFiles.length!=0)){
			for (int i = 0; i < listFiles.length; i++) {
				File fils = new File(Path + "\\" + listFiles[i]);
				if (ID3TagPresent == false) {
					AudioFile fh;
					try {
						
						fh = AudioFileIO.read(fils);
						tag = fh.getTag();
						if (tag != null)
						{
							ID3TagPresent = true;
							
							break;
							//System.out.println(":) - IDTag Present "+tag.getFieldCount()+ " - "+fils.getAbsolutePath());
						}
					} catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
						// System.err.println("Cannot threat : " + fils);
						continue;
					}
				}
			}
		}

		System.out.println(":) - SCANNING : " + Path + "(" + listFiles.length + ") ");
		
		if (level>0) {
			if ((ID3TagPresent == true) ) {
				
				System.out.println(":) - IDTag Present "+tag.getFieldCount());
				String YEAR = tag.getFirst(FieldKey.YEAR);
				if ((YEAR == null) || (YEAR.length() == 0)) {
					YEAR = "UNKNOWN_YEAR";
				}
				String ALBUM = tag.getFirst(FieldKey.ALBUM);
				if ((ALBUM == null) || (ALBUM.length() == 0)) {
					ALBUM = "UNKONWN_ALBUM";
				}
				String ARTIST = tag.getFirst(FieldKey.ARTIST);
				if ((ARTIST == null) || (ARTIST.length() == 0)) {
					// On connais pas l'artist... ni l'année... donc on mets le nom du repertoire original.
					ARTIST = "UNKONWN_ARTIST";
					ALBUM = f.getName();
				}
								
				ARTIST = filterInvalidCaracters(ARTIST);
				ALBUM = filterInvalidCaracters(ALBUM);
				// TODO : a tester deja vu un truc genre 2008/2015
				//YEAR = filterInvalidCaracters(YEAR);
				
				System.out.printf(":) - SCN RSLT [%s] [%s] [%s] \r\n", ARTIST, ALBUM, YEAR);
				
		
				// TODO : Attention que parfois certains ARTIST ou ALBUM ont des caract�res foireux genre : ou ? ou encore dieu sait quoi ... faut virer tout �a
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
					try {
						CopieRepertoire(f, destinationDirectoryM);
						
						// System.err.println("Will delete :" + f);
					//	boolean ret = f.delete();
						
					//	if (ret==false)
					//		System.out.println(":( - Error DELETING "+f.getAbsolutePath());
						
					} catch (Exception e) {
						e.printStackTrace();
						file_copy_failed++;
						System.out.printf(":( - Error COPYING [%s] to [%s] ",f.getAbsolutePath(), destinationDirectoryM.getAbsolutePath());
						
						listCOPYERROR.add(f);
					}

				}
			}
			else
			{
				// Si y'a aucun fichier dans ce reperoite c'est vide donc osef.
				if ((listFiles != null) && (listFiles.length!=0))
				{
					System.out.println(":( - No Tag present for "+Path);
					
					listNOTAG.add(Path);
				}
				else
					// TODO : ce test doit etre mis avant le reste du code...
					System.out.println(":) - Empty directory "+Path);
			}
		}
		System.out.println();
	}

	private static String filterInvalidCaracters(String str) {
		
		String regexp = "\\/?%*:|\"<>";
//		 regexp = "[?]";
		String filteredstr = str.replaceAll(regexp, "_");
		
		filteredstr = filteredstr.replaceAll("/", "-");
		filteredstr = filteredstr.replaceAll("\\|", "-");
		
		
		if (str.equals(filteredstr)==false)	{
			System.out.println("This string ["+str+"] has been filtered to ["+filteredstr+"]");
		}	
				
		return filteredstr;
	}

	private static void CopieRepertoire(File actualDirectory, File destinationDirectory) throws Exception  {
		String[] filestoCopy = actualDirectory.list(new FilenameFilter_FILES_ALL());
		for (int i = 0; i < filestoCopy.length; i++) {
			File fin = new File(actualDirectory + "\\" + filestoCopy[i]);
			File fout = new File(destinationDirectory + "\\" + filestoCopy[i]);
			//System.err.println(fin+"->"+fout);
			
			try {
				Files.copy(fin.toPath(), fout.toPath(), REPLACE_EXISTING);
				
				file_copy_success++;
				//boolean ret = fin.delete();
				//	boolean ret = fin.renameTo(fout);
				//if (ret == false)
				//	throw new Exception("Failed to delete : "+fin.getAbsolutePath());
				//
				
			} catch (InvalidPathException e) {
				file_copy_failed++;
				e.printStackTrace();
			} catch (IOException e) {
				file_copy_failed++;
				e.printStackTrace();
			}
			
	
		}
	}

}
