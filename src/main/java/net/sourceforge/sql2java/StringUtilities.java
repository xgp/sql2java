//$Id: StringUtilities.java,v 1.3 2005/10/17 22:04:10 framiere Exp $

package net.sourceforge.sql2java;

import java.io.*;

/**
 * @author Kelvin Nishikawa
 *
 * This utility class handles various SQL -> Java String conversions for code in various templates.
 * Use sharedInstance() to get the singleton instance.
 *
 * Most of these methods were excised to here from the CodeWriter class for good housekeeping.
 *
 */
public final class StringUtilities {
	static private StringUtilities singleton = new StringUtilities();

	// TODO: convert into velocity macros
	static public final String PREFIX = "";
	static public final String BASE_SUFFIX = "";
	static public final String MANAGER_SUFFIX = "Manager";
    //static public final String BEAN_SUFFIX = "Bean";
	static public final String BEAN_SUFFIX = "";
        static public final String RELATIONNAL_BEAN_SUFFIX = "Relationnal_Bean";
        static public final String HIBERNATE_MANAGER_SUFFIX = "Hibernate_Manager";
	static public final String ITERATOR_SUFFIX = "Iterator";
	static public final String FACTORY_SUFFIX = "Factory";
	static public final String COMPARATOR_SUFFIX = "Comparator";
	static public final String HTTP_FACTORY_SUFFIX = "Http_Factory";
	static public final String LISTENER_SUFFIX = "Listener";
	static public final String RENDERER_SUFFIX = "Renderer";
	static public final String EXCEPTION_SUFFIX = "Exception";
	static public final String WIDGETFACTORY_SUFFIX = "Widget_Factory";
	static public final String WIDGET_SUFFIX = "Widget";

	private StringUtilities () { }

	/** This is the default method for obtaining a StringUtilities instance.
	 * @return The shared instance
	 */
	static public synchronized StringUtilities getInstance() {
		return singleton;
	}

    public String getPackageAsPath(String pkg){
        if (pkg == null)
            return "";
        return pkg.replace('.', '/');
    }

    public String getDefaultRules(Column col){
        return col.getDefaultRules();
    }

    public String getPropertyTag(Column col){
        return col.getPropertyTag();
    }

	// TODO: convert into velocity macros
	public String convertClass(String table, String type)
    {
        String suffix = "";
        String postfix = "";
        if ("".equalsIgnoreCase(PREFIX) == false)
            suffix = suffix + "_";
        if ("".equalsIgnoreCase(type) == false)
            postfix =  "_" + type;
        return convertName(suffix + table + postfix);
    }

    public String getCoreClass(Table table)
    {
        return convertClass(table.getName(), BASE_SUFFIX);
    }

    public String getCoreClass(String table)
    {
        return convertClass(table, BASE_SUFFIX);
    }

    public String getBeanClass(Table table)
    {
        return convertClass(table.getName(), BEAN_SUFFIX);
    }

    public String getBeanClass(String table)
    {
        return convertClass(table, BEAN_SUFFIX);
    }

    public String getRelationnalBeanClass(Table table)
    {
        return convertClass(table.getName(), RELATIONNAL_BEAN_SUFFIX);
    }

    public String getRelationnalBeanClass(String table)
    {
        return convertClass(table, RELATIONNAL_BEAN_SUFFIX);
    }

    public String getFactoryClass(Table table)
    {
        return convertClass(table.getName(), FACTORY_SUFFIX);
    }

    public String getFactoryClass(String table)
    {
        return convertClass(table, FACTORY_SUFFIX);
    }

    public String getHttpFactoryClass(Table table)
    {
        return convertClass(table.getName(), HTTP_FACTORY_SUFFIX);
    }

    public String getHttpFactoryClass(String table)
    {
        return convertClass(table, HTTP_FACTORY_SUFFIX);
    }

    public String getListenerClass(Table table)
    {
        return convertClass(table.getName(), LISTENER_SUFFIX);
    }

    public String getListenerClass(String table)
    {
        return convertClass(table, LISTENER_SUFFIX);
    }

    public String getRendererClass(Table table)
    {
        return convertClass(table.getName(), RENDERER_SUFFIX);
    }

    public String getRendererClass(String table)
    {
        return convertClass(table, RENDERER_SUFFIX);
    }

    public String getWidgetFactoryClass(Table table)
    {
        return convertClass(table.getName(), WIDGETFACTORY_SUFFIX);
    }

    public String getWidgetFactoryClass(String table)
    {
        return convertClass(table, WIDGETFACTORY_SUFFIX);
    }

    public String getExceptionClass(Table table)
    {
        return convertClass(table.getName(), EXCEPTION_SUFFIX);
    }

    public String getExceptionClass(String table)
    {
        return convertClass(table, EXCEPTION_SUFFIX);
    }

