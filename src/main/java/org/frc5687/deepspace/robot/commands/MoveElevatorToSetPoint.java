package org.frc5687.deepspace.robot.commands;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;
import jaci.pathfinder.followers.DistanceFollower;
import org.frc5687.deepspace.robot.Constants;
import org.frc5687.deepspace.robot.subsystems.Elevator;

import static org.frc5687.deepspace.robot.Constants.Elevator.*;
import static org.frc5687.deepspace.robot.utils.Helpers.limit;

public class MoveElevatorToSetPoint extends OutliersCommand {

    private Elevator _elevator;
    private Elevator.Setpoint _setpoint;
    private Elevator.MotionMode _mode;
    private RampingState _rampingState;
    private double _position = 0;

    private double _pidOutput;
    private double _pathOutput;

    private PIDController _pidController;

    private Trajectory _trajectory;
    private DistanceFollower _pathFollower;
    private Notifier _pathNotifier;
    private long _startTime;
    private double _step;
    private int _rampDirection = 0;
    private double _rampMid = 0;

    public MoveElevatorToSetPoint(Elevator elevator, Elevator.Setpoint setpoint, Elevator.MotionMode mode) {
        _elevator = elevator;
        requires(_elevator);
        _setpoint = setpoint;
        _mode = mode;

        _pidController = new PIDController(PID.kP, PID.kI, PID.kD, _elevator, new PIDListener());
        _pidController.setAbsoluteTolerance(TOLERANCE);
        _pidController.setOutputRange(-MAX_SPEED_DOWN, MAX_SPEED_UP);
        _pidController.setInputRange(Elevator.Setpoint.Bottom.getValue(), Elevator.Setpoint.Top.getValue());
        _pidController.disable();
    }

    @Override
    protected void initialize() {
        _step = 0;
        _position = _elevator.getPosition();
        if (withinTolerance()) { return; }
        DriverStation.reportError("Moving to setpoint " + _setpoint.name() + " (" + _setpoint.getValue() + ") using " + _mode.name() + " mode.", false);
        info("Moving to setpoint " + _setpoint.name() + " (" + _setpoint.getValue() + ") using " + _mode.name() + " mode.");
        switch(_mode) {
            case Simple:
                break;
            case PID:
                _pidController.setSetpoint(_setpoint.getValue());
                _pidController.enable();
                break;
            case Path:
                _trajectory = getTrajectory((long)_elevator.getPosition(), _setpoint.getValue());
                _pathFollower = new DistanceFollower(_trajectory);
                _pathFollower.configurePIDVA(Path.kP, Path.kI, Path.kD, 1/MAX_VELOCITY_IPS, 0);

                _pathNotifier = new Notifier(this::followPath);
                _pathNotifier.startPeriodic(_trajectory.get(0).dt);
                break;
            case Ramp:
                _rampingState = RampingState.RampUp;
                double startPosition = _elevator.getPosition();
                if (_setpoint.getValue() > startPosition) {
                    _rampDirection = +1;
                    _rampMid = startPosition + (_setpoint.getValue() - startPosition)/2;
                } else if (_setpoint.getValue() < _elevator.getPosition()) {
                    _rampDirection = -1;
                    _rampMid = startPosition + (_setpoint.getValue() - startPosition)/2;
                } else {
                    _rampDirection = 0;
                }
                break;
        }
        _startTime = System.currentTimeMillis();
    }

    @Override
    protected void execute() {
        _step++;
        double speed;

        _position = _elevator.getPosition();
        switch(_mode) {
            case Simple:
                if (_position  < _setpoint.getValue() - TOLERANCE) {
                    _elevator.setSpeed(SPEED_UP);
                } else if (_position > _setpoint.getValue() + TOLERANCE) {
                    _elevator.setSpeed(-SPEED_DOWN);
                } else {
                    _elevator.setSpeed(0);
                }
                break;
            case PID:
                _elevator.setSpeed(_pidOutput, true);
                break;
            case Path:
                _elevator.setSpeed(_pathOutput);
                break;
            case Ramp:
                _elevator.setSpeed(getRampedSpeed());
                break;
            default:
                _elevator.setSpeed(0);
                break;
        }
    }

