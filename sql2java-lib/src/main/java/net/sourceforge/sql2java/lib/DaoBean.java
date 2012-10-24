package net.sourceforge.sql2java.lib;

import java.util.Map;

/**
 * Base interface that is implemented by generated beans.
 */
public interface DaoBean
{
    public boolean isNew();
    public boolean isModified();
    public void resetIsModified();
    public Map<String,Object> getDictionary();
    public Map<String,Object> getPkDictionary();
    public String toString(String delim);
}
