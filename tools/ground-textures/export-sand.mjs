import fs from 'node:fs';
import {execFileSync as run} from 'node:child_process';
import crypto from 'node:crypto';
const [input,out]=process.argv.slice(2);
if(!input||!out)throw Error('Usage: node export.mjs MASTER.png OUTPUT_DIRECTORY');
fs.mkdirSync(out,{recursive:true});
const a=Array.from(run('convert',[input,'-colorspace','Gray','-filter','Lanczos','-resize','64x64!','-depth','8','gray:-']));
for(const vertical of [false,true])for(let i=0;i<64;i++){
 const at=k=>vertical?k*64+i:i*64+k;
 const first=a[at(0)],last=a[at(63)],target=(first+last)/2;
 for(let k=0;k<4;k++){const t=k/4,w=1-t*t*(3-2*t);a[at(k)]+=(target-first)*w;a[at(63-k)]+=(target-last)*w;}
}
const data=Buffer.from(a.map(x=>Math.max(0,Math.min(255,Math.round(x)))));
const file=out+'/sand.png';
run('convert',['-size','64x64','-depth','8','gray:-','-strip','-define','png:exclude-chunk=all','-define','png:color-type=0',file],{input:data});
if(!run('convert',[file,'-depth','8','gray:-']).equals(data))throw Error('Roundtrip mismatch');
for(let i=0;i<64;i++)if(data[i]!==data[4032+i]||data[i*64]!==data[i*64+63])throw Error('Repeat edge mismatch');
run('convert',[file,'-filter','point','-resize','512x512',out+'/preview.png']);
run('convert',['-size','192x192','tile:'+file,'-filter','point','-resize','576x576',out+'/repeat-proof.png']);
fs.writeFileSync(out+'/validation.json',JSON.stringify({size:[64,64],mode:'8-bit grayscale',opaque:true,repeat_axes:'xy',opposite_edge_difference:0,png_roundtrip:true,sha256:crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex'),in_game_tested:false},null,2)+'\n');
console.log(fs.readFileSync(out+'/validation.json','utf8'));