    private double getRampedSpeed() {
        double speed = 0;
        double goalSpeed = 0;
        double minSpeed = MIN_SPEED;
        if (_rampDirection > 0) {
            goalSpeed = SPEED_UP;
            if (_elevator.isAtTop() || _position >= _setpoint.getValue() - TOLERANCE) {
                _rampingState = RampingState.Done;
                metric("RampUp/RampedSpeed", 0);
                return 0;
            };
        } else if (_rampDirection < 0) {
            goalSpeed = SPEED_DOWN;
            if (_elevator.isAtBottom() || _position <= _setpoint.getValue() + TOLERANCE) {
                _rampingState = RampingState.Done;
                metric("RampUp/RampedSpeed", 0);
                return 0;
            };
        }
        speed = goalSpeed;

        metric("RampUp/RawSpeed", speed);
        metric("RampUp/Mode", _rampingState.name());
        metric("RampUp/Step", _step);

        switch(_rampingState) {
            case RampUp:
                speed = minSpeed + _step * ((goalSpeed - minSpeed) / STEPS_UP);
                if (_rampDirection>0 && _position >= _rampMid
                        || _rampDirection < 0 && _position <= _rampMid) {
                    // Halfway there . . . switch to ramping down
                    _step = 0;
                    _rampingState = RampingState.RampDown;
                } else if(Math.abs(_setpoint.getValue() - _elevator.getPosition()) <=  TICKS_PER_STEP * STEPS_DOWN) {
                    // We've reached the slow-down range
                    _step = 0;
                    _rampingState = RampingState.RampDown;
                } else if (_step >= STEPS_UP) {
                    // Fully ramped up--switch to steady-state
                    _rampingState = RampingState.Steady;
                }
                break;
            case Steady:
                speed = goalSpeed;
                if(Math.abs(_setpoint.getValue() - _elevator.getPosition()) <=  TICKS_PER_STEP * STEPS_DOWN) {
                    _step = 0;
                    _rampingState = RampingState.RampDown;
                }
                break;
            case RampDown:
                //DriverStation.reportError("" + (minSpeed + (STEPS_DOWN - _step) * ((speed - minSpeed) / STEPS_DOWN)) + " = " + minSpeed + " + ( " + STEPS_DOWN + " - " + _step + ") * (( " + speed + " - " + minSpeed + ") / " + STEPS_DOWN+ ")", false );
                speed = minSpeed + (STEPS_DOWN - _step) * ((goalSpeed - minSpeed) / STEPS_DOWN);
                //speed = (Constants.Elevator.GOAL_SPEED -(_step/Constants.Elevator.STEPS))* (Constants.Elevator.GOAL_SPEED - Constants.Elevator.MIN_SPEED);
                //if (_elevator.getRawMAGEncoder() == _setpoint.getValue()) {
                //}
                break;
            case Done:
                speed = 0;
                break;
        }
        speed = limit(speed, minSpeed, goalSpeed);
        speed = speed * _rampDirection;

        metric("RampUp/RampedSpeed", speed);

        return speed;
    }
    private boolean withinTolerance() {
        return Math.abs(_position-_setpoint.getValue()) <= TOLERANCE;
    }
    @Override
    protected boolean isFinished() {
        if (withinTolerance()) {
            return true;
        };
        switch (_mode) {
            case PID:
                return _pidController.onTarget();
            case Path:
                return _pathFollower.isFinished();
            case Ramp:
                return _rampingState == RampingState.Done;
        }
        return false;
    }

    @Override
    protected void end() {
        long endTime = System.currentTimeMillis();
        DriverStation.reportError("MoveElevatorToSetpoint Ran for " + (endTime - _startTime) + " millis", false);
        if (_pidController!=null) {
            _pidController.disable();
        }
        if (_pathNotifier!=null) {
            _pathNotifier.stop();
        }
        _elevator.setSpeed(0);
        info("Reached setpoint " + _setpoint.name() + " (" + _position + ")");
    }

    private class PIDListener implements PIDOutput {

        public double get() {
            return _pidOutput;
        }

        @Override
        public void pidWrite(double output) {
            _pidOutput = output;
        }

    }

    private void followPath() {
        if (_pathFollower.isFinished()) {
            _pathNotifier.stop();
        } else {
            double _speed = _pathFollower.calculate(_elevator.getPosition());
            _pathOutput = _speed;
        }
    }


    private Trajectory getTrajectory(long startPosition, long endPosition) {
        double startInches = startPosition/Constants.Elevator.TICKS_PER_INCH;
        double endInches = endPosition/Constants.Elevator.TICKS_PER_INCH;

        Waypoint[] points = new Waypoint[] {
                new Waypoint(0, startInches, 0),      // Waypoint @ x=-4, y=-1, exit angle=-45 degrees
                new Waypoint(0, endInches, 0),                        // Waypoint @ x=-2, y=-2, exit angle=0 radians
        };

        Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC, Trajectory.Config.SAMPLES_FAST, 0.02, MAX_VELOCITY_IPS, MAX_VELOCITY_IPS, 60.0);
        Trajectory trajectory = Pathfinder.generate(points, config);

        return trajectory;
    }


    private enum RampingState {
        RampUp(0),
        Steady(1),
        RampDown(2),
        Done(3);

        private int _value;

        RampingState(int value) { this._value = value; }

        public int getValue() { return _value; }
    }

}