    public String getWidgetClass(Table table)
    {
        return convertClass(table.getName(), WIDGET_SUFFIX);
    }

    public String getWidgetClass(String table)
    {
        return convertClass(table, WIDGET_SUFFIX);
    }

    public String getIteratorClass(Table table)
    {
        return convertClass(table.getName(), ITERATOR_SUFFIX);
    }

    public String getIteratorClass(String table)
    {
        return convertClass(table, ITERATOR_SUFFIX);
    }

    public String getManagerClass(Table table)
    {
        return convertClass(table.getName(), MANAGER_SUFFIX);
    }

    public String getManagerClass(String table)
    {
        return convertClass(table, MANAGER_SUFFIX);
    }

    public String getHibernateManagerClass(Table table)
    {
        return convertClass(table.getName(), HIBERNATE_MANAGER_SUFFIX);
    }

    public String getHibernateManagerClass(String table)
    {
        return convertClass(table, HIBERNATE_MANAGER_SUFFIX);
    }

    public String getComparatorClass(Table table)
    {
        return convertClass(table.getName(), COMPARATOR_SUFFIX);
    }

    public String getComparatorClass(String table)
    {
        return convertClass(table, COMPARATOR_SUFFIX);
    }

    public String getStringConvertionMethod(Column col){
        return col.getStringConvertionMethod();
    }

    public String getGetMethod(Column col) {
        return getGetMethod(col.getName());
    }

    public String getSetMethod(Column col) {
        return getSetMethod(col.getName());
    }

    public String getModifiedMethod(Column col) {
        return getModifiedMethod(col.getName());
    }

    public String getInitializedMethod(Column col) {
        return getInitializedMethod(col.getName());
    }

    public String getWidgetMethod(Column col) {
        return getWidgetMethod(col.getName());
    }


    public String getGetMethod(String  col) {
        return convertName("get_" + escape(col), true);
    }

    public String getSetMethod(String  col) {
        return convertName("set_" + escape(col), true);
    }

    public String getModifiedMethod(String  col) {
        return convertName("is_" + escape(col) + "_modified", true);
    }

    public String getInitializedMethod(String col) {
        return convertName("is_" + escape(col) + "_initialized", true);
    }

    public String getWidgetMethod(String col) {
        return convertName("get_" + escape(col) + "_widget", true);
    }

    public String getVarName(Column c) {
        return convertName(escape(c), true);
    }

    public String getVarName(String c) {
        return convertName(escape(c), true);
    }

    public String getModifiedVarName(Column c) {
        return getVarName(c) + "_is_modified"; // already escaped
    }

    public String getInitializedVarName(Column c) {
        return getVarName(c) + "_is_initialized";// already escaped
    }

    // foreign keys
    public String getForeignKeyVarName(Column c) {
        return convertName(escape(c) + "_object", true);
    }

    public String getForeignKeyModifiedVarName(Column c) {
        return getVarName(c) + "_object_is_modified"; // already escaped
    }

    public String getForeignKeyInitializedVarName(Column c) {
        return getVarName(c) + "_object_is_initialized";// already escaped
    }

    public String getForeignKeyInitializedMethod(Column col) {
        return convertName("is_" + escape(col) + "_object_initialized", true);
    }

    public String getForeignKeyGetMethod(Column col) {
        return convertName("get_" + escape(col) + "_object", true);
    }

    public String getForeignKeySetMethod(Column col) {
        return convertName("set_" + escape(col) + "_object", true);
    }

    public String getForeignKeyModifiedMethod(Column col) {
        return convertName("is_" + escape(col) + "_object_modified", true);
    }

    public String getForeignKeyVarName(String col) {
        return convertName(escape(col) + "_object", true);
    }

    public String getForeignKeyModifiedVarName(String c) {
        return getVarName(c) + "_object_is_modified"; // already escaped
    }

    public String getForeignKeyInitializedVarName(String c) {
        return getVarName(c) + "_object_is_initialized";// already escaped
    }

    public String getForeignKeyInitializedMethod(String col) {
        return convertName("is_" + escape(col) + "_object_initialized", true);
    }

    public String getForeignKeyGetMethod(String  col) {
        return convertName("get_" + escape(col) + "_object", true);
    }

    public String getForeignKeySetMethod(String  col) {
        return convertName("set_" + escape(col) + "_object", true);
    }

    public String getForeignKeyModifiedMethod(String  col) {
        return convertName("is_" + escape(col) + "_object_modified", true);
    }

    // imported keys
    public String getImportedKeyVarName(Column c) {
        return convertName(escape(c) + "_collection", true);
    }

    public String getImportedKeyModifiedVarName(Column c) {
        return getVarName(c) + "_collection_is_modified"; // already escaped
    }

    public String getImportedKeyInitializedVarName(Column c) {
        return getVarName(c) + "_collection_is_initialized";// already escaped
    }

