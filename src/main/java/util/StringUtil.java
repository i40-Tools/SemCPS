package util;



/**
 * This class is a support class for string util methods
 * @author igrangel
 *
 */
public class StringUtil {

	public static String ontExpressivity = "";
	
	/**
	 * Return the String with the first letter in lowercase
	 * @param str
	 * @return
	 */
	public static String lowerCaseFirstChar(String str){
		return str.substring(0, 1).toLowerCase() + str.substring(1); 
	}
	
	public static String removeLastMinus(String str){
		return str.replaceAll("-","");
	}
	
	/**
	 * 
	 * @param str
	 * @param character
	 * @return
	 */
	public static String replaceChar(String str,String character){
		return str.replaceAll("\\",character);
	}
	
}
