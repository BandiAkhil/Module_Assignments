package client;

import protocol.IMACProtocol;
import protocol.MediumState;
import protocol.TransmissionInfo;
import protocol.TransmissionType;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ProtocolException;
import java.net.Socket;
import java.util.Scanner;

/*
 *
 * DO NOT EDIT
 *
 */

/**
 * Class for maintainging communications with the challenge server
 *
 * @author Jaco ter Braak & Frans van Dijk, Twente University
 * @version 19-02-2018
 */
public class MACChallengeClient implements Runnable {
    private static String protocolString = "MACCHALLENGE/2.0";

    // server address
    private String host;

    // server port
    private int port;

    // student group key
    private String groupKey;

    // thread for handling server messages
    private Thread eventLoopThread;

    // server socket
    private Socket socket;

    // scanner over socket input stream
    private Scanner inputScanner;

    // socket output stream
    private PrintStream outputStream;

    // currently pending control message from server
    private String currentControlMessage = null;

    // protocol implementation for handling timeslot announcements
    private IMACProtocol listener = null;

    // whether the simulation was started
    private boolean simulationStarted = false;

    // whether the simulation is finished
    private boolean simulationFinished = false;

    /**
     * Constructs the client and connects to the server.
     *
     * @param groupKey The group key
     * @throws IOException          if the connection failed
     * @throws InterruptedException if the operation was interrupted
     */
    public MACChallengeClient(String serverAddress, int serverPort,
                              String groupKey) throws IOException,
            InterruptedException {
        if (groupKey.equals("get-your-key-from-the-website")) {
            throw new IllegalArgumentException(
                    "Please set your group key in Program.java");
        }

        this.host = serverAddress;
        this.port = serverPort;
        this.groupKey = groupKey;

        eventLoopThread = new Thread(this, "Event Loop Thread");

        // connect to the server. Throws IOException if failure
        connect();
    }

    /**
     * Connects to the challenge server
     *
     * @throws IOException if the connection failed
     */
    private void connect() throws IOException, InterruptedException {
        try {
            // Open comms
            socket = new Socket(host, port);
            inputScanner = new Scanner(new BufferedInputStream(
                    socket.getInputStream()));
            outputStream = new PrintStream(new BufferedOutputStream(
                    socket.getOutputStream()));

            if (!getControlMessageBlocking().equals("REGISTER")) {
                throw new ProtocolException(
                        "Did not get expected hello from server");
            }
            clearControlMessage();

            // register
            sendControlMessage("REGISTER " + this.groupKey);

            String reply = getControlMessageBlocking();
            if (!reply.equals("OK")) {
                String reason = reply.substring(reply.indexOf(' ') + 1);
                throw new ProtocolException("Could not register with server: "
                        + reason);
            }
            clearControlMessage();

            // start handling messages
            eventLoopThread.start();

        } catch (IOException | InterruptedException e) {
            throw e;
        }
    }

    /**
     * Sets the protocol used for medium access control.
     *
     * @param listener An implementation of IMACProtocol
     */
    public void setListener(IMACProtocol listener) {
        this.listener = listener;
    }

    /**
     * Reqests a simulation start from the server
     */
    public void requestStart() {
        if (!simulationStarted) {
            sendControlMessage("START");
        }
    }

    /**
     * Starts the simulation
     */
    public void start() {
        if (!simulationStarted) {
            simulationStarted = true;
        }
    }

    /**
     * @return whether the simulation has been started
     */
    public boolean isSimulationStarted() {
        return simulationStarted;
    }

    /**
     * @return whether the simulation has finished
     */
    public boolean isSimulationFinished() {
        return simulationFinished;
    }

