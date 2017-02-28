
/*
 * Demo the properties of the triangular pixel grid.
 */

import me.lsdo.processing.*;

public class GridTest extends DomeAnimation {

    public GridTest(Dome dome, OPC opc) {
        super(dome, opc);
    }

    @Override
    public int drawPixel(DomeCoord c, double t) {
        double demoPeriod = 4;
        int mode = (int)Math.floor(t / demoPeriod);
        int axisMode = mode % 3;
        int typeMode = (mode / 3) % 3;

        double marchPeriod = .0666;
        int marchCycle = 3;
        int k = (int)Math.floor(t / marchPeriod) % marchCycle;
        double sat = .75;

        TriCoord coord = c.getCoord(new TriCoord.CoordType[] {
                TriCoord.CoordType.UNIVERSAL,
                TriCoord.CoordType.PANEL,
                TriCoord.CoordType.PIXEL}[typeMode]);
        int val = coord.getAxis(TriCoord.axes[axisMode]);
        if (typeMode == 2) {
            if (coord.getOrientation() == TriCoord.PanelOrientation.B) {
                val = coord.panel_length - 1 - val;
                sat = .5;
            } else {
                sat = .9;
            }
        }

        return OpcColor.getHsbColor(
                axisMode / 3.,
                sat,
                MathUtil.mod(val, marchCycle) == k ? 1 : 0);

    }

}
