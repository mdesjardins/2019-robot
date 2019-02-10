package org.frc5687.deepspace.robot.commands;

import org.frc5687.deepspace.robot.Constants;
import org.frc5687.deepspace.robot.subsystems.Gripper;

public class StopGripper extends OutliersCommand {
    public Gripper _gripper;

    public StopGripper(Gripper gripper){
        _gripper = gripper;
        requires(_gripper);
    }


    @Override
    protected boolean isFinished() {
        return false;
    }
    @Override
    protected void initialize() {

    }

    @Override
    protected void execute(){
        _gripper.setSpeed(Constants.Gripper.VACUUM_STOP);
    }
}