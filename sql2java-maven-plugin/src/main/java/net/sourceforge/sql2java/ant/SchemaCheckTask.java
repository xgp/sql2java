//$Id: SchemaCheckTask.java,v 1.1 2005/10/12 18:44:24 framiere Exp $

package net.sourceforge.sql2java.ant;

import java.util.*;

import net.sourceforge.sql2java.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class SchemaCheckTask extends Task {
	private String propertyFile;

	// The method executing the task
	public void execute() throws BuildException
	{
		System.out.println("SchemaCheckTask: " + propertyFile);
		String args[] = new String[] {propertyFile};
		Map map = new HashMap();
		map.put("check.only.database", "true");
		Main.main(args, map);
	}

	// The setter for the "propertyFile" attribute
	public void setPropertyFile(String msg) {
		this.propertyFile = msg;
	}
}