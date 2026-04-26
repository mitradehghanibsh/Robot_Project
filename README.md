# EV3 Robot Project - Group 5

## Project Description
Our robot follows a black line and can automatically go around obstacles. We used a PID system for smooth steering and a multi-thread setup so the sensors are always checking for objects in the background. If the robot sees an obstacle, it takes a detour and then "hunts" for the line to get back on track.

## How it works
* **PID Control:** We used math to make the robot stay on the edge of the line without wobbling or shaking.
* **Threading:** We put the sensor logic in its own thread so the distance sensor is always active and doesn't lag the main movement.
* **Line Recovery:** After going around an object, the robot is programmed to find the black line again so it doesn't get lost.

## Team Responsibilities

**Eljona Pacolli**
* Wrote the final version of the code and restructured everything into a clean system.
* Programmed the obstacle avoidance logic and the recovery system for finding the line.
* Created the JavaDocs, the generated documentation, and the final README.
* Handled the final integration to make sure everyone's code worked together in one project.

**Allan Jakubovits**
* Led the technical setup and calibration for the light and ultrasonic sensors.
* Developed the core PID math and spent hours fine-tuning the constants to make the robot stable.
* Responsible for all physical testing on the track, hardware troubleshooting, and motor calibration.
* Ensured the sensors and hardware were perfectly synced with the software logic.

**Mitra Dehghani**
* Set up the GitHub repository for the team.
* Worked on the very first version of the PID math before it was refined.

**Ermira Zhitia**
* Worked on an early version of the obstacle avoidance code.
* Assisted with setting up the track and the hardware for our test runs.

## Port Setup
* **Motors:** Left (Port B), Right (Port A)
* **Sensors:** Color (Port S1), Ultrasonic (Port S2)