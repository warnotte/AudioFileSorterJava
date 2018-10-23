/**
 * 
 */
package be.warnotte.audiofilesortert;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author user
 *
 */
public class FilenameFilter_DIR implements FilenameFilter
{

	public boolean accept(File dir, String name)
	{
		File file = new File(dir.getAbsolutePath()+"\\"+name);
		if (file.isDirectory()==true)
			return true;
		return false;
	}
}
