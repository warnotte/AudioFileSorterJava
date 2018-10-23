package be.warnotte.audiofilesorter;

import java.io.File;
import java.io.IOException;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

public class ExtensionLister {
	
	static int DirectoryProcessed = 0;
	
	static String inputDirectory = "O:\\123";

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		doRecursive(new File(inputDirectory), false);
		System.err.println("Directory processed : " + DirectoryProcessed);
	}

	private static void doRecursive(File f, boolean deleteOriginalFile) {
		DirectoryProcessed = 0;
		doRecursive(f, deleteOriginalFile, 0);
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
	private static void doRecursive(File f, boolean deleteOriginalFile, int level) {

		System.err.println("---------------------------");
		String Path = f.getPath();

		String listDir[] = f.list(new FilenameFilter_DIR());
		String listFiles[] = f.list(new FilenameFilter_FILES());

		boolean ID3TagPresent = false;
		Tag tag = null;

		if (listDir != null)
			for (int i = 0; i < listDir.length; i++) {

				File fils = new File(Path + "\\" + listDir[i]);
				doRecursive(fils, deleteOriginalFile, level + 1);
			}
		if (listFiles != null) {
			for (int i = 0; i < listFiles.length; i++) {
				File fils = new File(Path + "\\" + listFiles[i]);
				if (fils.getName().contains("."))
					System.err.println(""+fils.getName().substring(fils.getName().lastIndexOf(".")+1));
			}
		}

		
	}


}
