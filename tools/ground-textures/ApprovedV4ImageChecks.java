import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
/** Read-only ImageIO and actual material packing regression; needs extracted MaterialMath. */
public class ApprovedV4ImageChecks {
  static int pixels, compositions;
  public static void main(String[] args) throws Exception {
    Path root=Path.of(args[0]);
    for(String name:List.of("grass_top","grass_side","dirt_top","dirt_side","podzol_top","podzol_side",
        "masks/grass_side_surface","masks/grass_side_soil","masks/podzol_side_surface","masks/podzol_side_soil")) {
      var im=ImageIO.read(root.resolve(name+".png").toFile());
      if(im.getWidth()!=64||im.getHeight()!=64)throw new AssertionError(name);
      for(int y=0;y<64;y++)for(int x=0;x<64;x++){
        int c=im.getRGB(x,y),v=c&255;
        if((c>>>24)!=255||((c>>8)&255)!=v||((c>>16)&255)!=v||im.getRaster().getSample(x,y,0)!=v)
          throw new AssertionError("RGB/grayscale interpretation "+name);
        pixels++;
      }
    }
    for(String name:List.of("grass","podzol")){
      var im=ImageIO.read(root.resolve(name+"_side.png").toFile());
      var mask=ImageIO.read(root.resolve("masks/"+name+"_side_surface.png").toFile());
      var soil=ImageIO.read(root.resolve("masks/"+name+"_side_soil.png").toFile());
      for(int y=0;y<64;y++)for(int x=0;x<64;x++){
        int lum=im.getRGB(x,y)&255,w=mask.getRGB(x,y)&255;
        if(w+(soil.getRGB(x,y)&255)!=255)throw new AssertionError("mask sum");
        for(int surface: new int[]{0x759742,0x9d7642,0xffffff,0x000000,0xb6a080}){
          int c=MaterialMath.compose(MaterialMath.pack(lum,w,0xb6a080),surface);
          if((c>>>24)!=255)throw new AssertionError("opaque composite");
          for(int shift:new int[]{0,8,16}){
            double expected=lum*((255-w)*((0xb6a080>>shift)&255)+w*((surface>>shift)&255))/65025.0;
            if(Math.abs(((c>>shift)&255)-expected)>1.51)throw new AssertionError("blend rounding");
          }
          compositions++;
        }
      }
    }
    System.out.println("APPROVED_V4_IMAGE_PASS pixels="+pixels+" actualPackComposeCases="+compositions+" masks=8192 nativeGame=false");
  }
}
