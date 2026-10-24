from pathlib import Path
import math,random,struct,json
R=Path(__file__).resolve().parent;r=random.Random(433);maximum=0
for i in range(200000):
 target=r.uniform(-.5,1.5);previous=r.uniform(-.5,1.5);elapsed=r.randrange(101);span=r.uniform(.0001,.5);start=r.uniform(0,1-span)
 delta=round(max(-1,min(1,previous-target))*16384)/16384;t=elapsed/100;displayed=target+delta*(1-t)
 uv=start+(1-target)*span;corrected=max(start,min(start+span,uv-delta*(1-t)*span));expected=start+max(0,min(1,1-displayed))*span
 error=abs(corrected-expected);maximum=max(maximum,error);assert error<1e-12
light_cases=0
for block in range(0,256,16):
 for sky in range(0,256,16):
  for side,flow in [(False,False),(True,False),(True,True)]:
   packed=block|(256 if side else 0)|(512 if flow else 0)
   assert (packed&255)//16==block//16 and (packed&256!=0)==side and (packed&512!=0)==flow;light_cases+=1
result={'pass':True,'numeric_height_uv_cases':200000,'maximum_double_precision_error':maximum,'light_flag_cases':light_cases,'scope':'Independent algebra/bounds and light packing only, not GPU float precision or performance; native pixel/readback checks are separate.'}
(R/'side-uv-math-checks.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result))
