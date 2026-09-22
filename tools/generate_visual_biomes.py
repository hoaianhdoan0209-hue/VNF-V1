from PIL import Image,ImageDraw
import os,sys,math,random,json,hashlib

ROOT=os.path.abspath(sys.argv[1] if len(sys.argv)>1 else ".")
OUT=os.path.join(ROOT,"app/src/main/res/drawable-nodpi")
PRE=os.path.join(ROOT,"app/build/visual-preview")
os.makedirs(OUT,exist_ok=True);os.makedirs(PRE,exist_ok=True)

W,H=800,360
REV="authored-organic-biome-v2-2026-09"
C={
 "home":((72,109,128),(181,186,151),(48,72,66),(91,112,75),(66,83,55),(215,171,103)),
 "garden":((97,139,154),(211,202,151),(53,86,63),(95,136,75),(68,98,54),(231,184,106)),
 "lakeside":((84,128,151),(191,202,174),(48,72,72),(75,108,83),(58,87,67),(213,187,122)),
 "grove":((54,87,84),(137,151,116),(32,52,48),(63,91,62),(45,69,51),(181,158,99))
}

def mix(a,b,t): return tuple(int(a[i]*(1-t)+b[i]*t) for i in range(3))
def palette(c):
    top,bot,dark,mid,ground,acc=c
    p=[(0,0,0)]
    ramps=[(top,bot),(dark,mid),(mid,ground),(ground,acc),(acc,(228,218,180))]
    for a,b in ramps:
        p += [mix(a,b,i/15) for i in range(16)]
    while len(p)<192:
        p.append(mix(dark,(205,202,170),(len(p)-81)/111))
    while len(p)<256: p.append((0,0,0))
    return [v for rgb in p for v in rgb], bytes([0]+[255]*191+[0]*64)

def layer(c,opaque=False):
    pal,alpha=palette(c)
    im=Image.new("P",(W,H),0); im.putpalette(pal); im.info["transparency"]=alpha
    d=ImageDraw.Draw(im)
    if opaque:
        for y in range(H):
            d.line((0,y,W,y),fill=1+int(15*y/(H-1)))
    return im,d,alpha

def save(im,a,path): im.save(path,optimize=True,compress_level=9,transparency=a)

