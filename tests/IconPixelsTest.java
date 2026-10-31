package local.tv.lightlauncher;
import java.util.Arrays;
public class IconPixelsTest {
    static void check(boolean condition,String name){if(!condition)throw new AssertionError(name);}
    public static void main(String[] args){
        int[] white=new int[100];Arrays.fill(white,0xffffffff);for(int y=3;y<7;y++)for(int x=3;x<7;x++)white[y*10+x]=0xffff0000;
        int[] a=IconPixels.clean(white,10,10);check(a[0]==0,"white backdrop removed");check(a[44]==0xffff0000,"red mark preserved");
        int[] black=new int[100];Arrays.fill(black,0xff000000);for(int y=3;y<7;y++)for(int x=3;x<7;x++)black[y*10+x]=0xffff0000;
        a=IconPixels.clean(black,10,10);check(a[0]==0,"black backdrop removed");check(a[44]==0xffff0000,"red on black preserved");
        int[] coloured=new int[100];Arrays.fill(coloured,0xff1ed760);coloured[44]=0xff000000;
        a=IconPixels.clean(coloured,10,10);check(a[0]==0xff1ed760,"brand colour preserved");check(a[44]==0xff000000,"black detail on green preserved");
        int[] mark=new int[100];for(int i=0;i<10;i++)mark[i*10+i]=0xff000000;
        a=IconPixels.clean(mark,10,10);check(a[44]==0xffeeeeee,"isolated dark mark lifted");
        for(int i=0;i<10;i++)mark[i*10+i]=0xffffffff;
        a=IconPixels.clean(mark,10,10);check(a[44]==0xffffffff,"white transparent mark retained");
        a=IconPixels.clean(new int[100],10,10);check(Arrays.equals(a,new int[100]),"empty icon safe");
        int[] fringe=new int[400];Arrays.fill(fringe,0xffffffff);
        for(int y=5;y<15;y++)for(int x=5;x<15;x++)fringe[y*20+x]=0xffff0000;
        fringe[10*20+4]=0xffffaaaa;
        a=IconPixels.clean(fringe,20,20);
        check((a[204]&0xffffff)==0xff0000,"white matte removed from red edge");
        check((a[204]>>>24)>70&&(a[204]>>>24)<100,"edge coverage recovered");
        int[] detail=new int[400];for(int y=5;y<15;y++)for(int x=5;x<15;x++)detail[y*20+x]=0xffff0000;
        detail[204]=0xff202020;detail[210]=0xffffffff;
        a=IconPixels.clean(detail,20,20);
        check(a[204]==0xff202020,"dark logo keyline never inverted");
        check(a[210]==0xffffffff,"internal white detail retained");
        a=IconPixels.redWordmark(new int[]{0xffffffff,0xffffaaaa,0xffe50914});
        check(a[0]==0,"enclosed white in red wordmark removed");
        check((a[1]&0xffffff)==0xff0000&&(a[1]>>>24)==85,"red wordmark edge unmatted");
        check((a[2]>>>24)>240,"red wordmark body retained");
        System.out.println("PASS: 16 original-icon background/contrast checks");
    }
}
