public class ReceptionOfFileThread implements Runnable{
    public ReceptionOfFileThread(){
        //TODO GET THE PACKAGE FROM THE P2PRECHEPTIONTHREAD
    }

    @Override
    public void run() {
        //TODO FIND A NEW FREE SOCKET
        //TODO SEND AN OK MESSAGE TO THE SENDER (USE THE PACKAGE IP AND PORT)
        //TODO PARSE THE MESSAGE. THE LAST 24 BYTES FROM 1024 ARE THE PACKAGE NUMBER
        //TODO SEND THE PACKAGE NUMBER
        //TODO SAVE THE 1000 BYTES OF THE PACKAGE
        //TODO THE FIRST WHILE LOOP OF THE RECEPTION HAS TO GET THE PACKAGE AND CHECK FOR A FILENAME (tipp use regex and split with //s* and get the first key)
        //TODO AFTER RECEPTION OF FILENAME SAVE IT AND WAIT FOR THE RECEPTION OF END
        //TODO AFTER SECOND END OR TIMEOUT HAS BEEN RECIEVED  CLOSE THE SOCKET CONNECTION
        //TODO AFTER FIRST END HAS REACHED SEND END AS WELL TO CONFIRME THE CONNECTION BEEING CLOSED
        //TODO AFTER X SECONDS AFTER NOT RECIEVING A PACKAGE USE THE TIMEOUT TO DO A PING REQUEST
        //TODO IF PING HASN'T RESEND ANYTHING BRAKE THE CONNECTION AND DROP THE PACKAGES
        //TODO IF GOT AT LEAST ONE END OR AT LEAST THE FILENAME BUT DIDN'T PROPERLY CLOSE THE CONNECTION START THE FILE COMPOSITION PROCESS
        //TODO SAVE THE FILE USING THE SAME NAME AS PROVIDED(DO THIS OUT OF THE WHILE) THEN CLOSE THE THREAD AND ADD A MESSAGE WITH THE NAME OF THE FILE INTO THE ACTUAL CLIENT
    }
}
