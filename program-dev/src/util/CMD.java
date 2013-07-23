package util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class CMD implements FileVisitor<Path>
{
	private Path source;
	private Path destination;
	private int operation;
	
	public static final int COPY = 0, MOVE = 1, DELETE = 2;
	
	public CMD (Path file, Path destination, int operation)
	{
		this.source = file;
		this.destination = destination;
		this.operation = operation;
	}
	
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
	{
		Path new_directory = destination.resolve(source.relativize(dir));
		try
		{
			if (operation == COPY)
			{
				Files.copy(dir, new_directory);
			}
			else if (operation == MOVE)
			{
				Files.move(dir, new_directory);
			}
		}
		catch (FileAlreadyExistsException x) {}
		catch (IOException x)
		{
			System.err.format("Unable to create directory");
			return FileVisitResult.SKIP_SUBTREE;
		}
		return FileVisitResult.CONTINUE;
	}
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
	{
		Path new_path = destination.resolve(source.relativize(file));
		try
		{
			if (operation == COPY)
			{
				Files.copy(file, new_path);
			}
			else if (operation == MOVE)
			{
				Files.move(file, new_path);
			}
			else if (operation == DELETE)
			{
				Files.delete(file);
			}
		}
		catch (FileAlreadyExistsException x) {}
		catch (IOException x)
		{
			System.err.format("Unable to create file." + file);
		}
		
		return FileVisitResult.CONTINUE;
	}
	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc)
	{
		System.err.format("Unable to visit file: %s", file);
		return FileVisitResult.CONTINUE;
	}
	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc)
	{
		if (operation == DELETE)
		{
			try
			{
				Files.delete(dir);
			}
			catch (IOException e)
			{
				System.err.format("Unable to delete directory." + dir);
			}
		}
		return FileVisitResult.CONTINUE;
	}
}
