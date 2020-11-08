package com.hassanassowe.raspberrycam.Classes.RaspberryPi_Instance;

public class RaspberryPi {
    //Required parameters
    private String id; // Universally Unique Generated ID of Raspberry PI
    private String name; //Registered Name of Raspberry Pi
    private String connectionType; //Registered connection type of GStreamer pipeline (TCP/IPC, UDP ...)
    private String address; //Registered IP Address of Raspberry Pi
    private int port; //Registered Port of Raspberry Pi
    private String tint; //Cosmetic Highlight Tint of Raspberry Pi

    //Optional parameters
    private String SSHUsername = null;
    private String SSHPassword = null;
    private int SSHPort = -1;

    private int bitRate = -1; //Bitrate of advanced options
    private int frameRate= -1; //Framerate of advanced options
    private int height = -1; //Height of advanced options (Stream Resolution)
    private int width = -1; //Width of advanced options (Stream Resolution)
    private String screenshotFormat = null; //Optionally selected Screenshot format (PNG, JPEG ...)

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getTint() {
        return tint;
    }

    public String getSSHUsername(){return SSHUsername;}

    public String getSSHPassword(){return SSHPassword;}

    public int getSSHPort(){return SSHPort;}

    public int getBitRate(){return bitRate;}

    public int getFrameRate() { return frameRate;}

    public int getHeight() { return height;}

    public int getWidth() { return width;}

    public String getScreenshotFormat() { return screenshotFormat;}

    private RaspberryPi(RaspberryPiBuilder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.connectionType = builder.connectionType;
        this.address = builder.address;
        this.port = builder.port;
        this.tint = builder.tint;

        this.SSHUsername = builder.SSHUsername;
        this.SSHPassword = builder.SSHPassword;
        this.SSHPort = builder.SSHPort;

        this.bitRate = builder.bitRate;
        this.frameRate = builder.frameRate;
        this.height = builder.height;
        this.width = builder.width;
        this.screenshotFormat = builder.screenshotFormat;
    }

    public static class RaspberryPiBuilder {
        //Required parameters
        private String id; // Universally Unique Generated ID of Raspberry PI
        private String name; //Registered Name of Raspberry Pi
        private String connectionType; //Registered connection type of GStreamer pipeline (TCP/IPC, UDP ...)
        private String address; //Registered IP Address of Raspberry Pi
        private int port; //Registered Port of Raspberry Pi
        private String tint; //Cosmetic Highlight Tint of Raspberry Pi

        //Optional parameters
        private String SSHUsername;
        private String SSHPassword;
        private int SSHPort;
        private int bitRate; //Bitrate of advanced options
        private int frameRate; //Framerate of advanced options
        private int height; //Height of advanced options (Stream Resolution)
        private int width; //Width of advanced options (Stream Resolution)
        private String screenshotFormat; //Optionally selected Screenshot format (PNG, JPEG ...)


        public RaspberryPiBuilder(String id, String name, String connectionType, String address, int port, String tint) {
            this.id = id;
            this.name = name;
            this.connectionType = connectionType;
            this.address = address;
            this.port = port;
            this.tint = tint;
        }

        public RaspberryPiBuilder setSSHUsername(String SSHUsername) {
            this.SSHUsername = SSHUsername;
            return this;
        }

        public RaspberryPiBuilder setSSHPassword(String SSHPassword) {
            this.SSHPassword = SSHPassword;
            return this;
        }

        public RaspberryPiBuilder setSSHPort(int SSHPort) {
            this.SSHPort = SSHPort;
            return this;
        }

        public RaspberryPiBuilder setBitRate(int bitRate) {
            this.bitRate = bitRate;
            return this;
        }

        public RaspberryPiBuilder setFrameRate(int frameRate) {
            this.frameRate = frameRate;
            return this;
        }

        public RaspberryPiBuilder setHeight(int height) {
            this.height = height;
            return this;
        }

        public RaspberryPiBuilder setWidth(int width) {
            this.width = width;
            return this;
        }

        public RaspberryPiBuilder setScreenshotFormat(String screenshotFormat) {
            this.screenshotFormat = screenshotFormat;
            return this;
        }

        public RaspberryPi build(){
            return new RaspberryPi(this);
        }
    }
}
