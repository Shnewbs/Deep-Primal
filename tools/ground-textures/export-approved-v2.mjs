// Extract the owner-approved sheet. No generative redraw or third-party texture extraction.
import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import {execFileSync} from 'node:child_process';
const root=path.dirname(new URL(import.meta.url).pathname);
const input=process.argv[2];
if(!input)throw Error('Usage: node export.mjs APPROVED_SHEET.png [OUTPUT_DIRECTORY]');
const repo=path.resolve(process.argv[3]||path.join(root,'git-ready'));
const assets=path.join(repo,'src/main/resources/assets/deepprimal/textures/block/ground');
const docs=path.join(repo,'docs/art/ground-textures-v2');
const source=path.join(docs,'source-crops');
const work=path.join(repo,'.texture-export-work');
for(const d of [assets,docs,source,path.join(assets,'masks'),work])fs.mkdirSync(d,{recursive:true});
const N=64, arrays={}, entries=[];
const panels=[['grass',12,462],['dirt',506,455],['podzol',993,462]];
const sha=b=>crypto.createHash('sha256').update(b).digest('hex');
const readRaw=(file,w=64,h=64)=>execFileSync('convert',[file,'-colorspace','Gray','-filter','Lanczos','-resize',`${w}x${h}!`,'-depth','8','gray:-']);
function wrap(a,vertical){
  for(let i=0;i<N;i++){
    const at=k=>vertical?k*N+i:i*N+k;
    const first=a[at(0)],last=a[at(63)],target=(first+last)/2;
    for(let k=0;k<4;k++){
      const t=k/4,weight=1-t*t*(3-2*t);
      a[at(k)]+=(target-first)*weight;
      a[at(63-k)]+=(target-last)*weight;
    }
  }
}
function save(name,a,axes='xy',mask=false){
  const v=Array.from(a);wrap(v,false);if(axes==='xy')wrap(v,true);
  const data=Buffer.from(v.map(x=>Math.max(0,Math.min(255,Math.round(x)))));
  const output=path.join(assets,name+'.png');
  execFileSync('convert',['-size','64x64','-depth','8','gray:-','-strip','-define','png:exclude-chunk=all','-define','png:color-type=0',output],{input:data});
  const decoded=execFileSync('convert',[output,'-depth','8','gray:-']);
  if(!decoded.equals(data))throw Error('PNG round-trip failed: '+name);
  const rgb=execFileSync('convert',[output,'-depth','8','rgb:-']);
  if(rgb.length!==12288)throw Error('Wrong size');
  for(let k=0;k<4096;k++)if(rgb[3*k]!==rgb[3*k+1]||rgb[3*k]!==rgb[3*k+2])throw Error('Not neutral grayscale');
  let lr=0,tb=0;
  for(let i=0;i<N;i++){lr=Math.max(lr,Math.abs(data[i*N]-data[i*N+63]));tb=Math.max(tb,Math.abs(data[i]-data[4032+i]));}
  if(lr!==0||(axes==='xy'&&tb!==0))throw Error('Edge mismatch');
  const colors=new Set(data);
  entries.push({file:path.relative(repo,output),size:[64,64],mode:'8-bit grayscale',opaque:true,mask,repeat_axes:axes,edge_max_difference:{x:lr,y:tb},unique_gray_levels:colors.size,sha256:sha(fs.readFileSync(output))});
  arrays[name]=data;return data;
}
for(const [name,x,w] of panels){
  for(const [face,y,h] of [['top',42,399],['side',449,103]]){
    const f=path.join(source,name+'_'+face+'.png');
    execFileSync('convert',[input,'-crop',`${w}x${h}+${x}+${y}`,'+repage',f]);
  }
}
for(const [name] of panels)save(name+'_top',readRaw(path.join(source,name+'_top.png')));
for(const [name] of panels){
  // Preserve the side sample's proportions: 103/462 * 64 ~= 14 rows.
  // Keep its original surface and roots, then extend with the approved dirt texture.
  const strip=readRaw(path.join(source,name+'_side.png'),64,14);
  const soil=arrays.dirt_top;
  const full=new Float64Array(4096);
  for(let y=0;y<64;y++)for(let x=0;x<64;x++){
    if(y<11)full[y*64+x]=strip[y*64+x];
    else if(y<14){const blend=(y-10)/4;full[y*64+x]=strip[y*64+x]*(1-blend)+soil[y*64+x]*blend;}
    else full[y*64+x]=soil[y*64+x];
  }
  save(name+'_side',full,name==='dirt'?'xy':'x');
}
// Side masks separate surface tint from the mineral-soil tint; no RGB recoloring.
for(const name of ['grass','podzol']){
  const mask=new Float64Array(4096);
  for(let x=0;x<64;x++){
    const phase=x/63*Math.PI*2;
    const depth=(name==='grass'?7:6)+.8*Math.sin(phase*3)+.6*Math.sin(phase*5);
    for(let y=0;y<64;y++)mask[y*64+x]=255*Math.max(0,Math.min(1,(depth+2-y)/4));
  }
  const surface=save(`masks/${name}_side_surface`,mask,'x',true);
  const soil=save(`masks/${name}_side_soil`,Array.from(surface,v=>255-v),'x',true);
  for(let i=0;i<4096;i++)if(surface[i]+soil[i]!==255)throw Error('Mask weights do not sum to 255');
}
const report={source:{name:path.basename(input),sha256:sha(fs.readFileSync(input)),size:[1476,903],owner_selected:true},operations:['Exact panel extraction from owner-selected sheet','Neutral grayscale conversion and 64x64 resampling','Preserve side-strip aspect ratio as upper 14 rows; extend below using approved dirt','Blend narrow 3-row side junction','Match wrap edges through 4-texel bands','Complementary surface/soil masks for grass and podzol sides'],assets:entries,exact_original_crop_files:6,mask_partition_checks:8192,full_build:false,in_game_validation:false,live_models_or_tint_providers:false,connected_material_transitions:false};
fs.writeFileSync(path.join(docs,'validation.json'),JSON.stringify(report,null,2)+'\n');
const names=['grass_top','dirt_top','podzol_top','grass_side','dirt_side','podzol_side'];
for(const name of names){
  execFileSync('convert',[path.join(assets,name+'.png'),'-filter','point','-resize','256x256','-background','#eeeade','-gravity','center','-extent','288x288','-gravity','south','-splice','0x30','-font','DejaVu-Sans','-pointsize','17','-fill','#25362b','-annotate','+0+8',name,path.join(work,name+'-preview.png')]);
  const vertical=!['grass_side','podzol_side'].includes(name);
  const w=192,h=vertical?192:64,proof=Buffer.alloc(w*h),a=arrays[name];
  for(let y=0;y<h;y++)for(let x=0;x<w;x++)proof[y*w+x]=a[(y%64)*64+x%64];
  execFileSync('convert',['-size',`${w}x${h}`,'-depth','8','gray:-','-filter','point','-resize',`${w*3}x${h*3}`,path.join(work,name+'-repeat.png')],{input:proof});
}
execFileSync('montage',names.map(n=>path.join(work,n+'-preview.png')).concat(['-tile','3x2','-geometry','+8+8','-background','#eeeade',path.join(docs,'preview.png')]));
execFileSync('montage',['grass_top','grass_side','dirt_top','dirt_side','podzol_top','podzol_side'].map(n=>path.join(work,n+'-repeat.png')).concat(['-tile','2x3','-geometry','576x576+12+12','-background','#eeeade',path.join(docs,'repeat-proof.png')]));
fs.rmSync(work,{recursive:true});
console.log(JSON.stringify({runtime_pngs:entries.length,opaque_textures:6,masks:4,source_crops:6,wrap_checks:'passed',grayscale_roundtrip:'passed',mask_partitions:8192,source_sha256:report.source.sha256},null,2));
