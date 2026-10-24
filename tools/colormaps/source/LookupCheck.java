import java.awt.image.BufferedImage;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.DryFoliageColor;

/** Exercise the actual Minecraft 26.1.2 lookup classes against generated PNGs. */
public final class LookupCheck {
  public static void main(String[] args) throws Exception {
    long checks=0;
    for(String name:new String[]{"grass","foliage","dry_foliage"}) {
      BufferedImage image=ImageIO.read(Path.of(args[0],name+".png").toFile());
      int[] pixels=image.getRGB(0,0,256,256,null,0,256);
      switch(name) {
        case "grass" -> GrassColor.init(pixels);
        case "foliage" -> FoliageColor.init(pixels);
        default -> DryFoliageColor.init(pixels);
      }
      for(int ti=0;ti<=256;ti++) for(int hi=0;hi<=256;hi++) {
        double t=ti/256.0,h=hi/256.0;
        int expected=image.getRGB((int)((1-t)*255),(int)((1-h*t)*255));
        int actual=switch(name) {
          case "grass" -> GrassColor.get(t,h);
          case "foliage" -> FoliageColor.get(t,h);
          default -> DryFoliageColor.get(t,h);
        };
        if(actual!=expected) throw new AssertionError(name+" "+t+" "+h);
        checks++;
      }
    }
    System.out.println("PASS actual Minecraft 26.1.2 lookup checks="+checks);
  }
}
