package globals.map;

import java.util.ArrayList;
import java.util.List;

public class DefaultMap extends Map {
    private static DefaultMap instance;
    private static final int LANE_WIDTH = 100;
    private static final int HALF_LANE_WIDTH = LANE_WIDTH / 2;
    private static final int BUILDING_WIDTH = 300;

    public static DefaultMap getInstance() {
        if(instance == null) {
            instance = new DefaultMap();
            for(Waypoint w: allWaypoints())
            {
                instance.addWaypoint(w);
            }
        }
        return instance;
    }

    private static List<Waypoint> allWaypoints() {
        List<Waypoint> r = new ArrayList<Waypoint>();
        int topY            = HALF_LANE_WIDTH;
        int cross1TopY      = BUILDING_WIDTH - HALF_LANE_WIDTH;
        int cross1Lane1Y    = cross1TopY + LANE_WIDTH;
        int cross1Lane2Y    = cross1Lane1Y + LANE_WIDTH;
        int cross1BottomY   = cross1Lane2Y + LANE_WIDTH;

        int cross2TopY      = 2*BUILDING_WIDTH + 2*LANE_WIDTH - HALF_LANE_WIDTH;
        int cross2Lane1Y    = cross2TopY + LANE_WIDTH;
        int cross2Lane2Y    = cross2Lane1Y + LANE_WIDTH;
        int cross2BottomY   = cross2Lane2Y + LANE_WIDTH;

        int cross3TopY      = 3*BUILDING_WIDTH + 4*LANE_WIDTH - HALF_LANE_WIDTH;
        int cross3Lane1Y    = cross3TopY + LANE_WIDTH;
        int cross3Lane2Y    = cross3Lane1Y + LANE_WIDTH;
        int cross3BottomY   = cross3Lane2Y + LANE_WIDTH;
        int bottomY         = 4*BUILDING_WIDTH + 6*LANE_WIDTH - HALF_LANE_WIDTH;

        int leftX           = HALF_LANE_WIDTH;
        int cross1LeftX     = BUILDING_WIDTH - HALF_LANE_WIDTH;
        int cross1Lane1X    = cross1LeftX + LANE_WIDTH;
        int cross1Lane2X    = cross1Lane1X + LANE_WIDTH;
        int cross1RightX    = cross1Lane2X + LANE_WIDTH;

        int cross2LeftX     = 2*BUILDING_WIDTH + 2*LANE_WIDTH - HALF_LANE_WIDTH;
        int cross2Lane1X    = cross2LeftX + LANE_WIDTH;
        int cross2Lane2X    = cross2Lane1X + LANE_WIDTH;
        int cross2RightX    = cross2Lane2X + LANE_WIDTH;

        int cross3LeftX     = 3*BUILDING_WIDTH + 4*LANE_WIDTH - HALF_LANE_WIDTH;
        int cross3Lane1X    = cross3LeftX + LANE_WIDTH;
        int cross3Lane2X    = cross3Lane1X + LANE_WIDTH;
        int cross3RightX    = cross3Lane2X + LANE_WIDTH;
        int rightX          = 4*BUILDING_WIDTH + 6*LANE_WIDTH - HALF_LANE_WIDTH;

        // First row
        Waypoint w0  = new Waypoint(topY, cross1Lane1X, "W0");
        Waypoint w1  = new Waypoint(topY, cross1Lane2X, "W1");
        Waypoint w2  = new Waypoint(topY, cross2Lane1X, "W2");
        Waypoint w3  = new Waypoint(topY, cross2Lane2X, "W3");
        Waypoint w4  = new Waypoint(topY, cross3Lane1X, "W4");
        Waypoint w5  = new Waypoint(topY, cross3Lane2X, "W5");

        // Second row
        Waypoint w6  = new Waypoint(cross1TopY, cross1Lane1X, "W6");
        Waypoint w7  = new Waypoint(cross1TopY, cross1Lane2X, "W7");
        Waypoint w8  = new Waypoint(cross1TopY, cross2Lane1X, "W8");
        Waypoint w9  = new Waypoint(cross1TopY, cross2Lane2X, "W9");
        Waypoint w10 = new Waypoint(cross1TopY, cross3Lane1X, "W10");
        Waypoint w11 = new Waypoint(cross1TopY, cross3Lane2X, "W11");

        //Third row ()
        Waypoint w12 = new Waypoint(cross1Lane1Y, leftX, "W12");
        Waypoint w14 = new Waypoint(cross1Lane1Y, cross1LeftX, "W13");
        Waypoint w15 = new Waypoint(cross1Lane1Y, cross1RightX, "W14");
        Waypoint w13 = new Waypoint(cross1Lane1Y, cross2LeftX, "W15");
        Waypoint w16 = new Waypoint(cross1Lane1Y, cross2RightX, "W16");
        Waypoint w17 = new Waypoint(cross1Lane1Y, cross3LeftX, "W17");
        Waypoint w18 = new Waypoint(cross1Lane1Y, cross3RightX, "W18");
        Waypoint w19 = new Waypoint(cross1Lane1Y, rightX, "W19");

        // Fourth row
        Waypoint w20 = new Waypoint(cross1Lane2Y, leftX, "W20");
        Waypoint w21 = new Waypoint(cross1Lane2Y, cross1LeftX, "W21");
        Waypoint w22 = new Waypoint(cross1Lane2Y, cross1RightX, "W22");
        Waypoint w23 = new Waypoint(cross1Lane2Y, cross2LeftX, "W23");
        Waypoint w24 = new Waypoint(cross1Lane2Y, cross2RightX, "W24");
        Waypoint w25 = new Waypoint(cross1Lane2Y, cross3LeftX, "W25");
        Waypoint w26 = new Waypoint(cross1Lane2Y, cross3RightX, "W26");
        Waypoint w27 = new Waypoint(cross1Lane2Y, rightX, "W27");

        // Fifth row
        Waypoint w28 = new Waypoint(cross1BottomY, cross1Lane1X, "W28");
        Waypoint w29 = new Waypoint(cross1BottomY, cross1Lane2X, "W29");
        Waypoint w30 = new Waypoint(cross1BottomY, cross2Lane1X, "W30");
        Waypoint w31 = new Waypoint(cross1BottomY, cross2Lane2X, "W31");
        Waypoint w32 = new Waypoint(cross1BottomY, cross3Lane1X, "W32");
        Waypoint w33 = new Waypoint(cross1BottomY, cross3Lane2X, "W33");

        // Sixth row
        Waypoint w34 = new Waypoint(cross2TopY, cross1Lane1X, "W34");
        Waypoint w35 = new Waypoint(cross2TopY, cross1Lane2X, "W35");
        Waypoint w36 = new Waypoint(cross2TopY, cross2Lane1X, "W36");
        Waypoint w37 = new Waypoint(cross2TopY, cross2Lane2X, "W37");
        Waypoint w38 = new Waypoint(cross2TopY, cross3Lane1X, "W38");
        Waypoint w39 = new Waypoint(cross2TopY, cross3Lane2X, "W39");

        // Seventh row
        Waypoint w40 = new Waypoint(cross2Lane1Y, leftX, "W40");
        Waypoint w41 = new Waypoint(cross2Lane1Y, cross1LeftX, "W41");
        Waypoint w42 = new Waypoint(cross2Lane1Y, cross1RightX, "W42");
        Waypoint w43 = new Waypoint(cross2Lane1Y, cross2LeftX, "W43");
        Waypoint w44 = new Waypoint(cross2Lane1Y, cross2RightX, "W44");
        Waypoint w45 = new Waypoint(cross2Lane1Y, cross3LeftX, "W45");
        Waypoint w46 = new Waypoint(cross2Lane1Y, cross3RightX, "W46");
        Waypoint w47 = new Waypoint(cross2Lane1Y, rightX, "W47");

        // Eight row
        Waypoint w48 = new Waypoint(cross2Lane2Y, leftX, "W48");
        Waypoint w49 = new Waypoint(cross2Lane2Y, cross1LeftX, "W49");
        Waypoint w50 = new Waypoint(cross2Lane2Y, cross1RightX, "W50");
        Waypoint w51 = new Waypoint(cross2Lane2Y, cross2LeftX, "W51");
        Waypoint w52 = new Waypoint(cross2Lane2Y, cross2RightX, "W52");
        Waypoint w53 = new Waypoint(cross2Lane2Y, cross3LeftX, "W53");
        Waypoint w54 = new Waypoint(cross2Lane2Y, cross3RightX, "W54");
        Waypoint w55 = new Waypoint(cross2Lane2Y, rightX, "W55");

        // Ninth row
        Waypoint w56 = new Waypoint(cross2BottomY, cross1Lane1X, "W56");
        Waypoint w57 = new Waypoint(cross2BottomY, cross1Lane2X, "W57");
        Waypoint w58 = new Waypoint(cross2BottomY, cross2Lane1X, "W58");
        Waypoint w59 = new Waypoint(cross2BottomY, cross2Lane2X, "W59");
        Waypoint w60 = new Waypoint(cross2BottomY, cross3Lane1X, "W60");
        Waypoint w61 = new Waypoint(cross2BottomY, cross3Lane2X, "W61");

        // Tenth row
        Waypoint w62 = new Waypoint(cross3TopY, cross1Lane1X, "W62");
        Waypoint w63 = new Waypoint(cross3TopY, cross1Lane2X, "W63");
        Waypoint w64 = new Waypoint(cross3TopY, cross2Lane1X, "W64");
        Waypoint w65 = new Waypoint(cross3TopY, cross2Lane2X, "W65");
        Waypoint w66 = new Waypoint(cross3TopY, cross3Lane1X, "W66");
        Waypoint w67 = new Waypoint(cross3TopY, cross3Lane2X, "W67");

        // 11 row
        Waypoint w68 = new Waypoint(cross3Lane1Y, leftX, "W68");
        Waypoint w69 = new Waypoint(cross3Lane1Y, cross1LeftX, "W69");
        Waypoint w70 = new Waypoint(cross3Lane1Y, cross1RightX, "W70");
        Waypoint w71 = new Waypoint(cross3Lane1Y, cross2LeftX, "W71");
        Waypoint w72 = new Waypoint(cross3Lane1Y, cross2RightX, "W72");
        Waypoint w73 = new Waypoint(cross3Lane1Y, cross3LeftX, "W73");
        Waypoint w74 = new Waypoint(cross3Lane1Y, cross3RightX, "W74");
        Waypoint w75 = new Waypoint(cross3Lane1Y, rightX, "W75");

        // 12 row
        Waypoint w76 = new Waypoint(cross3Lane2Y, leftX, "W76");
        Waypoint w77 = new Waypoint(cross3Lane2Y, cross1LeftX, "W77");
        Waypoint w78 = new Waypoint(cross3Lane2Y, cross1RightX, "W78");
        Waypoint w79 = new Waypoint(cross3Lane2Y, cross2LeftX, "W79");
        Waypoint w80 = new Waypoint(cross3Lane2Y, cross2RightX, "W80");
        Waypoint w81 = new Waypoint(cross3Lane2Y, cross3LeftX, "W81");
        Waypoint w82 = new Waypoint(cross3Lane2Y, cross3RightX, "W82");
        Waypoint w83 = new Waypoint(cross3Lane2Y, rightX, "W83");

        // 13 row
        Waypoint w84 = new Waypoint(cross3BottomY, cross1Lane1X, "W84");
        Waypoint w85 = new Waypoint(cross3BottomY, cross1Lane2X, "W85");
        Waypoint w86 = new Waypoint(cross3BottomY, cross2Lane1X, "W86");
        Waypoint w87 = new Waypoint(cross3BottomY, cross2Lane2X, "W87");
        Waypoint w88 = new Waypoint(cross3BottomY, cross3Lane1X, "W88");
        Waypoint w89 = new Waypoint(cross3BottomY, cross3Lane2X, "W89");

        // 14 row
        Waypoint w90 = new Waypoint(bottomY, cross1Lane1X, "W90");
        Waypoint w91 = new Waypoint(bottomY, cross1Lane2X, "W91");
        Waypoint w92 = new Waypoint(bottomY, cross2Lane1X, "W92");
        Waypoint w93 = new Waypoint(bottomY, cross2Lane2X, "W93");
        Waypoint w94 = new Waypoint(bottomY, cross3Lane1X, "W94");
        Waypoint w95 = new Waypoint(bottomY, cross3Lane2X, "W95");

        w0.addAdjancie(w6);
        w1.addAdjancie(w0);
        w2.addAdjancie(w8);
        w3.addAdjancie(w2);
        w4.addAdjancie(w10);
        w5.addAdjancie(w4);

        w6.addAdjancie(w13);
        w6.addAdjancie(w28);
        w7.addAdjancie(w1);
        w8.addAdjancie(w15);
        w8.addAdjancie(w30);
        w9.addAdjancie(w3);
        w10.addAdjancie(w17);
        w10.addAdjancie(w32);
        w11.addAdjancie(w5);

        w12.addAdjancie(w20);
        w13.addAdjancie(w12);
        w14.addAdjancie(w7);
        w14.addAdjancie(w13);
        w15.addAdjancie(w14);
        w16.addAdjancie(w15);
        w16.addAdjancie(w9);
        w17.addAdjancie(w16);
        w18.addAdjancie(w11);
        w18.addAdjancie(w17);
        w19.addAdjancie(w18);

        w20.addAdjancie(w21);
        w21.addAdjancie(w28);
        w21.addAdjancie(w22);
        w22.addAdjancie(w23);
        w23.addAdjancie(w30);
        w23.addAdjancie(w24);
        w24.addAdjancie(w25);
        w25.addAdjancie(w32);
        w25.addAdjancie(w26);
        w26.addAdjancie(w27);
        w27.addAdjancie(w19);

        w28.addAdjancie(w34);
        w29.addAdjancie(w22);
        w29.addAdjancie(w7);
        w30.addAdjancie(w36);
        w31.addAdjancie(w24);
        w31.addAdjancie(w9);
        w32.addAdjancie(w38);
        w33.addAdjancie(w26);
        w33.addAdjancie(w11);

        w34.addAdjancie(w41);
        w34.addAdjancie(w56);
        w35.addAdjancie(w29);
        w36.addAdjancie(w43);
        w36.addAdjancie(w58);
        w37.addAdjancie(w31);
        w38.addAdjancie(w45);
        w38.addAdjancie(w60);
        w39.addAdjancie(w33);

        w40.addAdjancie(w48);
        w41.addAdjancie(w40);
        w42.addAdjancie(w35);
        w42.addAdjancie(w41);
        w43.addAdjancie(w42);
        w44.addAdjancie(w43);
        w44.addAdjancie(w37);
        w45.addAdjancie(w44);
        w46.addAdjancie(w39);
        w46.addAdjancie(w45);
        w47.addAdjancie(w46);

        w48.addAdjancie(w49);
        w49.addAdjancie(w56);
        w49.addAdjancie(w50);
        w50.addAdjancie(w51);
        w51.addAdjancie(w52);
        w51.addAdjancie(w58);
        w52.addAdjancie(w53);
        w53.addAdjancie(w54);
        w53.addAdjancie(w60);
        w54.addAdjancie(w55);
        w55.addAdjancie(w47);

        w56.addAdjancie(w62);
        w57.addAdjancie(w35);
        w57.addAdjancie(w50);
        w58.addAdjancie(w64);
        w59.addAdjancie(w37);
        w59.addAdjancie(w52);
        w60.addAdjancie(w66);
        w61.addAdjancie(w39);
        w61.addAdjancie(w54);

        w62.addAdjancie(w69);
        w62.addAdjancie(w84);
        w63.addAdjancie(w57);
        w64.addAdjancie(w71);
        w64.addAdjancie(w86);
        w65.addAdjancie(w59);
        w66.addAdjancie(w73);
        w66.addAdjancie(w88);
        w67.addAdjancie(w61);

        w68.addAdjancie(w76);
        w69.addAdjancie(w68);
        w70.addAdjancie(w63);
        w70.addAdjancie(w69);
        w71.addAdjancie(w70);
        w72.addAdjancie(w65);
        w72.addAdjancie(w71);
        w73.addAdjancie(w72);
        w74.addAdjancie(w67);
        w74.addAdjancie(w73);
        w75.addAdjancie(w74);

        w76.addAdjancie(w77);
        w77.addAdjancie(w78);
        w77.addAdjancie(w84);
        w78.addAdjancie(w79);
        w79.addAdjancie(w80);
        w79.addAdjancie(w86);
        w80.addAdjancie(w81);
        w81.addAdjancie(w82);
        w81.addAdjancie(w88);
        w82.addAdjancie(w83);
        w83.addAdjancie(w75);

        w84.addAdjancie(w90);
        w85.addAdjancie(w63);
        w85.addAdjancie(w78);
        w86.addAdjancie(w92);
        w87.addAdjancie(w65);
        w87.addAdjancie(w80);
        w88.addAdjancie(w94);
        w89.addAdjancie(w67);
        w89.addAdjancie(w82);

        w90.addAdjancie(w91);
        w91.addAdjancie(w85);
        w92.addAdjancie(w93);
        w93.addAdjancie(w87);
        w94.addAdjancie(w95);
        w95.addAdjancie(w89);

        r.add(w0);
        r.add(w1);
        r.add(w2);
        r.add(w3);
        r.add(w4);
        r.add(w5);
        r.add(w6);
        r.add(w7);
        r.add(w8);
        r.add(w9);
        r.add(w10);
        r.add(w11);
        r.add(w12);
        r.add(w13);
        r.add(w14);
        r.add(w15);
        r.add(w16);
        r.add(w17);
        r.add(w18);
        r.add(w19);
        r.add(w20);
        r.add(w21);
        r.add(w22);
        r.add(w23);
        r.add(w24);
        r.add(w25);
        r.add(w26);
        r.add(w27);
        r.add(w28);
        r.add(w29);
        r.add(w30);
        r.add(w31);
        r.add(w32);
        r.add(w33);
        r.add(w34);
        r.add(w35);
        r.add(w36);
        r.add(w37);
        r.add(w38);
        r.add(w39);
        r.add(w40);
        r.add(w41);
        r.add(w42);
        r.add(w43);
        r.add(w44);
        r.add(w45);
        r.add(w46);
        r.add(w47);
        r.add(w48);
        r.add(w49);
        r.add(w50);
        r.add(w51);
        r.add(w52);
        r.add(w53);
        r.add(w54);
        r.add(w55);
        r.add(w56);
        r.add(w57);
        r.add(w58);
        r.add(w59);
        r.add(w60);
        r.add(w61);
        r.add(w62);
        r.add(w63);
        r.add(w64);
        r.add(w65);
        r.add(w66);
        r.add(w67);
        r.add(w68);
        r.add(w69);
        r.add(w70);
        r.add(w71);
        r.add(w72);
        r.add(w73);
        r.add(w74);
        r.add(w75);
        r.add(w76);
        r.add(w77);
        r.add(w78);
        r.add(w79);
        r.add(w80);
        r.add(w81);
        r.add(w82);
        r.add(w83);
        r.add(w84);
        r.add(w85);
        r.add(w86);
        r.add(w87);
        r.add(w88);
        r.add(w89);
        r.add(w90);
        r.add(w91);
        r.add(w92);
        r.add(w93);
        r.add(w94);
        r.add(w95);

        return r;
    }
}