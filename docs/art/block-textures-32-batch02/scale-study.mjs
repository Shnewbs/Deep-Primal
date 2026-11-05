// Mechanical area-average scale study, not native renderer mipmap validation.
import {readFileSync,mkdirSync} from 'node:fs';
import {resolve} from 'node:path';
import {execFileSync} from 'node:child_process';
const root=resolve(process.argv[2]||'.');
const selected=['diorite','chalk','gneiss','banded_iron_formation','lignite','subbituminous_coal','bituminous_coal','anthracite'];
const assets=JSON.parse(readFileSync(resolve(root,'assets.json'),'utf8')).assets;
const work=resolve(root,'.preview-work');mkdirSync(work,{recursive:true});
const args=['-font','DejaVu-Sans','-pointsize','12','-background','#242424','-fill','#eeeeee'];
for(const name of selected){
 const a=assets.find(x=>x.id==='stone/'+name);
 if(!a)throw new Error('Missing scale-study asset '+name);
 for(const size of [32,16,8,4]){
  const out=resolve(work,name+'-scale-'+size+'.png');
  execFileSync('convert',[resolve(root,'tiles',a.id+'.png'),'-filter','Box','-resize',size+'x'+size+'!','-filter','Point','-resize','144x144!',out]);
  args.push('-label',a.label+'\n'+size+'px',out);
 }
}
args.push('-geometry','+8+8','-tile','4x8',resolve(root,'scale-study.png'));
execFileSync('montage',args);
console.log('Created mechanical 32/16/8/4px scale study for eight selected rocks.');
