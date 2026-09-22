from PIL import Image,ImageDraw,ImageFilter,ImageEnhance,ImageChops
import os,sys,math,random,json,hashlib

ROOT=os.path.abspath(sys.argv[1] if len(sys.argv)>1 else ".")
OUT=os.path.join(ROOT,"app/src/main/res/drawable-nodpi")
PRE=os.path.join(ROOT,"app/build/visual-preview")
os.makedirs(OUT,exist_ok=True);os.makedirs(PRE,exist_ok=True)

W,H=800,360
OUT_W,OUT_H=1600,720
REV="authored-organic-biome-v7-cinematic-light-2026-09"
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

def _masked_texture(base,mask,r,kind,biome):
    ov=Image.new("RGBA",base.size,(0,0,0,0)); d=ImageDraw.Draw(ov)
    w,h=base.size
    if kind=="sky":
        # atmospheric glow and horizon haze; true RGBA is intentional
        glow={"home":(255,205,132),"garden":(255,217,154),"lakeside":(208,231,242),"grove":(174,210,174)}[biome]
        gx,gy={"home":(1285,112),"garden":(1290,108),"lakeside":(220,108),"grove":(800,130)}[biome]
        for rad,alpha in ((150,12),(105,18),(65,25)):
            d.ellipse((gx-rad,gy-rad,gx+rad,gy+rad),fill=(*glow,alpha))
        for i in range(10):
            y=390+i*15
            d.rectangle((0,y,w,y+8),fill=(*glow,max(2,10-i)))
    else:
        # layer-local micro texture, masked so transparency stays clean
        count={"distant":420,"mid":620,"ground":1100,"foreground":780}.get(kind,350)
        light={"home":(232,205,146),"garden":(234,219,154),"lakeside":(196,224,219),"grove":(184,202,155)}[biome]
        dark={"home":(45,54,43),"garden":(49,72,48),"lakeside":(42,66,65),"grove":(28,43,38)}[biome]
        for _ in range(count):
            x=r.randrange(w); y=r.randrange(h)
            if r.random()<.56:
                col=(*light,r.randint(10,32))
            else:
                col=(*dark,r.randint(8,25))
            if kind in ("ground","foreground") and y<h*.55: continue
            sz=r.choice((1,1,2,2,3))
            d.rectangle((x,y,x+sz,y+r.choice((0,1,2))),fill=col)
        if kind=="distant":
            fog={"home":(214,205,174),"garden":(222,214,175),"lakeside":(205,224,220),"grove":(146,169,145)}[biome]
            for i in range(5):
                y=390+i*18
                d.rectangle((0,y,w,y+10),fill=(*fog,max(3,15-i*2)))
        if kind=="foreground":
            # soft edge vignette from authored foreground only, center remains open
            shade=(12,20,17,0)
            for pad,alpha in ((0,36),(40,24),(80,14)):
                d.rectangle((0,0,170-pad,h),fill=(12,20,17,alpha))
                d.rectangle((w-170+pad,0,w,h),fill=(12,20,17,alpha))
    if mask is not None:
        ov.putalpha(ImageChops.multiply(ov.getchannel("A"),mask))
    return Image.alpha_composite(base,ov)

