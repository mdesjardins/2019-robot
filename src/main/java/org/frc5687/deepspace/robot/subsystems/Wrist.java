package org.frc5687.deepspace.robot.subsystems;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import org.frc5687.deepspace.robot.Robot;
import org.frc5687.deepspace.robot.RobotMap;
import org.frc5687.deepspace.robot.commands.WristDrive;

public class Wrist extends OutliersSubsystem {
    private Robot robot;
    private DoubleSolenoid _wristSolenoid;


    public Wrist (Robot robot){
        _wristSolenoid = new DoubleSolenoid(RobotMap.PCM.WRIST_DOWN, RobotMap.PCM.WRIST_UP);
    }
    public void Up(){
        _wristSolenoid.set(DoubleSolenoid.Value.kForward);
    }
    public void Down(){
        _wristSolenoid.set(DoubleSolenoid.Value.kReverse);
    }
    public void Release(){
        _wristSolenoid.set(DoubleSolenoid.Value.kOff);
    }
    @Override
    public void updateDashboard() {
    }

    @Override
    protected void initDefaultCommand() {
        setDefaultCommand(new WristDrive());
    }
}
