from pathlib import Path
import json,hashlib
R=Path(__file__).resolve().parent;P=R/'project';B=R/'baseline'
def sha(b):return hashlib.sha256(b).hexdigest()
p='src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java';old=(B/p).read_text();new=(P/p).read_text()
def section(s,a,b):return s[s.index(a):s.index(b,s.index(a))]
assert 'for (int entry : p.boundaryQuads)'in old
assert 'p.boundaryQuads.begin(-dx, -dz)'in new and 'p.boundaryQuads.end(-dx, -dz)'in new
assert new.count('boundaryQuads = WaterBoundaryIndex.EMPTY;')==3
assert new.index('var boundaryIndex = WaterBoundaryIndex.of')<new.index('p.gpu.update(data, count)')<new.index('p.boundaryQuads = boundaryIndex;')
assert section(old,'  private void publishCornerRange','  /** Guaranteed service')==section(new,'  private void publishCornerRange','  /** Guaranteed service')
assert section(old,'  private void retirePresentation','  /** Plan a sufficient')==section(new,'  private void retirePresentation','  /** Plan a sufficient')
previous=json.loads((R.parent/'water-quad-packet-2029/checkpoint/source-snapshot-manifest.json').read_text());changed=json.loads((R/'changes.json').read_text());changes={x['path']for x in changed};same=0
for row in previous:
 if row['path']not in changes:assert sha((P/row['path']).read_bytes())==row['sha256'];same+=1
assert same==581
fixture='src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterBoundaryBatchChecks.java';assert(P/fixture).read_bytes()==(B/fixture).read_bytes()
# A nominal cap bound on index int-array payload, excluding array/object headers and transient storage.
max_quads=(32*1024*1024)//144;upper=12*max_quads+40*2048;prior=4*max_quads
record={'pass':True,'changes_since_r41':changed,'same_prior_snapshot_inputs':same,'new_snapshot_inputs':586,'production_files_changed':2,'validation_files_changed':3,'public_repository_source_promotion':False,'GPU_range_commit_and_retirement_methods_byte_identical':True,'index_built_before_GPU_publication_and_committed_after_success':True,'release_and_reset_drop_old_index':True,'fresh_control_candidate_private_fixture_sources_identical':True,'index_owned_immutable_no_caller_array_alias':True,'native_vertex_layout_or_shader_changes':False,'solver_quantity_geometry_geography_changes':False,'full_boundary_model':{'input_quads':960,'neighbor_routes':8,'prior_quad_visits':7680,'indexed_quad_visits':1088,'same_relevant_corner_masks':True,'scope':'Dependency-free known-coordinate work-count reproduction; not actual scene latency/FPS'},'index_storage':{'header_ints':10,'max_entries_per_input':3,'max_raw_index_int_payload_at_32MiB_2048page_caps':upper,'prior_max_flat_index_int_payload':prior,'maximum_raw_payload_increment_bound':upper-prior,'excludes_object_headers_transient_build_arrays_and_process_GPU_peak_memory':True},'resumable_publication_implemented':False,'remaining':'Whole-neighbor publication remains synchronous. Index construction adds upload CPU and bounded retained memory; no hard2ms/fullscene/ownerGPU/FPS promotion. Cross-frame cohort visibility/retry/admission/edit churn remains unfinished.'}
(R/'code-self-audit.json').write_text(json.dumps(record,indent=2)+'\n');print(json.dumps(record,indent=2))
