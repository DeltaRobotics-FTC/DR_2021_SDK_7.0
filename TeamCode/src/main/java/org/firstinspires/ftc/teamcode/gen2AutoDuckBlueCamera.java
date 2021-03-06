package org.firstinspires.ftc.teamcode;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.teamcode.pinkCode.ContourPipeline;
import org.opencv.core.Scalar;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;


@Autonomous(name="gen2AutoDuckBlueCamera")
//@Disabled

public class gen2AutoDuckBlueCamera extends LinearOpMode
{
    BNO055IMU imu;

    public DcMotor intake1 = null;
    public DcMotor intake2 = null;
    public DcMotor duckSpinnerLeft = null;
    public  DcMotor duckSpinnerRight = null;
    public Servo baseRight = null;
    public Servo  armRight = null;
    public Servo  bucketRight = null;
    Orientation angles;

    private OpenCvCamera webcam;//find webcam statement

    private static final int CAMERA_WIDTH  = 320; // width  of wanted camera resolution
    private static final int CAMERA_HEIGHT = 240; // height of wanted camera resolution

    double barPos = 1;



    double CrLowerUpdate = 150;
    double CbLowerUpdate = 120;
    double CrUpperUpdate = 255;
    double CbUpperUpdate = 255;

    double lowerruntime = 0;
    double upperruntime = 0;

    // Pink Range                                      Y      Cr     Cb
    public static Scalar scalarLowerYCrCb = new Scalar(  0.0, 200, 0);
    public static Scalar scalarUpperYCrCb = new Scalar(255.0, 255.0, 100);

    @Override
    public void runOpMode() throws InterruptedException
    {
        RobotHardware robot = new RobotHardware(hardwareMap);

        intake1 = hardwareMap.dcMotor.get("intake1");
        intake2 = hardwareMap.dcMotor.get("intake2");
        duckSpinnerLeft = hardwareMap.dcMotor.get("duckSpinnerLeft");
        duckSpinnerRight = hardwareMap.dcMotor.get("duckSpinnerRight");
        baseRight = hardwareMap.servo.get("baseRight");
        armRight = hardwareMap.servo.get("armRight");
        bucketRight = hardwareMap.servo.get("bucketRight");

        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);

        DemoBotDriveMecanum drive = new DemoBotDriveMecanum();
        //meetOneRedRightWithIMU turn = new meetOneRedRightWithIMU();

        DcMotor[] motors = new DcMotor[4];
        {
            motors[0] = robot.motorLF;
            motors[1] = robot.motorLB;
            motors[2] = robot.motorRF;
            motors[3] = robot.motorRB;

        }

