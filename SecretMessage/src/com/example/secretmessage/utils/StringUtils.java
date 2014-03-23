package com.example.secretmessage.utils;


public class StringUtils
{
	static final String TAG = StringUtils.class.getSimpleName();

	public static String removeSpecialCharacters(String input)
	{
		String result = input;
        if(result.contains("-")){
            result = result.replace("-", "");
        }
        if(result.contains("(")){
            result = result.replace("(", "");
        }
        if(result.contains(")")){
            result = result.replace(")", "");
        }
        if(result.contains(".")){
            result = result.replace(".", "");
        }
        if(result.contains("/")){
            result = result.replace("/", "");
        }
        if(result.contains(",")){
            result = result.replace(",", "");
        }
        if(result.contains("#")){
            result = result.replace("#", "");
        }
        if(result.contains("*")){
            result = result.replace("*", "");
        }
        if(result.contains("+")){
            result = result.replace("+", "");
        }
        if(result.contains("N")){
            result = result.replace("N", "");
        }
        if(result.contains(";")){
            result = result.replace(";", "");
        }
        if(result.contains("%")){
            result = result.replace("%", "");
        }
        if(result.contains(" ")){
            result = result.replace(" ", "");
        }
        if(result.contains("@")){
            result = result.replace("@", "");
        }
        if(result.contains("$")){
            result = result.replace("$", "");
        }
        if(result.contains("^")){
            result = result.replace("^", "");
        }
        if(result.contains("&")){
            result = result.replace("&", "");
        }
        if(result.contains("_")){
            result = result.replace("_", "");
        }
        if(result.contains("=")){
            result = result.replace("=", "");
        }
        if(result.contains("~")){
            result = result.replace("~", "");
        }
        if(result.contains("`")){
            result = result.replace(";", "");
        }

        return result;

	}
	
}
