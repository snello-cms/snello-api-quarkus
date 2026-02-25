package io.snello;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class SnelloHelloTest {

    @Test
    public void testHelloEndpoint() throws Exception{
        BufferedImage png = ImageIO.read(new File("docs/snello1.png"));
        BufferedImage jpeg = ImageIO.read(new File("docs/snello3.jpeg"));
        BufferedImage svg = ImageIO.read(new File("docs/snello-logo.svg"));
        BufferedImage avif = ImageIO.read(new File("docs/fox.profile0.10bpc.yuv420.odd-height.avif"));

        ImageIO.write(png, "webp", new File("docs/webp/snello1.webp"));
        ImageIO.write(jpeg, "webp", new File("docs/webp/snello3.webp"));
      //  ImageIO.write(svg, "webp", new File("docs/webp/snello-logo.webp"));
       // ImageIO.write(avif, "webp", new File("docs/webp/avif.webp"));
    }


}
