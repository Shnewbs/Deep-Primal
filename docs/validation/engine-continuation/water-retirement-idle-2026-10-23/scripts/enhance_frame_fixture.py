from pathlib import Path
R=Path(__file__).resolve().parent
p=R/'project/src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterFrameRetirementChecks.java';s=p.read_text()
s=s.replace('job.samples.size() == Job.TURNS','job.samples.size() == Job.TURNS + Job.IDLE_TURNS')
s=s.replace('    static final int TURNS =', '    static final int IDLE_TURNS = 16;\n    static final int TURNS =')
s=s.replace('    boolean ready, readQueued, closed;', '    boolean ready, readQueued, closed, followup;')
s=s.replace('      WaterPublicationFailureChecks.field(type, "clock").setLong(renderer, 201L + samples.size() * 250L);','      last.put("queueBefore", (long) queue.size());\n      last.put("idle", samples.size() >= TURNS ? 1L : 0L);\n      WaterPublicationFailureChecks.field(type, "clock").setLong(renderer, 201L + samples.size() * 250L);')
s=s.replace('      samples.add(last);','      last.put("queueAfter", (long) queue.size());\n      samples.add(last);')
s=s.replace('result.put("turns", TURNS);','result.put("turns", TURNS + IDLE_TURNS); result.put("sweepTurns", TURNS); result.put("idleTurns", IDLE_TURNS); result.put("queueAfterIdle", queue.size());')
s=s.replace('          Files.writeString(output.resolve("water-frame-retirement.json"), new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result) + "\\n");\n          System.out.println("WATER_FRAME_RETIREMENT_PASS moving=" + moving + " pages=" + PAGES + " turns=" + TURNS + " rangeWrites=" + totalWrites + " bytes=" + totalBytes + " differentBytes=0 wraps=" + wraps);','          lifecycle(result);')
s=s.replace('        finally { active = null; close(); }','        finally { if (!followup) { active = null; close(); } }')
a=s.index('    public void close() {')
s=s[:a]+'''    boolean hasIdlePolicy() {
      try { type.getDeclaredMethod("armRetirement", pageType); return true; }
      catch (NoSuchMethodException expected) { return false; }
    }

    void lifecycle(Map<String, Object> result) throws Exception {
      boolean candidate = hasIdlePolicy();
      if (candidate && !queue.isEmpty()) throw new AssertionError("Steady queue did not drain");
      if (!candidate && queue.size() != PAGES) throw new AssertionError("Control retention changed");
      for (GpuBuffer buffer : readbacks) buffer.close(); readbacks.clear();
      type.getMethod("reset").invoke(renderer);
      var constructor = pageType.getDeclaredConstructor(long.class); constructor.setAccessible(true);
      long key = SectionPos.asLong(0, 0, 0); Object page = constructor.newInstance(key);
      int bytes = 393120;
      ByteBuffer data = MemoryUtil.memCalloc(bytes).order(ByteOrder.nativeOrder());
      WaterGpuMesh mesh;
      try {
        long pairs = (6 * 17L + 6) * (1 + 289 + 83521 + 24137569L);
        for (int v = 0; v < bytes; v += 36) WaterCornerPacket.write(data,v,pairs,0,0,0,0,true);
        data.limit(144); mesh = new WaterGpuMesh(data,4); ((GpuBuffer)vertices.get(mesh)).close();
        data.limit(bytes); vertices.set(mesh,RenderSystem.getDevice().createBuffer(() -> "Idle retirement lifecycle readable page",
            GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_COPY_SRC,data));
        WaterPublicationFailureChecks.field(WaterGpuMesh.class,"bytes").setLong(mesh,bytes);
        WaterPublicationFailureChecks.field(WaterGpuMesh.class,"indices").setInt(mesh,bytes/144*6);
        byte[] cpu = new byte[bytes]; data.get(0,cpu);
        WaterPublicationFailureChecks.field(pageType,"gpu").set(page,mesh);
        WaterPublicationFailureChecks.field(pageType,"presentation").set(page,cpu);
      } finally { MemoryUtil.memFree(data); }
      pages.put(key,page); queue.add(key);
      WaterPublicationFailureChecks.field(type,"residentBytes").setLong(renderer,bytes);
      var rows = new ArrayList<Map<String,Object>>();
      Method publish = type.getDeclaredMethod("publishCornerRange",pageType,int.class,ByteBuffer.class,long.class,boolean.class); publish.setAccessible(true);
      Method retry = type.getDeclaredMethod("retryCornerPublications"); retry.setAccessible(true);
      // First slice already scanned: a later accepted write behind its cursor needs a new sweep.
      step(1000); record(rows,"partial-sweep",page);
      if (WaterPublicationFailureChecks.field(pageType,"retireOffset").getInt(page) != WaterCornerPacket.SLICE_BYTES)
        throw new AssertionError("Large-page cursor did not advance");
      publish(publish,page,0,6,2,1000,false); record(rows,"behind-cursor-publication",page);
      for (int n=0;n<6;n++) step(1200); record(rows,"behind-cursor-expired",page);
      if (candidate && !queue.isEmpty()) throw new AssertionError("Behind-cursor sweep did not finish");
      publish(publish,page,144,7,6,5000,false); record(rows,"normal-publication-wake",page);
      if (!queue.contains(key)) throw new AssertionError("Normal publication did not arm retirement");
      for (int n=0;n<4;n++) step(5050); record(rows,"live-pair-retained",page);
      if (!queue.contains(key)) throw new AssertionError("Live pair was unscheduled");
      for (int n=0;n<6;n++) step(5200); record(rows,"normal-publication-expired",page);
      byte[] before = ((byte[])WaterPublicationFailureChecks.field(pageType,"presentation").get(page)).clone();
      publish(publish,page,288,8,6,6000,true); record(rows,"rejected-publication",page);
      if (!Arrays.equals(before,(byte[])WaterPublicationFailureChecks.field(pageType,"presentation").get(page)))
        throw new AssertionError("Rejected publication committed CPU data");
      WaterPublicationFailureChecks.call(retry,renderer); record(rows,"retry-wake",page);
      if (!queue.contains(key)) throw new AssertionError("Accepted retry did not arm retirement");
      for (int n=0;n<6;n++) step(6200); record(rows,"retry-expired",page);
      publish(publish,page,0,9,6,7000,false);
      WaterPublicationFailureChecks.field(type,"clock").setLong(renderer,7200);
      before = ((byte[])WaterPublicationFailureChecks.field(pageType,"presentation").get(page)).clone();
      int offset = WaterPublicationFailureChecks.field(pageType,"retireOffset").getInt(page);
      boolean pending = candidate && WaterPublicationFailureChecks.field(pageType,"retirePending").getBoolean(page);
      Object reject = rejectWrites(); backend.set(device,reject); boolean caught=false;
      try { WaterPublicationFailureChecks.call(retire,renderer); }
      catch (RuntimeException expected) { caught=true; }
      finally { backend.set(device,original); }
      if (!caught || !Arrays.equals(before,(byte[])WaterPublicationFailureChecks.field(pageType,"presentation").get(page))
          || offset != WaterPublicationFailureChecks.field(pageType,"retireOffset").getInt(page)
          || candidate && pending != WaterPublicationFailureChecks.field(pageType,"retirePending").getBoolean(page))
        throw new AssertionError("Rejected retirement committed data/cursor/pending state");
      record(rows,"rejected-retirement",page);
      for (int n=0;n<6;n++) step(7200); record(rows,"recovered-retirement",page);
      if (candidate && !queue.isEmpty()) throw new AssertionError("Recovered queue did not drain");
      byte[] cpu = ((byte[])WaterPublicationFailureChecks.field(pageType,"presentation").get(page)).clone();
      for (int q=0;q<bytes;q+=144) for(int n=0;n<4;n++) {
        ByteBuffer packet=ByteBuffer.wrap(cpu).order(ByteOrder.nativeOrder());
        int to=q==0?9:q==144?7:q==288?8:6;
        if(WaterCornerPacket.target(packet,q,n)!=to||WaterCornerPacket.source(packet,q,n)!=to)
          throw new AssertionError("Expired lifecycle pair remained active");
      }
      var read = RenderSystem.getDevice().createBuffer(() -> "Idle lifecycle final readback",GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_MAP_READ,bytes);
      readbacks.add(read); RenderSystem.getDevice().createCommandEncoder().copyToBuffer(((GpuBuffer)vertices.get(mesh)).slice(0,bytes),read.slice());
      followup=true;
      RenderSystem.queueFencedTask(() -> {
        try {
          byte[] gpu=new byte[bytes];
          try(var mapped=RenderSystem.getDevice().createCommandEncoder().mapBuffer(read,true,false)){mapped.data().get(0,gpu);}
          if(!Arrays.equals(cpu,gpu))throw new AssertionError("Lifecycle CPU/GPU differs");
          Files.write(output.resolve("water-idle-lifecycle-cpu.bin"),cpu); Files.write(output.resolve("water-idle-lifecycle-gpu.bin"),gpu);
          result.put("idlePolicy",candidate);result.put("lifecycle",rows);
          result.put("lifecycleCpuSha256",WaterPublicationFailureChecks.sha(cpu));result.put("lifecycleGpuSha256",WaterPublicationFailureChecks.sha(gpu));
          result.put("lifecycleBytes",bytes);result.put("lifecyclePublicationAndRetirementRejectionsPreserved",true);
          type.getMethod("reset").invoke(renderer);
          if(!queue.isEmpty())throw new AssertionError("Reset retained queued work");result.put("resetQueueEmpty",true);
          Files.writeString(output.resolve("water-frame-retirement.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result)+"\\n");
          System.out.println("WATER_FRAME_RETIREMENT_PASS moving="+moving+" pages="+PAGES+" turns="+(TURNS+IDLE_TURNS)+" idlePolicy="+candidate+" differentBytes=0 wraps="+wraps);
        }catch(Exception e){throw new RuntimeException(e);}
        finally{active=null;close();}
      });
    }

    void step(long clock) throws Exception {
      WaterPublicationFailureChecks.field(type,"clock").setLong(renderer,clock);
      WaterPublicationFailureChecks.call(retire,renderer);
    }
    void record(List<Map<String,Object>> rows,String phase,Object page)throws Exception {
      var row=new LinkedHashMap<String,Object>();row.put("phase",phase);row.put("queueSize",queue.size());
      row.put("clock",WaterPublicationFailureChecks.field(type,"clock").getLong(renderer));
      row.put("cursor",WaterPublicationFailureChecks.field(pageType,"retireOffset").getInt(page));
      row.put("pending",hasIdlePolicy()&&WaterPublicationFailureChecks.field(pageType,"retirePending").getBoolean(page));rows.add(row);
    }
    Object rejectWrites() {
      return Proxy.newProxyInstance(backend.getType().getClassLoader(),new Class<?>[]{backend.getType()},(o,m,args)->{
        Object encoder=invoke(m,original,args);if(!m.getName().equals("createCommandEncoder"))return encoder;
        return Proxy.newProxyInstance(m.getReturnType().getClassLoader(),new Class<?>[]{m.getReturnType()},(e,op,a)->{
          if(op.getName().equals("writeToBuffer"))throw new IllegalStateException("Expected idle-scheduler pre-backend rejection");
          return invoke(op,encoder,a);
        });
      });
    }
    void publish(Method method,Object page,int offset,int to,int from,long clock,boolean reject)throws Exception {
      WaterPublicationFailureChecks.field(type,"clock").setLong(renderer,clock);
      ByteBuffer packet=ByteBuffer.allocateDirect(144).order(ByteOrder.nativeOrder());
      long pairs=(to*17L+from)*(1+289+83521+24137569L);
      for(int v=0;v<4;v++)WaterCornerPacket.write(packet,v*36,pairs,(int)clock&65535,(int)clock&65535,(int)clock&65535,(int)clock&65535,true);
      packet.position(144);if(reject)backend.set(device,rejectWrites());
      try{WaterPublicationFailureChecks.call(method,renderer,page,offset,packet,clock+101,true);}
      finally{backend.set(device,original);}
    }

''' +s[a:]
p.write_text(s)
b=R/'baseline'/p.relative_to(R/'project');b.write_bytes(p.read_bytes())