    /**
     * Stops the client, and disconnects it from the server.
     */
    public void stop() {
        simulationStarted = false;
        simulationFinished = true;

        // stop the message loop
        eventLoopThread.interrupt();
        try {
            eventLoopThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // close comms
        try {
            socket.setTcpNoDelay(true);
            sendControlMessage("CLOSED");
            socket.getOutputStream().flush();
            // socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles communication between the server and the protocol implementation
     */
    public void run() {
        boolean stopThread = false;
        while (!stopThread && !simulationFinished) {
            try {
                String message = getControlMessageBlocking();

                if (message.startsWith("FAIL")) {
                    if (message.split(" ").length > 1) {
                        System.err
                                .println("Failure: "
                                        + message.substring(message
                                        .indexOf(' ') + 1));
                    }
                    clearControlMessage();
                    stopThread = true;
                    simulationStarted = false;
                    simulationFinished = true;
                } else if (message.startsWith("INFO")) {
                    System.err
                            .println("Info: "
                                    + message.substring(message
                                    .indexOf(' ') + 1));
                } else if (message.startsWith("START")) {
                    // start the simulation
                    simulationStarted = true;
                } else if (message.startsWith("TIMESLOT")) {
                    if (simulationStarted) {
                        String[] splitMessage = message.split(" ");

                        TransmissionInfo transmissionInfo = null;
                        int queueLength = 0;

                        // slot was idle
                        if (splitMessage.length == 4
                                && splitMessage[1].equals("IDLE")) {
                            queueLength = Integer.parseInt(splitMessage[3]);
                            transmissionInfo = listener.TimeslotAvailable(
                                    MediumState.Idle, 0, queueLength);
                        }

                        // slot was collision
                        if (splitMessage.length == 4
                                && splitMessage[1].equals("COLLISION")) {
                            queueLength = Integer.parseInt(splitMessage[3]);
                            transmissionInfo = listener.TimeslotAvailable(
                                    MediumState.Collision, 0, queueLength);
                        }

                        // slot was succesful
                        if (splitMessage.length == 5
                                && splitMessage[1].equals("SUCCESS")) {
                            queueLength = Integer.parseInt(splitMessage[4]);
                            transmissionInfo = listener.TimeslotAvailable(
                                    MediumState.Succes,
                                    Integer.parseInt(splitMessage[2]),
                                    queueLength);
                        }

                        // got strategy from protocol, send it back to server
                        if (transmissionInfo != null) {
                            if (transmissionInfo.GetTransmissionType() == TransmissionType.Data) {
                                if (queueLength <= 0) {
                                    throw new IllegalStateException(
                                            "Cannot transmit data without packets in the queue.");
                                }
                                sendControlMessage("TRANSMIT "
                                        + transmissionInfo
                                        .GetControlInformation()
                                        + " DATA");
                            }
                            if (transmissionInfo.GetTransmissionType() == TransmissionType.NoData) {
                                sendControlMessage("TRANSMIT "
                                        + transmissionInfo
                                        .GetControlInformation()
                                        + " NO_DATA");
                            }
                            if (transmissionInfo.GetTransmissionType() == TransmissionType.Silent) {
                                sendControlMessage("TRANSMIT 0 SILENT");
                            }
                        }
                    }
                } else if (message.startsWith("FINISH") || message.startsWith("CLOSED")) {
                    simulationStarted = false;
                    simulationFinished = true;
                }

                clearControlMessage();

                Thread.sleep(1);
            } catch (ProtocolException e) {
            } catch (InterruptedException e) {
                stopThread = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Waits for a control message from the server
     *
     * @return the message
     * @throws ProtocolException if a corrupt message was received
     */
    private String getControlMessageBlocking() throws ProtocolException,
            InterruptedException, IOException {
        try {
            // Block while waiting for message
            String controlMessage = getControlMessage();
            while (controlMessage == null) {
                Thread.sleep(10);
                controlMessage = getControlMessage();
            }
            return controlMessage;
        } catch (Exception e) {
            throw e;
        }

    }

    /**
     * Removes the first message from the queue Call this when you have
     * processed a message
     */
    private void clearControlMessage() {
        this.currentControlMessage = null;
    }

    /**
     * Obtains a message from the server, if any exists.
     *
     * @return the message, null if no message was present
     * @throws IOException
     */
    private synchronized String getControlMessage() throws IOException {
        if (!simulationFinished) {
            if (this.currentControlMessage == null
                    && inputScanner.hasNextLine()) {
                String line = inputScanner.nextLine();
                if (line.startsWith(protocolString)) {
                    this.currentControlMessage = line.substring(protocolString
                            .length() + 1);
                } else {
                    throw new ProtocolException("Protocol mismatch with server");
                }
            }
        }
        return this.currentControlMessage;
    }

    /**
     * Sends a message to the server
     *
     * @param message
     */
    private void sendControlMessage(String message) {
        outputStream.print(protocolString + " " + message + "\n");
        outputStream.flush();

    }
}