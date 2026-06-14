import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Sudoku app icon v2 — richer gradient, glossy panel, accent-filled centre cell.
 *  Adaptive foreground content stays inside the masking safe-zone. */
public class IconGen {
    static final Color BG_TL = new Color(0x4C,0x84,0xF6);   // brighter top-left
    static final Color BG_BR = new Color(0x16,0x36,0x82);   // deep bottom-right
    static final Color NUM_DARK = new Color(0x2A,0x44,0x6E);// slate-blue numerals
    static final Color NUM_BLUE = new Color(0x2F,0x6B,0xE0);
    static final Color NUM_ORANGE = new Color(0xF5,0xA6,0x23);
    static final Color ACCENT = new Color(0x2F,0x6B,0xE0);  // centre cell fill
    static final Color PANEL = new Color(0xFF,0xFF,0xFF);
    static final Color LINE = new Color(0xD3,0xDC,0xEC);

    // value, colorKind (0 dark,1 blue,2 orange,3 white-on-accent); -1 blank
    static final int[][] GRID = {
        {5,1},{3,2},{8,0},
        {6,0},{7,3},{2,1},
        {1,2},{-1,0},{9,0}
    };
    static final int ACCENT_CELL = 4; // the centre "7"

    static void drawIcon(Graphics2D g, int size, boolean withBg, boolean rounded) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        if (withBg) {
            g.setPaint(new GradientPaint(0,0,BG_TL,size,size,BG_BR));
            if (rounded) g.fill(new RoundRectangle2D.Float(0,0,size,size,size*0.23f,size*0.23f));
            else g.fillRect(0,0,size,size);
            // soft radial sheen, top-left
            Paint sheen = new RadialGradientPaint(new Point2D.Float(size*0.30f,size*0.24f), size*0.6f,
                new float[]{0f,1f}, new Color[]{new Color(255,255,255,46), new Color(255,255,255,0)});
            g.setPaint(sheen);
            if (rounded) g.fill(new RoundRectangle2D.Float(0,0,size,size,size*0.23f,size*0.23f));
            else g.fillRect(0,0,size,size);
        }

        float panel = withBg ? size*0.72f : size*0.50f;
        float ox=(size-panel)/2f, oy=(size-panel)/2f, pr=panel*0.17f;

        // drop shadow
        g.setColor(new Color(0x10,0x20,0x44,70));
        g.fill(new RoundRectangle2D.Float(ox, oy+panel*0.035f, panel, panel, pr, pr));
        // panel
        g.setColor(PANEL);
        g.fill(new RoundRectangle2D.Float(ox, oy, panel, panel, pr, pr));

        float cell = panel/3f;
        int ar=ACCENT_CELL/3, ac=ACCENT_CELL%3;
        // accent centre cell
        g.setColor(ACCENT);
        g.fill(new RoundRectangle2D.Float(ox+ac*cell+cell*0.07f, oy+ar*cell+cell*0.07f,
                cell*0.86f, cell*0.86f, cell*0.22f, cell*0.22f));

        // grid lines (inset so they don't touch rounded corners)
        g.setColor(LINE);
        g.setStroke(new BasicStroke(Math.max(1f,panel*0.007f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i=1;i<3;i++){
            g.draw(new Line2D.Float(ox+i*cell, oy+cell*0.10f, ox+i*cell, oy+panel-cell*0.10f));
            g.draw(new Line2D.Float(ox+cell*0.10f, oy+i*cell, ox+panel-cell*0.10f, oy+i*cell));
        }

        Font f = new Font("SansSerif", Font.BOLD, Math.round(cell*0.60f));
        g.setFont(f); FontMetrics fm=g.getFontMetrics();
        for (int i=0;i<9;i++){
            if (GRID[i][0]<0) continue;
            String s=String.valueOf(GRID[i][0]);
            switch (GRID[i][1]) {
                case 1: g.setColor(NUM_BLUE); break;
                case 2: g.setColor(NUM_ORANGE); break;
                case 3: g.setColor(Color.WHITE); break;
                default: g.setColor(NUM_DARK);
            }
            int r=i/3,c=i%3; float cx=ox+c*cell+cell/2f, cy=oy+r*cell+cell/2f;
            g.drawString(s, cx-fm.stringWidth(s)/2f, cy+(fm.getAscent()-fm.getDescent())/2f);
        }
    }

    static BufferedImage render(int size, boolean bg, boolean rounded){
        BufferedImage img=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=img.createGraphics(); drawIcon(g,size,bg,rounded); g.dispose(); return img;
    }
    static void write(BufferedImage img,String p) throws Exception {
        File f=new File(p); f.getParentFile().mkdirs(); ImageIO.write(img,"png",f); System.out.println("wrote "+p);
    }
    public static void main(String[] a) throws Exception {
        String base=a[0], assets=a[1];
        String[] dir={"mipmap-mdpi","mipmap-hdpi","mipmap-xhdpi","mipmap-xxhdpi","mipmap-xxxhdpi"};
        int[] fg={108,162,216,324,432};
        for (int i=0;i<fg.length;i++) write(render(fg[i],false,false), base+"/"+dir[i]+"/ic_launcher_foreground.png");
        int[] legacy={48,72,96,144,192};
        for (int i=0;i<legacy.length;i++){
            write(render(legacy[i],true,true), base+"/"+dir[i]+"/ic_launcher.png");
            write(render(legacy[i],true,true), base+"/"+dir[i]+"/ic_launcher_round.png");
        }
        write(render(512,true,false), assets+"/play_store_512.png");
        write(render(1024,true,false), assets+"/icon_1024.png");
    }
}
