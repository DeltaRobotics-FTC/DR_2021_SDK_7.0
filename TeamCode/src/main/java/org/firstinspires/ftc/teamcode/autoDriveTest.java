package org.firstinspires.ftc.teamcode;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

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


@Autonomous(name="autoDriveTest")
//@Disabled

public class autoDriveTest extends LinearOpMode
{
    BNO055IMU imu;

    public DcMotor intake1 = null;
    public DcMotor intake2 = null;
    public DcMotor duckSpinnerLeft = null;
    public  DcMotor duckSpinnerRight = null;
    public Servo clawL = null;
    public Servo  slidesL = null;
    public Servo  armL = null;
    double driveTimeVar = 0;
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
        clawL = hardwareMap.servo.get("clawL");
        armL = hardwareMap.servo.get("armL");
        slidesL = hardwareMap.servo.get("slidesL");

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

        //ElapsedTime driveTime = new ElapsedTime();

        waitForStart();

        webcam.stopStreaming();
        
        





        betterPivot(90);

        sleep(500);



        betterDrive(90, 0, 1, .75, 1000);

        sleep(500);

        betterDrive(90, 1, 0, .75, 1000);

        sleep(500);

        betterDrive(90, 0, -1, .75, 1000);

        sleep(500);

        betterDrive(90, -1, 0, .75, 1000);

        betterPivot(5);

        betterDrive(5, 0, 1, .75, 1000);

        sleep(500);

        betterDrive(5, 1, 0, .75, 1000);

        sleep(500);

        betterDrive(5, 0, -1, .75, 1000);

        sleep(500);

        betterDrive(5, -1, 0, .75, 1000);

        sleep(500);





            
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

                robot.motorRF.setPower(turnPower);
                robot.motorRB.setPower(turnPower);
                robot.motorLB.setPower(-turnPower);
                robot.motorLF.setPower(-turnPower);

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
    
    
    public void  betterDrive(int angle, double PowerX, double PowerY, double speed, double time)
    {
        RobotHardware robot = new RobotHardware(hardwareMap);
        ElapsedTime driveTime = new ElapsedTime();

        driveTimeVar = driveTime.milliseconds();

        while ((driveTimeVar + time) > driveTime.milliseconds())
        {

            double turnPower;

            while (angle > 180)
            {
                angle -= 360;
            }
            while (angle < -180)
            {
                angle += 360;
            }


            angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

            turnPower = ((angle - angles.firstAngle) / angle) + .2;

            robot.motorRF.setPower(speed * ((PowerY - PowerX) - (turnPower)));
            robot.motorRB.setPower(speed * (-(-PowerX - PowerY) - (turnPower)));
            robot.motorLB.setPower(speed * ((PowerY - PowerX) - (turnPower)));
            robot.motorLF.setPower(speed * ((PowerX + PowerY)) - (turnPower));

            telemetry.addData("motorRF Power", robot.motorRF.getPower());
            telemetry.addData("motorRB Power", robot.motorRB.getPower());
            telemetry.addData("motorLB Power", robot.motorLB.getPower());
            telemetry.addData("motorLF Power", robot.motorLF.getPower());
            telemetry.update();
        }

        stop(1);
    }
    
    public void  drive(int angle, double PowerX, double PowerY, double speed, double time)
    {
        RobotHardware robot = new RobotHardware(hardwareMap);
        ElapsedTime driveTime = new ElapsedTime();

        driveTimeVar = driveTime.milliseconds();

        while ((driveTimeVar + time) > driveTime.milliseconds())
        {
            robot.motorRF.setPower(speed * ((PowerY - PowerX) - (0)));
            robot.motorRB.setPower(speed * (-(-PowerX - PowerY) - (0)));
            robot.motorLB.setPower(speed * ((PowerY - PowerX) - (0)));
            robot.motorLF.setPower(speed * ((PowerX + PowerY)) - (0));

            telemetry.addData("motorRF Power", robot.motorRF.getPower());
            telemetry.addData("motorRB Power", robot.motorRB.getPower());
            telemetry.addData("motorLB Power", robot.motorLB.getPower());
            telemetry.addData("motorLF Power", robot.motorLF.getPower());
            telemetry.addData("time", driveTime.milliseconds());
            telemetry.update();
        }

        stop(1);
    }

    public void stop(int hi)
    {
        RobotHardware robot = new RobotHardware(hardwareMap);

        robot.motorRF.setPower(0);
        robot.motorRB.setPower(0);
        robot.motorLB.setPower(0);
        robot.motorLF.setPower(0);
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
