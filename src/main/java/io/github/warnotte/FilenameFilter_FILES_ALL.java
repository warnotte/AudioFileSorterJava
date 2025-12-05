/**
 * 
 */
package io.github.warnotte;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author user
 *
 */
public class FilenameFilter_FILES_ALL implements FilenameFilter
{
	public boolean accept(File dir, String name)
	{
		File file = new File(dir.getAbsolutePath()+"\\"+name);
		
		if (file.isDirectory()==true)
			return false;
		return true;
	}
}
