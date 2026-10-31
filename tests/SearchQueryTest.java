package local.tv.lightlauncher;
public class SearchQueryTest {
    static void check(boolean result,String name){if(!result)throw new AssertionError(name);}
    public static void main(String[] args){
        check(SearchQuery.matches("YouTube","com.google.android.youtube.tv","ютуб"),"Russian YouTube alias");
        check(SearchQuery.matches("YouTube","com.google.android.youtube.tv","  YOU tube "),"case and multiple terms");
        check(SearchQuery.matches("Ёлка","example.app","елка"),"Cyrillic yo normalization");
        check(SearchQuery.matches("Netflix","com.netflix.ninja","нетфликс"),"Russian Netflix alias");
        check(SearchQuery.matches("Google Play","com.android.vending","магазин"),"store alias");
        check(!SearchQuery.matches("Spotify","com.spotify.tv.android","ютуб"),"unrelated app excluded");
        check(SearchQuery.matches("Spotify","com.spotify.tv.android",""),"empty query");
        System.out.println("PASS: 7 search checks");
    }
}
