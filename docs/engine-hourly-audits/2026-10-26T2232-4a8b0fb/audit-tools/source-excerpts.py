from pathlib import Path
import json
r=Path(__file__).resolve().parent;p=r/'project';e=r/'evidence';core='engine-core/src/main/java/com/shnewbs/deepprimal/core/';game='src/main/java/com/shnewbs/deepprimal/engine/'
entries=[(core+'Landforms.java',1,38),(core+'TerrainEngine.java',210,257),(core+'Landscape.java',74,99),(core+'CaveNetwork.java',1,50),(core+'WatershedRegion.java',80,150),(game+'EngineChunkGenerator.java',27,54),(game+'EngineChunkGenerator.java',98,118),(game+'EngineChunkGenerator.java',423,478),(game+'EngineRegistry.java',25,45),(game+'TerrainProfile.java',52,75),(core+'WorldGenPipeline.java',1,50),(core+'ChunkCache.java',1,85),(core+'EngineCore.java',6,45),(core+'WaterSimulation.java',1,42),(core+'WaterPatchMesher.java',161,189),(core+'WaterTransport.java',1,100),('src/coreChecks/java/com/shnewbs/deepprimal/core/WaterfallSideChecks.java',1,135),('tools/server_smoke.py',17,100),('tools/test_rcon_protocol.py',1,77),('build.gradle',128,179)]
out=[]
for name,a,b in entries:
 lines=(p/name).read_text().splitlines();out.extend(['\n'+name]+[str(i)+': '+lines[i-1] for i in range(a,min(b,len(lines))+1)])
(e/'source-inspection.txt').write_text('\n'.join(out)+'\n');print('PINNED_SOURCE_EXCERPTS',len(entries))

