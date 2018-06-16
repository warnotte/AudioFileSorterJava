/**
 * 
 */
package main;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author user
 *
 */
public class FilenameFilter_FILES implements FilenameFilter
{
	public boolean accept(File dir, String name)
	{
		File file = new File(dir.getAbsolutePath()+"\\"+name);
		
		if (file.isDirectory()==true)
			return false;
	/*	if ((
		//		file.getAbsolutePath().toLowerCase().endsWith("m3u") || 
		//		file.getAbsolutePath().toLowerCase().endsWith("nfo") || 
		//		file.getAbsolutePath().toLowerCase().endsWith("sfv") || 
		//		file.getAbsolutePath().toLowerCase().endsWith("jpg") || 
		//		file.getAbsolutePath().toLowerCase().endsWith("png") || 
		//		file.getAbsolutePath().toLowerCase().endsWith("gif") 
		))
		return false;*/
		return true;
	}
}
