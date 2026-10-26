import {readFileSync,existsSync,mkdirSync,writeFileSync} from 'node:fs';
import {dirname,resolve} from 'node:path';
import {execFileSync} from 'node:child_process';
import {createHash} from 'node:crypto';
const root=resolve(process.argv[2]||'.');
const config=JSON.parse(readFileSync(resolve(root,'assets.json'),'utf8'));
const missing=config.assets.filter(a=>!existsSync(resolve(root,'masters',a.id+'.png')));
if(missing.length)throw new Error('Missing source masters: '+missing.map(a=>a.id).join(', ')+'. Retrieve the source bundle described in README.md before exporting.');
if(config.assets.some(a=>a.target!==32))throw new Error('This batch exporter supports only its recorded 32px targets.');
const hash=p=>createHash('sha256').update(readFileSync(p)).digest('hex');
const im=(...args)=>execFileSync('convert',args,{maxBuffer:8*1024*1024});
const rows=[];
for(const a of config.assets){
 const source=resolve(root,'masters',a.id+'.png');
 const output=resolve(root,'tiles',a.id+'.png');
 mkdirSync(dirname(output),{recursive:true});
 const args=[source,'-colorspace','Gray','-colorspace','sRGB','-filter','Box','-resize','32x32!','-depth','8'];
 if(a.alpha)args.push('-channel','A','-threshold','50%','+channel','-define','png:color-type=6');
 else args.push('-alpha','off','-define','png:color-type=2');
 args.push('-strip',output);im(...args);
 const proof=resolve(root,'proofs',a.id.replaceAll('/','_')+'.png');
 mkdirSync(dirname(proof),{recursive:true});
 if(a.tileable)im('-size','96x96','tile:'+output,'-filter','Point','-resize','384x384',proof);
 else im(output,'-filter','Point','-resize','384x384',proof);
 rows.push({id:a.id,label:a.label,source_catalog:a.source,tile:'tiles/'+a.id+'.png',master:'masters/'+a.id+'.png',master_sha256:hash(source),sha256:hash(output),width:32,height:32,alpha_mode:a.alpha?'binary_cutout':'opaque',repeat:a.tileable?'both_axes':'single_face',status:'exported_pending_native_review'});
}
const result={schema:1,source_commit:config.source_commit,method:'Built-in image generation; ImageMagick Box export to 32x32, neutral RGB8/RGBA8; binary alpha for cutouts. No procedural replacement art or painted seam correction.',runtime_integration:false,assets:rows};
writeFileSync(resolve(root,'manifest.json'),JSON.stringify(result,null,2)+'\n');
console.log(JSON.stringify({exported:rows.length,total:config.assets.length}));

