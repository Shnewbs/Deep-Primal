from pathlib import Path
R=Path(__file__).resolve().parent;P=R/'project'
p=P/'engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterFlowField.java';s=p.read_text();needle='  private WaterFlowField() {}';s=s.replace(needle,'''  /** Preserve an active height morph; expired offsets need no more than its full window. */
  public static int heightClock(int offset, int cornerAge) {
    return (Math.clamp(offset, -WaterSurfaceTransition.MILLIS, 0) & 255) | (cornerAge << 8);
  }
'''+needle);p.write_text(s)
p=P/'src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java';s=p.read_text();(R/'pre-clock-AnimatedWaterRenderer.java').write_text(s);old='(offset & 255) | ((cornerAges & 255) << 8)';assert old in s;s=s.replace(old,'WaterFlowField.heightClock(offset, cornerAges & 255)');p.write_text(s)
p=P/'src/coreChecks/java/com/shnewbs/deepprimal/core/WaterFlowFieldChecks.java';s=p.read_text();needle='    int edges=0;';s=s.replace(needle,'''    int clocks=0;
    for(int offset=-32768;offset<=0;offset++)for(int age:new int[]{0,25,100}) {
      int packed=WaterFlowField.heightClock(offset,age);int decoded=(byte)packed;
      need(decoded<=0 && decoded>=-100,"height clock starts in future or exceeds morph window");
      need(((packed>>>8)&255)==age,"expired height offset corrupts corner clock");
      if(offset>=-100)need(decoded==offset,"active height morph clock changed");clocks++;
    }
'''+needle);s=s.replace('" haloEdges="+edges','" heightClockCases="+clocks+" haloEdges="+edges');p.write_text(s)
print('clamped completed height offsets; added 98307 clock-boundary checks')
