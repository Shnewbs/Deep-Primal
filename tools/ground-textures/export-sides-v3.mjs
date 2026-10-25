// ImageGen-authored side materials; deterministic grayscale export and material masks.
import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import {execFileSync as run} from 'node:child_process';
const [source,out]=process.argv.slice(2);
if(!source||!out)throw Error('Usage: node export-sides-v3.mjs MASTER_DIRECTORY OUTPUT_DIRECTORY');
fs.mkdirSync(path.join(out,'masks'),{recursive:true});
const N=64,images={},report=[];
const sha=b=>crypto.createHash('sha256').update(b).digest('hex');
function wrap(a,vertical){
 for(let i=0;i<N;i++){
  const at=k=>vertical?k*N+i:i*N+k;
  const first=a[at(0)],last=a[at(63)],middle=(first+last)/2;
  for(let k=0;k<3;k++){const t=k/3,w=1-t*t*(3-2*t);a[at(k)]+=(middle-first)*w;a[at(63-k)]+=(middle-last)*w;}
 }
}
function read(name){
 const raw=run('convert',[path.join(source,name+'.png'),'-colorspace','Gray','-filter','Lanczos','-resize','64x64!','-depth','8','gray:-']);
 // Remove broad baked illumination before applying a separate material tint.
 const low=run('convert',['-size','64x64','-depth','8','gray:-','-virtual-pixel','mirror','-blur','0x5','-depth','8','gray:-'],{input:raw});
 return Array.from(raw,(v,i)=>192+1.35*(v-low[i]));
}
function save(name,values,axes,mask=false){
 const a=Array.from(values);wrap(a,false);if(axes==='xy')wrap(a,true);
 const data=Buffer.from(a.map(v=>Math.round(Math.max(0,Math.min(255,v)))));
 const f=path.join(out,name+'.png');
 run('convert',['-size','64x64','-depth','8','gray:-','-strip','-define','png:exclude-chunk=all','-define','png:color-type=0',f],{input:data});
 if(!run('convert',[f,'-depth','8','gray:-']).equals(data))throw Error('PNG roundtrip '+name);
 for(let i=0;i<N;i++)if(data[i*N]!==data[i*N+63]||(axes==='xy'&&data[i]!==data[4032+i]))throw Error('Wrap '+name);
 images[name]=data;
 report.push({file:name+'.png',axes,mask,sha256:sha(fs.readFileSync(f)),mean:data.reduce((s,v)=>s+v,0)/4096});
 return data;
}
const soil=read('dirt_side');
save('dirt_side',soil,'xy');
for(const name of ['grass','podzol']){
 const face=read(name+'_side');
 // Common lower substrate ensures consistent soil detail across surface types.
 for(let y=12;y<64;y++)for(let x=0;x<64;x++){
  const k=y*64+x,t=Math.min(1,(y-11)/5);face[k]=face[k]*(1-t)+soil[k]*t;
 }
 save(name+'_side',face,'x');
 // Authored region weights follow a shallow irregular fringe, not a dark cap.
 const mask=Array(4096);
 for(let x=0;x<64;x++){
  const p=x/63*Math.PI*2,depth=(name==='grass'?5.5:7)+1.2*Math.sin(p*3+.2)+.7*Math.sin(p*7)+.5*Math.cos(p*11);
  for(let y=0;y<64;y++)mask[y*64+x]=255*Math.max(0,Math.min(1,(depth+1.5-y)/3));
 }
 const surface=save('masks/'+name+'_side_surface',mask,'x',true);
 // Save exact complement without applying another edge filter.
 const f=path.join(out,'masks/'+name+'_side_soil.png'),data=Buffer.alloc(4096);
 for(let i=0;i<4096;i++)data[i]=255-surface[i];
 run('convert',['-size','64x64','-depth','8','gray:-','-strip','-define','png:exclude-chunk=all','-define','png:color-type=0',f],{input:data});
 images['masks/'+name+'_side_soil']=data;
 report.push({file:'masks/'+name+'_side_soil.png',axes:'x',mask:true,sha256:sha(fs.readFileSync(f))});
}
const proof=Buffer.alloc(1152*384*3);
const colors={dirt:[145,116,85],grass:[113,140,73],podzol:[145,110,68]};
for(const [col,name] of ['grass','dirt','podzol'].entries()){
 const a=images[name+'_side'],m=images['masks/'+name+'_side_surface'];
 for(let y=0;y<384;y++)for(let x=0;x<384;x++){
  const k=(Math.floor(y/3)%64)*64+(Math.floor(x/3)%64),t=m?m[k]/255:0;
  for(let c=0;c<3;c++)proof[(y*1152+col*384+x)*3+c]=Math.round(a[k]*(y<192?1:((colors[name][c]*t+colors.dirt[c]*(1-t))/255)));
 }
}
run('convert',['-size','1152x384','-depth','8','rgb:-',path.join(out,'side-proof.png')],{input:proof});
const current=Buffer.alloc(1152*192*3),soilTint=[182,160,128];
for(const [col,name] of ['grass','dirt','podzol'].entries())for(let y=0;y<192;y++)for(let x=0;x<384;x++){
 const k=Math.floor(y/3)*64+(Math.floor(x/3)%64);
 for(let c=0;c<3;c++)current[(y*1152+col*384+x)*3+c]=Math.round(images[name+'_side'][k]*soilTint[c]/255);
}
run('convert',['-size','1152x192','-depth','8','rgb:-',path.join(out,'current-soil-tint-proof.png')],{input:current});
fs.writeFileSync(path.join(out,'export-validation.json'),JSON.stringify({assets:report,dimensions:[64,64],color:'neutral grayscale',in_game_tested:false,proof:'CPU illustrative tint swatches only; not game lighting'},null,2)+'\n');
