package org.frc5687.deepspace.robot.commands;

import org.frc5687.deepspace.robot.subsystems.Gripper;

public class DropBall extends OutliersCommand {
    public Gripper _gripper;

    public DropBall(Gripper gripper){
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
        _gripper.dropBall();
    }
}