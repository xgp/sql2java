//$Id: GenerationTask.java,v 1.1 2005/10/12 18:44:24 framiere Exp $

package net.sourceforge.sql2java.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import net.sourceforge.sql2java.*;

public class GenerationTask extends Task {
	private String propertyFile;

	// The method executing the task
	public void execute() throws BuildException
	{
		System.out.println("GenerationTask: " + propertyFile);
		String args[] = new String[]{propertyFile};
		Main.main(args);
	}

	// The setter for the "propertyFile" attribute
	public void setPropertyFile(String msg) {
		this.propertyFile = msg;
	}
}