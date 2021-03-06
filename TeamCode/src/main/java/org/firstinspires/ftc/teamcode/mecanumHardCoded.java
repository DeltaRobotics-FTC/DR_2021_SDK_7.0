package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
@Disabled
@TeleOp (name="mecanumHardCoded" ,group = "")
public class mecanumHardCoded extends LinearOpMode
{

    double loopCounter = 0;
    private double speed = 0.7;
    private double zScale = 1.0;

    private DcMotor motorRF;
    private DcMotor motorRB;
    private DcMotor motorLF;
    private DcMotor motorLB;


    public void runOpMode()
    {

        motorRF = hardwareMap.dcMotor.get("motorRF");
        motorRB = hardwareMap.dcMotor.get("motorRB");
        motorLF = hardwareMap.dcMotor.get("motorLF");
        motorLB = hardwareMap.dcMotor.get("motorLB");

        waitForStart();

        while (opModeIsActive())
        {

            motorRF.setPower(speed*((-gamepad1.left_stick_y - gamepad1.left_stick_x) - (zScale * gamepad1.right_stick_x)));
            motorRB.setPower(speed*(-(-gamepad1.left_stick_x + gamepad1.left_stick_y) - (zScale * gamepad1.right_stick_x)));
            motorLB.setPower(speed*((gamepad1.left_stick_y + gamepad1.left_stick_x) - (zScale * gamepad1.right_stick_x)));
            motorLF.setPower(speed*((-gamepad1.left_stick_x + gamepad1.left_stick_y)) - (zScale * gamepad1.right_stick_x));

            loopCounter += 1;
            telemetry.addData("loopcounter", loopCounter);
            telemetry.update();
        }

    }
}