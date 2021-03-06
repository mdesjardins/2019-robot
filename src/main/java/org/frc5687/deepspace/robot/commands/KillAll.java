package org.frc5687.deepspace.robot.commands;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Command;
import org.frc5687.deepspace.robot.Robot;

public class KillAll extends OutliersCommand {
    private boolean _finished;
    private Robot _robot;

    public KillAll(Robot robot) {
        requires(robot.getArm());
        requires(robot.getElevator());
        requires(robot.getRoller());
        requires(robot.getDriveTrain());
        requires(robot.getGripper());
        requires(robot.getStilt());
        requires(robot.getWrist());
        requires(robot.getSpear());

        _robot = robot;
    }

    @Override
    protected void initialize() {
        _robot.getRoller().stop();
        _robot.getGripper().stop();
        _finished = true;
        DriverStation.reportError("Initialize KillAll Command", false);
    }

    @Override
    protected void end() {
        DriverStation.reportError("Ending KillAll Command", false);
    }

    @Override
    protected boolean isFinished() {
        return _finished;
    }
}
