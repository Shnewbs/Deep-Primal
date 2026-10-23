from pathlib import Path
R=Path(__file__).resolve().parent
for root in [R,Path('water-corners-baseline-1125')]:
 p=root/'project/src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterCornerFieldChecks.java';s=p.read_text();needle='''    var device=RenderSystem.getDevice();''';assert needle in s
 code='''    var provenance=new LinkedHashMap<String,Object>();
    try(var stream=AnimatedWaterRenderer.class.getResourceAsStream("AnimatedWaterRenderer.class")){provenance.put("renderer_class_sha256",HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())));}
    boolean hasCorners;try{field(AnimatedWaterRenderer.class,"cornerDirections");hasCorners=true;}catch(NoSuchFieldException baseline){hasCorners=false;}
    provenance.put("corner_history_field_present",hasCorners);
    for(String shader:List.of("animated_water.vsh","animated_water.fsh"))try(var stream=mc.getResourceManager().getResource(Identifier.fromNamespaceAndPath("deepprimal_graphics","shaders/core/"+shader)).orElseThrow().open()){
      provenance.put(shader+"_sha256",HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())));
    }
    Files.writeString(out.resolve("water-corner-runtime-provenance.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(provenance)+"\\n");
'''
 s=s.replace(needle,code+needle,1);p.write_text(s)
 p=root/'run_vulkan.py';s=p.read_text();needle='''started_utc=datetime.datetime.now(datetime.timezone.utc).isoformat();''';assert needle in s
 code='''identity_paths=['src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.vsh','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.fsh']
identity_before={name:hashlib.sha256((P/name).read_bytes()).hexdigest() for name in identity_paths}
identity_before['expected_corner_history_field']='cornerDirections' in (P/identity_paths[0]).read_text()
renderer_key='com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.class'
if 'packaged' in mode:
 with zipfile.ZipFile(outer) as archive:identity_before['expected_renderer_class_sha256']=hashlib.sha256(archive.read(renderer_key)).hexdigest()
else:identity_before['expected_renderer_class_sha256']=hashlib.sha256((P/'build/classes/java/main'/renderer_key).read_bytes()).hexdigest()
(capture/'source-identity-before.json').write_text(json.dumps(identity_before,indent=2)+'\\n')
'''
 s=s.replace(needle,code+needle)
 needle=''' (capture/'startup-marker-after.json').write_text''';assert needle in s
 s=s.replace(needle,''' (capture/'source-identity-after.json').write_text(json.dumps({name:hashlib.sha256((P/name).read_bytes()).hexdigest() for name in identity_paths},indent=2)+'\\n')
'''+needle);p.write_text(s)
print('added loaded-class, raw shader resource and before/after source identities')
