from PIL import Image,ImageDraw
import os,sys,math,random,json,hashlib
ROOT=os.path.abspath(sys.argv[1] if len(sys.argv)>1 else ".")
OUT=os.path.join(ROOT,"app/src/main/res/drawable-nodpi");PRE=os.path.join(ROOT,"app/build/visual-preview")
os.makedirs(OUT,exist_ok=True);os.makedirs(PRE,exist_ok=True)
W,H=800,360
C={"home":((76,112,126),(175,183,147),(67,92,77),(97,104,73),(73,88,62),(199,164,103)),
"garden":((104,142,151),(205,198,146),(68,106,73),(99,137,80),(74,104,61),(225,189,110)),
"lakeside":((91,133,151),(185,197,168),(67,91,83),(83,114,83),(66,94,67),(205,185,122)),
"grove":((66,96,91),(133,150,116),(43,67,57),(70,97,64),(52,75,55),(171,158,102))}
def mix(a,b,t):return tuple(int(a[i]*(1-t)+b[i]*t) for i in range(3))
def pal(c):
 p=[(0,0,0)];top,bot,dark,mid,ground,acc=c
 for a,b in [(top,bot),(dark,mid),(mid,ground),(ground,acc),(acc,(220,215,180))]:
  p += [mix(a,b,i/15) for i in range(16)]
 while len(p)<192:p.append(mix(dark,(200,200,170),(len(p)-80)/112))
 while len(p)<256:p.append((0,0,0))
 flat=[v for rgb in p for v in rgb];alpha=bytes([0]+[255]*191+[0]*64)
 return flat,alpha
def img(c,opaque=False):
 p,a=pal(c);im=Image.new("P",(W,H),0);im.putpalette(p);im.info["transparency"]=a;d=ImageDraw.Draw(im)
 if opaque:
  for y in range(H):
   q=1+int(15*y/(H-1));d.line((0,y,W,y),fill=q)
 return im,d,a
