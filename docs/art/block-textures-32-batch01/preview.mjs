import {readFileSync,mkdirSync} from 'node:fs';
import {resolve} from 'node:path';
import {execFileSync} from 'node:child_process';
const root=resolve(process.argv[2]||'.');
const assets=JSON.parse(readFileSync(resolve(root,'assets.json'),'utf8')).assets;
const run=(tool,args)=>execFileSync(tool,args,{maxBuffer:8*1024*1024});
const temp=resolve(root,'.preview-work');mkdirSync(temp,{recursive:true});
const montage=(rows,out)=>{
 const args=['-font','DejaVu-Sans','-pointsize','14','-background','#242424','-fill','#eeeeee'];
 for(const row of rows)args.push('-label',row.label,row.file);
 args.push('-geometry','+8+8','-tile','5x2',out);run('montage',args);
};
for(let start=0;start<assets.length;start+=10){
 const singles=[],repeats=[];
 for(const a of assets.slice(start,start+10)){
  const key=a.id.replaceAll('/','_');
  const file=resolve(temp,key+'.png');
  run('convert',[resolve(root,'tiles',a.id+'.png'),'-filter','Point','-resize','160x160',file]);
  singles.push({label:a.label,file});
  const proof=resolve(temp,key+'-repeat.png');
  run('convert',[resolve(root,'proofs',key+'.png'),'-filter','Point','-resize','192x192',proof]);
  repeats.push({label:a.label+(a.tileable?' / 3x3':' / face'),file:proof});
 }
 montage(singles,resolve(root,'contact-'+(start/10+1)+'.png'));
 montage(repeats,resolve(root,'repeat-'+(start/10+1)+'.png'));
}
// Mechanical scale-loss study, not the Minecraft renderer's mip implementation.
for(const a of assets.filter(x=>x.alpha)){
 const rows=[];
 for(const size of [32,16,8,4]){
  const file=resolve(temp,a.id.replaceAll('/','_')+'-'+size+'.png');
  run('convert',[resolve(root,'tiles',a.id+'.png'),'-filter','Box','-resize',size+'x'+size,'-filter','Point','-resize','128x128',file]);
  rows.push({label:size+' px',file});
 }
 montage(rows,resolve(root,'proofs',a.id.replaceAll('/','_')+'-scale.png'));
}
run('convert',[...Array.from({length:4},(_,i)=>resolve(root,'contact-'+(i+1)+'.png')),'-append',resolve(root,'contact-all.png')]);
console.log('Created native-export contact, repeat, and cutout scale studies.');
