package MainProject.Utils;


import java.util.Scanner;

/*
 * This class contain 3 usefull tools to deal with json responses
 */
public class JSONParser {

    static public String parseFromString(String s, String value){
        s = s.replace("{", "");
        s = s.replace("}", "");
        s = s.replace("\"", "");
        s = s.replace(",", ":");
        s = s.replace("[", "");
        s = s.replace("]", "");
        Scanner sc = new Scanner(s).useDelimiter(":");
        Object obj = "";
        for (Scanner it = sc; it.hasNext(); ) {
            if(obj.toString().equals(value)){
                obj = it.next();
                return String.valueOf(obj);
            }else{
                obj = it.next();
            }
        }
        return null;
    }
    static public String[] parseFromString(String s, String[] values){
        String[] result = new String[values.length];
        s = s.replace("{", "");
        s = s.replace("}", "");
        s = s.replace("\"", "");
        s = s.replace(",", ":");
        s = s.replace("[", "");
        s = s.replace("]", "");
        Object obj = "";
        Integer index = 0;
        for(String value : values) {
            for (Scanner it = new Scanner(s).useDelimiter(":"); it.hasNext(); ) {
                if (obj.toString().equals(value)) {
                    obj = it.next();
                    if(value.equals("image")||value.equals("discord")||value.equals("twitter")||value.equals("website")){
                        if( String.valueOf(obj).equals("")){
                            result[index] = "null";
                            index++;
                            break;
                        }
                        result[index] = obj +":"+ it.next();
                    }else{
                        result[index] = String.valueOf(obj);
                    }
                    index++;
                    break;
                } else {
                    obj = it.next();
                }
            }
        }
        
        return result;
    }
    static public String[][] parseFromString(String s, String[] values, Integer nitem){

        String[][] result = new String[nitem][values.length];
        s = s.replace("{", "");
        s = s.replace("}", "");
        s = s.replace("\"", "");
        s = s.replace(",", ":");
        s = s.replace("[", "");
        s = s.replace("]", "");
        Object obj = "";
        Integer index, index2=0;
        for(String value : values) {
            index=0;
            for (Scanner it = new Scanner(s).useDelimiter(":"); it.hasNext(); ) {
                if (obj.toString().equals(value)) {
                    if(value.equals("image")||value.equals("discord")||value.equals("twitter")||value.equals("website")){
                        if( String.valueOf(obj).equals("")){
                            result[index][index2] = "null";
                            index++;
                            if(index>=nitem)break;
                        }
                        obj = it.next();
                        result[index][index2] = obj +":"+ it.next();
                    }else {
                        obj = it.next();
                        result[index][index2] = String.valueOf(obj);
                    }
                    index++;
                    if(index>=nitem)break;
                } else {
                    obj = it.next();
                }
            }
            index2++;
        }
        return result;
    }

}