def blob(d,r,x,y,rx,ry,lo,hi,n=16):
 for _ in range(n):
  xx=x+r.randint(-rx,rx);yy=y+r.randint(-ry,ry);w=r.randint(max(3,rx//5),max(5,rx//2));h=r.randint(max(2,ry//4),max(4,ry//2))
  d.ellipse((xx-w,yy-h,xx+w,yy+h),fill=r.randint(lo,hi))
def tree(d,r,x,y,h,lo=18,hi=44):
 d.line((x,y,x+r.randint(-8,8),y-h),fill=r.randint(lo,lo+6),width=max(2,h//35))
 blob(d,r,x,y-h,h//7,h//11,lo+8,hi,10)
def ridge(d,r,y,amp,fill):
 pts=[(0,H)]
 for x in range(0,812,12):pts.append((x,int(y+math.sin(x/70)*amp+math.sin(x/29)*amp*.25+r.uniform(-2,2))))
 pts.append((800,H));d.polygon(pts,fill=fill)
def grass(d,r,x,y,h,lo,hi):
 for _ in range(5):
  dx=r.randint(-7,7);d.line((x+dx,y,x+dx+r.randint(-8,8),y-r.randint(h//2,h)),fill=r.randint(lo,hi),width=r.choice((1,1,2)))
def save(im,a,path):im.save(path,optimize=True,compress_level=9,transparency=a)
def make(name,c,seed):
 r=random.Random(seed);top,bot,dark,mid,ground,acc=c
 im,d,a=img(c,True)
 for x,y,rx,ry in [(140,82,62,15),(410,64,82,18),(680,104,70,16)]:blob(d,r,x,y,rx,ry,10,16,18)
 if name in ("home","garden"):d.ellipse((625,45,657,77),fill=70)
 save(im,a,os.path.join(OUT,name+"_sky.png"))
 im,d,a=img(c);ridge(d,r,176 if name!="grove" else 115,24,22);ridge(d,r,220,13,27)
 step=24 if name!="home" else 32
 for x in range(-8,820,step):tree(d,r,x,250,r.randint(30,64),20,42)
 if name=="grove":
  for x in range(0,820,48):tree(d,r,x,290,r.randint(150,240),17,38)
 save(im,a,os.path.join(OUT,name+"_distant.png"))
 im,d,a=img(c)
 if name=="home":
  tree(d,r,72,310,190,18,48);tree(d,r,730,310,178,18,48)
  d.polygon([(125,235),(174,201),(221,236)],fill=19);d.polygon([(141,237),(207,237),(202,280),(144,280)],fill=39);d.rectangle((163,243,179,264),fill=69)
  for st,en in ((10,250),(560,798)):d.line((st,267,en,263),fill=44,width=4)
 elif name=="garden":
  for x in list(range(0,275,35))+list(range(530,805,35)):blob(d,r,x,278,30,18,35,52,14)
  for bx in (150,665):
   pts=[(int(bx+math.cos(math.pi*i/24)*53),int(278-math.sin(math.pi*i/24)*88)) for i in range(25)];d.line(pts,fill=21,width=4)
 elif name=="lakeside":
  tree(d,r,55,315,175,18,48);tree(d,r,755,315,165,18,48)
  for x in list(range(0,230,10))+list(range(590,800,10)):
   h=r.randint(35,92);d.line((x,315,x+r.randint(-5,5),315-h),fill=r.randint(35,50),width=r.choice((1,2)))
 else:
  tree(d,r,55,325,245,17,46);tree(d,r,745,325,250,17,46)
  for x in (130,185,620,675):d.line((x,312,x+r.randint(-8,8),110),fill=r.randint(18,25),width=r.randint(4,7))
 for x in list(range(0,250,34))+list(range(550,800,34)):blob(d,r,x,292,24,14,34,50,10)
 save(im,a,os.path.join(OUT,name+"_mid.png"))
 im,d,a=img(c)
 edge=[(-20,360)]+[(x,int(276+math.sin(x/28)*4+r.uniform(-2,2))) for x in range(-20,821,10)]+[(820,360)];d.polygon(edge,fill=50)
 if name in ("home","garden"):
  left=[];right=[]
  for i in range(25):
   t=i/24;y=int(270+90*t);center=400+(30 if name=="garden" else -40)*t+math.sin(t*3.1)*22;width=90+220*t
   left.append((int(center-width/2),y));right.append((int(center+width/2),y))
  d.polygon(left+right[::-1],fill=65 if name=="garden" else 58)
 elif name=="lakeside":
  d.polygon([(0,284),(225,281),(290,310),(300,360),(0,360)],fill=48);d.polygon([(800,284),(575,281),(515,310),(500,360),(800,360)],fill=48)
 else:
  for sx in (35,120,705,775):d.line((sx,290,400+r.randint(-80,80),355),fill=r.randint(20,28),width=r.randint(3,6))
 for x in range(0,800,13):
  if r.random()<.75:grass(d,r,x,359,r.randint(9,22),42,58)
 save(im,a,os.path.join(OUT,name+"_ground.png"))
 im,d,a=img(c);tree(d,r,20,360,175,17,45);tree(d,r,780,360,175,17,45)
 for x in list(range(-5,180,12))+list(range(620,805,12)):grass(d,r,x,360,r.randint(28,82),30,49)
 save(im,a,os.path.join(OUT,name+"_foreground.png"))
for i,(n,c) in enumerate(C.items()):make(n,c,4100+i*100)
for n in C:
 base=Image.open(os.path.join(OUT,n+"_sky.png")).convert("RGBA")
 for l in ("distant","mid","ground"):base=Image.alpha_composite(base,Image.open(os.path.join(OUT,n+"_"+l+".png")).convert("RGBA"))
 fg=Image.open(os.path.join(OUT,n+"_foreground.png")).convert("RGBA");aa=fg.getchannel("A").point(lambda x:int(x*198/255));fg.putalpha(aa)
 Image.alpha_composite(base,fg).resize((1600,720),Image.Resampling.NEAREST).save(os.path.join(PRE,n+".png"))
mp=os.path.join(ROOT,"app/src/main/assets/visual/asset_manifest.json")
with open(mp,encoding="utf-8") as f:j=json.load(f)
sizes={n+"_"+l:os.path.getsize(os.path.join(OUT,n+"_"+l+".png")) for n in C for l in ("sky","distant","mid","ground","foreground")}
count=0
for a in j.get("assets",[]):
 if a.get("id") in sizes:a["bytes"]=sizes[a["id"]];a["visualRevision"]="organic-layered-pixel-art-2026-09";a.pop("sha256",None);count+=1
if count!=20:raise RuntimeError("expected 20 biome assets")
with open(mp,"w",encoding="utf-8") as f:json.dump(j,f,ensure_ascii=False,indent=2);f.write("\n")
for k,v in sizes.items():print(k,v,hashlib.sha256(open(os.path.join(OUT,k+".png"),"rb").read()).hexdigest()[:12])
