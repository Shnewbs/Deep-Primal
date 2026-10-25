// Restore the approved artwork at a legible 64x64 feature scale. No generative redraw.
import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import {execFileSync as run} from 'node:child_process';
const [source,root]=process.argv.slice(2);
if(!source||!root)throw Error('Usage: node export-approved-v4.mjs V2_SOURCE_CROPS OUTPUT_REPO');
const assets=path.join(root,'src/main/resources/assets/deepprimal/textures/block/ground');
const docs=path.join(root,'docs/art/ground-textures-v4');
for(const p of [assets,path.join(assets,'masks'),docs])fs.mkdirSync(p,{recursive:true});
const hash=b=>crypto.createHash('sha256').update(b).digest('hex');
const images={},entries=[],inputs=[];
function crop(name,x,y,w,h,oh=64){
 const file=path.join(source,name+'.png');
 inputs.push({file:name+'.png',sha256:hash(fs.readFileSync(file)),crop:[x,y,w,h],output:[64,oh]});
 return Array.from(run('convert',[file,'-crop',`${w}x${h}+${x}+${y}`,'+repage','-colorspace','Gray','-filter','Lanczos','-resize',`64x${oh}!`,'-depth','8','gray:-']));
}
function wrap(a,vertical){
 for(let i=0;i<64;i++){
  const at=k=>vertical?k*64+i:i*64+k;
  const first=a[at(0)],last=a[at(63)],target=(first+last)/2;
  for(let k=0;k<3;k++){const t=k/3,w=1-t*t*(3-2*t);a[at(k)]+=(target-first)*w;a[at(63-k)]+=(target-last)*w;}
 }
}
function save(name,raw,axes='xy',mask=false,alreadyWrapped=false){
 const a=Array.from(raw);if(!alreadyWrapped){wrap(a,false);if(axes==='xy')wrap(a,true);}
 const data=Buffer.from(a.map(v=>Math.max(0,Math.min(255,Math.round(v))))),rgb=Buffer.alloc(12288);
 for(let i=0;i<4096;i++)rgb.fill(data[i],i*3,i*3+3);
 const file=path.join(assets,name+'.png');
 run('convert',['-size','64x64','-depth','8','rgb:-','-strip','-define','png:exclude-chunk=all','-define','png:color-type=2',file],{input:rgb});
 if(!run('convert',[file,'-depth','8','rgb:-']).equals(rgb))throw Error('PNG decode mismatch '+name);
 for(let i=0;i<64;i++)if(data[i*64]!==data[i*64+63]||(axes==='xy'&&data[i]!==data[4032+i]))throw Error('Wrap '+name);
 images[name]=data;entries.push({file:path.relative(root,file),size:[64,64],mode:'RGB8 neutral grayscale',opaque:true,mask,repeat_axes:axes,sha256:hash(fs.readFileSync(file))});return data;
}
save('grass_top',crop('grass_top',140,110,128,128));
const soil=crop('dirt_top',145,140,128,128);
save('dirt_top',soil);
save('dirt_side',images.dirt_top,'xy',false,true);
save('podzol_top',crop('podzol_top',120,90,128,128));
for(const name of ['grass','podzol']){
 const strip=crop(name+'_side',140,0,128,103,52),a=Array.from(images.dirt_top);
 for(let y=0;y<52;y++)for(let x=0;x<64;x++){
  const k=y*64+x,t=y<36?0:(y-35)/17;a[k]=strip[k]*(1-t)+a[k]*t;
 }
 // Match only the exposed organic region; lower soil remains byte-identical.
 for(let y=0;y<52;y++){
  const start=y*64,first=a[start],last=a[start+63],target=(first+last)/2;
  for(let k=0;k<3;k++){const t=k/3,w=1-t*t*(3-2*t);a[start+k]+=(target-first)*w;a[start+63-k]+=(target-last)*w;}
 }
 save(name+'_side',a,'x',false,true);
 const weights=Array(4096);
 for(let x=0;x<64;x++){
  const p=x/63*Math.PI*2,depth=(name==='grass'?15:22)+2*Math.sin(p*3+.4)+1.5*Math.cos(p*5)+.8*Math.sin(p*9);
  for(let y=0;y<64;y++)weights[y*64+x]=255*Math.max(0,Math.min(1,(depth+2-y)/4));
 }
 const s=save('masks/'+name+'_side_surface',weights,'x',true);
 save('masks/'+name+'_side_soil',Array.from(s,v=>255-v),'x',true,true);
}
const manifest={source:'Owner-approved v2 source crops; selected 128px regions instead of full-panel reduction',operations:['Direct crop','Neutral grayscale conversion','Lanczos downsample','Three-texel wrap correction','Shared original-art dirt base','Aspect-preserving side crop with soil transition'],inputs,assets:entries,in_game_validation:false,full_build:false};
fs.writeFileSync(path.join(docs,'validation.json'),JSON.stringify(manifest,null,2)+'\n');
// Actual exported pixels; top/side then tint top/side. No invented high-res preview.
const W=768,H=1024,proof=Buffer.alloc(W*H*3),soilColor=[182,160,128],surfaceColors={grass:[117,151,66],dirt:soilColor,podzol:[157,118,66]};
for(let col=0;col<3;col++){
 const name=['grass','dirt','podzol'][col];
 for(let y=0;y<H;y++)for(let x=0;x<256;x++){
  const row=Math.floor(y/256),side=row%2===1,k=(Math.floor(y%256/4))*64+Math.floor(x/4),a=images[name+(side?'_side':'_top')][k];
  const weight=side?(name==='dirt'?0:images['masks/'+name+'_side_surface'][k]/255):1;
  for(let c=0;c<3;c++)proof[(y*W+col*256+x)*3+c]=Math.round(a*(row<2?1:(surfaceColors[name][c]*weight+soilColor[c]*(1-weight))/255));
 }
}
run('convert',['-size',`${W}x${H}`,'-depth','8','rgb:-',path.join(docs,'actual-texture-proof.png')],{input:proof});
for(const name of ['grass_top','dirt_top','podzol_top','grass_side','podzol_side'])
 run('convert',['-size',name.endsWith('top')?'192x192':'192x64','tile:'+path.join(assets,name+'.png'),'-filter','point','-resize','300%',path.join(docs,name+'-repeat.png')]);
console.log(JSON.stringify({exported:entries.length,inputs:inputs.length,dimensions:[64,64],tops_and_dirt_use_approved_art:true}));
