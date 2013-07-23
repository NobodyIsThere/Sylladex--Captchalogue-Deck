package util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class RW
{
	/**
	 * Writes the given ArrayList to the specified file.
	 * @param lines - The ArrayList to write.
	 * @param file - The file to write.
	 */
	public static void writeFile(ArrayList<String> lines, File file)
	{
		try
		{
			if (!file.exists()) { Files.createFile(file.toPath()); }
			Files.write(file.toPath(), lines, Charset.defaultCharset());
		}
		catch (IOException x){ x.printStackTrace(); }
	}
	
	/**
	 * Reads the file to an ArrayList of String objects.
	 * @param file - The file to read from.
	 * @return An ArrayList of String objects corresponding to the lines of the file.
	 */
	public static ArrayList<String> readFile(File f)
	{
		List<String> lines = new ArrayList<String>();
		try
		{
			lines = Files.readAllLines(f.toPath(), Charset.defaultCharset());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return (ArrayList<String>) lines;
	}
}
