package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileVO
{
	public String file; //download url
	public String path; //where it should be saved to
	public List<String> listDelete;
	public Map<String, String> mapRename;
	public DownloadTask downloadTask;

	public FileVO()
	{
		listDelete = new ArrayList<String>();
		mapRename = new HashMap<String, String>();
	}
	public FileVO(String file, String path)
	{
		this();
		this.file = file;
		this.path = path;
	}
	public FileVO(String file, String path, List<String> listDelete, Map<String, String> mapRename)
	{
		this(file, path);
		this.listDelete = listDelete;
		this.mapRename = mapRename;
	}
}
