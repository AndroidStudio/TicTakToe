package tic.tack.toe.arduino.sockets;

public interface SocketConnectionListener {

    void onSocketOpen();

    void onSocketFailure();

    void onSocketClose();

}