    public String getImportedKeyInitializedMethod(Column col) {
        return convertName("is_" + escape(col) + "_collection_initialized", true);
    }

    public String getImportedKeyGetMethod(Column col) {
        return convertName("get_" + escape(col) + "_collection", true);
    }

    public String getImportedKeyAddMethod(Column col) {
        return convertName("add_" + escape(col) + "", true);
    }

    public String getImportedKeySetMethod(Column col) {
        return convertName("set_" + escape(col) + "_collection", true);
    }

    public String getImportedKeyModifiedMethod(Column col) {
        return convertName("is_" + escape(col) + "_collection_modified", true);
    }

    public String getImportedKeyVarName(String col) {
        return convertName(escape(col) + "_collection", true);
    }

    public String getImportedKeyModifiedVarName(String c) {
        return getVarName(c) + "_collection_is_modified"; // already escaped
    }

    public String getImportedKeyInitializedVarName(String c) {
        return getVarName(c) + "_collection_is_initialized";// already escaped
    }

    public String getImportedKeyInitializedMethod(String col) {
        return convertName("is_" + escape(col) + "_collection_initialized", true);
    }

    public String getImportedKeyGetMethod(String  col) {
        return convertName("get_" + escape(col) + "_collection", true);
    }

    public String getImportedKeyAddMethod(String col) {
        return convertName("add_" + escape(col) + "", true);
    }

    public String getImportedKeySetMethod(String  col) {
        return convertName("set_" + escape(col) + "_collection", true);
    }

    public String getImportedKeyModifiedMethod(String  col) {
        return convertName("is_" + escape(col) + "_collection_modified", true);
    }


    public String getJavaPrimaryType( Column c ) {
    	try {
    		return c.getJavaPrimaryType();
    	} catch ( Exception e ) {
    		return null;
    	}
    }

	public String convertName(String name) {
        return convertName(name, false);
    }

	public String convertName(Column col) {
        return convertName(col.getName(), false);
    }

	public String convertName(Table table) {
        return convertName(table.getName(), false);
    }

	/**
     * Converts name into a more Java-ish style name.
     * @author netkernel
     * <br>
     * Basically it looks for underscores, removes them, and makes the
     * letter after the underscore a capital letter. If wimpyCaps is true,
     * then the first letter of the name will be lowercase, otherwise it
     * will be uppercase. Here are some examples:
     * <p>
     * member_profile   becomes   MemberProfile
     * <br>
     * firstname&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp becomes&nbsp Firstname
     */
    public static String convertName(String name, boolean wimpyCaps) {
        StringBuffer buffer = new StringBuffer(name.length());
        //char list[] = name.toLowerCase().toCharArray();
        char list[] = name.toCharArray();
        for(int i = 0; i < list.length; i++) {
            if(i == 0 && !wimpyCaps) {
                buffer.append(Character.toUpperCase(list[i]));
            } else if(list[i] == '_' && (i+1) < list.length && i != 0) {
                buffer.append(Character.toUpperCase(list[++i]));
            } else buffer.append(list[i]);
        }
        return buffer.toString();
    }
// ORIGINAL CONVERT STRING
//     public static String convertName(String name, boolean wimpyCaps) {
//         StringBuffer buffer = new StringBuffer(name.length());
//         char list[] = name.toLowerCase().toCharArray();
//         for(int i = 0; i < list.length; i++) {
//             if(i == 0 && !wimpyCaps) {
//                 buffer.append(Character.toUpperCase(list[i]));
//             } else if(list[i] == '_' && (i+1) < list.length && i != 0) {
//                 buffer.append(Character.toUpperCase(list[++i]));
//             } else buffer.append(list[i]);
//         }
//         return buffer.toString();
//     }

    private String escape(String s) {
        return isReserved(s) ? ("my_"+s) : s;
    }

    private String escape(Column s) {
        return isReserved(s.getName()) ? ("my_"+s.getName()) : s.getName();
    }

    boolean isReserved(String s) {
        for(int i=0; i<reserved_words.length; i++) {
            if (s.compareToIgnoreCase(reserved_words[i]) == 0) {
                return true;
            }
        }
        return false;
    }

    static String [] reserved_words = new String[] {
        "null",
        "true",
        "false",
        "abstract",
        "double",
        "int",
        "strictfp",
        "boolean",
        "else",
        "interface",
        "super",
        "break",
        "extends",
        "long",
        "switch",
        "byte",
        "final",
        "native",
        "synchronized",
        "case",
        "finally",
        "new",
        "this",
        "catch",
        "float",
        "package",
        "throw",
        "char",
        "for",
        "private",
        "throws",
        "class",
        "goto",
        "protected",
        "transient",
        "const",
        "if",
        "public",
        "try",
        "continue",
        "implements",
        "return",
        "void",
        "default",
        "import",
        "short",
        "volatile",
        "do",
        "instanceof",
        "static",
        "while",
        "assert"
    };
}
