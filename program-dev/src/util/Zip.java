package util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Zip
{
	private static final int BUFFER = 4096;
	
	public static void unzipFile(File path, File destination)
	{
		try
		{
			if (!destination.exists())
			{
				destination.mkdir();
			}

			ZipInputStream input = new ZipInputStream(new FileInputStream(path));

			ZipEntry entry = input.getNextEntry();

			while (entry != null)
			{
				String filePath = destination + File.separator + entry.getName();
				if (!entry.isDirectory())
				{
					extractFile(input, filePath);
				}
				else
				{
					File dir = new File(filePath);
					dir.mkdir();
				}
				input.closeEntry();
				entry = input.getNextEntry();
			}
			input.close();
		}
		catch (IOException x)
		{
			Util.error("Unable to decompress file " + path);
		}
	}

	private static void extractFile(ZipInputStream input, String path) throws IOException
	{
		BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(path));
		byte[] bytesIn = new byte[BUFFER];
		int read = 0;
		while ((read = input.read(bytesIn)) != -1)
		{
			output.write(bytesIn, 0, read);
		}
		output.close();
	}
}
