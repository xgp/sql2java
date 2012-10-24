//$Id: TableGenerationTask.java,v 1.1 2005/10/12 18:44:24 framiere Exp $

package net.sourceforge.sql2java.ant;

import java.util.*;

import net.sourceforge.sql2java.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class TableGenerationTask extends Task {
	private String propertyFile;
	private String tables;

	// The method executing the task
	public void execute() throws BuildException
	{
		System.out.println("TableGenerationTask: " + propertyFile);
		String args[] = new String[]{propertyFile};
		Map map = new HashMap();
		map.put("mgrwriter.include", tables);
		Main.main(args, map);
	}

	// The setter for the "propertyFile" attribute
	public void setPropertyFile(String msg) {
		this.propertyFile = msg;
	}

	// The setter for the "table" attribute
	public void setTables(String msg) {
		this.tables = msg;
	}
}