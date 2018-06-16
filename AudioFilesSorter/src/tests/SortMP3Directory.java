

package tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import static java.nio.file.StandardCopyOption.*;

public class SortMP3Directory {
	
	/**
	 * 
	 * En théorie tu touche a rien d'autres qu'a ça... et tu backup et tu testes avant de lancer tout sinon...
	 * 
	 */
	static String inputDirectory = "E:\\MMM\\";
	static String outputDirectory = "E:\\MMM\\SORTED\\";
	static boolean debugMode = false;
	
	
	
	
	
	private static int DirectoryProcessed = 0;
	private static int file_copy_failed = 0;
	private static int file_copy_success = 0;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (debugMode==false)
			new File(outputDirectory).mkdir();
		// Mettre le dernier paramètre a true pour ne pas effectuer les opeération et debug.
		doRecursive(new File(inputDirectory), false, debugMode); // Ne deplace rien, affiche juste la console (dernier paramètre) // 22/02/2017
		System.out.println(":) - Directory processed : " + DirectoryProcessed);
		if (file_copy_failed>0)
			System.out.printf(":((- File copied [%s/%s]\r\n", file_copy_success, file_copy_failed+file_copy_success);
		else
			System.out.printf(":) - File copied [%s/%s]\r\n", file_copy_success, file_copy_failed+file_copy_success);
		
		
	}

	/**
	 * 
	 * @param f
	 * @param deleteOriginalFile
	 * @param debugMode N'execute aucune modification mais renvoye tout les messages de debbogages de ce qu'il est censé se passer.
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
	 * @param replaceOriginalFile Efface le fichier original pour le remplacer par le fichier processé
	 * @throws Exception
	 */
	private static void doRecursive(File f, boolean deleteOriginalFile, int level, boolean debugMode) {

//		System.err.println("---------------------------");
		String Path = f.getPath();
		
		// Ne retrie pas le repertoire de sortie prédefini par avant... (inutile dans cette méthode doit etre deporte ? ou ne pas sortir dans le repertoire de traitement et faire une vrai copie)
		if (Path.contains(outputDirectory))
			return;

		String listDir[] = f.list(new FilenameFilter_DIR());

		// Process directory recursively if exists
		if (listDir != null)
			for (int i = 0; i < listDir.length; i++) {
				File fils = new File(Path + "\\" + listDir[i]);
				doRecursive(fils, deleteOriginalFile, level + 1, debugMode);
		}
		
		boolean ID3TagPresent = false;
		Tag tag = null;

		String listFiles[] = f.list(new FilenameFilter_FILES());
		// Process files in this folder if exists
		if (listFiles != null) {
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
							System.out.println(":) - IDTag Present "+tag.getFieldCount()+ " - "+fils.getAbsolutePath());
						}
					} catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
						// System.err.println("Cannot threat : " + fils);
						continue;
					}
				}
			}
		}

		if (level>0) {
			if ((ID3TagPresent == true) ) {
				System.out.println(":) - SCANNING : " + Path + "(" + listFiles.length + ") ");
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
				System.out.printf(":) - SCN RSLT [%s] [%s] [%s] \r\n", ARTIST, ALBUM, YEAR);
				DirectoryProcessed++;
		
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
					} catch (Exception e) {
						e.printStackTrace();
						file_copy_failed++;
						System.err.printf(":( - Error COPYING [%s] to [%s] ",f.getAbsolutePath(), destinationDirectoryM.getAbsolutePath());
					}
					// System.err.println("Will delete :" + f);
					boolean ret = f.delete();
					
					if (ret==false)
						System.err.println(":( - Error DELETING "+f.getAbsolutePath());
				}
			}
			else
				System.err.println(":( - No Tag present for "+Path);
		}
	}

	private static void CopieRepertoire(File actualDirectory, File destinationDirectory) throws Exception {
		String[] filestoCopy = actualDirectory.list(new FilenameFilter_FILES_ALL());
		for (int i = 0; i < filestoCopy.length; i++) {
			File fin = new File(actualDirectory + "\\" + filestoCopy[i]);
			File fout = new File(destinationDirectory + "\\" + filestoCopy[i]);
			//System.err.println(fin+"->"+fout);
			
			Files.copy(fin.toPath(), fout.toPath(), REPLACE_EXISTING);
			boolean ret = fin.delete();
		//	boolean ret = fin.renameTo(fout);
			if (ret == false)
			{
				throw new Exception("Failed to delete : "+fin.getAbsolutePath());
			}
	
		}
	}

}