def _signature_detail(base,mask,r,kind,biome):
    ov=Image.new("RGBA",base.size,(0,0,0,0)); d=ImageDraw.Draw(ov)
    w,h=base.size

    def glow_dot(x,y,radius,col,alpha):
        for rr,aa in ((radius*3,alpha//5),(radius*2,alpha//3),(radius,alpha)):
            d.ellipse((x-rr,y-rr,x+rr,y+rr),fill=(*col,max(1,aa)))

    if biome=="home":
        if kind=="mid":
            # warm lived-in focal glow around the cottage windows/door
            for x,y,rr in ((409,520,8),(354,535,6),(430,520,5)):
                glow_dot(x,y,rr,(255,190,92),34)
            for _ in range(26):
                x=r.randint(210,560); y=r.randint(505,640)
                if r.random()<.55: glow_dot(x,y,1,(255,221,145),r.randint(16,34))
        elif kind=="ground":
            for _ in range(70):
                x=r.randint(40,w-40); y=r.randint(int(h*.77),h-8)
                d.line((x,y,x+r.randint(-5,5),y-r.randint(2,8)),fill=(229,210,153,r.randint(12,28)),width=1)
        elif kind=="foreground":
            # warm near-camera rim catches on leaves
            for _ in range(46):
                side=r.choice(("l","r")); x=r.randint(0,250) if side=="l" else r.randint(w-250,w)
                y=r.randint(int(h*.45),h-10)
                glow_dot(x,y,1,(246,199,118),r.randint(18,35))

    elif biome=="garden":
        if kind in ("mid","foreground"):
            # floating petals provide movement and a romantic authored identity
            for _ in range(52 if kind=="foreground" else 34):
                x=r.randint(25,w-25); y=r.randint(int(h*.33),h-20)
                if kind=="mid" and 560<x<1040 and y>520: continue
                col=r.choice(((255,213,208),(246,188,189),(255,228,171),(235,218,245)))
                ww=r.randint(2,6); hh=r.randint(1,3)
                d.ellipse((x-ww,y-hh,x+ww,y+hh),fill=(*col,r.randint(40,82)))
        if kind=="ground":
            for _ in range(95):
                x=r.choice((r.randint(70,510),r.randint(1090,1530))); y=r.randint(585,705)
                glow_dot(x,y,1,r.choice(((255,211,131),(255,186,180),(226,207,255))),r.randint(18,34))

    elif biome=="lakeside":
        if kind=="distant":
            # fine horizon sparkle bands
            for _ in range(90):
                y=r.randint(455,548); x=r.randint(25,w-25)
                ww=r.randint(4,20)
                d.line((x,y,x+ww,y),fill=(221,241,238,r.randint(15,40)),width=1)
        elif kind=="ground":
            # layered water reflection, narrow in center and brighter toward horizon
            for _ in range(110):
                y=r.randint(632,710)
                span=int(80+(y-632)*2.1)
                x=r.randint(max(0,w//2-span),min(w,w//2+span))
                ww=r.randint(5,36)
                col=r.choice(((214,236,231),(188,222,220),(237,215,158)))
                d.line((x,y,min(w-1,x+ww),y),fill=(*col,r.randint(18,55)),width=r.choice((1,1,2)))
        elif kind=="foreground":
            for _ in range(32):
                x=r.choice((r.randint(0,300),r.randint(w-300,w))); y=r.randint(500,h-10)
                d.line((x,y,x+r.randint(-8,8),y-r.randint(18,52)),fill=(176,203,157,r.randint(18,34)),width=2)

    elif biome=="grove":
        if kind=="sky":
            # cinematic canopy light shafts, intentionally subtle and translucent
            for cx,width,alpha in ((610,115,18),(805,90,24),(980,130,14)):
                d.polygon([(cx-width,90),(cx+width,90),(cx+width//3,h),(cx-width//3,h)],fill=(205,226,166,alpha))
        elif kind in ("mid","foreground"):
            # spores/fireflies at varying depths
            count=36 if kind=="mid" else 52
            for _ in range(count):
                x=r.randint(30,w-30); y=r.randint(220,h-30)
                if 590<x<1010 and y>535 and kind=="foreground": continue
                col=r.choice(((211,230,151),(238,214,125),(170,222,179)))
                glow_dot(x,y,r.choice((1,1,2)),col,r.randint(28,62))
        elif kind=="ground":
            for _ in range(44):
                x=r.choice((r.randint(20,520),r.randint(1080,w-20))); y=r.randint(590,h-10)
                glow_dot(x,y,1,(216,209,132),r.randint(18,42))

    if mask is not None and kind!="sky":
        ov.putalpha(ImageChops.multiply(ov.getchannel("A"),mask))
    return Image.alpha_composite(base,ov)

def _cinematic_light(base,mask,kind,biome):
    w,h=base.size
    light=Image.new("RGBA",(w,h),(0,0,0,0))
    d=ImageDraw.Draw(light)

    # High-resolution lighting pass only. Geometry remains crisp; blur is limited
    # to translucent illumination so the scene reads as polished rather than soft.
    cfg={
        "home":{"warm":(255,184,105),"cool":(77,109,125),"source":(1320,120)},
        "garden":{"warm":(255,214,150),"cool":(97,133,145),"source":(1275,105)},
        "lakeside":{"warm":(238,205,143),"cool":(102,157,177),"source":(230,110)},
        "grove":{"warm":(204,225,163),"cool":(48,81,72),"source":(820,115)}
    }[biome]
    warm,cool=cfg["warm"],cfg["cool"]
    sx,sy=cfg["source"]

    if kind=="sky":
        # broad ambient color separation
        for radius,alpha in ((430,20),(300,26),(190,34)):
            d.ellipse((sx-radius,sy-radius,sx+radius,sy+radius),fill=(*warm,alpha))
        d.rectangle((0,int(h*.58),w,h),fill=(*warm,8))
        d.rectangle((0,0,w,int(h*.35)),fill=(*cool,7))
    elif kind=="distant":
        # atmospheric perspective: distant planes receive haze, never foreground blur
        d.rectangle((0,int(h*.42),w,h),fill=(*cool,10))
        d.rectangle((0,int(h*.62),w,h),fill=(*warm,7))
    elif kind=="mid":
        # directional soft key from the authored light source
        cone=Image.new("RGBA",(w,h),(0,0,0,0)); cd=ImageDraw.Draw(cone)
        if biome=="home":
            cd.polygon([(sx-170,90),(sx+80,90),(980,h),(720,h)],fill=(*warm,14))
            cd.ellipse((320,455,520,640),fill=(*warm,20))
        elif biome=="garden":
            cd.polygon([(sx-210,80),(sx+100,80),(1020,h),(690,h)],fill=(*warm,13))
            cd.ellipse((690,455,920,625),fill=(*warm,12))
        elif biome=="lakeside":
            cd.polygon([(sx-80,70),(sx+170,70),(770,h),(430,h)],fill=(*warm,12))
            cd.rectangle((575,545,1030,640),fill=(*cool,8))
        else:
            for cx,ww,aa in ((610,115,16),(820,92,22),(1010,132,13)):
                cd.polygon([(cx-ww,85),(cx+ww,85),(cx+ww//3,h),(cx-ww//3,h)],fill=(*warm,aa))
        cone=cone.filter(ImageFilter.GaussianBlur(26))
        light=Image.alpha_composite(light,cone)
    elif kind=="ground":
        # contact glow and subtle lower-frame bounce
        d.rectangle((0,int(h*.72),w,h),fill=(*warm,7 if biome!="grove" else 5))
        if biome=="lakeside":
            d.ellipse((570,610,1035,760),fill=(*warm,12))
        elif biome=="garden":
            d.ellipse((540,570,1060,760),fill=(*warm,8))
        elif biome=="home":
            d.ellipse((330,575,960,760),fill=(*warm,9))
    elif kind=="foreground":
        # near-camera foliage should frame, not flatten, the center
        edge=Image.new("RGBA",(w,h),(0,0,0,0)); ed=ImageDraw.Draw(edge)
        ed.ellipse((-260,220,380,h+180),fill=(*cool,20))
        ed.ellipse((w-380,220,w+260,h+180),fill=(*cool,20))
        if biome in ("home","garden"):
            ed.ellipse((-220,390,300,h+80),fill=(*warm,10))
            ed.ellipse((w-300,390,w+220,h+80),fill=(*warm,10))
        edge=edge.filter(ImageFilter.GaussianBlur(34))
        light=Image.alpha_composite(light,edge)

    if kind in ("sky","distant","ground"):
        light=light.filter(ImageFilter.GaussianBlur(18 if kind=="sky" else 12))

    if mask is not None and kind!="sky":
        light.putalpha(ImageChops.multiply(light.getchannel("A"),mask))
    return Image.alpha_composite(base,light)

def save(im,a,path):
    name=os.path.basename(path).replace(".png","")
    biome,kind=name.split("_",1)
    rgba=im.convert("RGBA").resize((OUT_W,OUT_H),Image.Resampling.NEAREST)
    # Preserve authored transparent layers while allowing richer light/texture detail.
    mask=rgba.getchannel("A")
    r=random.Random("hires-"+name)
    rgba=_masked_texture(rgba,mask,r,kind,biome)
    rgba=_signature_detail(rgba,mask,r,kind,biome)
    rgba=_cinematic_light(rgba,mask,kind,biome)
    # Subtle tonal polish differs by depth. No blur on authored geometry.
    if kind=="sky":
        rgba=ImageEnhance.Color(rgba).enhance(1.10)
        rgba=ImageEnhance.Contrast(rgba).enhance(1.04)
    elif kind in ("mid","foreground"):
        rgba=ImageEnhance.Contrast(rgba).enhance(1.10)
        rgba=ImageEnhance.Color(rgba).enhance(1.12)
    else:
        rgba=ImageEnhance.Contrast(rgba).enhance(1.04)
        rgba=ImageEnhance.Color(rgba).enhance(1.08)
    rgba.save(path,optimize=True,compress_level=9)

def cluster(d,r,x,y,rx,ry,lo,hi,count=20):
    for _ in range(count):
        xx=x+r.randint(-rx,rx); yy=y+r.randint(-ry,ry)
        rw=r.randint(max(2,rx//7),max(4,rx//3)); rh=r.randint(max(2,ry//6),max(4,ry//2))
        d.ellipse((xx-rw,yy-rh,xx+rw,yy+rh),fill=r.randint(lo,hi))

def cloud(d,r,x,y,s):
    # layered, hand-shaped pixel cloud rather than a single flat blob
    for ox,oy,rx,ry,shade in [(-34,4,30,9,11),(0,0,40,13,12),(36,6,28,8,11),(4,-8,25,9,13)]:
        d.ellipse((x+ox*s-rx*s,y+oy*s-ry*s,x+ox*s+rx*s,y+oy*s+ry*s),fill=shade+r.randint(-1,1))
    d.line((x-38*s,y+10*s,x+38*s,y+10*s),fill=9,width=max(1,int(s*2)))

def pixel_sparkle(d,x,y,fill=74):
    d.point((x,y),fill=fill)
    d.point((x-1,y),fill=fill)
    d.point((x+1,y),fill=fill)
    d.point((x,y-1),fill=fill)

def dither_patch(d,r,box,lo,hi,count):
    x0,y0,x1,y1=box
    for _ in range(count):
        x=r.randint(x0,x1); y=r.randint(y0,y1)
        d.rectangle((x,y,x+r.choice((1,1,2)),y+r.choice((0,1))),fill=r.randint(lo,hi))

def sun_rays(d,x,y,fill):
    for w in (110,78,46):
        d.polygon([(x-w,y),(x+w,y),(x+int(w*.38),H),(x-int(w*.38),H)],fill=fill)

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
    # subtle horizontal color banding keeps the pixel-art sky from feeling flat
    for y in range(18,H,26):
        if r.random()<.7:
            d.line((0,y,W,y),fill=min(15,2+y//32),width=1)
    cloud(d,r,140,75,.85); cloud(d,r,420,55,1.0); cloud(d,r,682,93,.72)
    if name in ("home","garden"):
        d.ellipse((624,38,661,75),fill=69)
        d.ellipse((630,44,655,69),fill=73)
        for dx,dy in [(-10,8),(9,-7),(14,10)]:
            pixel_sparkle(d,643+dx,56+dy,75)
    if name=="lakeside":
        d.ellipse((95,45,119,69),fill=67)
        d.line((105,69,105,98),fill=16,width=1)
    if name=="grove":
        d.rectangle((0,0,W,28),fill=4)
        for x in range(0,W,22):
            d.rectangle((x,18,x+r.randint(8,18),42+r.randint(0,22)),fill=r.randint(18,24))
        sun_rays(d,395,42,7)
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
            # distant cottage mass + chimney smoke silhouette
            d.polygon([(296,226),(362,184),(432,226)],fill=20)
            d.polygon([(307,224),(362,190),(418,224)],fill=26)
            d.rectangle((314,226,416,269),fill=37)
            d.rectangle((351,236,374,269),fill=64)
            d.rectangle((384,234,402,250),fill=69)
            d.rectangle((326,204,334,222),fill=22)
            for i in range(4):
                d.ellipse((320+i*7,195-i*6,334+i*7,205-i*6),fill=12+i)
        else:
            d.line((80,246,720,242),fill=44,width=3)
            for x in range(100,720,48): d.line((x,242,x,260),fill=45,width=2)
    save(im,a,os.path.join(OUT,name+"_distant.png"))

def draw_mid(name,c,r):
    im,d,a=layer(c)
    if name=="home":
        tree(d,r,62,316,202); tree(d,r,742,316,190)
        # main cottage: asymmetric roof, porch, warm windows, roof texture
        d.polygon([(102,239),(176,189),(252,239)],fill=18)
        d.polygon([(116,237),(177,198),(238,237)],fill=26)
        for x in range(126,230,13):
            d.line((x,214+(x%3),x+18,226+(x%4)),fill=r.randint(22,29),width=2)
        d.rectangle((122,238,232,296),fill=39)
        d.rectangle((155,247,184,296),fill=62)
        d.rectangle((194,247,219,272),fill=69)
        d.rectangle((198,251,215,268),fill=74)
        d.line((206,251,206,268),fill=61,width=1)
        d.line((198,259,215,259),fill=61,width=1)
        d.rectangle((113,285,242,292),fill=33)
        # porch lantern + tiny table make home feel inhabited
        d.line((230,247,230,286),fill=31,width=2)
        d.rectangle((224,252,236,264),fill=68)
        d.rectangle((226,254,234,262),fill=74)
        d.line((246,278,270,278),fill=42,width=3)
        d.line((250,278,248,290),fill=40,width=2); d.line((266,278,268,290),fill=40,width=2)
        d.line((92,286,286,282),fill=44,width=4); d.line((516,282,792,286),fill=44,width=4)
        for x in list(range(105,286,30))+list(range(522,793,30)):
            d.line((x,272,x,298),fill=43,width=2)
            d.line((x,278,x+24,278),fill=45,width=1)
        cluster(d,r,101,292,58,18,34,49,22); cluster(d,r,699,292,66,18,34,49,24)
        flowers(d,r,38,150,312,16); flowers(d,r,650,776,312,16)
        dither_patch(d,r,(125,241,229,294),36,43,42)
    elif name=="garden":
        for x in range(0,260,28): cluster(d,r,x,292,25,16,34,51,11)
        for x in range(545,805,28): cluster(d,r,x,292,25,16,34,51,11)
        # twin rose arches frame the path without blocking Haru
        for bx in (150,650):
            pts=[(int(bx+math.cos(math.pi*i/30)*60),int(286-math.sin(math.pi*i/30)*99)) for i in range(31)]
            d.line(pts,fill=21,width=6)
            d.line([(x+4,y) for x,y in pts],fill=43,width=1)
            for i,(x,y) in enumerate(pts[3:-3:4]):
                d.ellipse((x-3,y-3,x+3,y+3),fill=70+(i%4))
        flowers(d,r,14,252,307,52); flowers(d,r,552,792,307,50)
        # small pergola/bench focal point
        d.rectangle((316,260,484,272),fill=43)
        d.rectangle((337,245,345,286),fill=42); d.rectangle((455,245,463,286),fill=42)
        for x in range(322,483,22): d.line((x,251,x,282),fill=44,width=2)
        d.rectangle((362,278,438,285),fill=47)
        d.rectangle((370,286,377,300),fill=45); d.rectangle((423,286,430,300),fill=45)
        # small stone fountain sits behind Haru's readable center
        d.ellipse((382,244,418,253),fill=52)
        d.rectangle((389,252,411,265),fill=51)
        d.ellipse((383,260,417,270),fill=55)
        d.line((400,238,400,247),fill=64,width=2)
        d.arc((394,235,406,247),180,355,fill=67,width=1)
    elif name=="lakeside":
        tree(d,r,44,320,184); tree(d,r,758,320,172)
        for x in list(range(0,230,9))+list(range(584,800,9)):
            h=r.randint(38,104); d.line((x,320,x+r.randint(-6,6),320-h),fill=r.randint(35,50),width=r.choice((1,2)))
        d.polygon([(0,291),(180,279),(245,302),(242,326),(0,334)],fill=48)
        d.polygon([(800,291),(622,279),(558,302),(560,326),(800,334)],fill=48)
        # low wooden pier creates a strong lakeside identity
        d.polygon([(302,296),(468,290),(492,300),(323,307)],fill=45)
        for x in (323,372,421,470):
            d.line((x,299,x-2,327),fill=39,width=3)
        for x in range(0,150,24): d.ellipse((x,310,x+20,324),fill=r.randint(34,42))
        for x,y in [(286,271),(520,259),(548,282)]:
            d.arc((x,y,x+18,y+9),180,355,fill=66,width=1)
        # rowboat parked away from center; adds scale without blocking Haru
        d.polygon([(604,286),(676,281),(690,288),(617,294)],fill=42)
        d.line((621,287,674,284),fill=60,width=2)
        d.line((643,283,657,270),fill=45,width=2)
    else:
        for x in (38,90,145,655,715,770):
            tx,ty=trunk(d,r,x,330,r.randint(195,282),r.randint(6,10),r.randint(18,24))
            cluster(d,r,tx,ty+18,48,26,29,47,20)
        for x in (132,182,618,672):
            d.line((x,319,x+r.randint(-8,8),122),fill=r.randint(18,25),width=r.randint(4,8))
            d.arc((x-48,182,x+54,333),190,330,fill=r.randint(26,34),width=2)
        # glowing mushroom/fern pockets make the grove feel authored and magical
        for _ in range(32):
            x=r.choice(list(range(25,245))+list(range(555,785))); y=r.randint(265,320)
            d.ellipse((x,y,x+r.randint(4,10),y+r.randint(2,6)),fill=r.randint(31,46))
        for x in (78,116,205,592,684,738):
            d.rectangle((x,306,x+2,315),fill=38)
            d.ellipse((x-4,302,x+6,308),fill=r.randint(68,74))
        # fallen mossy log and exposed roots reinforce the old-growth grove identity
        d.polygon([(48,300),(225,292),(241,302),(62,313)],fill=23)
        d.line((64,302,222,295),fill=39,width=3)
        for x in (72,108,151,193,225):
            d.line((x,306,x+r.randint(-12,12),322),fill=r.randint(22,31),width=2)
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
        # scattered stepping stones and small yard props
        for x,y,rw in [(92,333,10),(132,345,13),(625,334,12),(690,347,9)]:
            d.ellipse((x-rw,y-3,x+rw,y+4),fill=r.randint(55,62))
        dither_patch(d,r,(0,300,799,359),44,54,95)
    elif name=="garden":
        stone_path(d,r,404,430)
        d.rectangle((38,296,260,324),fill=47); d.rectangle((540,296,770,324),fill=47)
        flowers(d,r,45,250,318,56); flowers(d,r,548,760,318,58)
        # irregular garden stones avoid a flat lawn read
        for _ in range(16):
            x=r.choice((r.randint(55,245),r.randint(555,750))); y=r.randint(327,354)
            d.ellipse((x-5,y-2,x+7,y+3),fill=r.randint(52,60))
        dither_patch(d,r,(38,298,260,324),46,54,38); dither_patch(d,r,(540,298,770,324),46,54,38)
    elif name=="lakeside":
        d.polygon([(0,278),(222,276),(292,309),(304,H),(0,H)],fill=48)
        d.polygon([(800,278),(579,276),(510,309),(495,H),(800,H)],fill=48)
        d.polygon([(286,317),(514,317),(500,H),(302,H)],fill=34)
        for y in range(324,H,7):
            d.line((304,y,496,y),fill=r.randint(35,40),width=1)
            if y%14==0: d.line((340,y+2,454,y+2),fill=63,width=1)
        for _ in range(12):
            x=r.randint(315,485); y=r.randint(326,356); pixel_sparkle(d,x,y,r.randint(62,70))
        # lily pads + shore stones, kept outside Haru's center lane
        for x,y in [(252,333),(276,344),(532,339),(556,350)]:
            d.ellipse((x-7,y-2,x+8,y+3),fill=r.randint(43,51))
        for x,y in [(210,326),(590,329),(618,341)]:
            d.ellipse((x-10,y-4,x+11,y+5),fill=r.randint(50,58))
        for x in list(range(0,220,14))+list(range(580,800,14)): grass(d,r,x,H,r.randint(12,28),40,55)
    else:
        for sx in (28,102,168,631,702,774):
            d.line((sx,289,400+r.randint(-95,95),H),fill=r.randint(20,28),width=r.randint(3,7))
        for _ in range(52):
            x=r.randint(0,W); y=r.randint(292,H)
            d.line((x,y,x+r.randint(-12,12),y+r.randint(0,4)),fill=r.randint(30,46),width=1)
        for _ in range(22):
            x=r.choice(list(range(20,250))+list(range(560,780))); y=r.randint(305,352)
            d.rectangle((x,y,x+2,y+4),fill=r.randint(66,73))
        # fern silhouettes and root knots fill the side pockets, not the play lane
        for bx in (52,104,174,626,696,752):
            for k in range(5):
                d.line((bx,350,bx+r.randint(-20,20),330-r.randint(0,18)),fill=r.randint(34,49),width=1)
        dither_patch(d,r,(0,300,799,359),29,46,82)
    for x in range(0,W,13):
        if r.random()<.78: grass(d,r,x,H,r.randint(8,23),42,58)
    save(im,a,os.path.join(OUT,name+"_ground.png"))

def draw_foreground(name,c,r):
    im,d,a=layer(c)
    if name=="grove":
        for x in (6,56,746,796): trunk(d,r,x,H,r.randint(215,325),r.randint(8,13),r.randint(17,23))
        cluster(d,r,74,315,84,38,28,47,34); cluster(d,r,726,315,84,38,28,47,34)
        for x in range(-5,205,9): grass(d,r,x,H,r.randint(34,100),29,49)
        for x in range(595,805,9): grass(d,r,x,H,r.randint(34,100),29,49)
        # dark top corners create a natural vignette while center stays readable
        cluster(d,r,18,36,78,38,18,32,20); cluster(d,r,782,36,78,38,18,32,20)
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
    preview=Image.alpha_composite(base,fg)
    # Preview is already full-resolution because each authored layer is 1600x720.
    preview.save(os.path.join(PRE,n+".png"),optimize=True,compress_level=6)

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
