    import java.io.BufferedReader;
    import java.io.BufferedWriter;
    import java.io.IOException;
    import java.io.InputStreamReader;
    import java.io.OutputStreamWriter;
    import java.net.Socket;
    import java.util.Scanner;
    import java.util.UUID;


    public class Client {
        private Socket socket;
        private BufferedReader bufferedReader;
        private BufferedWriter bufferedWriter;
        private String clientID;

        public Client(Socket socket){
            try{
                this.socket = socket;
                this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.clientID = UUID.randomUUID().toString();

            }catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }

        public void sendMessage(){
            try{
                bufferedWriter.write(clientID);
                bufferedWriter.newLine();
                bufferedWriter.flush();
                
                Scanner scanner = new Scanner(System.in);
                while (socket.isConnected()){
                    String messageToSend = scanner.nextLine();
                    bufferedWriter.write(clientID + ": " + messageToSend);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            } catch(IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);

            }
        }

        public void listenForMessage(){
            new Thread(new Runnable(){
                @Override
                public void run(){
                    String messageFromGroupChat;

                    while (socket.isConnected()){
                        try{
                            messageFromGroupChat = bufferedReader.readLine();
                            System.out.println(messageFromGroupChat);

                        } catch(IOException e){
                            closeEverything(socket, bufferedReader, bufferedWriter);
                        }
                    }


                }
            }).start();
        }

        public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
            try{
                if(bufferedReader != null){
                    bufferedReader.close();
                }
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                if (socket != null){
                    socket.close();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }

            
        }
        public static void main(String[] args) throws IOException{
            Scanner scanner = new Scanner(System.in);
            System.out.println("You have connected to the server");
            Socket socket = new Socket("localhost", 1234);
            Client client = new Client(socket);
            client.listenForMessage();
            client.sendMessage();
        }
        
    }
