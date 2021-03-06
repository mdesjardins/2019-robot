package org.frc5687.deepspace.robot.utils;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class Limelight extends OutliersProxy {
    NetworkTable _table;
    NetworkTableEntry _tx;
    NetworkTableEntry _ty;
    NetworkTableEntry _tv;
    NetworkTableEntry _ta;
    NetworkTableEntry _ts;
    NetworkTableEntry _tl;
    NetworkTableEntry _tshort;
    NetworkTableEntry _tlong;
    NetworkTableEntry _tvert;
    NetworkTableEntry _thor;
    NetworkTableEntry _getpipe;

    NetworkTableEntry _ledmode;
    NetworkTableEntry _cammode;
    NetworkTableEntry _pipeline;
    NetworkTableEntry _stream;









    public Limelight() {
        this("limelight");
    }


    public Limelight(String key) {
        _table = NetworkTableInstance.getDefault().getTable(key);
        _tx = _table.getEntry("tx");
        _ty = _table.getEntry("ty");
        _tv = _table.getEntry("tv");
        _ta = _table.getEntry("ta");
        _ts = _table.getEntry("ts");
        _tl = _table.getEntry("tl");
        _tshort = _table.getEntry("tshort");
        _tlong = _table.getEntry("tlong");
        _tvert = _table.getEntry("tvert");
        _thor = _table.getEntry("thor");
        _getpipe = _table.getEntry("getpipe");

        _ledmode = _table.getEntry("ledmode");
        _cammode = _table.getEntry("cammode");
        _pipeline = _table.getEntry("pipeline");
        _stream = _table.getEntry("stream");
    }

    public void enableVision() {
        _cammode.setNumber(0);
    }

    public void disableVision() {
        _cammode.setNumber(1);
    }


    public void defaultLEDs() {
        _ledmode.setNumber(0);
    }

    public void enableLEDs() {
        _ledmode.setNumber(3);
    }

    public void disableLEDs() {
        _ledmode.setNumber(1);

    }
    public void blinkLEDs() {
        _ledmode.setNumber(3);
    }

    public void setPipeline(int pipeline) {
        _pipeline.setNumber(pipeline);
    }

    public void setStreamingMode(StreamMode mode) {
        _stream.setNumber(mode.getValue());
    }

    public boolean isTargetSighted() {
        return (int)_tv.getNumber(0) > 0;
    }

    public double getHorizontalAngle() {
        return _tx.getDouble(0.0);
    }

    @Override
    public void updateDashboard() {
        metric("tx",_tx.getDouble(0.0));
        metric("ty",_ty.getDouble(0.0));
        metric("tv", _tv.getDouble(0.0));
    }

    public enum StreamMode {
        SIDE_BY_SIDE(0),
        PIP_MAIN(1),
        PIP_SECONDARY(2);

        private int _value;

        StreamMode(int value) {
            this._value = value;
        }

        public int getValue() {
            return _value;
        }

    }


}
