# Setting up a Raspberry Pi Camera Module.

## Secure Shell
The following assumes that you have Secure Shell (SSH) enabled on your Raspberry Pi. [Click here to learn how to do that.](https://www.raspberrypi.org/documentation/remote-access/ssh/). If you have command linke access already.
Disregard this step.

If SSH is enabled on your Pi, you require a way to access it. Services such as [PuTTY](https://www.putty.org/) allow this.

On PuTTY or your application of choice. Ensure the SSH connection type is selected, input the "Host Name/IP address" of your Pi,
as well as its port number (22 is default).

Once connecting, you will be prompted to input your Pi's username and password to sucessfully connect. Default **username:password** for a 
Pi is **pi:raspberry**. Confirm these entries by pressing enter and you will sucessfully connect to your Pi.


## Command Line
If you have not already, it is suggested that you update your Raspberry Pi. To do this, type the following on the command line:
**sudo apt update** followed by **sudo apt full-upgrade**

Aftering updating, enter **raspi-config** into the command line and press Enter, then do the following:
1) <pre>Select 3.Interface Options & Press Enter</pre> 
2) <pre>Select P1 Camera & Press Enter</pre> 
3) <pre>Select Yes & Press Enter then hit Ok. </pre>
4) <pre>Select Finish & Press Enter. </pre>

After your device has sucessfully been configured. **Raspivid** is the command line tool responsible for for capturing video on a Camera Module.

## Raspivid
[Info on Raspivid can be found here](https://www.raspberrypi.org/documentation/usage/camera/raspicam/raspivid.md)

If you would like to have your Raspberry Pi Camera Module capture video everytime it is powered. We must create a **Services File**
Input the following:

1) <pre>cd /etc/systemd/system/</pre>
2) <pre>sudo nano **filename**.service</pre>
where filename is the name of the file you choose (raspivid.services) for example.

3) Paste the following example:
<pre>
[Unit]
Description=raspivid
After=network.target

[Service]
ExecStart=/bin/sh -c "raspivid -n -ih -t 0 -rot 0 -w 1920 -h 1080 -b 25000000 -fps 30 -o - | nc -lkv4 5001"

[Install]
WantedBy=default.target
</pre>

This example will display a 1920x1080 stream @ 25Mb & 30 FPS, this stream will go over port 5001. For information on how to
manipulate this for a specific output, check the link above.

4) Press **ctrl+x** then type **y** to save this file. You will be brought back to the original command line screen.

5) To register the service: <pre>sudo systemctl daemon-reload</pre>

6) To test the service: <pre>sudo systemctl start raspivid.service</pre> where **raspivid.service** is the name of the file you created to start & test the service. Errors will display with incorrect information.

7) To ensure that the services file runs everytime the Raspberry Pi is connected: <pre>sudo systemctl enable NAMEOFFILE.service</pre>
