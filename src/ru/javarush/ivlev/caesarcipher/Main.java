package ru.javarush.ivlev.caesarcipher;


//import javafx.application.Application;

public class Main  {
  /*  @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(JavaRushM1Application.class.getResource("caesar-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("JavaRushCaesar");
        stage.setScene(scene);
        stage.show();
    }
*/
    public static void main(String[] args) {

        if (args.length>0 && args[0]=="window") {
          //  launch();
        }else {
            UserDialog dialog  = new UserDialog(System.in, System.out);
            dialog.startDialog();
        }
    }
}