def cluster(d,r,x,y,rx,ry,lo,hi,count=20):
    for _ in range(count):
        xx=x+r.randint(-rx,rx); yy=y+r.randint(-ry,ry)
        rw=r.randint(max(2,rx//7),max(4,rx//3)); rh=r.randint(max(2,ry//6),max(4,ry//2))
        d.ellipse((xx-rw,yy-rh,xx+rw,yy+rh),fill=r.randint(lo,hi))

def cloud(d,r,x,y,s):
    for ox,oy,rx,ry in [(-34,4,30,9),(0,0,40,13),(36,6,28,8),(4,-8,25,9)]:
        d.ellipse((x+ox*s-rx*s,y+oy*s-ry*s,x+ox*s+rx*s,y+oy*s+ry*s),fill=r.randint(10,15))

def ridge(d,r,y,amp,fill,step=10):
    pts=[(0,H)]
    for x in range(0,W+step,step):
        yy=y+math.sin(x/66)*amp+math.sin(x/23)*amp*.22+r.uniform(-2,2)
        pts.append((x,int(yy)))
    pts.append((W,H)); d.polygon(pts,fill=fill)

def trunk(d,r,x,y,h,w=5,fill=20):
    lean=r.randint(-7,7)
    d.polygon([(x-w,y),(x+w,y),(x+lean+w//2,y-h),(x+lean-w//2,y-h)],fill=fill)
    return x+lean,y-h

def tree(d,r,x,y,h,canopy=True):
    tx,ty=trunk(d,r,x,y,h,max(2,h//38),r.randint(18,24))
    if canopy:
        cluster(d,r,tx,ty+8,max(18,h//5),max(10,h//9),30,48,18)

def grass(d,r,x,y,h,lo=39,hi=55):
    for _ in range(4):
        dx=r.randint(-5,5); lean=r.randint(-8,8)
        d.line((x+dx,y,x+dx+lean,y-r.randint(max(4,h//2),h)),fill=r.randint(lo,hi),width=1)

def flowers(d,r,x0,x1,y,count):
    cols=[66,68,70,72,74]
    for _ in range(count):
        x=r.randint(x0,x1); stem=r.randint(7,18)
        d.line((x,y,x+r.randint(-2,2),y-stem),fill=r.randint(42,49),width=1)
        d.rectangle((x-1,y-stem-2,x+2,y-stem+1),fill=r.choice(cols))

def stone_path(d,r,center0,center1,y0=272):
    left=[];right=[]
    for i in range(25):
        t=i/24; y=int(y0+(H-y0)*t)
        center=center0*(1-t)+center1*t+math.sin(t*4.2)*9
        width=45+215*t
        left.append((int(center-width/2),y)); right.append((int(center+width/2),y))
    d.polygon(left+right[::-1],fill=58)
    for i in range(28):
        y=r.randint(y0+8,H-3); t=(y-y0)/(H-y0)
        center=center0*(1-t)+center1*t
        spread=24+100*t; x=int(center+r.uniform(-spread,spread))
        rw=r.randint(5,14); d.line((x-rw,y,x+rw,y+r.randint(-1,2)),fill=r.randint(59,64),width=2)

def draw_sky(name,c,r):
    im,d,a=layer(c,True)
    cloud(d,r,140,75,.85); cloud(d,r,420,55,1.0); cloud(d,r,682,93,.72)
    if name in ("home","garden"):
        d.ellipse((628,42,657,71),fill=70)
        d.rectangle((637,52,648,62),fill=72)
    if name=="grove":
        d.rectangle((0,0,W,32),fill=4)
        for x in range(0,W,26): d.rectangle((x,22,x+14,45+r.randint(0,18)),fill=r.randint(18,24))
    save(im,a,os.path.join(OUT,name+"_sky.png"))

def draw_distant(name,c,r):
    im,d,a=layer(c)
    ridge(d,r,165 if name!="grove" else 120,21,21)
    ridge(d,r,205 if name!="lakeside" else 187,12,26)
    if name=="lakeside":
        d.rectangle((0,228,W,274),fill=33)
        for y in range(233,272,9): d.line((20,y,780-r.randint(0,80),y),fill=r.randint(34,40),width=1)
        for x in range(-10,820,32): tree(d,r,x,229,r.randint(25,50))
    elif name=="grove":
        for x in range(-15,830,38): tree(d,r,x,255,r.randint(95,155))
        cluster(d,r,400,120,390,34,28,44,55)
    else:
        for x in range(-10,820,31): tree(d,r,x,252,r.randint(35,68))
        if name=="home":
            d.polygon([(302,226),(362,187),(426,226)],fill=20)
            d.rectangle((316,226,414,267),fill=37)
            d.rectangle((350,236,372,267),fill=65)
        else:
            d.line((80,246,720,242),fill=44,width=3)
            for x in range(100,720,48): d.line((x,242,x,260),fill=45,width=2)
    save(im,a,os.path.join(OUT,name+"_distant.png"))

def draw_mid(name,c,r):
    im,d,a=layer(c)
    if name=="home":
        tree(d,r,70,316,196); tree(d,r,734,316,184)
        d.polygon([(110,236),(174,195),(242,236)],fill=19)
        d.rectangle((126,236,225,292),fill=39)
        d.rectangle((159,246,183,292),fill=64)
        d.rectangle((194,248,214,269),fill=70)
        d.line((92,286,285,282),fill=44,width=4); d.line((520,282,792,286),fill=44,width=4)
        for x in list(range(105,286,30))+list(range(526,793,30)):
            d.line((x,273,x,296),fill=43,width=2)
        cluster(d,r,103,291,54,18,34,49,18); cluster(d,r,695,291,62,18,34,49,20)
    elif name=="garden":
        for x in range(0,260,30): cluster(d,r,x,292,24,15,34,51,10)
        for x in range(545,805,30): cluster(d,r,x,292,24,15,34,51,10)
        for bx in (155,650):
            pts=[(int(bx+math.cos(math.pi*i/28)*58),int(286-math.sin(math.pi*i/28)*96)) for i in range(29)]
            d.line(pts,fill=21,width=5)
            d.line([(x+4,y) for x,y in pts],fill=43,width=1)
        flowers(d,r,18,245,305,36); flowers(d,r,560,790,305,34)
        d.rectangle((315,262,485,273),fill=43)
        for x in range(320,486,28): d.line((x,252,x,282),fill=44,width=2)
    elif name=="lakeside":
        tree(d,r,48,320,178); tree(d,r,755,320,165)
        for x in list(range(0,230,9))+list(range(584,800,9)):
            h=r.randint(38,102); d.line((x,320,x+r.randint(-6,6),320-h),fill=r.randint(35,50),width=r.choice((1,2)))
        d.polygon([(0,291),(180,279),(245,302),(242,326),(0,334)],fill=48)
        d.polygon([(800,291),(622,279),(558,302),(560,326),(800,334)],fill=48)
        for x in range(0,150,24): d.ellipse((x,310,x+20,324),fill=r.randint(34,42))
    else:
        for x in (45,96,150,650,710,765):
            tx,ty=trunk(d,r,x,330,r.randint(190,275),r.randint(5,9),r.randint(18,24))
            cluster(d,r,tx,ty+18,45,25,29,47,18)
        for x in (135,185,617,670):
            d.line((x,319,x+r.randint(-8,8),125),fill=r.randint(18,25),width=r.randint(4,8))
            d.arc((x-45,185,x+50,330),190,330,fill=r.randint(26,34),width=2)
        for _ in range(26):
            x=r.choice(list(range(25,245))+list(range(555,785))); y=r.randint(265,318)
            d.ellipse((x,y,x+r.randint(4,10),y+r.randint(2,6)),fill=r.randint(31,46))
    save(im,a,os.path.join(OUT,name+"_mid.png"))

def draw_ground(name,c,r):
    im,d,a=layer(c)
    edge=[(-20,H)]+[(x,int(276+math.sin(x/28)*4+r.uniform(-2,2))) for x in range(-20,821,10)]+[(820,H)]
    d.polygon(edge,fill=50)
    if name=="home":
        stone_path(d,r,178,405)
        for x in range(20,780,38):
            if 290<x<520: continue
            d.ellipse((x,318+r.randint(-5,10),x+r.randint(8,17),326+r.randint(1,12)),fill=r.randint(47,56))
    elif name=="garden":
        stone_path(d,r,404,430)
        d.rectangle((38,296,260,324),fill=47); d.rectangle((540,296,770,324),fill=47)
        flowers(d,r,45,250,318,42); flowers(d,r,548,760,318,44)
    elif name=="lakeside":
        d.polygon([(0,278),(222,276),(292,309),(304,H),(0,H)],fill=48)
        d.polygon([(800,278),(579,276),(510,309),(495,H),(800,H)],fill=48)
        d.polygon([(286,317),(514,317),(500,H),(302,H)],fill=34)
        for y in range(326,H,8): d.line((305,y,495,y),fill=r.randint(35,40),width=1)
        for x in list(range(0,220,14))+list(range(580,800,14)): grass(d,r,x,H,r.randint(12,28),40,55)
    else:
        for sx in (28,102,168,631,702,774):
            d.line((sx,289,400+r.randint(-95,95),H),fill=r.randint(20,28),width=r.randint(3,7))
        for _ in range(42):
            x=r.randint(0,W); y=r.randint(292,H)
            d.line((x,y,x+r.randint(-12,12),y+r.randint(0,4)),fill=r.randint(30,46),width=1)
        for _ in range(14):
            x=r.choice(list(range(20,250))+list(range(560,780))); y=r.randint(305,352)
            d.rectangle((x,y,x+2,y+4),fill=r.randint(66,73))
    for x in range(0,W,13):
        if r.random()<.78: grass(d,r,x,H,r.randint(8,23),42,58)
    save(im,a,os.path.join(OUT,name+"_ground.png"))

def draw_foreground(name,c,r):
    im,d,a=layer(c)
    if name=="grove":
        for x in (9,62,742,792): trunk(d,r,x,H,r.randint(210,320),r.randint(7,12),r.randint(17,23))
        cluster(d,r,76,315,80,35,28,47,30); cluster(d,r,722,315,80,35,28,47,30)
        for x in range(-5,205,10): grass(d,r,x,H,r.randint(32,95),29,49)
        for x in range(595,805,10): grass(d,r,x,H,r.randint(32,95),29,49)
    elif name=="lakeside":
        tree(d,r,20,H,190); tree(d,r,780,H,188)
        for x in list(range(-5,180,9))+list(range(620,805,9)): grass(d,r,x,H,r.randint(36,105),31,49)
        for x in (35,80,715,760):
            d.line((x,H,x+r.randint(-5,5),278),fill=r.randint(34,48),width=2)
    else:
        tree(d,r,18,H,190); tree(d,r,782,H,190)
        cluster(d,r,62,324,50,28,31,50,16); cluster(d,r,738,324,50,28,31,50,16)
        for x in list(range(-5,195,11))+list(range(605,805,11)): grass(d,r,x,H,r.randint(28,82),31,50)
        if name=="garden":
            flowers(d,r,8,165,350,24); flowers(d,r,635,792,350,24)
    save(im,a,os.path.join(OUT,name+"_foreground.png"))

def make(name,c,seed):
    r=random.Random(seed)
    draw_sky(name,c,r); draw_distant(name,c,r); draw_mid(name,c,r); draw_ground(name,c,r); draw_foreground(name,c,r)

for i,(n,c) in enumerate(C.items()): make(n,c,6100+i*313)

for n in C:
    base=Image.open(os.path.join(OUT,n+"_sky.png")).convert("RGBA")
    for l in ("distant","mid","ground"):
        base=Image.alpha_composite(base,Image.open(os.path.join(OUT,n+"_"+l+".png")).convert("RGBA"))
    fg=Image.open(os.path.join(OUT,n+"_foreground.png")).convert("RGBA")
    aa=fg.getchannel("A").point(lambda x:int(x*198/255)); fg.putalpha(aa)
    Image.alpha_composite(base,fg).resize((1600,720),Image.Resampling.NEAREST).save(os.path.join(PRE,n+".png"))

mp=os.path.join(ROOT,"app/src/main/assets/visual/asset_manifest.json")
with open(mp,encoding="utf-8") as fh: j=json.load(fh)
sizes={n+"_"+l:os.path.getsize(os.path.join(OUT,n+"_"+l+".png")) for n in C for l in ("sky","distant","mid","ground","foreground")}
count=0
for a in j.get("assets",[]):
    if a.get("id") in sizes:
        a["bytes"]=sizes[a["id"]]
        a["visualRevision"]=REV
        a.pop("sha256",None)
        count+=1
if count!=20: raise RuntimeError("expected 20 biome assets")
j["visualRevision"]=REV
with open(mp,"w",encoding="utf-8") as fh:
    json.dump(j,fh,ensure_ascii=False,indent=2); fh.write("\n")

for k,v in sizes.items():
    digest=hashlib.sha256(open(os.path.join(OUT,k+".png"),"rb").read()).hexdigest()[:12]
    print(k,v,digest)
