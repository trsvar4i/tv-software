package local.tv.lightlauncher;
import java.util.Locale;
public final class SearchQuery {
    private SearchQuery() {}
    public static String normalize(String text){return text.toLowerCase(Locale.ROOT).replace('ё','е').trim();}
    public static boolean matches(String label,String pkg,String query){
        String aliases="";
        if(pkg.contains("youtube"))aliases+=" ютуб ютюб";
        if(pkg.contains("spotify"))aliases+=" спотифай музыка";
        if(pkg.contains("netflix"))aliases+=" нетфликс";
        if(pkg.contains("amazon"))aliases+=" амазон прайм видео";
        if(pkg.equals("com.android.vending"))aliases+=" гугл плей магазин";
        if(pkg.contains("browser"))aliases+=" браузер интернет";
        if(pkg.contains("games"))aliases+=" игры";
        String text=normalize(label+" "+pkg+aliases),value=normalize(query);
        if(value.isEmpty())return true;
        for(String token:value.split("\\s+"))if(!text.contains(token))return false;
        return true;
    }
}
