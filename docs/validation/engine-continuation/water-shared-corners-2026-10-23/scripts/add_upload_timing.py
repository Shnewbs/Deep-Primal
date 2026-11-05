from pathlib import Path
R=Path(__file__).resolve().parent;P=R/'project'
p=P/'src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterCornerFieldChecks.java';s=p.read_text();s=s.replace('long uploaded,int topVertex,int count,','long uploaded,long uploadNanos,int topVertex,int count,')
needle='''      upload.invoke(renderer,ready.newInstance(field(type,"session").getLong(renderer),++revision,page,s,mesh));'''
assert needle in s;s=s.replace(needle,'''      long started=System.nanoTime();
      upload.invoke(renderer,ready.newInstance(field(type,"session").getLong(renderer),++revision,page,s,mesh));
      long uploadNanos=System.nanoTime()-started;''',1)
needle='''upload.invoke(renderer,ready.newInstance(field(type,"session").getLong(renderer),++revision,page,s,mesh));}'''
assert needle in s;s=s.replace(needle,'''started=System.nanoTime();upload.invoke(renderer,ready.newInstance(field(type,"session").getLong(renderer),++revision,page,s,mesh));uploadNanos=System.nanoTime()-started;}''')
s=s.replace('drawTime,time,top,count,','drawTime,time,uploadNanos,top,count,');s=s.replace('row.put("topVertex",input.topVertex);','row.put("uploadNanos",input.uploadNanos);row.put("topVertex",input.topVertex);');p.write_text(s)
p=R/'build_candidate.py';s=p.read_text().replace("required=['BUILD SUCCESSFUL',","required=['WATER_FLOW_FIELD_CORE_PASS', 'BUILD SUCCESSFUL',");p.write_text(s)
p=R/'audit_vulkan_package.py';s=p.read_text();needle="                assert b'Normal.z * 127.0' in nested.read(key)";s=s.replace(needle,needle+"\n                key='assets/deepprimal_graphics/shaders/include/water_flow.glsl'\n                assert nested.read(key)==(PROJECT/module/'src/main/resources'/key).read_bytes()");p.write_text(s)
print('added matched actual-upload CPU region observations')
