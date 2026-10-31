package local.tv.lightlauncher;

/** Background extraction for original app artwork. No Android dependencies. */
public final class IconPixels {
    private IconPixels() {}
    /** Red wordmarks have no white lettering; remove the baked-in white matte
     * including enclosed counters that a background flood fill cannot reach. */
    public static int[] redWordmark(int[] input){
        int[] out=input.clone();
        for(int i=0;i<out.length;i++){
            int c=out[i],a=c>>>24,r=(c>>>16)&255,g=(c>>>8)&255,b=c&255;
            int neutral=Math.min(r,Math.min(g,b));float coverage=1-neutral/255f;
            if(coverage<.04f||r<=g+12||r<=b+12){out[i]=0;continue;}
            out[i]=(Math.round(a*coverage)<<24)|(Math.min(255,Math.round((r-neutral)/coverage))<<16)|(Math.min(255,Math.round((g-neutral)/coverage))<<8)|Math.min(255,Math.round((b-neutral)/coverage));
        }
        return out;
    }
    private static int kind(int c) {
        if ((c >>> 24) < 16) return 0;
        int r=(c>>>16)&255,g=(c>>>8)&255,b=c&255;
        int hi=Math.max(r,Math.max(g,b)),lo=Math.min(r,Math.min(g,b));
        if (hi-lo>32) return 0;
        return lo>205 ? 1 : hi<48 ? 2 : 0;
    }
    public static int[] clean(int[] input,int w,int h) {
        int[] out=input.clone();int n=w*h,left=w,top=h,right=-1,bottom=-1,opaque=0;
        for(int i=0;i<n;i++)if((input[i]>>>24)>32){int x=i%w,y=i/w;left=Math.min(left,x);right=Math.max(right,x);top=Math.min(top,y);bottom=Math.max(bottom,y);opaque++;}
        if(right<left)return out;
        int area=(right-left+1)*(bottom-top+1);boolean removed=false;
        boolean[] seen=new boolean[n];int[] queue=new int[n];int[] matte=new int[n];
        if(opaque>area*.65f)for(int start=0;start<n;start++){
            int background=kind(input[start]);if(background==0||seen[start])continue;
            int head=0,tail=0,l=w,t=h,r=-1,b=-1;queue[tail++]=start;seen[start]=true;
            while(head<tail){int i=queue[head++],x=i%w,y=i/w;l=Math.min(l,x);r=Math.max(r,x);t=Math.min(t,y);b=Math.max(b,y);
                if(x>0&&!seen[i-1]&&kind(input[i-1])==background){seen[i-1]=true;queue[tail++]=i-1;}
                if(x<w-1&&!seen[i+1]&&kind(input[i+1])==background){seen[i+1]=true;queue[tail++]=i+1;}
                if(y>0&&!seen[i-w]&&kind(input[i-w])==background){seen[i-w]=true;queue[tail++]=i-w;}
                if(y<h-1&&!seen[i+w]&&kind(input[i+w])==background){seen[i+w]=true;queue[tail++]=i+w;}
            }
            boolean plate=tail>area*.18f&&(r-l+1)>(right-left+1)*.72f&&(b-t+1)>(bottom-top+1)*.72f&&opaque-tail>area*.025f;
            if(plate){for(int j=0;j<tail;j++){out[queue[j]]=0;matte[queue[j]]=background;}removed=true;}
        }
        // Undo background colour mixed into antialiased edges. Work from the original
        // mask so this cannot erode progressively into white details inside a logo.
        if(removed)for(int i=0;i<n;i++){
            int c=out[i],a=c>>>24;if(a==0)continue;
            int x=i%w,y=i/w,bg=0,best=100;
            for(int dy=-4;dy<=4;dy++)for(int dx=-4;dx<=4;dx++){
                int xx=x+dx,yy=y+dy,d=dx*dx+dy*dy;
                if(xx>=0&&xx<w&&yy>=0&&yy<h&&d<best&&matte[yy*w+xx]!=0){bg=matte[yy*w+xx];best=d;}
            }
            if(bg==0)continue;
            int r=(c>>>16)&255,g=(c>>>8)&255,b=c&255;
            int lo=Math.min(r,Math.min(g,b)),hi=Math.max(r,Math.max(g,b));
            float coverage=bg==1?1-lo/255f:hi/255f;
            if(coverage<.035f){out[i]=0;continue;}
            int base=bg==1?lo:0;
            r=Math.min(255,Math.round((r-base)/coverage));g=Math.min(255,Math.round((g-base)/coverage));b=Math.min(255,Math.round((b-base)/coverage));
            out[i]=(Math.round(a*coverage)<<24)|(r<<16)|(g<<8)|b;
        }
        // A dark keyline beside colour belongs to the original logo. Only lift
        // standalone monochrome artwork; never turn coloured-logo shadows white.
        if(removed||opaque<area*.65f)for(int i=0;i<n;i++){
            int c=out[i],a=c>>>24;if(a==0)continue;int r=(c>>>16)&255,g=(c>>>8)&255,b=c&255;
            int hi=Math.max(r,Math.max(g,b)),lo=Math.min(r,Math.min(g,b));
            if(hi<112&&hi-lo<28){
                boolean colour=false;int x=i%w,y=i/w;
                for(int dy=-4;dy<=4&&!colour;dy++)for(int dx=-4;dx<=4;dx++){
                    int xx=x+dx,yy=y+dy;if(xx<0||xx>=w||yy<0||yy>=h)continue;
                    int p=input[yy*w+xx],pr=(p>>>16)&255,pg=(p>>>8)&255,pb=p&255;
                    if((p>>>24)>32&&Math.max(pr,Math.max(pg,pb))-Math.min(pr,Math.min(pg,pb))>45){colour=true;break;}
                }
                if(!colour)out[i]=(a<<24)|0xeeeeee;
            }
        }
        return out;
    }
}