        // OpenCV webcam
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        webcam = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
        //OpenCV Pipeline
        ContourPipeline myPipeline;
        webcam.setPipeline(myPipeline = new ContourPipeline());
        // Configuration of Pipeline
        myPipeline.ConfigurePipeline(0, 0,50,40,  CAMERA_WIDTH, CAMERA_HEIGHT);
        myPipeline.ConfigureScalarLower(scalarLowerYCrCb.val[0],scalarLowerYCrCb.val[1],scalarLowerYCrCb.val[2]);
        myPipeline.ConfigureScalarUpper(scalarUpperYCrCb.val[0],scalarUpperYCrCb.val[1],scalarUpperYCrCb.val[2]);
        // Webcam Streaming

        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
        {
            @Override
            public void onOpened()
            {
                webcam.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode)
            {

            }
        });

        telemetry.update();

        while (!isStarted())
        {
            if(myPipeline.error){
                telemetry.addData("Exception: ", myPipeline.debug);
            }
            // Only use this line of the code when you want to find the lower and upper values, using Ftc Dashboard (https://acmerobotics.github.io/ftc-dashboard/gettingstarted)
            // testing(myPipeline);

            // Watch our YouTube Tutorial for the better explanation

            telemetry.addData("RectArea: ", myPipeline.getRectArea());
            telemetry.update();

            if(myPipeline.getRectArea() > 2000){
                if(myPipeline.getRectMidpointX() > 200){
                    AUTONOMOUS_C();
                    barPos = 1;
                }
                else if(myPipeline.getRectMidpointX() > 100){
                    AUTONOMOUS_B();
                    barPos = 2;
                }
                else {
                    AUTONOMOUS_A();
                    barPos = 3;
                }
            }
        }

        waitForStart();

        webcam.stopStreaming();

            if(barPos == 1)
            {
                bucketRight.setPosition(.17);

                robot.motorLF.setPower(.75);
                robot.motorLB.setPower(-.75);
                robot.motorRF.setPower(-.75);
                robot.motorRB.setPower(.75);

                sleep(500);

                robot.motorLF.setPower(0);
                robot.motorLB.setPower(0);
                robot.motorRF.setPower(0);
                robot.motorRB.setPower(0);

                sleep(750);

                betterPivot(90);

                sleep(750);

                drive.timeDrive(500, .75, driveStyle.STRAFE_LEFT, motors);

                sleep(750);

                duckSpinnerLeft.setPower(-.5);
                duckSpinnerRight.setPower(-.5);

                robot.motorLF.setPower(.4);
                robot.motorLB.setPower(.4);
                robot.motorRF.setPower(.4);
                robot.motorRB.setPower(.4);

                sleep(750);

                robot.motorLF.setPower(0);
                robot.motorLB.setPower(0);
                robot.motorRF.setPower(0);
                robot.motorRB.setPower(0);

                sleep(4500);

                duckSpinnerLeft.setPower(0);
                duckSpinnerRight.setPower(0);

                sleep(750);

                //robot.motorLF.setPower(.75);
                //robot.motorLB.setPower(-.75);
                //robot.motorRF.setPower(-.75);
                //robot.motorRB.setPower(.75);
//
                //sleep(200);
//
                //robot.motorLF.setPower(0);
                //robot.motorLB.setPower(0);
                //robot.motorRF.setPower(0);
                //robot.motorRB.setPower(0);

                drive.encoderDrive(2150, driveStyle.BACKWARD, 1, motors);

                sleep(750);

                robot.motorLF.setPower(.75);
                robot.motorLB.setPower(-.75);
                robot.motorRF.setPower(-.75);
                robot.motorRB.setPower(.75);

                sleep(400);

                robot.motorLF.setPower(0);
                robot.motorLB.setPower(0);
                robot.motorRF.setPower(0);
                robot.motorRB.setPower(0);

                sleep(750);

                betterPivot(180);

                sleep(750);

                armRight.setPosition(.1);

                sleep(750);

                robot.motorLF.setPower(.75);
                robot.motorLB.setPower(.75);
                robot.motorRF.setPower(.75);
                robot.motorRB.setPower(.75);

                sleep(500);

                robot.motorLF.setPower(0);
                robot.motorLB.setPower(0);
                robot.motorRF.setPower(0);
                robot.motorRB.setPower(0);

                sleep(750);

                baseRight.setPosition(.6);

                sleep(750);

                drive.encoderDrive(2500, driveStyle.BACKWARD, 1, motors);

                sleep(750);

                armRight.setPosition(.55);

                sleep(1250);

                armRight.setPosition(.1);

                robot.motorLF.setPower(.75);
                robot.motorLB.setPower(.75);
                robot.motorRF.setPower(.75);
                robot.motorRB.setPower(.75);

                sleep(500);

                robot.motorLF.setPower(0);
                robot.motorLB.setPower(0);
                robot.motorRF.setPower(0);
                robot.motorRB.setPower(0);

                sleep(750);

                baseRight.setPosition(.23);

                sleep(750);

                robot.motorLF.setPower(.75);
                robot.motorLB.setPower(.75);
                robot.motorRF.setPower(.75);
                robot.motorRB.setPower(.75);

                sleep(2000);

                robot.motorLF.setPower(.75);
                robot.motorLB.setPower(-.75);
                robot.motorRF.setPower(-.75);
                robot.motorRB.setPower(.75);

                sleep(1200);

                robot.motorLF.setPower(0);
                robot.motorLB.setPower(0);
                robot.motorRF.setPower(0);
                robot.motorRB.setPower(0);

            }

            if(barPos == 2)
            {
                bucketRight.setPosition(.17);

                robot.motorLF.setPower(.75);
                robot.motorLB.setPower(-.75);
                robot.motorRF.setPower(-.75);
                robot.motorRB.setPower(.75);

                sleep(500);

                robot.motorLF.setPower(0);
                robot.motorLB.setPower(0);
                robot.motorRF.setPower(0);
                robot.motorRB.setPower(0);

                sleep(750);

                betterPivot(90);

                sleep(750);

                drive.timeDrive(500, .75, driveStyle.STRAFE_LEFT, motors);

                sleep(750);

                duckSpinnerLeft.setPower(-.5);
                duckSpinnerRight.setPower(-.5);

                robot.motorLF.setPower(.4);
                robot.motorLB.setPower(.4);
                robot.motorRF.setPower(.4);
                robot.motorRB.setPower(.4);

                sleep(750);

                robot.motorLF.setPower(0);
                robot.motorLB.setPower(0);
                robot.motorRF.setPower(0);
                robot.motorRB.setPower(0);

                sleep(4500);

                duckSpinnerLeft.setPower(0);
                duckSpinnerRight.setPower(0);

                sleep(750);

                //robot.motorLF.setPower(.75);
                //robot.motorLB.setPower(-.75);
                //robot.motorRF.setPower(-.75);
                //robot.motorRB.setPower(.75);
//
                //sleep(200);
//
                //robot.motorLF.setPower(0);
                //robot.motorLB.setPower(0);
                //robot.motorRF.setPower(0);
                //robot.motorRB.setPower(0);

                drive.encoderDrive(2150, driveStyle.BACKWARD, 1, motors);

                sleep(750);

                robot.motorLF.setPower(.75);
                robot.motorLB.setPower(-.75);
                robot.motorRF.setPower(-.75);
                robot.motorRB.setPower(.75);

                sleep(400);

                robot.motorLF.setPower(0);
                robot.motorLB.setPower(0);
                robot.motorRF.setPower(0);
                robot.motorRB.setPower(0);

                sleep(750);

                betterPivot(180);

                sleep(750);

                bucketRight.setPosition(.17);

                armRight.setPosition(.1);

                sleep(750);

                robot.motorLF.setPower(.75);
                robot.motorLB.setPower(.75);
                robot.motorRF.setPower(.75);
                robot.motorRB.setPower(.75);

                sleep(500);

                robot.motorLF.setPower(0);
                robot.motorLB.setPower(0);
                robot.motorRF.setPower(0);
                robot.motorRB.setPower(0);

                sleep(750);

                baseRight.setPosition(.6);

                sleep(750);

                drive.encoderDrive(2500, driveStyle.BACKWARD, 1, motors);

                sleep(750);

                bucketRight.setPosition(0);

                armRight.setPosition(.3);

                sleep(250);

                baseRight.setPosition(.23);

                sleep(500);

                armRight.setPosition(.55);

                sleep(1250);

                baseRight.setPosition(.6);

                bucketRight.setPosition(.17);

                armRight.setPosition(.1);

                robot.motorLF.setPower(.75);
                robot.motorLB.setPower(.75);
                robot.motorRF.setPower(.75);
                robot.motorRB.setPower(.75);

                sleep(500);

                robot.motorLF.setPower(0);
                robot.motorLB.setPower(0);
                robot.motorRF.setPower(0);
                robot.motorRB.setPower(0);

                sleep(250);

                baseRight.setPosition(.23);

                sleep(250);

                robot.motorLF.setPower(.75);
                robot.motorLB.setPower(.75);
                robot.motorRF.setPower(.75);
                robot.motorRB.setPower(.75);

                sleep(2000);

                robot.motorLF.setPower(.75);
                robot.motorLB.setPower(-.75);
                robot.motorRF.setPower(-.75);
                robot.motorRB.setPower(.75);

                sleep(1200);

                robot.motorLF.setPower(0);
                robot.motorLB.setPower(0);
                robot.motorRF.setPower(0);
                robot.motorRB.setPower(0);
            }

            if(barPos == 3)
            {
                bucketRight.setPosition(.17);

                robot.motorLF.setPower(.75);
                robot.motorLB.setPower(-.75);
                robot.motorRF.setPower(-.75);
                robot.motorRB.setPower(.75);

                sleep(500);

                robot.motorLF.setPower(0);
                robot.motorLB.setPower(0);
                robot.motorRF.setPower(0);
                robot.motorRB.setPower(0);

                sleep(750);

                betterPivot(90);

                sleep(750);

                drive.timeDrive(500, .75, driveStyle.STRAFE_LEFT, motors);

                sleep(750);

                duckSpinnerLeft.setPower(-.5);
                duckSpinnerRight.setPower(-.5);

                robot.motorLF.setPower(.4);
                robot.motorLB.setPower(.4);
                robot.motorRF.setPower(.4);
                robot.motorRB.setPower(.4);

                sleep(750);

                robot.motorLF.setPower(0);
                robot.motorLB.setPower(0);
                robot.motorRF.setPower(0);
                robot.motorRB.setPower(0);

                sleep(4500);

                duckSpinnerLeft.setPower(0);
                duckSpinnerRight.setPower(0);

                sleep(750);

                //robot.motorLF.setPower(.75);
                //robot.motorLB.setPower(-.75);
                //robot.motorRF.setPower(-.75);
                //robot.motorRB.setPower(.75);
//
                //sleep(200);
//
                //robot.motorLF.setPower(0);
                //robot.motorLB.setPower(0);
                //robot.motorRF.setPower(0);
                //robot.motorRB.setPower(0);

                drive.encoderDrive(2150, driveStyle.BACKWARD, 1, motors);

                sleep(750);

                robot.motorLF.setPower(.75);
                robot.motorLB.setPower(-.75);
                robot.motorRF.setPower(-.75);
                robot.motorRB.setPower(.75);

                sleep(400);

                robot.motorLF.setPower(0);
                robot.motorLB.setPower(0);
                robot.motorRF.setPower(0);
                robot.motorRB.setPower(0);

                sleep(750);

                betterPivot(180);

                sleep(750);

                armRight.setPosition(.1);

                sleep(750);

                robot.motorLF.setPower(.75);
                robot.motorLB.setPower(.75);
                robot.motorRF.setPower(.75);
                robot.motorRB.setPower(.75);

                sleep(500);

                robot.motorLF.setPower(0);
                robot.motorLB.setPower(0);
                robot.motorRF.setPower(0);
                robot.motorRB.setPower(0);

                sleep(750);

                baseRight.setPosition(.6);

                sleep(750);

                drive.encoderDrive(2500, driveStyle.BACKWARD, 1, motors);

                sleep(750);

                armRight.setPosition(.55);

                sleep(1250);

                armRight.setPosition(.1);

                robot.motorLF.setPower(.75);
                robot.motorLB.setPower(.75);
                robot.motorRF.setPower(.75);
                robot.motorRB.setPower(.75);

                sleep(500);

                robot.motorLF.setPower(0);
                robot.motorLB.setPower(0);
                robot.motorRF.setPower(0);
                robot.motorRB.setPower(0);

                sleep(750);

                baseRight.setPosition(.23);

                sleep(750);

                robot.motorLF.setPower(.75);
                robot.motorLB.setPower(.75);
                robot.motorRF.setPower(.75);
                robot.motorRB.setPower(.75);

                sleep(2000);

                robot.motorLF.setPower(.75);
                robot.motorLB.setPower(-.75);
                robot.motorRF.setPower(-.75);
                robot.motorRB.setPower(.75);

                sleep(1200);

                robot.motorLF.setPower(0);
                robot.motorLB.setPower(0);
                robot.motorRF.setPower(0);
                robot.motorRB.setPower(0);
            }

        }


    public void betterPivot(int angle)
    {
        RobotHardware robot = new RobotHardware(hardwareMap);


        double I = 0;
        double turnPower;


        while (angle > 180)
        {
            angle -= 360;
        }
        while (angle < -180)
        {
            angle += 360;
        }

        while (I == 0)
        {

            angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

            while ( angles.firstAngle < angle + 10 && I == 0)
            {
                angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

                turnPower = ((angle - angles.firstAngle) /angle)+ .2;

                robot.motorRF.setPower(turnPower);
                robot.motorRB.setPower(turnPower);
                robot.motorLB.setPower(-turnPower);
                robot.motorLF.setPower(-turnPower);

                telemetry.addData("Left", I);
                telemetry.addData("current angle" , angles.firstAngle);
                telemetry.addData("target angle" , angle);
                telemetry.update();

                if(angles.firstAngle > angle - 2 && I == 0)
                {
                    angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

                    robot.motorRF.setPower(0);
                    robot.motorRB.setPower(0);
                    robot.motorLB.setPower(0);
                    robot.motorLF.setPower(0);

                    I = 3;

                    telemetry.addData("Dead", I);
                    telemetry.addData("current angle" ,angles.firstAngle);
                    telemetry.addData("target angle" , angle);
                    telemetry.update();
                }
            }

            while(angles.firstAngle > angle -  10 && I == 0)
            {
                angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

                turnPower = ((angle - angles.firstAngle) /angle)+ .2;

                robot.motorRF.setPower(-turnPower);
                robot.motorRB.setPower(-turnPower);
                robot.motorLB.setPower(turnPower);
                robot.motorLF.setPower(turnPower);

                telemetry.addData("right", I);
                telemetry.addData("current angle" ,angles.firstAngle);
                telemetry.addData("target angle" , angle);
                telemetry.update();

                if(angles.firstAngle < angle + 2 && I == 0)
                {
                    angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

                    robot.motorRF.setPower(0);
                    robot.motorRB.setPower(0);
                    robot.motorLB.setPower(0);
                    robot.motorLF.setPower(0);

                    I = 3;

                    telemetry.addData("Dead", I);
                    telemetry.addData("current angle" ,angles.firstAngle);
                    telemetry.addData("target angle" , angle);
                    telemetry.update();
                }
            }


        }


    }


    /*
    void forward (int distance, double power) {
        RobotHardware robot = new RobotHardware(hardwareMap);

        robot.motorRF.setTargetPosition(distance + robot.motorRF.getCurrentPosition());
        robot.motorRB.setTargetPosition(distance + robot.motorRB.getCurrentPosition());
        robot.motorLM.setTargetPosition(distance + robot.motorLM.getCurrentPosition());
        robot.motorLB.setTargetPosition(distance + robot.motorLB.getCurrentPosition());

        robot.motorRF.setPower(power * .75);
        robot.motorRB.setPower(power * .75);
        robot.motorLB.setPower(power * .75);
        robot.motorLF.setPower(power * .75);

        robot.motorRF.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.motorRB.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.motorLB.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.motorLF.setMode(DcMotor.RunMode.RUN_TO_POSITION);

    }


     */

    public void AUTONOMOUS_A(){
        telemetry.addLine("Autonomous A");
    }
    public void AUTONOMOUS_B(){
        telemetry.addLine("Autonomous B");
    }
    public void AUTONOMOUS_C(){
        telemetry.addLine("Autonomous C");
    }
}
